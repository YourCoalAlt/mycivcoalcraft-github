package com.avrgaming.civcraft.items.components;

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
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementProtection;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementUnitGainProtection;
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
			CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"You do not have the required technology for this item. Its protection output will be reduced in half.");
		}
	}
	
	@Override
	public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {
		double def = this.getDouble("value");
//		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
//		if (craftMat == null) return;
		
		double extraDef = 0.0;
		AttributeUtil attrs = new AttributeUtil(stack);
		for (LoreEnhancement enh : attrs.getEnhancements()) {
			if (enh instanceof LoreEnhancementProtection) {
				extraDef += (((LoreEnhancementProtection)enh).getExtraDamage(attrs) * 0.15);
			}
		}
		def += extraDef;	
		
		/*double defProt = 0.0;
		Map<Enchantment, Integer> enchant = stack.getEnchantments();
		if (enchant.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL)) {
			int level = enchant.get(Enchantment.PROTECTION_ENVIRONMENTAL);
			if (level == 1) defProt = 0.1;
			else if (level == 2) defProt = 0.2;
			else if (level == 3) defProt = 0.3;
			else if (level == 4) defProt = 0.4;
			else defProt = 0.0;
		}
		def += defProt;*/
		
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			for (PotionEffect effect : p.getActivePotionEffects()) {
				if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
					int weaknessDmg = 2+(2*effect.getAmplifier());
					def -= weaknessDmg;
				}
				
				if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
					int strengthDmg = 2+(2*effect.getAmplifier());
					def += strengthDmg;
				}
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
		
		double damage = event.getDamage() - def;
		if (damage < 0.5) damage = 0.5;
		event.setDamage(damage);
	}
}
