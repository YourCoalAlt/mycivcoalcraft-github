package com.avrgaming.civcraft.items.components;

import org.bukkit.Material;
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
		attrs.setCivCraftProperty("lives_left", String.valueOf(livesLeft));
		attrs.setCivCraftProperty("death_percent_value", String.valueOf(this.getDouble("value")));
		attrs.setCivCraftProperty("last_death", String.valueOf((System.currentTimeMillis()- 4*1000)));
	}
	
	@Override
	public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemChangeResult result, ItemStack sourceStack) {
		if (result == null) {
			result = new ItemChangeResult();
			result.stack = sourceStack;
			result.destroyItem = false;
		}
		
		if (result.destroyItem) return result;
		
		ItemStack newStack = LoreEnhancement.deductLivesAndDurability(event.getEntity(), result.stack, this.getDouble("value"), true);
		if (newStack == null || newStack.getType() == Material.BEDROCK) {
			result.destroyItem = true;
		} else {
			result.stack = newStack;
			sourceStack.setType(newStack.getType());
			sourceStack.setData(newStack.getData());
			sourceStack.setItemMeta(newStack.getItemMeta());
			sourceStack.setDurability(newStack.getDurability());
		}
		
		return result;
	}

}