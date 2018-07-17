package com.avrgaming.civcraft.loreenhancements;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class LoreEnhancementVelocity extends LoreEnhancement {
	
	@Override
	public String getInitName() {
		return "LoreEnhancementVelocity";
	}
	
	public String getLoreString(int level) {
		return CivColor.Gray+"  Velocity "+level;
	}
	
	@Override
	public String getDisplayName() {
		return "Velocity";
	}
	
	@Override
	public Integer getMaxLevel() {
		return 1;
	}
	
	public AttributeUtil add(AttributeUtil attrs) {
		attrs.addEnhancement("LoreEnhancementVelocity", null, null);
		attrs.addLore(getLoreString(1));
		return attrs;
	}
	
	@Override
	public void onBlockClick(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if ((event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR)) {
			CivMessage.global("Test 1");
			if (p.isSneaking()) {
				CivMessage.global("Test Pass");
			} else {
				CivMessage.global("Test Fail");
			}
		}
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