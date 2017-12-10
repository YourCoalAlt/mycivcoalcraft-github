package com.avrgaming.civcraft.main;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bukkit.plugin.java.JavaPlugin;

import com.avrgaming.civcraft.exception.CivException;

public class CivLog {
	
	private static Logger civLogger;
	private static final float sessionTime = System.currentTimeMillis();
	private static final String sessionLog = "civ_log-"+sessionTime;
	
	public static void init(JavaPlugin plugin) {
		civLogger = Logger.getLogger("civ_log-"+sessionLog);
		FileHandler fh;
		
		try {
			fh = new FileHandler(plugin.getDataFolder().getPath()+"/logs/civ_log-"+sessionLog+".log");
			civLogger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void heading(String title) {
		civLogger.info("======== "+title+" ========");
	}
	
	public static void info(String message) {
		civLogger.info(message);
	}
	
	public static void debug(String message) {
		civLogger.info("[DEBUG] "+message);
	}
	
	public static void warning(String message) {
		if (message == null) {
			try {
				throw new CivException("Null warning message!");
			} catch (CivException e){
				e.printStackTrace();
			}
		}
		if (CivGlobal.warningsEnabled) {
			civLogger.warning("[WARNING] "+message);
		}
	}
	
	public static void error(String message) {
		civLogger.severe(message);
	}
	
	public static void adminlog(String name, String message) {
		civLogger.info("[ADMIN:"+name+"] "+message);
	}
	
	public static void moderatorlog(String name, String message) {
		civLogger.info("[MODERATOR:"+name+"] "+message);
	}
	
	public static void cleanupLog(String message) {
		info(message);
	}
	
	public static void exception(String string, Exception e) {
		e.printStackTrace();		
	}
}
