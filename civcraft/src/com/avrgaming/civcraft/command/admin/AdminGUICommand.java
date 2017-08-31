package com.avrgaming.civcraft.command.admin;

import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;

public class AdminGUICommand extends CommandBase implements Listener {

	@Override
	public void init() {
		command = "/ad gui";
		displayName = "Admin GUI";
		
		commands.put("test", "Opens the GUI for Admins.");
	}
	
	public void test_cmd() {
		CivMessage.send(sender, "oh");
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		Resident res = CivGlobal.getResident(p);
		
		if (res.getCiv() == null || res == null) {
			return;
		}
		
/*		if (event.getInventory().getName().contains(res.getCiv().getName()+" Research Menu")) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
				return;
			}
			
			if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
				event.setCancelled(true);
			}
			
			switch (event.getCurrentItem().getType()) {
			case BOOK_AND_QUILL:
				res.getTown().getTownHall().openResearchTechGUI(p, event.getCurrentItem().getItemMeta().getDisplayName().toString());
				break;
			case AIR:
				break;
			default:
				break;
			}
		} else {
			return;
		}*/
	}
	
	@SuppressWarnings("unused")
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) throws SQLException {
		Inventory inv = event.getInventory();
		Player p = (Player) event.getPlayer();
		Resident res = CivGlobal.getResident(p);
		Civilization civ = res.getCiv();
		
		if (res.getCiv() == null || res == null) {
			return;
		}
		
/*		if (inv.getName().contains(res.getCiv().getName()+" Research Menu")) {
			inv.clear();
		}
		
		if (inv.getName().contains("Researching ")) {
			boolean addedNotRequiredItems = false;
			String techName = inv.getName().substring(12);
			if (techName != null) {
				ConfigTech techTech = CivSettings.getTechByName(techName);
				Technology tech = CivGlobal.getTechnology(techTech.id+civ.getId());
				int beakersGiven = 0;
				
				for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
					if (stack == null || stack.getType() == Material.AIR) {
						continue;
					}
					
					if (stack.hasItemMeta() && stack.getItemMeta().getDisplayName().contains("Beakers Left")) {
						inv.removeItem(stack);
						continue;
					}
					
					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
					if (craftMat != null && craftMat.getConfigId().toLowerCase().equals("civ_beakers")) { //Collect all the beakers found in the GUI
						beakersGiven += stack.getAmount();
						inv.removeItem(stack);
					} else if (craftMat != null && !craftMat.getConfigId().toLowerCase().equals("civ_beakers")) { //Allow non-beakers but custom items to be dropped
						ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
						newMat.setData(stack.getData());
						p.getWorld().dropItemNaturally(event.getPlayer().getEyeLocation(), newMat);
						addedNotRequiredItems = true;
					} else if (craftMat == null) { //Drop any vanilla items in the inventory
						p.getWorld().dropItemNaturally(event.getPlayer().getEyeLocation(), stack);
						addedNotRequiredItems = true;
					}
				}
				
				if (beakersGiven >= tech.getBeakersLeft()) { //Drop extra beakers collected
					int beakersToDrop = beakersGiven - tech.getBeakersLeft();
					tech.setBeakersLeft(0);
					tech.saveNow();
					civ.addTech(techTech);
					civ.removeTechFromProgress(techTech);
					civ.saveNow();
					CivMessage.global("The civilization of "+civ.getName()+" has completed researching "+techTech.name+"!");
					if (beakersToDrop > 0) {
						for (int i = 0; i < beakersToDrop; i++) {
							ItemStack newMat = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_beakers"));
							p.getWorld().dropItemNaturally(event.getPlayer().getEyeLocation(), newMat);
							addedNotRequiredItems = true;
						}
					}
				} else if (beakersGiven <= tech.getBeakersLeft() && beakersGiven > 0) { //Deposit anything left to the tech
					tech.setBeakersLeft(tech.getBeakersLeft()-beakersGiven);
					tech.saveNow();
					CivGlobal.removeTechnology(tech);
					CivGlobal.addTechnology(tech);
					CivMessage.sendCiv(civ, "Our research on "+techTech.name+" recived "+beakersGiven+" beakers. It now has "+tech.getBeakersLeft()+" beakers left to completion.");
				} else if (beakersGiven <= 0) {
					CivMessage.sendError(p, "Please deposit beakers in order to research!");
				}
			}
			
			if (addedNotRequiredItems == true) {
				CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
			}
		}*/
		
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
		//Admin is checked in parent command
	}
}
