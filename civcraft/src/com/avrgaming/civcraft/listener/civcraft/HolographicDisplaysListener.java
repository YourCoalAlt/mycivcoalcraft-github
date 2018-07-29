package com.avrgaming.civcraft.listener.civcraft;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.TradeGood;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

@SuppressWarnings("deprecation")
public class HolographicDisplaysListener {
	
	private final static Plugin cp = CivCraft.getPlugin();
	
	public static void generateTradeGoodHolograms() {
		if (!CivCraft.softdependHolographicDisplays) {
			CivLog.warning("Tried generating Trade Good Holograms without HolographicDisplays plugin! This is fine, but no holograms can generate.");
			return;
		}
		
		int deleted = 0;
		int created = 0;
		for (TradeGood good : CivGlobal.getTradeGoods()) {
			BlockCoord coord = good.getCoord();
			int cX = (coord.getBlock().getChunk().getX()*16)+7;
			int cZ = (coord.getBlock().getChunk().getZ()*16)+7;
			Location loc = new Location(coord.getBlock().getWorld(), cX+0.5, coord.getBlock().getY()+6.75, cZ+0.5);
			for (com.gmail.filoghost.holograms.api.Hologram hologram : HolographicDisplaysAPI.getHolograms(CivCraft.getPlugin())) {
				if (hologram.getLocation().equals(loc)) {
					hologram.delete();
					deleted++;
				}
			}
			
			Hologram hologram = HologramsAPI.createHologram(cp, loc);
			hologram.appendItemLine(new ItemStack(good.getInfo().material, 1, (short)good.getInfo().material_data));
			hologram.appendTextLine(CivColor.GoldBold+" « Trade Resource » ");
			created++;
			if (good.getInfo().water == true) {
				hologram.appendTextLine(CivColor.GreenBold+"Name: "+CivColor.LightBlueItalic+good.getInfo().name);
			} else {
				hologram.appendTextLine(CivColor.GreenBold+"Name: "+CivColor.YellowItalic+good.getInfo().name);
			}
			hologram.appendTextLine(CivColor.GreenBold+"Value: "+CivColor.LightGreenItalic+good.getInfo().value+" Coins");
		}
		CivLog.info(deleted+" Trade Good Holograms deleted.");
		CivLog.info(created+" Trade Good Holograms created.");
	}
	
	private static Map<Location, Hologram> barracks_training_holos = new HashMap<Location, Hologram>();
	public static void updateBarracksHolo(Location loc, String title, String per) {
		if (!CivCraft.softdependHolographicDisplays) return;
		if (barracks_training_holos.containsKey(loc)) {
			Hologram hg = barracks_training_holos.get(loc);
			double pr = Double.valueOf(per.replace("%", ""));
			if (pr >= 100) {
				barracks_training_holos.remove(loc);
				hg.delete();
			}
			else
				if (hg.getLine(1).toString().contains(per)) return;
				else {
					hg.removeLine(1);
					hg.appendTextLine(per);
				}
		} else {
			Hologram hg = HologramsAPI.createHologram(CivCraft.getPlugin(), loc);
			hg.appendTextLine(title);
			hg.appendTextLine(per);
			barracks_training_holos.put(loc, hg);
		}
	}
	
/*	public static void generateBankHolograms() {
		if (!CivSettings.hasHolographicDisplays) {
			CivLog.warning("Tried generating Bank Holograms without HolographicDisplays plugin! This is fine, but no holograms can generate.");
			return;
		}
		
		int deleted = 0;
		int created = 0;
		for (Structure s : CivGlobal.getStructures()) {
			if (s instanceof Bank) {
				Bank b = (Bank)s;
				for (StructureSign ss : b.getSigns()) {
					switch (ss.getAction().toLowerCase()) {
					case "banker":
						BlockCoord coord = ss.getCoord();
						for (com.gmail.filoghost.holograms.api.Hologram hologram : HolographicDisplaysAPI.getHolograms(CivCraft.getPlugin())) {
							if (hologram.getLocation().equals(coord.getLocation())) {
								hologram.delete();
								deleted++;
							}
						}
						Location loc = new Location(coord.getBlock().getWorld(), coord.getX(), coord.getBlock().getY()+4, coord.getZ());
						Hologram hologram = HologramsAPI.createHologram(cp, loc);
						hologram.appendItemLine(new ItemStack(Material.NETHER_STAR, 1));
						hologram.appendTextLine(CivColor.GoldBold+"Bank Level: "+CivColor.LightGreenBold+b.getLevel());
						hologram.appendTextLine(CivColor.GoldBold+"Exchange Rate: "+CivColor.LightGreenBold+b.getBankExchangeRate()*100+"%");
						hologram.appendTextLine(CivColor.GoldBold+"Non-Resident Fee: "+CivColor.LightGreenBold+b.getNonResidentFee()*100+"%");
						created++;
						break;
					}
				}
			}
		}
		CivLog.info(deleted+" Bank Holograms deleted.");
		CivLog.info(created+" Bank Holograms created.");
	}*/
}
