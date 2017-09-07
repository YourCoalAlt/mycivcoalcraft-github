package com.avrgaming.civcraft.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigEXPGenericLevel;
import com.avrgaming.civcraft.database.SQL;
import com.avrgaming.civcraft.database.SQLUpdate;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidNameException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;

public class ResidentExperience extends SQLObject {
	
	private UUID uid;
	private Player player;
	
	private double questEXP;
	private double miningEXP;
	private double fishingEXP;
	
	public ResidentExperience(UUID uid, String name) throws InvalidNameException {
		this.setName(name);
		this.uid = uid;
		loadSettings();
	}
	
	public ResidentExperience(ResultSet rs) throws SQLException, InvalidNameException {
		this.load(rs);
		loadSettings();
	}
	
	public void loadSettings() {
		this.setQuestEXP(0);
	}
	
	public static final String TABLE_NAME = "RESIDENTS_EXPERIENCE";
	public static void init() throws SQLException {
		if (!SQL.hasTable(TABLE_NAME)) {
			String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME+" (" + 
					"`id` int(11) unsigned NOT NULL auto_increment," +
					"`name` VARCHAR(64) NOT NULL," +
					"`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN',"+
					"`questEXP` double DEFAULT 0," +
					"`miningEXP` double DEFAULT 0," +
					"`fishingEXP` double DEFAULT 0," +
					"UNIQUE KEY (`name`), " +
					"PRIMARY KEY (`id`)" + ")";
			
			SQL.makeTable(table_create);
			CivLog.info("Created "+TABLE_NAME+" table");
		} else {
			CivLog.info(TABLE_NAME+" table OK!");
			
			if (!SQL.hasColumn(TABLE_NAME, "uuid")) {
				CivLog.info("\tCouldn't find `uuid` for resident experience.");
				SQL.addColumn(TABLE_NAME, "`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN'");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "questEXP")) {
				CivLog.info("\tCouldn't find `questEXP` for resident experience.");
				SQL.addColumn(TABLE_NAME, "`questEXP` double DEFAULT 0");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "miningEXP")) {
				CivLog.info("\tCouldn't find `miningEXP` for resident experience.");
				SQL.addColumn(TABLE_NAME, "`miningEXP` double DEFAULT 0");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "fishingEXP")) {
				CivLog.info("\tCouldn't find `fishingEXP` for resident experience.");
				SQL.addColumn(TABLE_NAME, "`fishingEXP` double DEFAULT 0");
			}
		}		
	}

	@Override
	public void load(ResultSet rs) throws SQLException, InvalidNameException {
		this.setId(rs.getInt("id"));
		this.setName(rs.getString("name"));
		this.uid = UUID.fromString(rs.getString("uuid"));
		this.setQuestEXP(rs.getDouble("questEXP"));
		this.setMiningEXP(rs.getDouble("miningEXP"));
		this.setFishingEXP(rs.getDouble("fishingEXP"));
	}
	
	@Override
	public void save() {
		SQLUpdate.add(this);
	}
	
	@Override
	public void saveNow() throws SQLException {
		HashMap<String, Object> hashmap = new HashMap<String, Object>();
		hashmap.put("name", this.getName());
		hashmap.put("uuid", this.getUUIDString());
		hashmap.put("questEXP", this.getQuestEXP());
		hashmap.put("miningEXP", this.getMiningEXP());
		hashmap.put("fishingEXP", this.getFishingEXP());
		SQL.updateNamedObject(this, hashmap, TABLE_NAME);
	}
	
	@Override
	public void delete() throws SQLException {	
		SQL.deleteByName(this.getName(), TABLE_NAME);
	}
	
	// Quest EXP
	public double getQuestEXP() {
		return questEXP;
	}
	
	public void addQuestEXP(double generated) throws CivException {
		player = CivGlobal.getPlayerE(this.getName());
		ConfigEXPGenericLevel clc = CivSettings.expGenericLevels.get(this.getQuestLevel());
		DecimalFormat df = new DecimalFormat("0.00");
		this.questEXP = Double.valueOf(df.format(this.questEXP + generated));
		this.save();
		if (this.getQuestLevel() < getMaxQuestLevel()) {
			if (this.questEXP >= clc.amount) {
				CivMessage.sendQuestExp(player, "You have became Quest Level "+this.getQuestLevel()+"!");
			}
		}
		CivMessage.sendQuestExp(player, "+"+generated+" Quest EXP");
	}
	
	public void setQuestEXP(double generated) {
		DecimalFormat df = new DecimalFormat("0.00");
		double gen = Double.valueOf(df.format(generated));
		this.questEXP = gen;
	}
	
	public int getQuestLevel() {
		// Get the first level
		int bestLevel = 0;
		ConfigEXPGenericLevel level = CivSettings.expGenericLevels.get(0);
		
		while (this.questEXP >= level.amount) {
			level = CivSettings.expGenericLevels.get(bestLevel+1);
			if (level == null) {
				level = CivSettings.expGenericLevels.get(bestLevel);
				break;
			}
			bestLevel++;
		}
		return level.level;
	}
	
	public static int getMaxQuestLevel() {
		int returnLevel = 0;
		for (Integer level : CivSettings.expGenericLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		return returnLevel;
	}
	
	// Mining EXP
	public double getMiningEXP() {
		return miningEXP;
	}
	
	public void addMiningEXP(double generated) throws CivException {
		player = CivGlobal.getPlayerE(this.getName());
		ConfigEXPGenericLevel clc = CivSettings.expGenericLevels.get(this.getMiningLevel());
		DecimalFormat df = new DecimalFormat("0.00");
		this.miningEXP = Double.valueOf(df.format(this.miningEXP + generated));
		this.save();
		if (this.getMiningLevel() < getMaxMiningLevel()) {
			if (this.miningEXP >= clc.amount) {
				CivMessage.sendQuestExp(player, "You have became Mining Level "+this.getMiningLevel()+"!");
			}
		}
		CivMessage.sendQuestExp(player, "+"+generated+" Mining EXP");
	}
	
	public void setMiningEXP(double generated) {
		DecimalFormat df = new DecimalFormat("0.00");
		double gen = Double.valueOf(df.format(generated));
		this.miningEXP = gen;
	}
	
	public int getMiningLevel() {
		// Get the first level
		int bestLevel = 0;
		ConfigEXPGenericLevel level = CivSettings.expGenericLevels.get(0);
		
		while (this.miningEXP >= level.amount) {
			level = CivSettings.expGenericLevels.get(bestLevel+1);
			if (level == null) {
				level = CivSettings.expGenericLevels.get(bestLevel);
				break;
			}
			bestLevel++;
		}
		return level.level;
	}
	
	public static int getMaxMiningLevel() {
		int returnLevel = 0;
		for (Integer level : CivSettings.expGenericLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		return returnLevel;
	}
	
	// Fishing EXP
	public double getFishingEXP() {
		return fishingEXP;
	}
	
	public void addFishingEXP(double generated) throws CivException {
		player = CivGlobal.getPlayerE(this.getName());
		ConfigEXPGenericLevel clc = CivSettings.expGenericLevels.get(this.getFishingLevel());
		DecimalFormat df = new DecimalFormat("0.00");
		this.fishingEXP = Double.valueOf(df.format(this.fishingEXP + generated));
		this.save();
		
		if (this.getFishingLevel() < getMaxFishingLevel()) {
			if (this.fishingEXP >= clc.amount) {
				CivMessage.sendQuestExp(player, "You have became Fishing Level "+this.getFishingLevel()+"!");
			}
		}
		CivMessage.sendQuestExp(player, "+"+generated+" Fishing EXP");
	}
	
	public void setFishingEXP(double generated) {
		DecimalFormat df = new DecimalFormat("0.00");
		double gen = Double.valueOf(df.format(generated));
		this.fishingEXP = gen;
	}
	
	public int getFishingLevel() {
		// Get the first level
		int bestLevel = 0;
		ConfigEXPGenericLevel level = CivSettings.expGenericLevels.get(0);
		
		while (this.fishingEXP >= level.amount) {
			level = CivSettings.expGenericLevels.get(bestLevel+1);
			if (level == null) {
				level = CivSettings.expGenericLevels.get(bestLevel);
				break;
			}
			bestLevel++;
		}
		return level.level;
	}
	
	public static int getMaxFishingLevel() {
		int returnLevel = 0;
		for (Integer level : CivSettings.expGenericLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		return returnLevel;
	}
	
	// UUID Info
	public UUID getUUID() {
		return uid;
	}
	
	public String getUUIDString() {
		return uid.toString();
	}
	
	public void setUUID(UUID uid) {
		this.uid = uid;
	}
}
