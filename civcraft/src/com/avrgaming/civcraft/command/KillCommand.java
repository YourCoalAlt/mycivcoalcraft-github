package com.avrgaming.civcraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class KillCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			CivMessage.sendErrorPlayerCmd(sender);
			return false;
		}
		
		Player player = (Player)sender;
		player.setLastDamageCause(null);
		Resident res = CivGlobal.getResident(player);
		res.isSuicidal = true;
		player.setHealth(0);
		CivMessage.send(sender, CivColor.YellowBold+"You couldn't take it anymore.");
		return true;
	}
}
