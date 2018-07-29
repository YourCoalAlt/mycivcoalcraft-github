package com.avrgaming.civcraft.config.perms;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;

public class CivPerms {
	
	public static final String CONTROL = "civ.control";
	public static final String ADMIN_OP = "civ.admin_op";
	public static final String ADMIN = "civ.admin";
	public static final String MINI_ADMIN = "civ.mini_admin";
	public static final String DEVELOPER = "civ.developer";
	public static final String MODERATOR = "civ.moderator";
	public static final String HELPER = "civ.helper";
	
	public static final String ECON = "civ.econ";
	public static final String RANKS = "civ.ranks";
	public static final String RELOAD = "civ.refresh";
	public static final String ADPERK = "civ.adperk";
	public static final String ADWAR = "civ.adwar";
	public static final String ADMOB = "civ.admob";
	
	public static final String FREE_PERKS = "civ.freeperks";
	
	public static final String yourcoalUUID = "a8e4c9db-3f73-41ae-ba65-65620c65e8b3";
	
	public static void validYourCoal(Player p) throws CivException {
		if (!p.getUniqueId().toString().equals(yourcoalUUID)) {
			throw new CivException("You must be YourCoal to use this command.");
		}
	}
	
	public static void validControl(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.getUniqueId().toString().equals(yourcoalUUID)) {
			throw new CivException("You must be ranked Control to use this command.");
		}
	}
	
	public static void validAdminOP(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP)) {
			throw new CivException("You must be ranked Admin OP or higher to use this command.");
		}
	}
	
	public static void validAdmin(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN)) {
			throw new CivException("You must be ranked Admin or higher to use this command.");
		}
	}
	
	public static void validMiniAdmin(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN)) {
			throw new CivException("You must be ranked Mini Admin or higher to use this command.");
		}
	}
	
	public static void validDev(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER)) {
			throw new CivException("You must be ranked Developer or higher to use this command.");
		}
	}
	
	public static void validMod(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(MODERATOR)) {
			throw new CivException("You must be ranked Moderator or higher to use this command.");
		}
	}
	
	public static void validHelper(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(MODERATOR) ||
				!p.hasPermission(HELPER)) {
			throw new CivException("You must be ranked Helper or higher to use this command.");
		}
	}
	
	public static void validEcon(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(CivSettings.ECON)) {
			throw new CivException("You must have Econ permission, or be ranked Admin or higher to use this command.");
		}
	}
	
	// Change these ranks on players
	public static void validRanks(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(RANKS)) {
			throw new CivException("You must have Rank permission, or be ranked Admin or higher to use this command.");
		}
	}
	
	// Reload configs
	public static void validReload(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(RELOAD)) {
			throw new CivException("You must have Reload permission, or be ranked Developer or higher to use this command.");
		}
	}
	
	// Use admin perk cmds
	public static void validAdPerk(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(ADPERK)) {
			throw new CivException("You must have AdPerk permission, or be ranked Developer or higher to use this command.");
		}
	}
	
	// Use admin war cmds
	public static void validAdWar(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(ADWAR)) {
			throw new CivException("You must have AdWar permission, or be ranked Developer or higher to use this command.");
		}
	}
	
	// Use admin mob cmds
	public static void validAdMob(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(ADMOB)) {
			throw new CivException("You must have AdMob permission, or be ranked Developer or higher to use this command.");
		}
	}
	
	
	
	
	
	
	public static boolean isYourCoal(Player p) {
		if (!p.hasPermission(CONTROL) || !p.getUniqueId().toString().equals(yourcoalUUID)) {
			return false;
		}
		return true;
	}
	
	public static boolean isControl(Player p) {
		if (!p.hasPermission(CONTROL) || !p.getUniqueId().toString().equals(yourcoalUUID)) {
			return false;
		}
		return true;
	}
	
	public static boolean isAdminOP(Player p) {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP)) {
			return false;
		}
		return true;
	}
	
	public static boolean isAdmin(Player p) {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN)) {
			return false;
		}
		return true;
	}
	
	public static boolean isMiniAdmin(Player p) {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN)) {
			return false;
		}
		return true;
	}
	
	public static boolean isDev(Player p) {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER)) {
			return false;
		}
		return true;
	}
	
	public static boolean isMod(Player p) {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(MODERATOR)) {
			return false;
		}
		return true;
	}
	
	public static boolean isHelper(Player p) {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(MINI_ADMIN) || !p.hasPermission(DEVELOPER) || !p.hasPermission(MODERATOR) ||
				!p.hasPermission(HELPER)) {
			return false;
		}
		return true;
	}
	
	public static boolean isEcon(Player p) {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(CivSettings.ECON)) {
			return false;
		}
		return true;
	}
	
	public static boolean isReload(Player p) throws CivException {
		if (!p.hasPermission(CONTROL) || !p.hasPermission(ADMIN_OP) || !p.hasPermission(ADMIN) ||
				!p.hasPermission(DEVELOPER) || !p.hasPermission(RELOAD)) {
			return false;
		}
		return true;
	}
}
