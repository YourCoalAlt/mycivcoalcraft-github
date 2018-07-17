package com.avrgaming.civcraft.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;

public class AdminPlayerCommand extends CommandBase {
	
	@Override
	public void init() {
		command = "/ad player";
		displayName = "Admin Player";
		
		commands.put("givebasicadvancement", "[player] - Gives this player the basic advancement.");
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
