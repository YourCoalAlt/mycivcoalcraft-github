package com.avrgaming.civcraft.items.components;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.ItemChangeResult;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class DurabilityOnDeath extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		int livesLeft = (int) (1 / this.getDouble("value"));
		attrs.addLore(CivColor.YellowBold+(livesLeft)+CivColor.LightGreen+" Lives Left");
		attrs.setCivCraftProperty("death_percent_value", String.valueOf(this.getDouble("value")));
	}
	
	@Override
	public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemChangeResult result, ItemStack sourceStack) {
		if (result == null) {
			result = new ItemChangeResult();
			result.stack = sourceStack;
			result.destroyItem = false;
		}
		
		// No need to destroy item, we will do it now inside of this (via web)
		result.stack = LoreEnhancement.getItemLivesLeftViaDurability(event.getEntity(), result.stack, true);
		
		return result;
	}
}
