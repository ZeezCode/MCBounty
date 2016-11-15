package me.zee.mcbounty;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class Bounty {
	private UUID creator, target, killer;
	private boolean isPublic, isComplete;
	private double reward;
	private int bid;
	
	public Bounty(int bid, UUID creator, UUID target, UUID killer, double reward, boolean isPublic, boolean isComplete) {
		this.bid = bid;
		this.creator = creator;
		this.target = target;
		this.killer = killer;
		this.reward = reward;
		this.isPublic = isPublic;
		this.isComplete = isComplete;
	}
	
	public int getBID() {
		return bid;
	}
	
	public UUID getCreator() {
		return creator;
	}
	
	public UUID getTarget() {
		return target;
	}
	
	public UUID getKiller() {
		return killer;
	}
	
	public void setKiller(UUID killer) {
		this.killer = killer;
	}
	
	public double getReward() {
		return reward;
	}
	
	public void setReward(double reward) {
		this.reward = reward;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public void setIsPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	
	public boolean isComplete() {
		return isComplete;
	}
	
	public void setIsComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public String toString() {
		 OfflinePlayer creator = Bukkit.getOfflinePlayer(this.creator);
		 OfflinePlayer target = Bukkit.getOfflinePlayer(this.target);
		 
		 String returnMsg = ChatColor.DARK_GREEN+"***Bounty Info***\n"
				 +ChatColor.DARK_RED+"Target: "+ChatColor.WHITE+target.getName()+"\n"
				 +ChatColor.YELLOW+"Creator: "+ChatColor.WHITE + (this.isPublic ? creator.getName() : "ANONYMOUS")+"\n"
				 +ChatColor.GREEN+"Reward: "+ChatColor.WHITE+"$"+this.reward;
		 return returnMsg;
	}
}
