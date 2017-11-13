package com.avrgaming.civcraft.items.components;

import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementSharpness;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementUnitGainAttack;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagDouble;
import net.minecraft.server.v1_11_R1.NBTTagInt;
import net.minecraft.server.v1_11_R1.NBTTagList;
import net.minecraft.server.v1_11_R1.NBTTagString;

public class Damage extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		ItemStack item = attrs.getStack();
		attrs.nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound = (attrs.nmsStack.hasTag()) ? attrs.nmsStack.getTag() : new NBTTagCompound();
		NBTTagList modifiers = new NBTTagList();
		NBTTagCompound damage = new NBTTagCompound();
		
		damage.set("AttributeName", new NBTTagString("generic.attackDamage"));
		damage.set("Name", new NBTTagString("generic.attackDamage"));
		damage.set("Amount", new NBTTagDouble(this.getDouble("value")));
		damage.set("Operation", new NBTTagInt(0));
		damage.set("UUIDLeast", new NBTTagInt(894654));
		damage.set("UUIDMost", new NBTTagInt(2872));
		damage.set("Slot", new NBTTagString("mainhand"));
		modifiers.add(damage);
		
		compound.set("AttributeModifiers", modifiers);
		attrs.nmsStack.setTag(compound);
		item = CraftItemStack.asBukkitCopy(attrs.nmsStack);
		return;
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {	
		Resident resident = CivGlobal.getResident(event.getPlayer());
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"You do not have the required technology for this item. Its damage output will be reduced in half.");
		}
	}
	
	@Override
	public void onAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		double dmg = this.getDouble("value");
		
		double extraAtt = 0.0;
		AttributeUtil attrs = new AttributeUtil(inHand);
		for (LoreEnhancement enh : attrs.getEnhancements()) {
			if (enh instanceof LoreEnhancementSharpness) {
				extraAtt += (((LoreEnhancementSharpness)enh).getExtraDamage(attrs) * 0.5);
			}
		}
		dmg += extraAtt;
		
		/*double atkSharp = 0.0;
		Map<Enchantment, Integer> enchant = inHand.getEnchantments();
		if (enchant.containsKey(Enchantment.DAMAGE_ALL)) {
			int level = enchant.get(Enchantment.DAMAGE_ALL);
			if (level == 1) atkSharp = 0.5;
			else if (level == 2) atkSharp = 1.0;
			else if (level == 3) atkSharp = 1.5;
			else if (level == 4) atkSharp = 2.0;
			else if (level == 5) atkSharp = 2.5;
			else atkSharp = 0.0;
		}
		dmg += atkSharp;*/
		
		if (event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			for (PotionEffect effect : p.getActivePotionEffects()) {
				if (effect.getType().toString().contains(PotionEffectType.WEAKNESS.toString())) {
					int weaknessDmg = 2+(2*effect.getAmplifier());
					dmg -= weaknessDmg;
				}
				
				if (effect.getType().toString().contains(PotionEffectType.INCREASE_DAMAGE.toString())) {
					int strengthDmg = 2+(2*effect.getAmplifier());
					dmg += strengthDmg;
				}
			}
			
			ConfigUnit u = Unit.getPlayerUnit(p);
			ItemStack unit = Unit.getPlayerUnitStack(p);
			AttributeUtil a = new AttributeUtil(unit);
			
			double unitper = 1.0;
			if (u != null) { 
				if (u.id.equals("u_warrior")) { dmg *= 1.25; }
				else if (u != null && u.id.equals("u_archer")) { dmg *= 0.9; }
				// Additional attack dmg always gets added, reguardless of unit type.
				for (LoreEnhancement enh : a.getEnhancements()) {
					CivMessage.global(enh.getDisplayName());
					if (enh instanceof LoreEnhancementUnitGainAttack) {
						unitper += (enh.getLevel(a)*0.05);
					}
				}
			}
			
			dmg *= unitper;
			
			Resident resident = CivGlobal.getResident(p);
			if (!resident.hasTechForItem(inHand)) { dmg = dmg/2; }
		}
		
		if (dmg < 0.2) { dmg = 0.2; }
		event.setDamage(dmg);
	}
}
