package com.avrgaming.civcraft.command.moderator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;

public class ModeratorToolCommand extends CommandBase {
	
	@Override
	public void init() {
		command = "/mod tool";
		displayName = "Moderator Tools";
		
		commands.put("viewpinv", "[player] - View the player's inventory.");
		commands.put("setpinv", "[player] - Open player's inventory, but also be able to manipulate the items inside of it.");
	}
	
	//XXX TODO store player's contents in Resident.java for offline players when they log out, that way we can see the inv when they are offline
	public void viewpinv_cmd() throws CivException {
		OfflinePlayer op = null;
		if (getNamedOfflinePlayer(1) != null) op = getNamedOfflinePlayer(1);
		if (op.isOnline()) {
			Player p = op.getPlayer();
			Inventory inv = Bukkit.createInventory(null, 9*6, p.getName()+" [ON]");
			inv.setContents(p.getPlayer().getInventory().getStorageContents());
			int storageslot = 36;
			for (ItemStack is : p.getPlayer().getInventory().getArmorContents()) {
				storageslot++;
				if (is != null && is.getType() != Material.AIR) inv.setItem(storageslot-1, is);
				else inv.setItem(storageslot-1, new ItemStack(Material.BARRIER));
			}
			storageslot = 45;
			for (ItemStack is : p.getPlayer().getInventory().getExtraContents()) {
				if (is != null && is.getType() != Material.AIR) inv.setItem(storageslot-1, is);
				else inv.setItem(storageslot-1, new ItemStack(Material.BARRIER));
			}
			getPlayer().openInventory(inv);
			// DOES NOT WORK, NEEDS TO STORE INV
/*		} else {
			Inventory inv = Bukkit.createInventory(null, 9*6, op.getName()+" [OFF]");
			inv.setContents(op.getPlayer().getInventory().getStorageContents());
			int storageslot = 36;
			for (ItemStack is : op.getPlayer().getInventory().getArmorContents()) {
				storageslot++;
				if (is != null && is.getType() != Material.AIR) inv.setItem(storageslot-1, is);
				else inv.setItem(storageslot-1, new ItemStack(Material.BARRIER));
			}
			storageslot = 45;
			for (ItemStack is : op.getPlayer().getInventory().getExtraContents()) {
				if (is != null && is.getType() != Material.AIR) inv.setItem(storageslot-1, is);
				else inv.setItem(storageslot-1, new ItemStack(Material.BARRIER));
			}
			getPlayer().openInventory(inv);*/
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
