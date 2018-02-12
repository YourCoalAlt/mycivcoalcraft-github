/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.config;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.LibraryEnchantment;
import com.avrgaming.civcraft.object.StoreMaterial;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Bank;
import com.avrgaming.civcraft.structure.Granary;
import com.avrgaming.civcraft.structure.Grocer;
import com.avrgaming.civcraft.structure.Library;
import com.avrgaming.civcraft.structure.Quarry;
import com.avrgaming.civcraft.structure.Store;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.Trommel;
import com.avrgaming.civcraft.structure.Warehouse;

public class ConfigTownUpgrade {
	public String id;
	public String name;
	public Double cost;
	public String action;
	public String require_upgrade = null;
	public String require_tech = null;
	public String require_structure = null;
	public String category = null;
	
	public static HashMap<String, Integer> categories = new HashMap<String, Integer>();
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigTownUpgrade> upgrades) {
		upgrades.clear();
		List<Map<?, ?>> culture_levels = cfg.getMapList("upgrades");
		for (Map<?, ?> level : culture_levels) {
			ConfigTownUpgrade town_upgrade = new ConfigTownUpgrade();
			
			town_upgrade.id = (String)level.get("id");
			town_upgrade.name = (String)level.get("name");
			town_upgrade.cost = (Double)level.get("cost");
			town_upgrade.action = (String)level.get("action");
			town_upgrade.require_upgrade = (String)level.get("require_upgrade");
			town_upgrade.require_tech = (String)level.get("require_tech");
			town_upgrade.require_structure = (String)level.get("require_structure");
			town_upgrade.category = (String)level.get("category");
		
			Integer categoryCount = categories.get(town_upgrade.category);
			if (categoryCount == null) {
				categories.put(town_upgrade.category.toLowerCase(), 1);
			} else {
				categories.put(town_upgrade.category.toLowerCase(), categoryCount+1);
			}
			
			upgrades.put(town_upgrade.id, town_upgrade);
		}
		CivLog.info("Loaded "+upgrades.size()+" town upgrades.");		
	}
	
	public void processAction(Town town) throws CivException {
		if (this.action == null) 
			return;
		String[] args = this.action.split(",");

		Structure struct;
		
		switch (args[0]) {
		case "set_town_level":
			if (town.getLevel() < Integer.valueOf(args[1].trim())) {
				town.setLevel(Integer.valueOf(args[1].trim()));
				CivMessage.global(town.getName()+" is now a "+town.getLevelTitle()+"!");
			}
			break;
		
		case "set_bank_level":
			struct = town.getStructureByType("s_bank");
			if (struct != null && (struct instanceof Bank)) {
				Bank bank = (Bank)struct;
				if (bank.getLevel() < Integer.valueOf(args[1].trim())) {
					bank.setLevel(Integer.valueOf(args[1].trim()));
					bank.updateSignText();
					town.saved_bank_level = bank.getLevel();
					CivMessage.sendTown(town, "The Bank is now level "+bank.getLevel());
				}
			}
			break;
		case "set_bank_interest":
			struct = town.getStructureByType("s_bank");
			if (struct != null && (struct instanceof Bank)) {
				Bank bank = (Bank)struct;
				if (bank.getInterestRate() < Double.valueOf(args[1].trim())) {
					bank.setInterestRate(Double.valueOf(args[1].trim()));
					town.saved_bank_interest_amount = bank.getInterestRate();
					DecimalFormat df = new DecimalFormat();
					CivMessage.sendTown(town, "The Bank is now provides a "+df.format(bank.getInterestRate()*100)+"% interest rate.");
				}
			}
			break;
		case "set_store_level":
			struct = town.getStructureByType("s_store");
			if (struct != null && (struct instanceof Store)) {
				Store store = (Store)struct;
				if (store.getLevel() < Integer.valueOf(args[1].trim())) {
					store.setLevel(Integer.valueOf(args[1].trim()));
					store.updateSignText();
					CivMessage.sendTown(town, "The Store is now level "+store.getLevel());
				}
			}
			break;
		case "set_store_material":
			struct = town.getStructureByType("s_store");
			if (struct != null && (struct instanceof Store)) {
				Store store = (Store)struct;
				StoreMaterial mat = new StoreMaterial(args[1].trim(), args[2].trim(), args[3].trim(), args[4].trim());
				store.addStoreMaterial(mat);
				store.updateSignText();
			}
			break;
		case "set_library_level":
			struct = town.getStructureByType("s_library");
			if (struct != null && (struct instanceof Library)) {
				Library library = (Library)struct;
				if (library.getLevel() < Integer.valueOf(args[1].trim())) {
					library.setLevel(Integer.valueOf(args[1].trim()));
					library.updateSignText();
					CivMessage.sendTown(town, "The Library is now level "+library.getLevel());
				}
			}
			break;
		case "enable_library_enchantment":
			struct = town.getStructureByType("s_library");
			if (struct != null && (struct instanceof Library)) {
				Library library = (Library)struct;
				LibraryEnchantment removeEnchant = null;
				LibraryEnchantment enchant = new LibraryEnchantment(args[1].trim(), Integer.valueOf(args[2].trim()), Integer.valueOf(args[3].trim()));
				boolean addEnch = false;
				boolean newEnch = true;
				for (LibraryEnchantment e : library.getEnchants()) {
					if (args[1].trim().equals(e.displayName)) {
						newEnch = false;
						if (e.level >= Integer.valueOf(args[2].trim())) {
							if (CivCraft.isStarted) {
								CivMessage.sendTown(town, "Someone tried upgrading Library with "+e.displayName+" "+Integer.valueOf(args[2].trim())+" when we already have level "+e.level+". "
										+ "Refunded "+this.cost+" coins to the town without upgrading.");
								town.getTreasury().deposit(this.cost);
							} else {
								CivLog.warning("Skip upgrading Library with "+e.displayName+" "+Integer.valueOf(args[2].trim())+" when it already has level "+e.level+". Reason: [Startup Fix 0x00A]");
							}
						} else {
							if (CivCraft.isStarted) {
								ConfigTownUpgrade refund = CivSettings.getUpgradeByNameRegexSpecial(town, "Research "+e.displayName+" "+CivData.getNumeral(e.level));
								CivMessage.sendTown(town, "The Library previously had "+e.displayName+" "+e.level+" when upgrading to level "+Integer.valueOf(args[2].trim())+". "
										+ "Refunded "+refund.cost+" coins to the town for upgrading.");
								town.getTreasury().deposit(refund.cost);
							} else {
								CivLog.warning("Skipping Library previously had "+e.displayName+" "+e.level+" when upgrading to level "+Integer.valueOf(args[2].trim())+". Reason: [Startup Fix 0x00B]");
							}
							removeEnchant = new LibraryEnchantment(e.displayName, e.level, e.price);
							addEnch = true;
						}
					}
				}
				
				if (newEnch) {
					CivMessage.sendTown(town, "The Town now offers "+args[1].trim()+" "+args[2].trim()+" at the Library!");
					addEnch = true;
				}
				
				if (removeEnchant != null) {
					library.removeEnchant(removeEnchant);
				}
				
				if (addEnch) {
					library.addEnchant(enchant);
				}
			}
			town.save();
			break;
		case "set_grocer_level":
			struct = town.getStructureByType("s_grocer");
			if (struct != null && (struct instanceof Grocer)) {
				Grocer grocer = (Grocer)struct;
				if (grocer.getLevel() < Integer.valueOf(args[1].trim())) {
					grocer.setLevel(Integer.valueOf(args[1].trim()));
					grocer.updateSignText();
					CivMessage.sendTown(town, "The Grocer is now level "+grocer.getLevel()+"!");
				}
			}
			break;
		case "saved_structures_default_level":
			for (Structure structure : town.getStructures()) {
				if (structure != null && (structure instanceof Granary)) {
					Granary granary = (Granary)structure;
					if (granary.getLevel() < Integer.valueOf(args[1].trim())) {
						granary.setLevel(Integer.valueOf(args[1].trim()));
						town.saved_structures_default_level = granary.getLevel();
						CivMessage.sendTown(town, "The Town's Default Structure level is now level "+granary.getLevel()+"!");
					}
				}
			}
			break;
		case "set_warehouse_level":
			for (Structure structure : town.getStructures()) {
				if (structure != null && (structure instanceof Warehouse)) {
					Warehouse wh = (Warehouse)structure;
					if (wh.getLevel() < Integer.valueOf(args[1].trim())) {
						wh.setLevel(Integer.valueOf(args[1].trim()));
						town.saved_warehouse_level = wh.getLevel();
						CivMessage.sendTown(town, "The Warehouse is now level "+wh.getLevel()+"!");
					}
				}
			}
			break;
		case "set_quarry_level":
			for (Structure structure : town.getStructures()) {
				if (structure != null && (structure instanceof Quarry)) {
					Quarry quarry = (Quarry)structure;
					if (quarry.getLevel() < Integer.valueOf(args[1].trim())) {
						quarry.setLevel(Integer.valueOf(args[1].trim()));
						town.saved_quarry_level = quarry.getLevel();
						CivMessage.sendTown(town, "Our Quarries are now level "+quarry.getLevel()+"!");
					}
				}
			}
			break;
		case "set_trommel_level":
			for (Structure structure : town.getStructures()) {
				if (structure != null && (structure instanceof Trommel)) {
					Trommel trommel = (Trommel)structure;
					if (trommel.getLevel() < Integer.valueOf(args[1].trim())) {
						trommel.setLevel(Integer.valueOf(args[1].trim()));
						town.saved_trommel_level = trommel.getLevel();
						CivMessage.sendTown(town, "Our Trommels are now level "+trommel.getLevel()+"!");
					}
				}
			}
			break;
		}
	}

	public boolean isAvailable(Town town) {
		if (CivGlobal.testFileFlag("debug-norequire")) {
			CivMessage.global("Ignoring requirements! debug-norequire found.");
			return true;
		}
		
		if (town.hasUpgrade(this.require_upgrade)) {
			if (town.getCiv().hasTechnology(this.require_tech)) {
				if (town.hasStructure(require_structure)) {
					if (!town.hasUpgrade(this.id)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static int getAvailableCategoryCount(String category, Town town) {
		int count = 0;
		
		for (ConfigTownUpgrade upgrade : CivSettings.townUpgrades.values()) {
			if (upgrade.category.equalsIgnoreCase(category) || category.equalsIgnoreCase("all")) {
				if (upgrade.isAvailable(town)) {
					count++;
				}
			}
		}
		
		return count;
	}
		
}
