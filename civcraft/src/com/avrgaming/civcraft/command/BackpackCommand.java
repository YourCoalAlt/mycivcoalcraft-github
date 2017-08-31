package com.avrgaming.civcraft.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

public class BackpackCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			CivMessage.sendError(sender, "Only a player can execute this command.");
			return false;
		}
		
		Player p = (Player)sender;
		for (ItemStack stack : p.getInventory().getContents()) {
			if (stack == null) {
				continue;
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				continue;
			}
			
			if (craftMat.getConfigId().equals("civ_backpack")) {
				p.getInventory().removeItem(stack);
				CivMessage.sendSuccess(p, "Disabled your backpack.");
				return false;
			}
		}
		
		if (p.getInventory().getItem(8) != null && p.getInventory().getItem(8).getType() != Material.AIR) {
			ItemStack slot8Item = p.getInventory().getItem(8).clone();
			int newSlot = 0;
			if (p.getInventory().firstEmpty() == -1) {
				CivMessage.sendError(p, "You cannot hold anything else. Get some space open in your inventory first.");
				return false;
			} else {
				for (int i = 0; i < p.getInventory().getContents().length; i++) {
					if (p.getInventory().getContents()[i] == null && i < 36) {
						newSlot = i;
						continue;
					}
				}
			}
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(slot8Item);
			if (craftMat != null) {
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, slot8Item.getAmount());
				p.getInventory().getItem(8).setAmount(0);
				p.getInventory().setItem(newSlot, newMat);
			} else {
				p.getInventory().getItem(8).setAmount(0);
				p.getInventory().setItem(newSlot, slot8Item);
			}
			String displayName = "";
			if (slot8Item.hasItemMeta()) {
				displayName = slot8Item.getItemMeta().getDisplayName();
			} else {
				String itemName = slot8Item.getType().toString();
				displayName = itemName.substring(0,1).toUpperCase() + itemName.substring(1).toLowerCase();
			}
			CivMessage.send(p, CivColor.LightGrayItalic+"Since you had "+CivColor.White+displayName+CivColor.LightGrayItalic+" in hotbar slot 9, we moved it to slot "+(newSlot+1)+".");
		}
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId("civ_backpack");
		ItemStack backpack = LoreCraftableMaterial.spawn(craftMat);
		p.getInventory().setItem(8, backpack);
		CivMessage.sendSuccess(p, "Enabled your backpack.");
		return true;
	}
}
