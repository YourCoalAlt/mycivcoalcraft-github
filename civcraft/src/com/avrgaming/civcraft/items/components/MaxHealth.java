package com.avrgaming.civcraft.items.components;

import gpl.AttributeUtil;
import gpl.AttributeUtil.Attribute;
import gpl.AttributeUtil.AttributeType;

public class MaxHealth extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.add(Attribute.newBuilder().name("Speed").type(AttributeType.GENERIC_MAX_HEALTH).amount(this.getDouble("value")).build());
	}
}
