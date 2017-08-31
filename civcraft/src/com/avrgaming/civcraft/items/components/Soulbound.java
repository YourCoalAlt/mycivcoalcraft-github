package com.avrgaming.civcraft.items.components;

import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class Soulbound extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		//LoreMaterial.addEnhancement(attrs.getStack(), LoreEnhancement.enhancements.get("LoreEnhancementSoulBound"));
		attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
		attrs.addLore(CivColor.Gold+"Soulbound");
	}
}
