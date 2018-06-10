package com.avrgaming.civcraft.loreenhancements;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public class LoreEnhancementProspect extends LoreEnhancement {
	
	@Override
	public String getInitName() {
		return "LoreEnhancementProspect";
	}
	
	@Override
	public String getDisplayName() {
		return "Prospect";
	}
	
	@Override
	public Integer getMaxLevel() {
		return 1;
	}
	
	public AttributeUtil add(AttributeUtil attrs) {
		attrs.addEnhancement("LoreEnhancementProspect", null, null);
		attrs.addLore(CivColor.Gold+getDisplayName());
		return attrs;
	}
	
	@Override
	public void onBlockClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			ArrayList<Location> found_loc = new ArrayList<Location>();
			Player p = event.getPlayer();
			CivMessage.send(p, "Prospecting...");
			
			String high = "";
			boolean has_high = false;
			int high_radius = 2;
			for (int high_x = -high_radius; high_x <= high_radius; high_x++) {
				for (int high_y = -high_radius; high_y <= high_radius; high_y++) {
					for (int high_z = -high_radius; high_z <= high_radius; high_z++) {
						Location loc = new Location(p.getWorld(), p.getLocation().getX()+high_x, p.getLocation().getY()+high_y, p.getLocation().getZ()+high_z);
						int b = ItemManager.getId(event.getPlayer().getWorld().getBlockAt(loc).getType());
						if (b == CivData.AIR) continue;
						found_loc.add(loc);
						if (b == CivData.COAL_ORE) {
							if (high.contains("Coal")) continue;
							has_high = true; high += "Coal, ";
						}
						if (b == CivData.IRON_ORE) {
							if (high.contains("Iron")) continue;
							has_high = true; high += "Iron, ";
						}
						if (b == CivData.GOLD_ORE) {
							if (high.contains("Gold")) continue;
							has_high = true; high += "Gold, ";
						}
						if (b == CivData.LAPIS_ORE) {
							if (high.contains("Lapis")) continue;
							has_high = true; high += "Lapis, ";
						}
						if (b == CivData.REDSTONE_ORE || b == CivData.REDSTONE_ORE_GLOW) {
							if (high.contains("Redstone")) continue;
							has_high = true; high += "Redstone, ";
						}
						if (b == CivData.DIAMOND_ORE) {
							if (high.contains("Diamond")) continue;
							has_high = true; high += "Diamond, ";
						}
						if (b == CivData.EMERALD_ORE) {
							if (high.contains("Emerald")) continue;
							has_high = true; high += "Emerald, ";
						}
					}
		        }
		    }
			
			String med = "";
			boolean has_med = false;
			int med_radius = 4;
			for (int med_x = -med_radius; med_x <= med_radius; med_x++) {
				for (int med_y = -med_radius; med_y <= med_radius; med_y++) {
					for (int med_z = -med_radius; med_z <= med_radius; med_z++) {
						// Do not add medium items that are high
						Location loc = new Location(p.getWorld(), p.getLocation().getX()+med_x, p.getLocation().getY()+med_y, p.getLocation().getZ()+med_z);
						int b = ItemManager.getId(event.getPlayer().getWorld().getBlockAt(loc).getType());
						if (b == CivData.AIR) continue;
						if (found_loc.contains(loc)) continue;
						found_loc.add(loc);
						if (b == CivData.COAL_ORE) {
							if (med.contains("Coal")) continue;
							has_med = true; med += "Coal, ";
						}
						if (b == CivData.IRON_ORE) {
							if (med.contains("Iron")) continue;
							has_med = true; med += "Iron, ";
						}
						if (b == CivData.GOLD_ORE) {
							if (med.contains("Gold")) continue;
							has_med = true; med += "Gold, ";
						}
						if (b == CivData.LAPIS_ORE) {
							if (med.contains("Lapis")) continue;
							has_med = true; med += "Lapis, ";
						}
						if (b == CivData.REDSTONE_ORE || b == CivData.REDSTONE_ORE_GLOW) {
							if (med.contains("Redstone")) continue;
							has_med = true; med += "Redstone, ";
						}
						if (b == CivData.DIAMOND_ORE) {
							if (med.contains("Diamond")) continue;
							has_med = true; med += "Diamond, ";
						}
						if (b == CivData.EMERALD_ORE) {
							if (med.contains("Emerald")) continue;
							has_med = true; med += "Emerald, ";
						}
					}
		        }
		    }
			
			String sml = "";
			boolean has_sml = false;
			int sml_radius = 7;
			for (int sml_x = -sml_radius; sml_x <= sml_radius; sml_x++) {
				for (int sml_y = -sml_radius; sml_y <= sml_radius; sml_y++) {
					for (int sml_z = -sml_radius; sml_z <= sml_radius; sml_z++) {
						// Do not add small items that are high and medium
						Location loc = new Location(p.getWorld(), p.getLocation().getX()+sml_x, p.getLocation().getY()+sml_y, p.getLocation().getZ()+sml_z);
						int b = ItemManager.getId(event.getPlayer().getWorld().getBlockAt(loc).getType());
						if (b == CivData.AIR) continue;
						if (found_loc.contains(loc)) continue;
						if (b == CivData.COAL_ORE || b == CivData.IRON_ORE || b == CivData.GOLD_ORE || b == CivData.LAPIS_ORE || b == CivData.REDSTONE_ORE ||
								b == CivData.REDSTONE_ORE_GLOW || b == CivData.DIAMOND_ORE || b == CivData.EMERALD_ORE) {
							if (sml.contains("Unknown")) continue;
							has_sml = true; sml += "Unknown, ";
						}
					}
		        }
		    }
			
			String out = "Found ";
			if (!has_high && !has_med && !has_sml) out += "Nothing of Interest...";
			
			if (has_high) out+= "High amounts of "+high.substring(0, high.length()-2)+" Ore; ";
			if (has_med) out+= "Medium amounts of "+med.substring(0, med.length()-2)+" Ore; ";
			if (has_sml) out+= "Distant amounts of "+sml.substring(0, sml.length()-2).replace("Unknown", CivColor.MAGIC+"Unknown")+CivColor.RESET+" Ore; ";
			
			CivMessage.send(p, out.substring(0, out.length()-2));
		}
	}
	
	@Override
	public String serialize(ItemStack stack) {
		return "";
	}

	@Override
	public ItemStack deserialize(ItemStack stack, String data) {
		return stack;
	}

}