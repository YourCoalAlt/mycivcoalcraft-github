package com.avrgaming.civcraft.command.debug;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;

public class DebugWorldCommand extends CommandBase {
	
	@Override
	public void init() {
		command = "/dbg world";
		displayName = "Debug World";
		
		commands.put("tp", "[name] teleports you to spawn at the specified world.");
		commands.put("list", "Lists worlds according to bukkit.");
	}
	
	public void list_cmd() {
		CivMessage.sendHeading(sender, "Worlds");
		for (World world : Bukkit.getWorlds()) {
			CivMessage.send(sender, world.getName());
		}
	}
	
	public void tp_cmd() throws CivException {
		String name = getNamedString(1, "Enter a world name.");
		Player player = getPlayer();
		World world = Bukkit.getWorld(name);
		player.teleport(world.getSpawnLocation());
		CivMessage.sendSuccess(sender, "Teleported to spawn at world:"+name);
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
