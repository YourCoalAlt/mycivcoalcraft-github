package com.avrgaming.civcraft.loreenhancements;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class LoreEnhancementSoulBound extends LoreEnhancement {
	
	public AttributeUtil add(AttributeUtil attrs) {
		attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
		attrs.addLore(CivColor.Gold+getDisplayName());
		return attrs;
	}
	
	public boolean onDeath(final PlayerDeathEvent event, final ItemStack stack) {
		event.getDrops().remove(stack);
		return true;
	};
	
	public boolean canEnchantItem(ItemStack item) {
		return isWeaponOrArmor(item);
	}
	
	public boolean hasEnchantment(ItemStack item) {
		AttributeUtil attrs = new AttributeUtil(item);
		return attrs.hasEnhancement("LoreEnhancementSoulBound");
	}
	
	@Override
	public String getDisplayName() {
		return "Soulbound";
	}
	
	@Override
	public String serialize(ItemStack stack) {
		return "";
	}

	@Override
	public ItemStack deserialize(ItemStack stack, String data) {
		return stack;
	}
}
