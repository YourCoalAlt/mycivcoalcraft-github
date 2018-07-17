package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.backpack.Backpack;
import com.avrgaming.civcraft.backpack.Tutorial;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.camp.Camp;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;

public class OpenInventory implements GuiAction {

	@Override
	public void performAction(Player p, ItemStack stack) {
		p.closeInventory();
		
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
				
				Resident res = CivGlobal.getResident(player);
				if (res == null) {
					CivLog.warning("Player is not a real resident?: "+player.getName());
					return;
				}
				
				if (LoreGuiItem.getActionData(stack, "tutorialInv") != null) {
					switch (LoreGuiItem.getActionData(stack, "tutorialInv")) {
					case "wiki_mainmenu":
						Tutorial.wiki_mainmenu(player);
						break;
					case "camp_mainmenu":
						Tutorial.camp_mainmenu(player);
						break;
					default:
						CivLog.warning("Unknown GUI type for tutorialInv: '"+LoreGuiItem.getActionData(stack, "tutorialInv")+"'");
						break;
					}
				}
				
				if (LoreGuiItem.getActionData(stack, "campInv") != null) {
					Camp camp = res.getCamp();
					if (camp == null) {
						CivMessage.sendError(p, "You are not a member of a camp, and this inventory requires you to be in a camp to be viewed.");
						return;
					}
					
					switch (LoreGuiItem.getActionData(stack, "campInv")) {
					case "camp_manager_menu":
						camp.openMainMenuGUI(p, camp);
						break;
					default:
						CivLog.warning("Unknown GUI type for campInv: '"+LoreGuiItem.getActionData(stack, "campInv")+"'");
						break;
					}
				}
				
				if (LoreGuiItem.getActionData(stack, "invType") != null) {
					switch (LoreGuiItem.getActionData(stack, "invType")) {
					case "cancel_confirmation":
						CivMessage.send(player, CivColor.Gray+"Cancelled.");
						break;
					case "showBackpackMenu":
						Backpack.openBackpackGUI(player, true);
						break;
					case "showTutorialInventory":
						Backpack.showTutorialInventory(player);
						break;
					case "showCraftingHelp":
						Backpack.showCraftingHelp(player);
						break;
					case "showExperienceHelp":
						Backpack.showExperienceHelp(player);
						break;
//					case "showMiningRates":
//						Backpack.showMiningRates(player);
//						break;
					case "showFishingRates":
						Backpack.showFishingRates(player);
						break;
					case "showTownMenu":
						Backpack.showTownMenu(player);
						break;
					case "openResMail":
						res.openMainMailMenu(player, res);
						break;
					case "showGuiInv":
						String invName = LoreGuiItem.getActionData(stack, "invName");
						Inventory inv = LoreGuiItemListener.guiInventories.get(invName);
						if (inv != null) {
							player.openInventory(inv);
						} else {
							CivLog.error("Couldn't find GUI inventory: "+invName);
						}
						break;
					default:
						CivLog.warning("Unknown GUI type for invType: '"+LoreGuiItem.getActionData(stack, "invType")+"'");
						break;
					}
				}
			}
		}
		
		TaskMaster.syncTask(new SyncTaskDelayed(p.getName(), stack));		
	}

}
