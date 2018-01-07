package com.avrgaming.civcraft.loreenhancements;

import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class LoreEnhancementUnitGainProtection extends LoreEnhancement {
	
	public LoreEnhancementUnitGainProtection() {
		this.variables.put("amount", "1");
	}
	
	public String getLoreString(int level) {
		return CivColor.LightGray+"  Unit Protection +"+level;
	}
	
	@Override
	public String getDisplayName() {
		return "UnitProtection";
	}
	
	@Override
	public AttributeUtil add(AttributeUtil attrs) {		
		// Look for any existing protection enhancements. If we have one, add on to it instead of making a new one.
		
		Integer amount = Integer.valueOf(this.variables.get("amount"));
		Integer baseLevel = amount;
		if (attrs.hasEnhancement("LoreEnhancementUnitGainProtection")) {
			
			// Get base Level
			baseLevel = Integer.valueOf(attrs.getEnhancementData("LoreEnhancementUnitGainProtection", "level"));

			// Reset the lore
			String[] lore = attrs.getLore();
			for (int i = 0; i < lore.length; i++) {
				if (lore[i].equals(getLoreString(baseLevel))) {
					lore[i] = getLoreString(baseLevel+amount);
				}
			}
			attrs.setLore(lore);
			
			// Store the data back in the enhancement
			attrs.setEnhancementData("LoreEnhancementUnitGainProtection", "level", ""+(baseLevel+amount));
		} else {
			attrs.addEnhancement("LoreEnhancementUnitGainProtection", "level", ""+baseLevel);
			attrs.addLore(getLoreString(baseLevel));
		}

		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(attrs.getCivCraftProperty("mid"));
		if (craftMat == null) {
			CivLog.warning("Couldn't find craft mat with MID of:"+attrs.getCivCraftProperty("mid"));
			return attrs;
		}

		return attrs;
	}
	
	@Override
	public Integer getLevel(AttributeUtil attrs) {	
		if (attrs.hasEnhancement("LoreEnhancementUnitGainProtection")) {
			// Get base Level.
			int level = Integer.valueOf(attrs.getEnhancementData("LoreEnhancementUnitGainProtection", "level")); 
			return level;
		}
		return 0;
	}
	
	@Override
	public boolean canEnchantItem(ItemStack item) {
		return isSword(item);
	}
	
	public Integer getExtraDamage(AttributeUtil attrs) {
		return getLevel(attrs);
	}
	
	@Override
	public String serialize(ItemStack stack) {
		AttributeUtil attrs = new AttributeUtil(stack);
		return attrs.getEnhancementData("LoreEnhancementUnitGainProtection", "level");
	}
	
	@Override
	public ItemStack deserialize(ItemStack stack, String data) {
		AttributeUtil attrs = new AttributeUtil(stack);
		attrs.setEnhancementData("LoreEnhancementUnitGainProtection", "level", data);
		return attrs.getStack();
	}
}
