package me.zee.mcbounty;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CMDBounty implements CommandExecutor {
	private Utilities util;
	private CommandManager cmdMan;
	
	public CMDBounty(Utilities util, DatabaseHandler dbHandler) {
		this.util = util;
		cmdMan = new CommandManager(util, dbHandler);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("bounty") || label.equalsIgnoreCase("b")) {
			if (args.length==0) {
				cmdMan.bountyHelp(sender); //Done
			} else {
				String arg0 = args[0];
				if (arg0.equalsIgnoreCase("help")) { //bounty help
					cmdMan.bountyHelp(sender); //Done
				}
				else if (arg0.equalsIgnoreCase("create")) { //bounty create <player> <reward> <public>
					cmdMan.bountyCreate(sender, args); //Done? Needs testing
				}
				else if (arg0.equalsIgnoreCase("setreward")) { //bounty editreward <player> <newreward>
					cmdMan.bountySetReward(sender, args); //Done
				}
				else if (arg0.equalsIgnoreCase("setpublic")) { //bounty setpublic <player> <ispublic>
					cmdMan.bountySetPublic(sender, args); //Done
				}
				else if (arg0.equalsIgnoreCase("remove")) { //bounty remove <player>
					cmdMan.bountyRemove(sender, args); //Done
				}
				else if (arg0.equalsIgnoreCase("on")) { //bounty on <player>
					cmdMan.bountyOn(sender, args); //Done
				}
				else { //Unrecognized command
					util.sendMessage(sender, ChatColor.RED+"Unrecognized command! Type /bounty help for a list of all commands");
				}
			}
		}
		
		return true;
	}
}