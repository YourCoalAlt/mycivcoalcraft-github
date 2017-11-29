package com.avrgaming.civcraft.listener.civcraft;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.avrgaming.civcraft.config.CivSettings;
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
	
	public static void generateTradeGoodHolograms() {
		if (!CivSettings.hasHolographicDisplays) {
			CivLog.warning("Tried generating Trade Good Holograms without HolographicDisplays plugin! This is fine, but no holograms can generate.");
			return;
		}
		
		int deleted = 0;
		int created = 0;
		Plugin p = CivCraft.getPlugin();
		for (TradeGood good : CivGlobal.getTradeGoods()) {
			BlockCoord coord = good.getCoord();
			int cX = (coord.getBlock().getChunk().getX()*16)+7;
			int cZ = (coord.getBlock().getChunk().getZ()*16)+7;
			
			Location loc = new Location(coord.getBlock().getWorld(), cX+0.5, coord.getBlock().getY()+6.25, cZ+0.5);
			for (com.gmail.filoghost.holograms.api.Hologram hologram : HolographicDisplaysAPI.getHolograms(CivCraft.getPlugin())) {
				if (hologram.getLocation().equals(loc)) {
					hologram.delete();
					deleted++;
				}
			}
			
			Hologram hologram = HologramsAPI.createHologram(p, loc);
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
	
	public static void updateBarracksHolo(Location loc, String title, String per) {
		if (!CivSettings.hasHolographicDisplays) {
			return;
		}
		
		Plugin p = CivCraft.getPlugin();
		for (com.gmail.filoghost.holograms.api.Hologram hologram : HolographicDisplaysAPI.getHolograms(CivCraft.getPlugin())) {
			if (hologram.getLocation().equals(loc)) {
				hologram.delete();
			}
		}
		
		double pr = Double.valueOf(per.replace("%", ""));
		if (pr >= 100) {
			return;
		}
		
		Hologram hologram = HologramsAPI.createHologram(p, loc);
		hologram.appendTextLine(title);
		hologram.appendTextLine(per);
	}
	
/*	public static void generateBankHolograms() {
		if (!CivSettings.hasHolographicDisplays) {
			CivLog.warning("Tried generating Bank Holograms without HolographicDisplays plugin! This is fine, but no holograms can generate.");
			return;
		}
		
		int deleted = 0;
		int created = 0;
		Plugin p = CivCraft.getPlugin();
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
						Hologram hologram = HologramsAPI.createHologram(p, loc);
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
