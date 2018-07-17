package com.avrgaming.civcraft.anticheat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.util.UtilServer;

public class AC_MessageListener implements PluginMessageListener {
	
	public AC_MessageListener(CivCraft plugin) {
		UtilServer.registerIncomingChannel("FML|HS", this);
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] data) {
		// ModList has ID 2
		if (data[0] == 2) {
			AC_ModData modData = getModData(data);
			CivCraft.getACManager().addPlayer(player, modData);
		}
	}
	
	private AC_ModData getModData(byte[] data) {
		Map<String, String> mods = new HashMap<>();
		boolean store = false;
		String tempName = null;
		for (int i = 2; i < data.length; store = !store) {
			int end = i + data[i] + 1;
			byte[] range = Arrays.copyOfRange(data, i + 1, end);
			String string = new String(range);
			if (store) {
				mods.put(tempName, string);
			} else {
				tempName = string;
			}
			i = end;
		}
		return new AC_ModData(mods);
	}
	
}
