package com.avrgaming.civcraft.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.avrgaming.civcraft.main.CivCraft;

public final class UtilServer {
	
	private UtilServer(){}
	
	private static final CivCraft plugin = JavaPlugin.getPlugin(CivCraft.class);
	static CivCraft getPlugin() {
		return plugin;
	}
	
	public static void registerListener(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, plugin);
	}
	
	public static void registerOutgoingChannel(String channel) {
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channel);
	}
	
	public static void registerIncomingChannel(String channel, PluginMessageListener messageListener) {
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, channel, messageListener);
	}
	
}
