package com.avrgaming.civcraft.items.components;

import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.listener.civcraft.MinecraftListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class RangedAttack extends ItemComponent {
	
	private static double ARROW_MAX_VEL = 6.0; 
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(" ");
		attrs.addLore(CivColor.LightGray+"When in main hand:");
		attrs.addLore(CivColor.Navy+" +"+this.getDouble("value")+" Ranged Attack Damage");
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {	
		Resident resident = CivGlobal.getResident(event.getPlayer());
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"You do not have the required technology to use this item. Its attack output will be reduced in half.");
		}
	}
	
	@Override
	public void onInteract(PlayerInteractEvent event) {
		if (Unit.isWearingAnyMetal(event.getPlayer())) {
			event.setCancelled(true);
			CivMessage.sendError(event.getPlayer(), "Cannot use a bow while wearing metal armor.");
			return;
		}
	}
	
	@Override
	public void onRangedAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		double dmg = this.getDouble("value");
		
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player attacker = (Player)arrow.getShooter();
				if (Unit.isWearingAnyMetal(attacker)) {
					event.setCancelled(true);
					CivMessage.sendError(attacker, "Cannot use a bow while wearing metal armor.");
					return;
				}
				
				ItemStack a = MinecraftListener.getArrowStack(attacker);
				String an = null;
				if (a != null && a.hasItemMeta() && a.getItemMeta().hasDisplayName()) {
					an = a.getItemMeta().getDisplayName();
				} else {
					Resident r = CivGlobal.getResident(attacker);
					if (r.lastShotArrow != null) {
						an = r.lastShotArrow;
						r.lastShotArrow = null;
					}
				}
				
				if (an != null) {
					if (an.contains("Wood-Flint ")) {
						CivMessage.global("a");
						dmg *= 0.85;
					} else if (an.contains("Wood-Slime ")) {
						CivMessage.global("b");
						dmg *= 1.0;
					} else if (an.contains("Stone-Flint ")) {
						CivMessage.global("c");
						dmg *= 1.0;
					} else if (an.contains("Stone-Slime ")) {
						CivMessage.global("d");
						dmg *= 1.0;
					} else if (an.contains("Iron-Flint ")) {
						CivMessage.global("e");
						dmg *= 1.15;
					} else if (an.contains("Iron-Slime ")) {
						CivMessage.global("f");
						dmg *= 1.0;
					} else {
						CivMessage.global("0");
						dmg *= 1.0;
					}
				} else {
					CivMessage.sendError(attacker, "An arrow you shot came back with an unknown arrow varient, we cannot calculate custom arrow damage.");
					dmg *= 1.0;
				}
			}
		}
		
//		double extraAtt = 0.0;
//		AttributeUtil attrs = new AttributeUtil(inHand);
//		for (LoreEnhancement enh : attrs.getEnhancements()) {
//			if (enh instanceof LoreEnhancementAttack) {
//				extraAtt +=  ((LoreEnhancementAttack)enh).getExtraAttack(attrs);
//			}
//		}
//		dmg += extraAtt;
		
		double atkPower = 0.0;
		Map<Enchantment, Integer> enchant = inHand.getEnchantments();
		if (enchant.containsKey(Enchantment.ARROW_DAMAGE)) {
			int level = enchant.get(Enchantment.ARROW_DAMAGE);
			if (level == 1) atkPower = 0.5;
			else if (level == 2) atkPower = 1.0;
			else if (level == 3) atkPower = 1.5;
			else if (level == 4) atkPower = 2.0;
			else if (level == 5) atkPower = 2.5;
			else atkPower = 0.0;
		}
		dmg += atkPower;
		
		Vector vel = event.getDamager().getVelocity();
		double magnitudeSquared = Math.pow(vel.getX(), 2) + Math.pow(vel.getY(), 2) + Math.pow(vel.getZ(), 2);
		
		double percentage = magnitudeSquared / ARROW_MAX_VEL;
		double totalDmg = percentage * dmg;
		
		if (totalDmg > dmg) {
			totalDmg = dmg;
		}
		
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Resident resident = CivGlobal.getResident(((Player)arrow.getShooter()));
				if (!resident.hasTechForItem(inHand)) {
					totalDmg = totalDmg / 2;
				}
			}
		}
		
		if (totalDmg < 0.3) {
			totalDmg = 0.3;
		}
		event.setDamage(totalDmg);
	}
}