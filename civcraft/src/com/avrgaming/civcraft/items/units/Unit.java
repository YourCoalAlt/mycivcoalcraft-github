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
package com.avrgaming.civcraft.items.units;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.util.CivItem;

public abstract class Unit {
	
	public static Spy SPY_UNIT;
	public static Settler SETTLER_UNIT;
	public static Warrior WARRIOR_UNIT;
	public static Archer ARCHER_UNIT;
	
	public static void init() throws InvalidConfiguration {
		SPY_UNIT = new Spy("u_spy", CivSettings.units.get("u_spy"));
		SETTLER_UNIT = new Settler("u_settler", CivSettings.units.get("u_settler"));
		
		WARRIOR_UNIT = new Warrior("u_warrior", CivSettings.units.get("u_warrior"));
		ARCHER_UNIT = new Archer("u_archer", CivSettings.units.get("u_archer"));
		
		Unit.enhancement_unit_attack_amt = CivSettings.getDouble(CivSettings.unitConfig, "unit_info.enhancement_unit_attack_amt");
		Unit.enhancement_unit_protect_amt = CivSettings.getDouble(CivSettings.unitConfig, "unit_info.enhancement_unit_protect_amt");
		
		Unit.warrior_atk_dmg = CivSettings.getDouble(CivSettings.unitConfig, "unit_info.warrior.atk_dmg");
		Unit.warrior_bow_dmg = CivSettings.getDouble(CivSettings.unitConfig, "unit_info.warrior.bow_dmg");
		Unit.archer_atk_dmg = CivSettings.getDouble(CivSettings.unitConfig, "unit_info.archer.atk_dmg");
		Unit.archer_bow_dmg = CivSettings.getDouble(CivSettings.unitConfig, "unit_info.archer.atk_dmg");
	}
	
	public static double enhancement_unit_attack_amt;
	public static double enhancement_unit_protect_amt;
	
	public static double warrior_atk_dmg;
	public static double warrior_bow_dmg;
	
	public static double archer_atk_dmg;
	public static double archer_bow_dmg;
	
	protected static boolean addItemNoStack(Inventory inv, ItemStack stack) {
			ItemStack[] contents = inv.getContents();
			for (int i = 0; i < contents.length; i++) {
				if (contents[i] == null) {
					contents[i] = stack;
					inv.setContents(contents);
					return true;
				}
			}
			
			return false;
	}

	public static ConfigUnit getPlayerUnit(Player player) {
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack == null) {
				continue;
			}
			
			LoreMaterial material = LoreMaterial.getMaterial(stack);
			if (material != null && (material instanceof UnitMaterial)) {
				if(!UnitMaterial.validateUnitUse(player, stack)) {
					return null;
				}
				return ((UnitMaterial)material).getUnit();
			}
		}
		return null;
	}
	
	public static ItemStack getPlayerUnitStack(Player player) {
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack == null) {
				continue;
			}
			
			LoreMaterial material = LoreMaterial.getMaterial(stack);
			if (material != null && (material instanceof UnitMaterial)) {
				if(!UnitMaterial.validateUnitUse(player, stack)) {
					return null;
				}
				return stack;
			}
		}
		return null;
	}
	
	public static ConfigUnit getUnit(ItemStack stack) {
		if (stack == null) { return null; }
		LoreMaterial material = LoreMaterial.getMaterial(stack);
		if (material != null && (material instanceof UnitMaterial)) {
			return ((UnitMaterial)material).getUnit();
		}
		return null;
	}
	
	public static boolean isUnit(ItemStack stack) {
		if (stack == null) { return false; }
		LoreMaterial material = LoreMaterial.getMaterial(stack);
		if (material != null && (material instanceof UnitMaterial)) {
			return true;
		}
		return false;
	}
	
	public static void removeUnit(Player player) {
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack != null) {
				LoreMaterial material = LoreMaterial.getMaterial(stack);
				if (material != null) {
					if (material instanceof UnitMaterial) {
						player.getInventory().remove(stack);
						continue;
					}
					
					if (material instanceof UnitItemMaterial) {
						player.getInventory().remove(stack);
						continue;
					}
					
				}
			}
		}
		player.updateInventory();
	}
	
	public static boolean isWearingAnyLeather(Player player) {
		if (isWearingFullComposite(player)) return true;
		if (isWearingFullHardened(player)) return true;
		if (isWearingFullRefined(player)) return true;
		if (isWearingFullBasicLeather(player)) return true;
		return false;
	}
	
	
	public static boolean isWearingFullComposite(Player player) {
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_composite_leather_helmet"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_composite_leather_chestplate"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_composite_leather_leggings"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_composite_leather_boots"))) {
				return false;
			}
			
		}
		return true;	
	}
	
	public static boolean isWearingFullHardened(Player player) {
		
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_hardened_leather_helmet"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_hardened_leather_chestplate"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_hardened_leather_leggings"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_hardened_leather_boots"))) {
				return false;
			}
			
		}
		return true;	
	}
	
	public static boolean isWearingFullRefined(Player player) {
		
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_refined_leather_helmet"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_refined_leather_chestplate"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_refined_leather_leggings"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_refined_leather_boots"))) {
				return false;
			}
			
		}
		return true;	
	}
	
	public static boolean isWearingFullBasicLeather(Player player) {
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_leather_helmet"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_leather_chestplate"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_leather_leggings"))) {
				return false;
			}
			
			if ((!craftMat.getConfigId().equals("mat_leather_boots"))) {
				return false;
			}
			
		}
		return true;	
	}
	
	// TODO have a resident.isWearingMetal to check for bow useage instead of this, get value set when modifying speed.
//	public static boolean isWearingAnyMetal(Player player) {
//		return isWearingAnyChain(player) || isWearingAnyIron(player) || isWearingAnyGold(player) || isWearingAnyDiamond(player);
//	}
	
	public static Double speedWearingAnyChain(Player p, double mod) {
		double total = 0.0;
		if (p.getEquipment().getBoots() != null && CivItem.getId(p.getEquipment().getBoots()) == CivData.CHAIN_BOOTS) total += mod;
		if (p.getEquipment().getLeggings() != null && CivItem.getId(p.getEquipment().getLeggings()) == CivData.CHAIN_LEGGINGS) total += mod;
		if (p.getEquipment().getChestplate() != null && CivItem.getId(p.getEquipment().getChestplate()) == CivData.CHAIN_CHESTPLATE) total += mod;
		if (p.getEquipment().getHelmet() != null && CivItem.getId(p.getEquipment().getHelmet()) == CivData.CHAIN_HELMET) total += mod;
		return total;
	}
	
	public static Double speedWearingAnyIron(Player p, double mod) {
		double total = 0.0;
		if (p.getEquipment().getBoots() != null && CivItem.getId(p.getEquipment().getBoots()) == CivData.IRON_BOOTS) total += mod;
		if (p.getEquipment().getLeggings() != null && CivItem.getId(p.getEquipment().getLeggings()) == CivData.IRON_LEGGINGS) total += mod;
		if (p.getEquipment().getChestplate() != null && CivItem.getId(p.getEquipment().getChestplate()) == CivData.IRON_CHESTPLATE) total += mod;
		if (p.getEquipment().getHelmet() != null && CivItem.getId(p.getEquipment().getHelmet()) == CivData.IRON_HELMET) total += mod;
		return total;
	}
	
	public static Double speedWearingAnyGold(Player p, double mod) {
		double total = 0.0;
		if (p.getEquipment().getBoots() != null && CivItem.getId(p.getEquipment().getBoots()) == CivData.GOLD_BOOTS) total += mod;
		if (p.getEquipment().getLeggings() != null && CivItem.getId(p.getEquipment().getLeggings()) == CivData.GOLD_LEGGINGS) total += mod;
		if (p.getEquipment().getChestplate() != null && CivItem.getId(p.getEquipment().getChestplate()) == CivData.GOLD_CHESTPLATE) total += mod;
		if (p.getEquipment().getHelmet() != null && CivItem.getId(p.getEquipment().getHelmet()) == CivData.GOLD_HELMET) total += mod;
		return total;
	}
	public static Double speedWearingAnyDiamond(Player p, double mod) {
		double total = 0.0;
		if (p.getEquipment().getBoots() != null && CivItem.getId(p.getEquipment().getBoots()) == CivData.DIAMOND_BOOTS) total += mod;
		if (p.getEquipment().getLeggings() != null && CivItem.getId(p.getEquipment().getLeggings()) == CivData.DIAMOND_LEGGINGS) total += mod;
		if (p.getEquipment().getChestplate() != null && CivItem.getId(p.getEquipment().getChestplate()) == CivData.DIAMOND_CHESTPLATE) total += mod;
		if (p.getEquipment().getHelmet() != null && CivItem.getId(p.getEquipment().getHelmet()) == CivData.DIAMOND_HELMET) total += mod;
		return total;
	}
}
