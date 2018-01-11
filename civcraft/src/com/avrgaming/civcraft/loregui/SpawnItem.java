package com.avrgaming.civcraft.loregui;

import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import gpl.AttributeUtil;

public class SpawnItem implements GuiAction {

	@Override
	public void performAction(Player player, ItemStack stack) {
		AttributeUtil attrs = new AttributeUtil(stack);
		attrs.removeCivCraftProperty("GUI");
		attrs.removeCivCraftProperty("GUI_ACTION");
		
		ItemStack is = attrs.getStack();
		is.setAmount(is.getMaxStackSize());
		
		for (ListIterator<ItemStack> iter = player.getInventory().iterator(); iter.hasNext();) {
			ItemStack item = iter.next();
			if (item == null || item.getType() == Material.AIR) {
				player.getInventory().addItem(is);
				return;
			}
		}
	}
}
