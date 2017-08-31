package com.avrgaming.civcraft.items.components;

import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class EnhancedBook extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(CivColor.LightGrayItalic+this.getString("type")+" "+this.getInt("lvl"));
	}
}
