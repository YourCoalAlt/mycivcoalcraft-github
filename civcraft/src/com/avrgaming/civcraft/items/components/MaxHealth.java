package com.avrgaming.civcraft.items.components;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import gpl.AttributeUtil;
import gpl.AttributeUtil.AttributeType;

public class MaxHealth extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		ItemStack stack = attrs.getStack();
		double value = this.getDouble("value");
		
		if (EnchantmentTarget.ARMOR_HEAD.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Health").type(AttributeType.GENERIC_MAX_HEALTH).amount(value).slot("head").build());
		} else if (EnchantmentTarget.ARMOR_TORSO.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Health").type(AttributeType.GENERIC_MAX_HEALTH).amount(value).slot("chest").build());
		} else if (EnchantmentTarget.ARMOR_LEGS.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Health").type(AttributeType.GENERIC_MAX_HEALTH).amount(value).slot("legs").build());
		} else if (EnchantmentTarget.ARMOR_FEET.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Health").type(AttributeType.GENERIC_MAX_HEALTH).amount(value).slot("feet").build());
		} else {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Health").type(AttributeType.GENERIC_MAX_HEALTH).amount(value).slot("offhand").build());
		}
	}
}
