package com.avrgaming.civcraft.loregui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuildableInfo;
import com.avrgaming.civcraft.config.ConfigTech;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class _BuildingStructureBuildInventory implements GuiAction {
	
	static Inventory guiInventory;
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		Resident res = CivGlobal.getResident(p);
		Town t = res.getTown();
		guiInventory = Bukkit.getServer().createInventory(p,9*6, "Global Structure Info");
		
		for (int i = 0; i < 9*6; i++) {
			ItemStack is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		for (ConfigBuildableInfo b : CivSettings.structures.values()) {
			if (!res.hasTown()) {
				ItemStack is = LoreGuiItem.build(b.displayName, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must belong to a town build a structure.");
				guiInventory.setItem(b.position, is);
			} else if (!res.getTown().isMayor(res) && !res.getTown().isAssistant(res)) {
				ItemStack is = LoreGuiItem.build(b.displayName, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must be town mayor/assistant to build structures.");
				guiInventory.setItem(b.position, is);
			} else {
				String out = "";
				
				if (t.hasEnough(b.cost)) {
					out += CivColor.Green+"Cost: "+CivColor.LightGreen+b.cost+" Coins;";
				} else {
					out += CivColor.Red+"Cost: "+CivColor.Rose+b.cost+" Coins;";
				}
				
				out += CivColor.LightGreen+"       "+b.hammer_cost+" Hammers;";
				out += CivColor.Green+"Upkeep: "+CivColor.LightGreen+b.upkeep+" Coins/Day;";
				
				ConfigTech tech = CivSettings.techs.get(b.require_tech);
				if (tech == null) {
					out += CivColor.Green+"Required Tech: "+CivColor.YellowItalic+"None;";
				} else {
					if (!res.getCiv().hasTechnology(tech.id)) {
						out += CivColor.Red+"Required Tech: "+CivColor.Rose+tech.name+";";
					} else {
						out += CivColor.Green+"Required Tech: "+CivColor.LightGreen+tech.name+";";
					}
				}
				
				if (b.limit != 0) {
					if (t.getStructureTypeCount(b.id) >= b.limit) {
						out += CivColor.Rose+"Town Limit: "+CivColor.Rose+t.getStructureTypeCount(b.id)+" / "+b.limit+";";
					} else {
						out += CivColor.Green+"Town Limit: "+CivColor.LightGreen+t.getStructureTypeCount(b.id)+" / "+b.limit+";";
					}
				} else {
					out += CivColor.Green+"Limit Per Town: "+CivColor.LightGreen+t.getStructureTypeCount(b.id)+" / "+"Unlimited;";
				}
				
				out += CivColor.Green+"Max Hitpoints: "+CivColor.LightGreen+b.max_hitpoints+";";
				out += CivColor.Green+"Score Points: "+CivColor.LightGreen+b.points+";";
				out += CivColor.Gold+"<Click To Build>";
				
				ItemStack si = LoreGuiItem.build(b.displayName, b.itemId, b.itemData, out.split(";"));
				si = LoreGuiItem.setAction(si, "_BuildChooseStructureTemplate");
				si = LoreGuiItem.setActionData(si, "info", b.id);
				guiInventory.setItem(b.position, si);
			}
		}
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Build Menu");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", _BuildingInventory.guiInventory.getName());
		guiInventory.setItem((9*6)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//@Override
	public void performAct1ion(InventoryClickEvent event, ItemStack stack) {
		Player player = (Player)event.getWhoClicked();
		Resident res = CivGlobal.getResident(player);
		Inventory guiInventory = Bukkit.getServer().createInventory(player,9*6, "Build a Structure");
		
		for (ConfigBuildableInfo info : CivSettings.structures.values()) {
			int type = ItemManager.getId(Material.ANVIL);
			int data = info.itemData;
			if (info.itemId != 0) {
				type = info.itemId;
			}
			
			ItemStack is;
			if (!res.hasTown()) {
				is = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must belong to a town to view structure info.");
				guiInventory.addItem(is);
			} else if (!res.getTown().isMayor(res) && !res.getTown().isAssistant(res)) {
				is = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must be a town mayor or assistant build structures.");
				guiInventory.setItem(info.position, is);
			} else {
				String out = "";
				ConfigTech tech = CivSettings.techs.get(info.require_tech);
				if (tech == null) {
					out += CivColor.Green+"Required Tech: "+CivColor.YellowItalic+"None;";
				} else {
					if (!res.getCiv().hasTechnology(tech.id)) {
						out += CivColor.Red+"Required Tech: "+CivColor.Rose+tech.name+";";
					} else {
						out += CivColor.Green+"Required Tech: "+CivColor.LightGreen+tech.name+";";
					}
				}
				
				
				
				
				
					//ConfigTech tc = CivSettings.techs.get(tech);
					//is = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.PAPER), 0, CivColor.Rose+"Requires: "+tech.replace("tech_", ""));
					//guiInventory.setItem(info.position, is);
				//} else if (res.getTown().getStructureTypeCount(info.id) >= info.limit && info.limit != 0) {
				//	is = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.BARRIER), 0, CivColor.Rose+"Max Limit ("+info.limit+"/"+info.limit+")");
				//	guiInventory.setItem(info.position, is);
				//} else if (!info.isAvailable(res.getTown())) {
				//	is = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.BARRIER), 0, CivColor.Rose+"Not available", "Other Reason");
				//	guiInventory.setItem(info.position, is);
				//} else {
				
				
				out += CivColor.Gold+"<Click To Build>";
				is = LoreGuiItem.build(info.displayName, type, data, out.split(";"));
				is = LoreGuiItem.setAction(is, "_1BuildChooseStructureTemplate");
				is = LoreGuiItem.setActionData(is, "info", info.id);
//					AttributeUtil attrs = new AttributeUtil(is);
//					attrs.setShiny();
//				is = attrs.getStack();
				guiInventory.setItem(info.position, is);
			}
		}
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Build Menu");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", _BuildingInventory.guiInventory.getName());
		guiInventory.setItem((9*6)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
	}
}