package me.zee.mcbounty;

import me.zee.mcbounty.MCBounty.EventMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager {
	private DatabaseHandler dbHandler;
	private Utilities util;
	
	public CommandManager(Utilities util, DatabaseHandler dbHandler) {
		this.util = util;
		this.dbHandler = dbHandler;
	}
	
	public void bountyHelp(CommandSender sender) {
		if (MCBounty.senderHasP(sender, "mcbounty.help")) {
			util.sendMessage(sender, "MCBounty Command Help:\n"
					+"/bounty help\n"
					+"/bounty info\n"
					+"/bounty create <player> <reward> <public (true/false)\n"
					+"/bounty setreward <player> <newreward>\n"
					+"/bounty setpublic <player> <ispublic>\n"
					+"/bounty remove <player>\n"
					+"/bounty on <player>\n");
		} else {
			util.sendNoAccess(sender);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void bountyCreate(CommandSender sender, String[] args) { //bounty create <player> <reward> <public=true>
		if (MCBounty.senderHasP(sender, "mcbounty.createbounty")) {
			if (sender instanceof Player) {
				Player creator = (Player) sender;
				if (args.length>=3) { //Public arg isn't required, defaults to true
					OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
					if (target==null) {
						util.sendMessage(sender, ChatColor.RED+"Unable to find player");
						return;
					}
					if (target.getUniqueId().equals(creator.getUniqueId())) { //If creator tries to make a bounty on themself
						util.sendMessage(sender, ChatColor.RED+"You can't create a bounty on yourself!");
						return;
					}
					boolean hasBounty = dbHandler.playerHasOpenBounty(target.getUniqueId());
					if (hasBounty) {
						util.sendMessage(sender, ChatColor.RED+"There is already an open bounty on this player!");
						return;
					}
					boolean isValidReward = util.isValidDouble(args[2]);
					if (!isValidReward) {
						util.sendMessage(sender, ChatColor.RED+"You must supply a valid reward for the bounty!\nUse /bounty create <player> <reward> [public]");
						return;
					}
					
					double reward = Double.parseDouble(args[2]);
					if (!MCBounty.playerHasAtLeast((OfflinePlayer) creator, reward)) { //Check if reward isn't higher than creator's balance
						util.sendMessage(sender, ChatColor.RED+"You can't set a reward that's higher than your present balance!");
						return;
					}
					
					if (MCBounty.getMinimumReward() > reward) { //Check if player's desired reward is more than the minimum reward
						util.sendMessage(sender, ChatColor.RED+"Bounties have a minimum reward of $"+MCBounty.getMinimumReward()+"!");
						return;
					}
					
					boolean shouldBePublic = true;
					if (args.length>=4) {
						String desiredPublicity = args[3];
						if (desiredPublicity.equalsIgnoreCase("true") || desiredPublicity.equalsIgnoreCase("t")) {
							//Defaults to true, just checking this so I know if it isn't false I can throw an error
						} else if (desiredPublicity.equalsIgnoreCase("false") || desiredPublicity.equalsIgnoreCase("f")) {
							shouldBePublic = false;
						} else {
							util.sendMessage(sender, ChatColor.RED+"You must supply a valid publicity value! (true or false)\n"
									+ "Use /bounty create <player> <reward> [public]");
						}
					}
					
					dbHandler.createBounty(creator.getUniqueId(), target.getUniqueId(), reward, shouldBePublic);
					util.sendMessage(sender, "You have successfully created a bounty on "+target.getName()+" for $"+reward+"!");
					
					String rawMsg = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_CREATED_TO_ALL));
					String finalMsg = rawMsg.replaceAll("TARGET", target.getName()).replaceAll("CREATOR", (shouldBePublic ? creator.getName() : "ANONYMOUS"))
							.replaceAll("REWARD", Double.toString(reward));
					util.broadcastMessage(finalMsg);
				}
				else {
					util.sendMessage(sender, ChatColor.RED+"Invalid arguments! Use /bounty create <player> <reward> [public=true]");
				}
			} else {
				util.sendMessage(sender, ChatColor.RED+"You must be a player to use this command!");
			}
		} else {
			util.sendNoAccess(sender);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void bountySetReward(CommandSender sender, String[] args) { //bounty setreward <player> <reward>
		if (MCBounty.senderHasP(sender, "mcbounty.editreward")) {
			if (args.length>=3) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
				if (target==null) {
					util.sendMessage(sender, ChatColor.RED+"Unable to find specified player");
					return;
				}
				boolean hasBounty = dbHandler.playerHasOpenBounty(target.getUniqueId());
				if (!hasBounty)
					util.sendMessage(sender, target.getName()+" does not have an open bounty on them!");
				else {
					Bounty bounty = dbHandler.getBountyOnPlayer(target.getUniqueId());
					if (sender instanceof Player) {
						Player calling_ply = (Player) sender;
						if (!calling_ply.getUniqueId().equals(bounty.getCreator())) {
							if (!MCBounty.senderHasP(calling_ply, "mcbounty.admin")) {
								util.sendMessage(calling_ply, ChatColor.RED+"You don't have permission to edit bounties you haven't created");
								return;
							}
						}
					}
					String choice = args[2].toLowerCase();
					if (!util.isValidDouble(choice)) {
						util.sendMessage(sender, ChatColor.RED+"An invalid reward has been specified! Use /bounty setreward <player> <reward>");
						return;
					}
					Double newReward = Double.parseDouble(choice);
					if (newReward==bounty.getReward()) {
						util.sendMessage(sender, ChatColor.RED+"This bounty already has a reward of $"+newReward+"!");
						return;
					}
					if (!MCBounty.playerHasAtLeast((OfflinePlayer) Bukkit.getOfflinePlayer(bounty.getCreator()), newReward)) { //Check if reward isn't higher than creator's balance
						util.sendMessage(sender, ChatColor.RED+"The reward can't be higher than the creator's current balance!");
						return;
					}
					if (MCBounty.getMinimumReward() > newReward) { //Check if player's desired reward is more than the minimum reward
						util.sendMessage(sender, ChatColor.RED+"Bounties have a minimum reward of $"+MCBounty.getMinimumReward()+"!");
						return;
					}
					
					bounty.setReward(newReward);
					dbHandler.updateBountyOnDB(bounty, false);
					
					String rawMsgToSender = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_CHANGED_REWARD_TO_SENDER));
					String finalMsgToSender = rawMsgToSender.replaceAll("TARGET", Bukkit.getOfflinePlayer(bounty.getTarget()).getName())
							.replaceAll("REWARD", Double.toString(newReward));
					util.sendMessage(sender, finalMsgToSender);
					
					String rawMsgToAll = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_CHANGED_REWARD_TO_ALL));
					String finalMsgToAll = rawMsgToAll.replaceAll("TARGET", Bukkit.getOfflinePlayer(bounty.getTarget()).getName())
							.replaceAll("CREATOR", (bounty.isPublic() ? Bukkit.getOfflinePlayer(bounty.getCreator()).getName() : "ANONYMOUS"))
							.replaceAll("REWARD", Double.toString(newReward));
					util.broadcastMessage(finalMsgToAll);
				}
			} else {
				util.sendMessage(sender, ChatColor.RED+"Invalid arguments! Use /bounty setreward <player> <reward>");
			}
		} else {
			util.sendNoAccess(sender);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void bountySetPublic(CommandSender sender, String[] args) { //bounty setpublic <player> <publicity>
		if (MCBounty.senderHasP(sender, "mcbounty.setpublic")) {
			if (args.length>=3) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
				if (target==null) {
					util.sendMessage(sender, ChatColor.RED+"Unable to find specified player");
					return;
				}
				boolean hasBounty = dbHandler.playerHasOpenBounty(target.getUniqueId());
				if (!hasBounty)
					util.sendMessage(sender, target.getName()+" does not have an open bounty on them!");
				else {
					Bounty bounty = dbHandler.getBountyOnPlayer(target.getUniqueId());
					if (sender instanceof Player) {
						Player calling_ply = (Player) sender;
						if (!calling_ply.getUniqueId().equals(bounty.getCreator())) {
							if (!MCBounty.senderHasP(calling_ply, "mcbounty.admin")) {
								util.sendMessage(calling_ply, ChatColor.RED+"You don't have permission to edit bounties you haven't created");
								return;
							}
						}
					}
					String choice = args[2].toLowerCase();
					boolean choiceB = false, isValid = false;
					if (choice.equals("true") || choice.equals("t")) {
						if (bounty.isPublic()) {
							util.sendMessage(sender, ChatColor.RED+"This bounty is already public!");
							return;
						} else { //Bounty is nonpublic, making it public...
							bounty.setIsPublic(true);
							dbHandler.updateBountyOnDB(bounty, false);
						}
						choiceB = true;
						isValid = true;
					} else if (choice.equals("false") || choice.equals("f")) {
						if (!bounty.isPublic()) {
							util.sendMessage(sender, ChatColor.RED+"This bounty is already not public!");
							return;
						} else { //Bounty is public, making it not public...
							bounty.setIsPublic(false);
							dbHandler.updateBountyOnDB(bounty, false);
						}
						isValid = true;
					}
					
					if (!isValid) {//User tried to set the bounty's public value to a non-boolean value
						util.sendMessage(sender, ChatColor.RED+"Invalid arguments! Use /bounty setpublic <player> <true/false>");
						return;
					}
					
					String rawMsgToCaller = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_CHANGED_PUBLIC_TO_COMMAND_CALLER));
					String finalMsgToCaller = rawMsgToCaller.replaceAll("TARGET", Bukkit.getOfflinePlayer(bounty.getTarget()).getName())
							.replaceAll("CREATOR", (bounty.isPublic() ? Bukkit.getOfflinePlayer(bounty.getCreator()).getName() : "ANONYMOUS"))
							.replaceAll("PUBLICSTATUS", (choiceB ? "public" : "nonpublic"));
					util.sendMessage(sender, finalMsgToCaller);
					
					String rawMsgToAll = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_CHANGED_PUBLIC_TO_ALL));
					String finalMsgToAll = rawMsgToAll.replaceAll("TARGET", Bukkit.getOfflinePlayer(bounty.getTarget()).getName())
							.replace("CREATOR", (bounty.isPublic() ? Bukkit.getOfflinePlayer(bounty.getCreator()).getName() : "ANONYMOUS"))
							.replaceAll("PUBLICSTATUS", (choiceB ? "public" : "nonpublic"));
					util.broadcastMessage(finalMsgToAll);
				}
			} else { //Invalid args
				util.sendMessage(sender, ChatColor.RED+"Invalid arguments! Use /bounty setpublic <player> <true/false>");
			}
		} else { //Sender lacks permission
			util.sendNoAccess(sender);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void bountyRemove(CommandSender sender, String[] args) { //bounty remove <player>
		if (MCBounty.senderHasP(sender, "mcbounty.remove")) {
			if (args.length>=2) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
				if (target==null) {
					util.sendMessage(sender, ChatColor.RED+"Unable to find specified player");
					return;
				}
				boolean hasBounty = dbHandler.playerHasOpenBounty(target.getUniqueId());
				if (!hasBounty)
					util.sendMessage(sender, target.getName()+" does not have an open bounty on them!");
				else {
					Bounty bounty = dbHandler.getBountyOnPlayer(target.getUniqueId());
					if (sender instanceof Player) {
						Player calling_ply = (Player) sender;
						if (!calling_ply.getUniqueId().equals(bounty.getCreator())) {
							if (!MCBounty.senderHasP(calling_ply, "mcbounty.admin")) {
								util.sendMessage(calling_ply, ChatColor.RED+"You don't have permission to remove bounties you haven't created");
								return;
							}
						}
					}
					dbHandler.removeBounty(bounty);
					
					String rawMsgToCaller = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_REMOVED_TO_COMMAND_CALLER));
					String finalMsgToCaller = rawMsgToCaller.replaceAll("TARGET", Bukkit.getOfflinePlayer(bounty.getTarget()).getName())
							.replaceAll("CREATOR", Bukkit.getOfflinePlayer(bounty.getCreator()).getName());
					util.sendMessage(sender, finalMsgToCaller);
					
					String rawMsgToAll = ChatColor.translateAlternateColorCodes('&', util.getEventMessage(EventMessage.BOUNTY_REMOVED_TO_ALL));
					String finalMsgToAll = rawMsgToAll.replaceAll("TARGET", Bukkit.getOfflinePlayer(bounty.getTarget()).getName())
							.replaceAll("CREATOR", Bukkit.getOfflinePlayer(bounty.getCreator()).getName())
							.replaceAll("REWARD", Double.toString(bounty.getReward()));
					util.broadcastMessage(finalMsgToAll);
				}
			}
			else {
				util.sendMessage(sender, "Invalid arguments! Use /bounty remove <player>");
			}
		} else {
			util.sendNoAccess(sender);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void bountyOn(CommandSender sender, String[] args) { //bounty on <player>
		if (MCBounty.senderHasP(sender, "mcbounty.bountyon")) {
			if (args.length>=2) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
				if (target==null) {
					util.sendMessage(sender, ChatColor.RED+"Unable to find specified player");
					return;
				}
				boolean hasBounty = dbHandler.playerHasOpenBounty(target.getUniqueId());
				if (!hasBounty)
					util.sendMessage(sender, target.getName()+" does not have an open bounty on them!");
				else {
					Bounty bounty = dbHandler.getBountyOnPlayer(target.getUniqueId());
					util.sendMessage(sender, bounty.toString());
				}
			} else {
				util.sendMessage(sender, ChatColor.RED+"Invalid arguments! Use: /bounty on <player>");
			}
		} else {
			util.sendNoAccess(sender);
		}
	}
	
	public void bountyInfo(CommandSender sender) { //bounty info
		if (MCBounty.senderHasP(sender, "mcbounty.info")) {
			util.sendMessage(sender, "---MCBounty Plugin Info---\n"
					+"Author: Zee (AKA: Zmaster007)\n"
					+"Version: "+MCBounty.getDesc().getVersion()+"\n"
					+"This plugin is open source! \n"
					+"http://GitHub.com/ZeezCode/MCBounty/");
		} else {
			util.sendNoAccess(sender);
		}
	}
}