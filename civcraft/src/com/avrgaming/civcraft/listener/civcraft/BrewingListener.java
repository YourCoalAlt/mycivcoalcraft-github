package com.avrgaming.civcraft.listener.civcraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.ItemManager;

public class BrewingListener implements Listener {
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if (event.getInventory().getType() == InventoryType.BREWING) {
			final BrewerInventory inv = (BrewerInventory) event.getInventory();
			final Player p = (Player)event.getView().getPlayer();
			
			if (event.getClick() == ClickType.LEFT) { // Prepare to swap the items the player has
				final ItemStack is = event.getCurrentItem();
				final ItemStack is2 = event.getCursor().clone();
				if (is2 == null || is2.getType() == Material.AIR) {
					if (is == null) {
						event.setCursor(is);
						event.setCurrentItem(null);
						p.updateInventory();
						return;
					} else {
						return; // Nothing on cursor
					}
				}
				
				if (event.getSlot() == 3 && event.getSlotType() == SlotType.FUEL) {
					if (is == is2) {
						ItemManager.givePlayerItem(p, is, event.getView().getPlayer().getLocation(), null, (is.getAmount() + is2.getAmount()), true);
						CivMessage.sendError(p, "Sending all items back to inventory...");
						p.updateInventory();
						return;
					}
					Bukkit.getScheduler().scheduleSyncDelayedTask(CivCraft.getPlugin(), new Runnable() {
						@Override
						public void run() { //Now we make the switch
							event.setCursor(is);
							inv.setItem(event.getSlot(), is2);
							p.updateInventory();
						}
					}, 1L); //(Delay in 1 tick)
				}
			}
		}
	}
	
//	@EventHandler
//	public void onPotionBrew(BrewEvent event) {
//		BrewingStand bs = (BrewingStand) event.getBlock();
//	}
	
}
