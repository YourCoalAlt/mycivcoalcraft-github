package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.backpack.Backpack;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;

public class OpenInventory implements GuiAction {

	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player player = (Player)event.getWhoClicked();
		player.closeInventory();
		
		class SyncTaskDelayed implements Runnable {
			String playerName;
			ItemStack stack;
			
			public SyncTaskDelayed(String playerName, ItemStack stack) {
				this.playerName = playerName;
				this.stack = stack;
			}
			
			@Override
			public void run() {
				Player player;
				try {
					player = CivGlobal.getPlayer(playerName);
				} catch (CivException e) {
					e.printStackTrace();
					return;
				}
				
				switch (LoreGuiItem.getActionData(stack, "invType")) {
				case "showTutorialInventory":
					Backpack.showTutorialInventory(player);
					break;
				case "showCraftingHelp":
					Backpack.showCraftingHelp(player);
					break;
				case "showExperienceHelp":
					Backpack.showExperienceHelp(player);
					break;
//				case "showMiningRates":
//					Backpack.showMiningRates(player);
//					break;
				case "showFishingRates":
					Backpack.showFishingRates(player);
					break;
				case "showTownMenu":
					Backpack.showTownMenu(player);
					break;
				case "openResMail":
					Resident res = CivGlobal.getResident(player);
					res.openMainMailMenu(player, res);
					break;
				case "showGuiInv":
					String invName = LoreGuiItem.getActionData(stack, "invName");
					Inventory inv = LoreGuiItemListener.guiInventories.get(invName);
					if (inv != null) {
						player.openInventory(inv);
					} else {
						CivLog.error("Couldn't find GUI inventory:"+invName);
					}
					break;
				default:
					break;
				}
			}
		}
		
		TaskMaster.syncTask(new SyncTaskDelayed(player.getName(), stack));		
	}

}
