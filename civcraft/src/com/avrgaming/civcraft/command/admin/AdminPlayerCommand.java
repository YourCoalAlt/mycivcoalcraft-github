package com.avrgaming.civcraft.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.kitteh.vanish.staticaccess.VanishNoPacket;
import org.kitteh.vanish.staticaccess.VanishNotLoadedException;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

@SuppressWarnings("deprecation")
public class AdminPlayerCommand extends CommandBase {
	
	@Override
	public void init() {
		command = "/ad player";
		displayName = "Admin Player";
		
		commands.put("vanish", "[player] - puts this player in vanish mode.");
	}
	
	public static boolean isVanished = false;
	public void vanish_cmd() throws CivException, VanishNotLoadedException {
		Player p = null;
		if (getNamedPlayer(1) != null) p = getNamedPlayer(1);
		else p = getPlayer();
		
		if (CivSettings.hasVanishNoPacket) VanishNoPacket.getManager().toggleVanish(p);
		if (!isVanished) {
			isVanished = true;
			p.setGameMode(GameMode.SPECTATOR);
			CivMessage.sendSuccess(p, "You are now vanished!");
			Bukkit.getServer().broadcastMessage(CivColor.Yellow+p.getName()+" left the game");
		} else {
			isVanished = false;
			p.setGameMode(GameMode.CREATIVE);
			CivMessage.sendSuccess(p, "You are no longer vanished!");
			Bukkit.getServer().broadcastMessage(CivColor.Yellow+p.getName()+" joined the game");
		}
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
