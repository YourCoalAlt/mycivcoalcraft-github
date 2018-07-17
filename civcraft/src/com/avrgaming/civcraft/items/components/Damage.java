package com.avrgaming.civcraft.items.components;

import java.util.Map;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementUnitGainAttack;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;

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
			CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.Gray+"You do not have the required technology for this item. Its damage output will be reduced in half.");
		}
	}
	
	@Override
	public void onAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		double dmg = this.getDouble("value");
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(inHand);
		if (craftMat == null) return;
		
		Map<Enchantment, Integer> enchant = inHand.getEnchantments();
		if (enchant.containsKey(Enchantment.DAMAGE_ALL)) {
			int level = enchant.get(Enchantment.DAMAGE_ALL);
			dmg += (level*0.5);
		}
		
		if (event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			for (PotionEffect effect : p.getActivePotionEffects()) {
				double potionDifference = 0.4*(1+effect.getAmplifier());
				if (effect.getType().equals(PotionEffectType.WEAKNESS)) dmg -= potionDifference;
				if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) dmg += potionDifference;
			}
			
			double unitperk = 1.0;
			ConfigUnit u = Unit.getPlayerUnit(p);
			if (u != null) { 
				if (u.id.equals("u_warrior")) dmg *= Unit.warrior_atk_dmg;
				else if (u != null && u.id.equals("u_archer")) dmg *= Unit.archer_atk_dmg;
				
				// Additional attack dmg always gets added, reguardless of unit type.
				ItemStack unit = Unit.getPlayerUnitStack(p);
				AttributeUtil a = new AttributeUtil(unit);
				for (LoreEnhancement enh : a.getEnhancements()) {
					CivMessage.global(enh.getDisplayName());
					if (enh instanceof LoreEnhancementUnitGainAttack) {
						unitperk += (enh.getLevel(a) * Unit.enhancement_unit_attack_amt);
					}
				}
			}
			
			dmg *= unitperk;
			
			Resident resident = CivGlobal.getResident(p);
			if (!resident.hasTechForItem(inHand)) dmg = dmg/2;
		}
		
		if (dmg < 0.5) dmg = 0.5;
		event.setDamage(dmg);
	}
}
