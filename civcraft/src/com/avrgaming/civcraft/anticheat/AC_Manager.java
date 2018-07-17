package com.avrgaming.civcraft.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;
import com.google.common.collect.Lists;

public class AC_Manager {
	
	public AC_Manager(CivCraft plugin) {
		new AC_JoinListener(plugin);
		new AC_MessageListener(plugin);
		loadConfigValues();
	}
	
	private boolean blockForge;
	private ArrayList<String> modList = new ArrayList<>();
	private List<String> disallowedCommands;
	
	public void loadConfigValues() {
		loadMode();
		
		blockForge = false;
		modList.add("mercurius_updater");
		modList.add("schematica");
		modList.add("lunatriuscore");
		modList.add("journeymap");
		modList.add("xaerobetterpvp");
		
		disallowedCommands = Lists.newArrayList("kick %player% &cIllegal Mods - %disallowed_mods%");
	}
	
	private void loadMode() {
		Mode mode = EnumUtils.getEnum(Mode.class, ((String) "whitelist").toUpperCase());
		this.mode = mode == null ? Mode.BLACKLIST : mode;
	}
	
	private enum Mode {
		WHITELIST((mod, modList) -> modList.contains(mod)),
		BLACKLIST((mod, modList) -> !modList.contains(mod));
		
		Mode(BiFunction<String, List<String>, Boolean> function) {
			this.function = function;
		}
		
		private final BiFunction<String, List<String>, Boolean> function;
		
		public boolean isAllowed(String mod, List<String> modList) {
			if (mod.equalsIgnoreCase("FML") || mod.equalsIgnoreCase("minecraft") || mod.equalsIgnoreCase("mcp") || mod.equalsIgnoreCase("forge")) {
				return true;
			}
			return function.apply(mod, modList);
		}
	}
	
	private Mode mode;
	
	private boolean isDisallowed(String mod) {
		return !mode.isAllowed(mod, modList);
	}
	
	private final Map<Player, AC_ModData> playerData = new HashMap<>();
	
	public boolean isUsingForge(Player player) {
		return playerData.containsKey(player);
	}
	
	public AC_ModData getModData(Player player) {
		return playerData.get(player);
	}
	
	public void addPlayer(Player player, AC_ModData data) {
		playerData.put(player, data);
		checkForDisallowed(player, data.getMods());
	}
	
	private void checkForDisallowed(Player player, Set<String> mods) {
		if (CivPerms.isHelper(player)) {
			CivMessage.send(player, CivColor.GrayItalic+"[You are exempt from anti-cheat checks.]");
			return;
		}
		
		Set<String> disallowed = mods.stream().filter(this::isDisallowed).collect(Collectors.toSet());
		if (disallowed.size() > 0 || (mods.size() > 0 && blockForge)) {
			// Player is using disallowed mods
			String modsString = String.join(", ", mods);
			String disallowedString = String.join(", ", disallowed);
			sendDisallowedCommand(player, modsString, disallowedString);
		}
	}
	
	private void sendDisallowedCommand(Player player, String mods, String disallowedMods) {
		if (CivPerms.isHelper(player)) {
			CivMessage.send(player, CivColor.GrayItalic+"[You are exempt from anti-cheat checks.]");
			return;
		}
		disallowedCommands.forEach(command -> {
			command = formatCommand(command, player, mods, disallowedMods);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		});
	}
	
	private String formatCommand(String command, Player player, String mods, String disallowedMods) {
		return command.replace("%player%", player.getName()).replace("%mods%", mods).replace("%disallowed_mods%", disallowedMods);
	}
	
	public void removePlayer(Player player) {
		playerData.remove(player);
	}
}
