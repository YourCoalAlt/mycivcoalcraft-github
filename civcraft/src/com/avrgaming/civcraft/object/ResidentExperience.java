package com.avrgaming.civcraft.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

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
	private Map<EXPSlots, Double> exp_slots = new HashMap<EXPSlots, Double>();
	
	public enum EXPSlots {
		QUEST,
		MINING,
		FISHING,
		FARMING,
		SLAUGHTER,
		WEAPONDRY
	}
	
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
		if (this.exp_slots.keySet().size() != EXPSlots.values().length) {
			for (EXPSlots s : EXPSlots.values()) {
				this.exp_slots.put(s, 0.0);
			}
		}
	}
	
	public static final String TABLE_NAME = "RESIDENTS_EXPERIENCE";
	public static void init() throws SQLException {
		if (!SQL.hasTable(TABLE_NAME)) {
			String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME+" (" + 
					"`id` int(11) unsigned NOT NULL auto_increment," +
					"`name` VARCHAR(64) NOT NULL," +
					"`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN',"+
					"`exp_slots` mediumtext DEFAULT NULL," +
					"`questEXP` double DEFAULT 0," +
					"`miningEXP` double DEFAULT 0," +
					"`fishingEXP` double DEFAULT 0," +
					"`weapondryEXP` double DEFAULT 0," +
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
			
			if (!SQL.hasColumn(TABLE_NAME, "exp_slots")) {
				CivLog.info("\tCouldn't find `exp_slots` for resident.");
				SQL.addColumn(TABLE_NAME, "`exp_slots` mediumtext default null");
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
			
			if (!SQL.hasColumn(TABLE_NAME, "weapondryEXP")) {
				CivLog.info("\tCouldn't find `weapondryEXP` for resident experience.");
				SQL.addColumn(TABLE_NAME, "`weapondryEXP` double DEFAULT 0");
			}
		}		
	}

	@Override
	public void load(ResultSet rs) throws SQLException, InvalidNameException {
		this.setId(rs.getInt("id"));
		this.setName(rs.getString("name"));
		this.uid = UUID.fromString(rs.getString("uuid"));
		this.loadCategoriesFromString(rs.getString("exp_slots"));
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
		hashmap.put("exp_slots", this.saveCategoriesToString(this.exp_slots));
		SQL.updateNamedObject(this, hashmap, TABLE_NAME);
	}
	
	@Override
	public void delete() throws SQLException {	
		SQL.deleteByName(this.getName(), TABLE_NAME);
	}
	
	private void loadCategoriesFromString(String string) {
		if (string == null || string == "") {
			this.loadSettings();
			return;
		}
		String[] keyvalues = string.split(";");
		for (String keyvalue : keyvalues) {
			String key = keyvalue.split(":")[0];
			String value = keyvalue.split(":")[1];
			exp_slots.put(EXPSlots.valueOf(key), Double.valueOf(value));
		}
	}
	
	private String saveCategoriesToString(Map<EXPSlots, Double> exp_slots) {
		String out = "";
		for (EXPSlots key : exp_slots.keySet()) {
			double value = exp_slots.get(key);
			out += key+":"+value+";";
		}
		return out;
	}
	
	public Map<EXPSlots, Double> getEXPSlots() {
		return this.exp_slots;
	}
	
	public String getSlotString(EXPSlots slot) {
		String string = "";
		string = slot.toString().toUpperCase().substring(0, 1)+slot.toString().toLowerCase().substring(1);
		return string;
	}
	
	public int getEXPLevel(EXPSlots slot) {
		int level = 1;
		if (this.exp_slots.containsKey(slot)) {
			double exp = this.exp_slots.get(slot);
			for (int i = level; i < 300; i++) {
				int comp1 = (10*level);
				int comp2 = (int) (Math.multiplyExact((int) Math.pow(level, 2), 5));
				int tmplvl = (comp1+comp2)*2;
				if (exp >= tmplvl) level++;
				else return level;
			}
		}
		return level;
	}
	
	public double getEXPCount(EXPSlots slot) {
		if (this.exp_slots.containsKey(slot)) {
			return this.exp_slots.get(slot);
		} else {
			return 0.0;
		}
	}
	
	public int getEXPToNextLevel(EXPSlots slot) {
		int level = getEXPLevel(slot);
		int comp1 = (10*level);
		int comp2 = (int) (Math.multiplyExact((int) Math.pow(level, 2), 5));
		int next_level = (comp1+comp2)*2;
		return next_level;
	}
	
	public void addResEXP(EXPSlots slot, double base_points) throws CivException {
		player = CivGlobal.getPlayerE(this.getName());
		DecimalFormat df = new DecimalFormat("#.##");
		int level = this.getEXPLevel(slot);
		double gen1 = base_points*(level-1)*0.01;
		double gen2 = base_points+(level-1)*0.01;
		double gen3 = (1-level)*0.05;
		double genEq = (base_points+gen1)-(gen2*gen3);
		double generatedEXP = Double.valueOf(df.format(genEq));
//		double generatedEXP = Double.valueOf(df.format(base_points+(gen1)-((base_points+(level-1)*0.01)*(1-level)*0.05)));
		
		if (this.exp_slots.containsKey(slot)) {
			double newTotalEXP = Double.valueOf(df.format(Double.valueOf(this.exp_slots.get(slot)) + generatedEXP));
			int checkNewLvl = this.getEXPLevel(slot);
			if (checkNewLvl > level) {
				CivMessage.sendQuestExp(player, "You are now "+this.getSlotString(slot)+" Level "+(checkNewLvl)+"!");
			}
			this.exp_slots.put(slot, newTotalEXP);
		} else {
			this.exp_slots.put(slot, generatedEXP);
		}
		CivMessage.sendQuestExp(player, "+"+generatedEXP+" "+this.getSlotString(slot)+" EXP");
		try {
			this.saveNow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addResEXPviaAdmin(EXPSlots slot, double base_points) throws CivException {
		player = CivGlobal.getPlayerE(this.getName());
		DecimalFormat df = new DecimalFormat("#.##");
		int level = this.getEXPLevel(slot);
		
		if (this.exp_slots.containsKey(slot)) {
			double newTotalEXP = Double.valueOf(df.format(Double.valueOf(this.exp_slots.get(slot)) + base_points));
			int checkNewLvl = this.getEXPLevel(slot);
			if (checkNewLvl > level) {
				CivMessage.sendQuestExp(player, "You are now "+this.getSlotString(slot)+" Level "+(checkNewLvl)+"!");
			}
			this.exp_slots.put(slot, newTotalEXP);
		} else {
			this.exp_slots.put(slot, base_points);
		}
		CivMessage.sendQuestExp(player, "+"+base_points+" "+this.getSlotString(slot)+" EXP [via Admin]");
		try {
			this.saveNow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
