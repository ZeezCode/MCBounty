package me.zee.mcbounty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.UUID;

public class DatabaseHandler {
	private Connection connection;
	private MCBounty plugin;
	
	public DatabaseHandler(MCBounty plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * <p>Creates necessary database and table (if they don't already exist) for plugin usage</p>
	 */
	public void setupDatabase() {
		openConnection();
		try {
			PreparedStatement createTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `bounties` ("
				+"`id` int(11) NOT NULL AUTO_INCREMENT,"
    			+"`creator` text NOT NULL,"
    			+"`target` text NOT NULL,"
    			+"`killer` text NULL default NULL,"
    			+"`reward` double NOT NULL,"
    			+"`public` tinyint(1) NOT NULL,"
    			+"`complete` tinyint(1) NOT NULL,"
    			+"`timecreated` bigint(20) NOT NULL,"
    			+"`timecompleted` bigint(20) NULL default NULL,"
    			+"PRIMARY KEY (`id`)"
				+");");
			createTable.execute();
			createTable.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Adds a new bounty to the database, should be checked for valid values before using this method.</p>
	 * 
	 * @param creator The UUID of the creator of the bounty
	 * @param target The UUID of the target of the bounty
	 * @param reward The reward for completing the bounty
	 * @param isPublic Whether or not the bounty creator is public
	 */
	public void createBounty(UUID creator, UUID target, double reward, boolean isPublic) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("INSERT INTO bounties VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
			sql.setInt(1, 0); //BID
			sql.setString(2, creator.toString()); //Creator UUID
			sql.setString(3, target.toString()); //Target UUID
			sql.setNull(4, Types.VARCHAR); //Killer UUID
			sql.setDouble(5, reward); //Reward
			sql.setBoolean(6, isPublic); //Is public
			sql.setBoolean(7, false); //Is complete
			sql.setLong(8, (long) new Date().getTime()/1000); //Time created
			sql.setNull(9, Types.BIGINT); //Time completed
			
			sql.execute();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Sets a bounty's status to finished, effectively removing it from the game</p>
	 * 
	 * @param bounty The bounty to be removed
	 */
	public void removeBounty(Bounty bounty) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("UPDATE bounties SET complete=?, timecompleted=? WHERE bid=?");
			sql.setBoolean(1, true);
			sql.setLong(2, (long) new Date().getTime()/1000);
			sql.setInt(3, bounty.getBID());
			sql.executeUpdate();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Updates a bounty in the database w/ the given Bounty object</p>
	 * 
	 * @param bounty The bounty to be updated
	 * @param becameComplete Whether or not the bounty was completed since last update
	 */
	public void updateBountyOnDB(Bounty bounty, boolean becameComplete) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("UPDATE bounties SET killer=?, reward=?, public=?, complete=?, timecompleted=? WHERE bid=?");
			if (bounty.getKiller()==null)
				sql.setNull(1, Types.VARCHAR);
			else
				sql.setString(1, bounty.getKiller().toString());
			sql.setDouble(2, bounty.getReward());
			sql.setBoolean(3, bounty.isPublic());
			sql.setBoolean(4, bounty.isComplete());
			if (becameComplete)
				sql.setLong(5, (long) new Date().getTime()/1000);
			else
				sql.setNull(5, Types.BIGINT);
			sql.setInt(6, bounty.getBID());
			sql.executeUpdate();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Returns an open bounty on the given player or null if nonexistant</p>
	 * 
	 * @param uuid The UUID of the player with an open bounty
	 * @return Bounty A bounty object w/ all important information relating to the bounty
	 */
	public Bounty getBountyOnPlayer(UUID uuid) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("SELECT * FROM bounties WHERE target=? AND complete=?");
			sql.setString(1, uuid.toString());
			sql.setBoolean(2, false);
			ResultSet result = sql.executeQuery();
			result.next();
			
			String killer = result.getString("killer");
			boolean isNull = result.wasNull();
			
			return new Bounty(result.getInt("bid"), 
					UUID.fromString(result.getString("creator")), UUID.fromString(result.getString("target")), (isNull ? null : UUID.fromString(killer)),
					result.getDouble("reward"), result.getBoolean("public"), result.getBoolean("complete"));
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Returns whether or not the player with the given UUID has an open bounty on them.</p>
	 * 
	 * @param uuid The UUID of the player we're checking
	 * @return boolean Whether or not the given player has an open bounty on them
	 */
	public boolean playerHasOpenBounty(UUID uuid) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("SELECT COUNT(*) AS ExistingBounties FROM bounties WHERE target=? AND complete=?;");
			sql.setString(1, uuid.toString());
			sql.setBoolean(2, false);
			ResultSet result = sql.executeQuery();
			result.next();
			boolean hasOpenBounty = (result.getLong("ExistingBounties") > 0);
			sql.close();
			result.close();
			return hasOpenBounty;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection();
		}
	}
	
	private synchronized void openConnection() {
		boolean shouldOpen=false;
		if (connection==null) shouldOpen=true;
		if (connection!=null)
			try {
				if (connection.isClosed()) shouldOpen=true;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		if (shouldOpen) {
			try {
				connection = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("databaseHost") + ":" 
			+ plugin.getConfig().getString("databasePort") 
			+ "/" + plugin.getConfig().getString("databaseName"), 
			plugin.getConfig().getString("databaseUsername"), 
			plugin.getConfig().getString("databasePassword"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void closeConnection() {
		try {
			connection.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}