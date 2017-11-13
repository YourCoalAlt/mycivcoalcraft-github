package com.avrgaming.civcraft.items.units;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class Archer extends UnitMaterial {
	
	public Archer(String id, ConfigUnit configUnit) {
		super(id, configUnit);
	}
	
	public static void spawn(Inventory inv, Town town) throws CivException {
		ItemStack is = LoreMaterial.spawn(Unit.ARCHER_UNIT);
		UnitMaterial.setOwningTown(town, is);
		
		AttributeUtil attrs = new AttributeUtil(is);
		attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
		attrs.addLore(CivColor.Gold+"Soulbound");
		
		ConfigUnit u = CivSettings.units.get(Unit.getUnit(is).id);
		attrs.addLore(CivColor.Green+"On Death Destroy Chance: "+CivColor.LightGreen+u.destroy_chance+"%");
		for (String d : u.description) { attrs.addLore(CivColor.colorize(d)); }
		is = attrs.getStack();
		
		if (!Unit.addItemNoStack(inv, is)) {
			throw new CivException("Cannot make "+u.name+". Barracks chest is full! Make Room!");
		}
	}
}
