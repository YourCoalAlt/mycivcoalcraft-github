package com.avrgaming.civcraft.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivMessage;

public class AdminPlayerCommand extends CommandBase {
	
	@Override
	public void init() {
		command = "/ad player";
		displayName = "Admin Player";
		
		commands.put("givebasicadvancement", "[player] - Gives this player the basic advancement.");
		commands.put("updatetags", "Forcefully update player tags.");
		commands.put("permissions", "List the player's permissions.");
	}
	
	public void permissions_cmd() throws CivException {
		Player p = getNamedPlayer(1);
		String perms = "";
		for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
			if (perm.getPermission().contains("civ")) {
				perms += perm.getPermission()+", ";
			}
		}
		CivMessage.send(sender, perms);
	}
	
	public void updatetags_cmd() {
		CivCraft.playerTagUpdate();
	}
	
	public void givebasicadvancement_cmd() throws CivException {
		Player p = getNamedPlayer(1);
		Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), "minecraft:advancement grant "+p.getName()+" only civcraftgeneral:root");
		CivMessage.sendSuccess(sender, "Given basic advancement to "+p.getName());
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}
	
	@Override
	public void showHelp() {
		showBasicHelp();
	}
	
	@Override
	public void permissionCheck() throws CivException {
	}
	
}
