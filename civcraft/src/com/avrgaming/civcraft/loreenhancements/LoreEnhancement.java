package com.avrgaming.civcraft.loreenhancements;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.BuildableDamageBlock;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public abstract class LoreEnhancement {
	
	public AttributeUtil add(AttributeUtil attrs) {
		return attrs;
	}
	
	public static HashMap<String, LoreEnhancement> enhancements = new HashMap<String, LoreEnhancement>();
	public HashMap<String, String> variables = new HashMap<String, String>();
	
	public static void init() {
		enhancements.put("LoreEnhancementVelocity", new LoreEnhancementVelocity());
		
		enhancements.put("LoreEnhancementUnitGainAttack", new LoreEnhancementUnitGainAttack());
		enhancements.put("LoreEnhancementUnitGainProtection", new LoreEnhancementUnitGainProtection());
		
		enhancements.put("LoreEnhancementSoulBound", new LoreEnhancementSoulBound());
		enhancements.put("LoreEnhancementPunchout", new LoreEnhancementPunchout());
		
		enhancements.put("LoreEnhancementProspect", new LoreEnhancementProspect());
	}
	
	public abstract String getInitName();
	public abstract String getDisplayName();
	public abstract Integer getMaxLevel();
	
	public boolean onDeath(PlayerDeathEvent event, ItemStack stack) { return false; }
	public void onBlockClick(PlayerInteractEvent event) { }

	public boolean canEnchantItem(ItemStack item) {
		return true;
	}
	
	public static boolean isSword(ItemStack item) {
		if (item == null) return false;
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
		if (item == null) return false;
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
		if (item == null) return false;
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
		if (item == null) return false;
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
		if (item == null) return false;
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
		if (item == null) return false;
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
		if (item == null) return false;
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
	
	public int onStructureBlockBreak(BuildableDamageBlock dmgBlock, int damage) {
		return damage;
	}
	
	
	public static ItemStack deductLivesAndDurability(Player p, ItemStack stack, double percent, boolean msg) {
		AttributeUtil attrs = new AttributeUtil(stack);
		// if person died within last 4 seconds, do not take damage to prevent bug.
		if ((System.currentTimeMillis() - Long.valueOf(attrs.getCivCraftProperty("last_death"))) <= 4*1000) {
			CivMessage.send(p, CivColor.GrayBold+" » "+CivColor.DarkGrayItalic+"You died too fast, not deducting durability life.");
			return stack;
		}
		
		int lives_left = (int) (1 / percent) - 1;
		if (attrs.getCivCraftProperty("lives_left") != null) {
			lives_left = Integer.valueOf(attrs.getCivCraftProperty("lives_left")) - 1;
			attrs.setCivCraftProperty("lives_left", String.valueOf(lives_left));
		}
		
		if (lives_left < 0) {
			attrs.getStack().setType(Material.BEDROCK);
			if (msg) CivMessage.send(p, CivColor.GrayBold+" » "+attrs.getName()+CivColor.GrayBold+" has "+
					CivColor.YellowBold+"ran out of lives"+CivColor.GrayBold+" and broke!");
		} else {
			String saved = "";
			for (String l : attrs.getLore()) {
				if (!l.contains(" Lives Left")) {
					saved += l+";";
				} else {
					saved += CivColor.YellowBold+lives_left+CivColor.LightGreen+" Lives Left;";
				}
			}
			attrs.setLore(saved.split(";"));
			int reduction = (int)(attrs.getStack().getType().getMaxDurability()*percent);
			attrs.getStack().setDurability((short)(attrs.getStack().getDurability() + reduction));
			
			if (msg) CivMessage.send(p, CivColor.GrayBold+" » "+attrs.getName()+CivColor.GrayBold+" has "+
					CivColor.YellowBold+lives_left+CivColor.GrayBold+" Lives until it breaks!");
		}
		
		return attrs.getStack();
	}
	
	public static ItemStack resetLivesWithDurability(Player p, ItemStack stack, boolean msg) {
		AttributeUtil attrs = new AttributeUtil(stack);
		
		double percent;
		if (attrs.getCivCraftProperty("death_percent_value") != null) {
			percent = Double.valueOf(attrs.getCivCraftProperty("death_percent_value"));
		} else {
			CivLog.warning("Death percent null for "+stack+" for "+p.getName());
			percent = 0.1;
		}
		
		int durabilityLeft = stack.getType().getMaxDurability() - stack.getDurability();
		int dmgpert = (durabilityLeft*100) / stack.getType().getMaxDurability();
		int livesLeft = (int) (dmgpert / (percent*100)) - 1;
		
		String saved = "";
		for (String l : attrs.getLore()) {
			if (!l.contains(" Lives Left")) saved += l+";";
		}
		
		saved += CivColor.YellowBold+livesLeft+CivColor.LightGreen+" Lives Left";
		attrs.setLore(saved.split(";"));
		return attrs.getStack();
	}
	
	public double getLevelDouble(AttributeUtil attrs) { return 0; }
	public Integer getLevel(AttributeUtil attrs) { return 0; }
	public abstract String serialize(ItemStack stack);
	public abstract ItemStack deserialize(ItemStack stack, String data);
	
}
