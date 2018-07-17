package com.avrgaming.civcraft.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.util.UtilServer;

public class AC_JoinListener implements Listener {
	
	private final CivCraft plugin;
	
	public AC_JoinListener(CivCraft plugin) {
		this.plugin = plugin;
		UtilServer.registerListener(this);
		UtilServer.registerOutgoingChannel("FML|HS");
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				sendFmlPacket(player, (byte) -2, (byte) 0);
				sendFmlPacket(player, (byte) 0, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
				sendFmlPacket(player, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
			}
		}.runTaskLater(plugin, 30L);
	}
	
	private void sendFmlPacket(Player player, byte... data) {
		player.sendPluginMessage(plugin, "FML|HS", data);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		CivCraft.getACManager().removePlayer(event.getPlayer());
	}
	
}
