package me.zee.mcbounty;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MCBounty extends JavaPlugin implements Listener {
	private static Permission permission = null;
	private static Economy economy = null;
	private DatabaseHandler dbHandler;
	private Utilities util;
	private static double minReward;
	
	public enum EventMessage { // 3/line for legibility
		PLAYER_DEATH_NOT_TO_PLAYER, PLAYER_DEATH_TO_BOUNTY_CREATOR, BOUNTY_FINISHED_INSUFFICIENT_FUNDS,
		BOUNTY_FINISHED, BOUNTY_FINISHED_ALERT_CREATOR, BOUNTY_FINISHED_ALERT_KILLER,
		BOUNTY_REMOVED_TO_COMMAND_CALLER, BOUNTY_REMOVED_TO_ALL, BOUNTY_CHANGED_PUBLIC_TO_COMMAND_CALLER,
		BOUNTY_CHANGED_PUBLIC_TO_ALL, BOUNTY_CHANGED_REWARD_TO_SENDER, BOUNTY_CHANGED_REWARD_TO_ALL,
		BOUNTY_CREATED_TO_ALL
	}
	
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		getLogger().info(pdfFile.getName()+" has been enabled running version "+pdfFile.getVersion()+".");
		saveDefaultConfig();
		
		minReward = getConfig().getDouble("minimumReward");
		dbHandler = new DatabaseHandler(this);
		util = new Utilities(this);

		setupEconomy();
		setupPermissions();
		
		getCommand("bounty").setExecutor(new CMDBounty(util, dbHandler));
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player ply = e.getEntity();
		Player killer = ply.getKiller();
		
		if (dbHandler.playerHasOpenBounty(ply.getUniqueId())) {
			Bounty bounty = dbHandler.getBountyOnPlayer(ply.getUniqueId());
			
			if (killer==null) { //Player wasn't killed by another player, keep bounty open but announce
				String rawMsg = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.PLAYER_DEATH_NOT_TO_PLAYER));
				String finalMsg = rawMsg.replaceAll("TARGET", ply.getName());
				util.broadcastMessage(finalMsg);			
				return;
			}
			
			if (killer.getUniqueId().equals(bounty.getCreator())) {
				bounty.setIsComplete(true);
				bounty.setKiller(killer.getUniqueId());
				String rawMsg = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.PLAYER_DEATH_TO_BOUNTY_CREATOR));
				String finalMsg = rawMsg.replaceAll("TARGET", ply.getName()).replaceAll("KILLER", killer.getName());
				util.broadcastMessage(finalMsg);
				dbHandler.updateBountyOnDB(bounty, true);
				return;
			}
			
			//We now know the player who was killed had a bounty on them, they were killed by another player, and that other player was not the bounty's creator:
			
			bounty.setIsComplete(true);
			bounty.setKiller(killer.getUniqueId());
			
			OfflinePlayer creator = Bukkit.getOfflinePlayer(bounty.getCreator()), killerOP = Bukkit.getOfflinePlayer(killer.getUniqueId());
			
			double creatorBalance = economy.getBalance(creator);
			if (creatorBalance < bounty.getReward()) { //Creator doesn't have enough money to pay out reward
				String rawMsg = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_FINISHED_INSUFFICIENT_FUNDS));
				String finalMsg = rawMsg.replaceAll("TARGET", ply.getName()).replaceAll("KILLER", killer.getName()).replaceAll("CREATOR", creator.getName())
						.replaceAll("REWARD", Double.toString(bounty.getReward()));
				util.broadcastMessage(finalMsg);
			}
			else {
				String rawMsg = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_FINISHED));
				String finalMsg = rawMsg.replaceAll("TARGET", ply.getName()).replaceAll("KILLER", killer.getName()).replaceAll("CREATOR", creator.getName())
						.replaceAll("REWARD", Double.toString(bounty.getReward()));
				util.broadcastMessage(finalMsg);
			}
			economy.withdrawPlayer(creator, bounty.getReward());
			economy.depositPlayer(killerOP, bounty.getReward());
			
			if (creator.isOnline()) {
				String rawMsg = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_FINISHED_ALERT_CREATOR));
				String finalMsg = rawMsg.replaceAll("TARGET", ply.getName()).replaceAll("KILLER", killer.getName())
						.replaceAll("REWARD", Double.toString(bounty.getReward()));
				util.sendMessage(creator.getPlayer(), finalMsg);
			}
			String rawMsg = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_FINISHED_ALERT_KILLER));
			String finalMsg = rawMsg.replaceAll("TARGET", ply.getName()).replaceAll("CREATOR", creator.getName())
					.replaceAll("REWARD", Double.toString(bounty.getReward()));
			util.sendMessage(killer, finalMsg);
		}
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
	
	/**
	 * <p>Returns whether or not a CommandSender has a given permission</p>
	 * 
	 * @param sender The CommandSender whose permissions will be checked
	 * @param perm The permission to compare with the CommandSender
	 * 
	 * @return boolean Whether or not the CommandSender has the given permission
	 */
	public static boolean senderHasP(CommandSender sender, String perm) {
		return permission.has(sender, perm);
	}
	
	/**
	 * <p>Returns whether or not a player has at least a certain balance</p>
	 * 
	 * @param player The player whose balance will be checked
	 * @param amount The amount to compare with the given player
	 * 
	 * @return boolean Whether or not the given player has at least the given amount
	 */
	public static boolean playerHasAtLeast(OfflinePlayer player, double amount) {
		if (player!=null) {
			if (economy.hasAccount(player)) {
				return (economy.getBalance(player) >= amount);
			}
		}
		return false;
	}
	
	/**
	 * <p>Returns the minimum reward value</p>
	 * 
	 * @return double The minimum reward
	 */
	public static double getMinimumReward() {
		return minReward;
	}
}