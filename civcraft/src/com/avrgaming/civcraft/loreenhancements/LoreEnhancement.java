package com.avrgaming.civcraft.loreenhancements;

import java.util.HashMap;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.object.BuildableDamageBlock;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public abstract class LoreEnhancement {
	
	public AttributeUtil add(AttributeUtil attrs) {
		return attrs;
	}
	
	public static HashMap<String, LoreEnhancement> enhancements = new HashMap<String, LoreEnhancement>();
	public HashMap<String, String> variables = new HashMap<String, String>();
	
	public static void init() {
		enhancements.put("LoreEnhancementSharpness", new LoreEnhancementSharpness());
		enhancements.put("LoreEnhancementProtection", new LoreEnhancementProtection());
		
		enhancements.put("LoreEnhancementUnitGainAttack", new LoreEnhancementUnitGainAttack());
		
		enhancements.put("LoreEnhancementSoulBound", new LoreEnhancementSoulBound());
		enhancements.put("LoreEnhancementAttack", new LoreEnhancementAttack());
		enhancements.put("LoreEnhancementDefense", new LoreEnhancementDefense());
		enhancements.put("LoreEnhancementPunchout", new LoreEnhancementPunchout());
	}
	
	public boolean onDeath(PlayerDeathEvent event, ItemStack stack) { return false; }

	public boolean canEnchantItem(ItemStack item) {
		return true;
	}
	
	public static boolean isSword(ItemStack item) {
		switch (ItemManager.getId(item)) {
		case CivData.WOOD_SWORD:
		case CivData.STONE_SWORD:
		case CivData.IRON_SWORD:
		case CivData.GOLD_SWORD:
		case CivData.DIAMOND_SWORD:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isBow(ItemStack item) {
		switch (ItemManager.getId(item)) {
		case CivData.BOW:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isWeapon(ItemStack item) {
		return isSword(item) || isBow(item);
	}
	
	public static boolean isArmor(ItemStack item) {
		return isHelmet(item) || isChestplate(item) || isLeggings(item) || isBoots(item);
	}
	
	public static boolean isHelmet(ItemStack item) {
		switch (ItemManager.getId(item)) {
		case CivData.LEATHER_HELMET:
		case CivData.CHAIN_HELMET:
		case CivData.GOLD_HELMET:
		case CivData.IRON_HELMET:
		case CivData.DIAMOND_HELMET:
		case CivData.ELYTRA:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isChestplate(ItemStack item) {
		switch (ItemManager.getId(item)) {
		case CivData.LEATHER_CHESTPLATE:
		case CivData.CHAIN_CHESTPLATE:
		case CivData.GOLD_CHESTPLATE:
		case CivData.IRON_CHESTPLATE:
		case CivData.DIAMOND_CHESTPLATE:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isLeggings(ItemStack item) {
		switch (ItemManager.getId(item)) {
		case CivData.LEATHER_LEGGINGS:
		case CivData.CHAIN_LEGGINGS:
		case CivData.GOLD_LEGGINGS:
		case CivData.IRON_LEGGINGS:
		case CivData.DIAMOND_LEGGINGS:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isBoots(ItemStack item) {
		switch (ItemManager.getId(item)) {
		case CivData.LEATHER_BOOTS:
		case CivData.CHAIN_BOOTS:
		case CivData.GOLD_BOOTS:
		case CivData.IRON_BOOTS:
		case CivData.DIAMOND_BOOTS:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isTool(ItemStack item) {
		switch (ItemManager.getId(item)) {
		case CivData.WOOD_SHOVEL:
		case CivData.WOOD_PICKAXE:
		case CivData.WOOD_AXE:
		case CivData.STONE_SHOVEL:
		case CivData.STONE_PICKAXE:
		case CivData.STONE_AXE:
		case CivData.IRON_SHOVEL:
		case CivData.IRON_PICKAXE:
		case CivData.IRON_AXE:
		case CivData.DIAMOND_SHOVEL:
		case CivData.DIAMOND_PICKAXE:
		case CivData.DIAMOND_AXE:
		case CivData.GOLD_SHOVEL:
		case CivData.GOLD_PICKAXE:
		case CivData.GOLD_AXE:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isWeaponOrArmor(ItemStack item) {
		return isWeapon(item) || isArmor(item);
	}

	public boolean hasEnchantment(ItemStack item) {
		return false;
	}

	public abstract String getDisplayName();

	public int onStructureBlockBreak(BuildableDamageBlock dmgBlock, int damage) {
		return damage;
	}

	public double getLevelDouble(AttributeUtil attrs) { return 0; }
	public Integer getLevel(AttributeUtil attrs) { return 0; }
	public abstract String serialize(ItemStack stack);
	public abstract ItemStack deserialize(ItemStack stack, String data);
	
	
}
