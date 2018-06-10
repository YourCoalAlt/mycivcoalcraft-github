package com.avrgaming.civcraft.threading.timers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.listener.ActionBar;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;

public class ActionBarUpdateTimer implements Runnable {
	
	@Override
	public void run() {
		if (CivCraft.isDisable || War.isWarTime()) return;
		
		String weather = CivColor.GrayBold+" [";
		World w = Bukkit.getWorld(CivCraft.worldName);
		int weatherTime = w.getWeatherDuration() / 20;
		if (weatherTime > (60*10)) {
			w.setWeatherDuration(((60*10)-1)*20);
			weatherTime = w.getWeatherDuration();
		}
		String weatherLength = "";
	    int hours = (int) (weatherTime / 3600);
	    int remainder = (int) (weatherTime - (hours * 3600));
	    int mins = remainder / 60;
	    int secs = remainder - (mins * 60);
		if (hours <= 0) weatherLength += mins+" m "+secs+" s";
		else weatherLength += hours+" h "+mins+" m "+secs+" s";
		weatherLength.substring(weatherLength.length()-2);
		if (w.isThundering()) {
			if (day(w)) weather += CivColor.RedBold+"Storm "+CivColor.RESET+weatherLength;
			else weather += CivColor.RedBold+"Night Storm "+CivColor.RESET+weatherLength;
		}
		else if (w.hasStorm()) {
			if (day(w))	weather += CivColor.BlueBold+"Rain "+CivColor.RESET+weatherLength;
			else weather += CivColor.BlueBold+"Night Rain "+CivColor.RESET+weatherLength;
		}
		else {
			if (day(w)) weather += CivColor.GoldBold+"Sunny";
			else weather += CivColor.LightGrayBold+"Moon";
		}
		weather += CivColor.GrayBold+"]";
		
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Resident res = CivGlobal.getResident(p);
			ChunkCoord coord = new ChunkCoord(p.getLocation());
			TownChunk tc = CivGlobal.getTownChunk(coord);
			CultureChunk cc = CivGlobal.getCultureChunk(coord);
			
			String borders = "";
			if (cc != null && tc != null) {
				borders = CivColor.LightGreen+"Civ "+CivColor.LightPurple+cc.getCiv().getName()+CivColor.LightGreen+" - "+
						CivColor.Yellow+"Town "+CivColor.LightBlue+tc.getTown().getName()+weather;
			} else if (tc == null && cc != null) {
				borders = CivColor.LightGreen+"Civ "+CivColor.LightPurple+cc.getCiv().getName()+CivColor.LightGreen+" - "+
						CivColor.Rose+"near "+CivColor.LightBlue+cc.getTown().getName()+weather;
			} else if (tc != null && cc == null) {
				borders = CivColor.Rose+"Wilderness"+CivColor.LightGreen+" - "+
						CivColor.Yellow+"Town "+CivColor.LightBlue+tc.getTown().getName()+weather;
			} else {
				borders = CivColor.Rose+"[Wilderness]"+weather;
			}
			
			if (res != null) {
				int exposure = (int) res.getSpyExposure();
				if (exposure > 0) {
					String msg = borders+CivColor.GrayBold+" « » "+CivColor.GoldBold+"Spy XP: "+CivColor.LightGreenItalic+exposure;
					ActionBar.sendActionbar(p, msg);
				} else {
					ActionBar.sendActionbar(p, borders);
				}
			} else {
				String msg = borders+CivColor.GrayBold+" « » "+CivColor.GoldBold+"Spy XP: "+CivColor.RoseItalic+"null";
				ActionBar.sendActionbar(p, msg);
			}
		}
	}
	
	public boolean day(World w) {
	    long time = w.getTime();
	    return time < 12520 || time > 23020;
	}
	
}