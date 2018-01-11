package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface GuiAction {
	public void performAction(Player p, ItemStack stack);
}
