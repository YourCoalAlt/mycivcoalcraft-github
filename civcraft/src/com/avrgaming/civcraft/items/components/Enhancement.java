package com.avrgaming.civcraft.items.components;

import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivLog;

import gpl.AttributeUtil;

public class Enhancement extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrUtil) {
//		attrUtil.addLore(ChatColor.RESET+CivColor.Gold+"Catalyst");
	}

	public ItemStack getEnchantedItem(ItemStack stack) {
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat == null) {
			return null;
		}
		
		String enhStr = this.getString("enhancement");
		LoreEnhancement enhance = LoreEnhancement.enhancements.get(enhStr);
		if (enhance == null) {
			CivLog.error("Couldn't find enhancement titled:"+enhStr);
			return null;
		}
		
		if (enhance != null) {
			if (enhance.canEnchantItem(stack)) {
				AttributeUtil attrs = new AttributeUtil(stack);
				enhance.variables.put("amount", getString("amount"));
				attrs = enhance.add(attrs);	
				return attrs.getStack();
			}
		}
		return null;
	}
	
	public int getEnhancedLevel(ItemStack stack) {
		String enhStr = this.getString("enhancement");
		LoreEnhancement enhance = LoreEnhancement.enhancements.get(enhStr);
		if (enhance == null) {
			CivLog.error("Couldn't find enhancement titled:"+enhStr);
			return 0;
		}
		return (int)enhance.getLevel(new AttributeUtil(stack));
	}
}
