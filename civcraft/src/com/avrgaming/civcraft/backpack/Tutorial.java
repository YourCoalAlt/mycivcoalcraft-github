package com.avrgaming.civcraft.backpack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class Tutorial {
	
	private static ItemStack back_head = ItemManager.spawnPlayerHead("MHF_ArrowRight");
	
	public static void wiki_mainmenu(Player p) {
		Inventory inv = Bukkit.createInventory(null, InventoryType.DISPENSER, "Wiki - Main Menu");
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", CivData.PAPER, 0,
				CivColor.RESET+"Use this in-game wiki (which uses some imports from the code so you have the latest information!), to cover all your needs for having an increased chance"+
								"of more fun!"));
		
		ItemStack camp_mainmenu = LoreGuiItem.build(CivColor.GrayBold+"Camps", CivData.MINECART_WITH_FURNACE, 0,
				CivColor.RESET+"This menu contains the basics of camps, including how to make a civ, camp upgrades, and camp management.");
		camp_mainmenu = LoreGuiItem.setAction(camp_mainmenu, "OpenInventory");
		camp_mainmenu = LoreGuiItem.setActionData(camp_mainmenu, "tutorialInv", "camp_mainmenu");
		inv.addItem(camp_mainmenu);
		
		ItemStack civ_mainmenu = LoreGuiItem.build(CivColor.LightPurpleBold+"Civilizations", CivData.GOLD_HORSE_ARMOR, 0,
				CivColor.RESET+"This menu contains the basics of civs, including governments, techs, civics, diplomatic relations, civ-wide taxing, and civ mail.");
		inv.addItem(civ_mainmenu);
		
		ItemStack town_mainmenu = LoreGuiItem.build(CivColor.LightBlueBold+"Towns", CivData.IRON_HORSE_ARMOR, 0,
				CivColor.RESET+"This menu contains the basics of towns, including structures, upkeep, town stats (happiness, culture etc), claiming/plots, and town mail.");
		inv.addItem(town_mainmenu);
		
		ItemStack res_mainmenu = LoreGuiItem.build(CivColor.LightGreenBold+"Residents", CivData.ARMOR_STAND, 0,
				CivColor.RESET+"This menu contains the basics of residents, including friends, toggles, and resident mail.");
		inv.addItem(res_mainmenu);
		
		ItemStack mobsfarms_mainmenu = LoreGuiItem.build(CivColor.GoldBold+"Mobs And Farms", CivData.FARMLAND, 0,
				CivColor.RESET+"This menu contains the basics of mobs and farms, including custom mobs, mob spawners, and crop farming rules.");
		inv.addItem(mobsfarms_mainmenu);
		
		ItemStack backButton = LoreGuiItem.buildWithStack("Back", back_head, "Back to Backpack");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showBackpackMenu");
		inv.setItem(InventoryType.DISPENSER.getDefaultSize()-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
		if (p != null && p.isOnline() && p.isValid()) p.openInventory(inv);
	}
	
	public static void camp_mainmenu(Player p) {
		Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST, CivColor.GrayBold+"Camp - Main Menu");
		
		ItemStack camp_craftitem = LoreGuiItem.build(CivColor.RESET+"Founding City-State Flag", CivData.MINECART_WITH_FURNACE, 0,
				"Special", CivColor.RESET+"The following items will be required by the Camp Villager to get the flag: ");
		inv.addItem(camp_craftitem);
		
		ItemStack camp_upgrade_general = LoreGuiItem.build(CivColor.RESET+"Camp Upgrades", CivData.SIGN_ITEM, 0,
				CivColor.RESET+"Camp upgrades help you accelerate faster to your dreams of becoming a civ! The following is information for the upgrades. (Note: "+
					"The prices for all upgrades can be seen in the camp villager or by doing the '/camp upgrade buy' command)");
		inv.setItem(9, camp_upgrade_general);
		
		ItemStack camp_upgrade_sifter = LoreGuiItem.build(CivColor.RESET+"Sifter", CivData.GRAVEL, 0,
				CivColor.RESET+"The sifter allows the input of cobblestone only, which will output gravel, but as well as other goodies similar to a Trommel, but not as good of a rate (or items).");
		inv.setItem(10, camp_upgrade_sifter);
		
		ItemStack backButton = LoreGuiItem.buildWithStack("Back", back_head, "Back to Wiki Menu");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "tutorialInv", "wiki_mainmenu");
		inv.setItem((InventoryType.CHEST.getDefaultSize())-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
		if (p != null && p.isOnline() && p.isValid()) p.openInventory(inv);
	}
	
}
