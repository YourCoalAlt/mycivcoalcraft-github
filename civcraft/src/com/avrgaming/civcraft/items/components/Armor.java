package com.avrgaming.civcraft.items.components;

import java.text.DecimalFormat;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementUnitGainProtection;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;
import gpl.AttributeUtil.AttributeType;

public class Armor extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		ItemStack stack = attrs.getStack();
		double value = this.getDouble("value");
		
		attrs.clear();
		if (EnchantmentTarget.ARMOR_HEAD.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Armor").type(AttributeType.GENERIC_ARMOR).amount(value).slot("head").build());
		} else if (EnchantmentTarget.ARMOR_TORSO.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Armor").type(AttributeType.GENERIC_ARMOR).amount(value).slot("chest").build());
		} else if (EnchantmentTarget.ARMOR_LEGS.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Armor").type(AttributeType.GENERIC_ARMOR).amount(value).slot("legs").build());
		} else if (EnchantmentTarget.ARMOR_FEET.includes(stack)) {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Armor").type(AttributeType.GENERIC_ARMOR).amount(value).slot("feet").build());
		} else {
			attrs.add(gpl.AttributeUtil.Attribute.newBuilder().name("Armor").type(AttributeType.GENERIC_ARMOR).amount(value).slot("offhand").build());
		}
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {	
		Resident resident = CivGlobal.getResident(event.getPlayer());
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.Gray+"You do not have the required technology for this item. Its protection output will be reduced in half.");
		}
	}
	
	@Override
	public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {
		double def = this.getDouble("value");
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat == null) return;
		
		Map<Enchantment, Integer> enchant = stack.getEnchantments();
		if (enchant.containsKey(Enchantment.DAMAGE_ALL)) {
			int level = enchant.get(Enchantment.DAMAGE_ALL);
			def += (level*0.5);
		}
		
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			for (PotionEffect effect : p.getActivePotionEffects()) {
				double potionDifference = 0.48*(1+effect.getAmplifier());
				if (effect.getType().equals(PotionEffectType.WEAKNESS)) def -= potionDifference;
				if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) def += potionDifference;
			}
			
			double unitperk = 1.0;
			ConfigUnit u = Unit.getPlayerUnit(p);
			if (u != null) { 
//				if (u.id.equals("u_warrior")) def *= Unit.warrior_atk_dmg;
//				else if (u != null && u.id.equals("u_archer")) def *= Unit.archer_atk_dmg;
				
				// Additional attack dmg always gets added, reguardless of unit type.
				ItemStack unit = Unit.getPlayerUnitStack(p);
				AttributeUtil a = new AttributeUtil(unit);
				for (LoreEnhancement enh : a.getEnhancements()) {
					CivMessage.global(enh.getDisplayName());
					if (enh instanceof LoreEnhancementUnitGainProtection) {
						unitperk += (enh.getLevel(a) * Unit.enhancement_unit_protect_amt);
					}
				}
			}
			
			def *= unitperk;
			
			Resident resident = CivGlobal.getResident(p);
			if (!resident.hasTechForItem(stack)) def = def/2;
		}
		
		double evDmg = event.getDamage();
		CivMessage.global("Damage: "+evDmg);
		DecimalFormat df = new DecimalFormat("0.00");
		double returnDmg = Double.valueOf(df.format((evDmg * (1 - ((def + (evDmg/2)) - evDmg / (2)) / ((25+(evDmg/2.25))-.8687922)))));
		CivMessage.global("Return Damage: "+returnDmg);
		if (evDmg < 4.8687893) {
			// This number [4.8687893] was calculated on a TI-84 Plus C Silver Edition, so it is 105% correct.
			double fixedDamage = (-(returnDmg-3.4965076))+4.8687893;
			returnDmg = fixedDamage;
		}
		if (returnDmg < 0.5) returnDmg = 0.5;
		
//		double damage = event.getDamage() - def;
//		if (damage < 0.5) damage = 0.5;
		event.setDamage(returnDmg);
	}
}
