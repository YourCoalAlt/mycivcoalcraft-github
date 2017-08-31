package com.avrgaming.civcraft.items.units;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;

import gpl.AttributeUtil;

public class Warrior extends UnitMaterial {
	
	public Warrior(String id, ConfigUnit configUnit) {
		super(id, configUnit);
	}
	
	public static void spawn(Inventory inv, Town town) throws CivException {
		ItemStack is = LoreMaterial.spawn(Unit.WARRIOR_UNIT);
		UnitMaterial.setOwningTown(town, is);
		
		AttributeUtil attrs = new AttributeUtil(is);
		attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
		attrs.addLore(CivColor.Gold+"Soulbound");
		attrs.addLore(CivColor.LightGreen+"Level 1");
		attrs.addLore(CivColor.LightGray+"+25% Attack with Sword");
		attrs.addLore(CivColor.LightGray+"-10% Attack with Bow");
//		attrs.addLore(CivColor.LightGray+"-10% Defense on Armor");
		is = attrs.getStack();
		
		if (!Unit.addItemNoStack(inv, is)) {
			throw new CivException("Cannot make "+Unit.WARRIOR_UNIT.getUnit().name+". Barracks chest is full! Make Room!");
		}
	}
	
	@Override
	public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {
		Player p = (Player)event.getEntity();
		
		Random r = new Random();
		int destroyChance = r.nextInt(100);
		if (War.isWarTime()) {
			CivMessage.send(p, CivColor.YellowBold+"Your unit has been kept alive since it is War Time.");
		}
		if (5 < destroyChance) {
			Unit.removeUnit(p);
			CivMessage.send(p, CivColor.RoseBold+"Your unit has been destroyed! Rolled: 5 < "+destroyChance);
		}
	}
	
}