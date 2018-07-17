package com.avrgaming.civcraft.threading.timers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.listener.ActionBar;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;

public class ActionBarUpdateTimer implements Runnable {
	
	@Override
	public void run() {
//		if (CivCraft.isDisable || War.isWarTime()) return;
		
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Resident res = CivGlobal.getResident(p);
			ArrayList<String> queue = res.getActionBarQueue();
			if (queue != null && !queue.isEmpty()) {
				ActionBar.sendActionbar(p, queue.get(0)); // Send the result
				if (res.getActionBarSecondsLeft() > 0) res.subActionBarSecondsLeft(1); // Deduct seconds left to show the message
				else queue.remove(0); // Remove the result when it is completed showing
			} else {
				// No stored data, so send this as the result
				ActionBar.sendActionbar(p, getDefaultBar(p, res)); // Send the result
			}
		}
	}
	
	private boolean isDay(World w) {
	    long time = w.getTime();
	    return time < 12520 || time > 23020;
	}
	
	private String getWeatherString(Player p) {
		String weather = CivColor.DarkGrayBold+" [";
		World w = p.getWorld();
		int weatherTime = w.getWeatherDuration() / 20;
		if (weatherTime > (60*20)) {
			w.setWeatherDuration(((60*20)-1)*20);
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
			if (isDay(w)) weather += CivColor.RedBold+"Storm "+CivColor.RESET+weatherLength;
			else weather += CivColor.RedBold+"Night Storm "+CivColor.RESET+weatherLength;
		}
		else if (w.hasStorm()) {
			if (isDay(w))	weather += CivColor.AquaBold+"Rain "+CivColor.RESET+weatherLength;
			else weather += CivColor.AquaBold+"Night Rain "+CivColor.RESET+weatherLength;
		}
		else {
			if (isDay(w)) weather += CivColor.GoldBold+"Sunny";
			else weather += CivColor.GrayBold+"Moon";
		}
		weather += CivColor.DarkGrayBold+"]";
		return weather;
	}
	
	private String getDefaultBar(Player p, Resident res) {
		ChunkCoord coord = new ChunkCoord(p.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		
		String borders = "";
		if (cc != null && tc != null) {
			borders = CivColor.LightGreen+"Civ "+CivColor.LightPurple+cc.getCiv().getName()+CivColor.LightGreen+" - "+
					CivColor.Yellow+"Town "+CivColor.LightBlue+tc.getTown().getName();
		} else if (tc == null && cc != null) {
			borders = CivColor.LightGreen+"Civ "+CivColor.LightPurple+cc.getCiv().getName()+CivColor.LightGreen+" - "+
					CivColor.Rose+"near "+CivColor.LightBlue+cc.getTown().getName();
		} else if (tc != null && cc == null) {
			borders = CivColor.Rose+"Wilderness"+CivColor.LightGreen+" - "+
					CivColor.Yellow+"Town "+CivColor.LightBlue+tc.getTown().getName();
		} else {
			borders = CivColor.Rose+"[Wilderness]";
		}
		
		if (res != null) {
			int exposure = (int) res.getSpyExposure();
			if (exposure > 0) {
				return borders+CivColor.DarkGrayBold+" « » "+CivColor.GoldBold+"Spy XP: "+CivColor.LightGreenItalic+exposure;
			} else {
				return borders+getWeatherString(p);
			}
		} else {
			return borders+CivColor.DarkGrayBold+" « » "+CivColor.GoldBold+"Spy XP: "+CivColor.RoseItalic+"null";
		}
	}
	
}