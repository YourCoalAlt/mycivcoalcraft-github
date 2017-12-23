package com.avrgaming.civcraft.main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.avrgaming.civcraft.exception.CivException;

public class CivLog {
	
	private static Logger civLogger;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy HH:mm:ss");
	private static final String sessionTime = sdf.format(System.currentTimeMillis()).replaceAll("/", "-").replaceAll(":", ";").replaceAll(" ", "_");
	
	private static final String sessionLog = "civ_log-"+sessionTime+".log";
	private static FileWriter fw;
	
	public static void init(JavaPlugin plugin) {
		civLogger = Logger.getLogger(sessionLog);
		
		try {
			setFw(new FileWriter(plugin.getDataFolder().getPath()+"/logs/"+sessionLog, true)); // Our custom logger
			civLogger = Bukkit.getLogger(); // Tag Bukkit's logger to make sure everything is still logged
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void heading(String title) {
		title = "======== "+title+" ========";
		printWriter(title);
		civLogger.info(title);
	}
	
	public static void info(String message) {
		printWriter(message);
		civLogger.info(message);
	}
	
	public static void chat(Object type, String message) {
		message = "[CHAT] ["+type+"] "+message;
		printWriter(message);
		civLogger.info(message);
	}
	
	public static void debug(String message) {
		message = "[DEBUG] "+message;
		printWriter(message);
		civLogger.info(message);
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
			message = "[WARNING] "+message;
			printWriter(message);
			civLogger.warning(message);
		}
	}
	
	public static void error(String message) {
		printWriter("[SEVERE] "+message);
		civLogger.severe(message);
	}
	
	public static void adminlog(String name, String message) {
		message = "[ADMIN:"+name+"] "+message;
		printWriter(message);
		civLogger.info(message);
	}
	
	public static void moderatorlog(String name, String message) {
		message = "[MODERATOR:"+name+"] "+message;
		printWriter(message);
		civLogger.info(message);
	}
	
	public static void exception(String string, Exception e) {
		e.printStackTrace();		
	}
	
	public static void printWriter(String string) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy HH:mm:ss");
		String date = sdf.format(cal.getTime()).replaceAll("/", "-");
		PrintWriter pw = new PrintWriter(fw);
		pw.println("[SERVER "+date+"] "+string);
		pw.flush();
	}
	
	public static FileWriter getFw() {
		return fw;
	}
	
	public static void setFw(FileWriter fw) {
		CivLog.fw = fw;
	}
}
