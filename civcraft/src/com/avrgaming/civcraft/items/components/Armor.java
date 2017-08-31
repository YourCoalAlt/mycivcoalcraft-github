package com.avrgaming.civcraft.items.components;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementProtection;
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

public class Armor extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		ItemStack item = attrs.getStack();
		attrs.nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound = (attrs.nmsStack.hasTag()) ? attrs.nmsStack.getTag() : new NBTTagCompound();
		NBTTagList modifiers = new NBTTagList();
		NBTTagCompound damage = new NBTTagCompound();
		
		damage.set("AttributeName", new NBTTagString("generic.armor"));
		damage.set("Name", new NBTTagString("generic.armor"));
		damage.set("Amount", new NBTTagDouble(this.getDouble("value")));
		damage.set("Operation", new NBTTagInt(0));
		damage.set("UUIDLeast", new NBTTagInt(894654));
		damage.set("UUIDMost", new NBTTagInt(2872));
		if (item.getType() == Material.LEATHER_HELMET || item.getType() == Material.GOLD_HELMET || item.getType() == Material.CHAINMAIL_HELMET ||
					item.getType() == Material.IRON_HELMET || item.getType() == Material.DIAMOND_HELMET) {
			damage.set("Slot", new NBTTagString("head"));
		} else if (item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.GOLD_CHESTPLATE || item.getType() == Material.CHAINMAIL_CHESTPLATE ||
				item.getType() == Material.IRON_CHESTPLATE || item.getType() == Material.DIAMOND_CHESTPLATE) {
			damage.set("Slot", new NBTTagString("chest"));
		} else if (item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.GOLD_LEGGINGS || item.getType() == Material.CHAINMAIL_LEGGINGS ||
				item.getType() == Material.IRON_LEGGINGS || item.getType() == Material.DIAMOND_LEGGINGS) {
			damage.set("Slot", new NBTTagString("legs"));
		} else if (item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.GOLD_BOOTS || item.getType() == Material.CHAINMAIL_BOOTS ||
				item.getType() == Material.IRON_BOOTS || item.getType() == Material.DIAMOND_BOOTS) {
			damage.set("Slot", new NBTTagString("feet"));
		} else {
			damage.set("Slot", new NBTTagString("offhand"));
			List<String> lore = new ArrayList<String>();
						 lore.add("Unknown Armor Type");
						 item.getItemMeta().setLore(lore);
		}
		modifiers.add(damage);
		
		compound.set("AttributeModifiers", modifiers);
		attrs.nmsStack.setTag(compound);
		item = CraftItemStack.asBukkitCopy(attrs.nmsStack);
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
		double def = this.getDouble("value")/10;
//		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
//		if (craftMat == null) {
//			return;
//		}
		
		double extraAtt = 0.0;
		AttributeUtil attrs = new AttributeUtil(stack);
		for (LoreEnhancement enh : attrs.getEnhancements()) {
			if (enh instanceof LoreEnhancementProtection) {
				extraAtt += (((LoreEnhancementProtection)enh).getExtraDamage(attrs) * 0.1);
			}
		}
		def += extraAtt;	
		
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
		
		double damage = event.getDamage();
		if (event.getEntity() instanceof Player) {
			Resident resident = CivGlobal.getResident(((Player)event.getEntity()));
			if (!resident.hasTechForItem(stack)) {
				def = def/2;
			}
		}
		
		damage -= def;
		if (damage < 0.1) {
			damage = 0.1;
		}
		event.setDamage(damage);
	}
}
