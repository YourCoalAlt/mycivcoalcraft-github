package com.avrgaming.civcraft.items.components;

import gpl.AttributeUtil;

public class MoveSpeed extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.setCivCraftProperty("modified_speed", String.valueOf(this.getDouble("value")));
	}
}
