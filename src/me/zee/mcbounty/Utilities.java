package me.zee.mcbounty;

import me.zee.mcbounty.MCBounty.EventMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Utilities {
	private MCBounty plugin;
	
	public Utilities(MCBounty plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * <p>Returns whether or not a given String can be a double</p>
	 * 
	 * @param toParse The String to be checked
	 * @return Whether or not the given String is a valid double
	 */
	public boolean isValidDouble(String toParse) {
		try {
			Double.parseDouble(toParse);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * <p>Returns the specified String set in the config for the given event</p>
	 * 
	 * @param eMsg The specific event message to get the String for
	 * @return String The String specified in the config for the given event
	 */
	public String getEventMessage(EventMessage eMsg) {
		switch (eMsg) {
		case PLAYER_DEATH_NOT_TO_PLAYER:
			return plugin.getConfig().getString("PLAYER_DEATH_NOT_TO_PLAYER");
		case PLAYER_DEATH_TO_BOUNTY_CREATOR:
			return plugin.getConfig().getString("PLAYER_DEATH_TO_BOUNTY_CREATOR");
		case BOUNTY_FINISHED_INSUFFICIENT_FUNDS:
			return plugin.getConfig().getString("BOUNTY_FINISHED_INSUFFICIENT_FUNDS");
		case BOUNTY_FINISHED:
			return plugin.getConfig().getString("BOUNTY_FINISHED");
		case BOUNTY_FINISHED_ALERT_CREATOR:
			return plugin.getConfig().getString("BOUNTY_FINISHED_ALERT_CREATOR");
		case BOUNTY_FINISHED_ALERT_KILLER:
			return plugin.getConfig().getString("BOUNTY_FINISHED_ALERT_KILLER");
		case BOUNTY_REMOVED_TO_COMMAND_CALLER:
			return plugin.getConfig().getString("BOUNTY_REMOVED_TO_COMMAND_CALLER");
		case BOUNTY_REMOVED_TO_ALL:
			return plugin.getConfig().getString("BOUNTY_REMOVED_TO_ALL");
		case BOUNTY_CHANGED_PUBLIC_TO_COMMAND_CALLER:
			return plugin.getConfig().getString("BOUNTY_CHANGED_PUBLIC_TO_COMMAND_CALLER");
		case BOUNTY_CHANGED_PUBLIC_TO_ALL:
			return plugin.getConfig().getString("BOUNTY_CHANGED_PUBLIC_TO_ALL");
		case BOUNTY_CHANGED_REWARD_TO_SENDER:
			return plugin.getConfig().getString("BOUNTY_CHANGED_REWARD_TO_SENDER");
		case BOUNTY_CHANGED_REWARD_TO_ALL:
			return plugin.getConfig().getString("BOUNTY_CHANGED_REWARD_TO_ALL");
		case BOUNTY_CREATED_TO_ALL:
			return plugin.getConfig().getString("BOUNTY_CREATED_TO_ALL");
		default:
			return null;
		}
	}
	
	/**
	 * <p>Returns the colored version of the plugin's prefix set in the config</p>
	 * 
	 * @return String the colored version of the plugin's prefix
	 */
	public String getPrefix() {
		return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix")) + ChatColor.RESET;
	}
	
	/**
	 * <p>Broadcasts a message to the server with the plugin's prefix</p>
	 * 
	 * @param msg The message to be broadcasted
	 */
	public void broadcastMessage(String msg) {
		Bukkit.broadcastMessage(getPrefix() + ChatColor.YELLOW + msg);
	}
	
	/**
	 * <p>Sends a specific message to a player with the plugin's prefix</p>
	 * 
	 * @param player The player the message is being sent to
	 * @param msg The message the player is being sent
	 */
	public void sendMessage(Player player, String msg) {
		player.sendMessage(getPrefix() + ChatColor.YELLOW + msg);
	}
	
	/**
	 * <p>Sends a specific message to a CommandSender with the plugin's prefix</p>
	 * 
	 * @param sender The sender the message is being sent to
	 * @param msg The message the sender is being sent
	 */
	public void sendMessage(CommandSender sender, String msg) {
		sender.sendMessage(getPrefix() + ChatColor.YELLOW + msg);
	}
	
	/**
	 * <p>Sends a red message to a CommandSender telling them they don't have access to a command.</p>
	 * 
	 * @param sender The CommandSender to send the message to
	 */
	public void sendNoAccess(CommandSender sender) {
		sender.sendMessage(getPrefix() + ChatColor.RED + "Sorry, but you don't have access to this command!");
	}
}