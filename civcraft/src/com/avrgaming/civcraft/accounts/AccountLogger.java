package com.avrgaming.civcraft.accounts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.database.SQL;
import com.avrgaming.civcraft.database.SQLUpdate;
import com.avrgaming.civcraft.exception.InvalidNameException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.SQLObject;

public class AccountLogger extends SQLObject {
	
	private UUID uid;
	private ArrayList<String> logged_ips = new ArrayList<String>();
	
	public static String TABLE_NAME = "ACCOUNT_LOGGER";
	public AccountLogger(ResultSet rs) throws SQLException, InvalidNameException {
		this.load(rs);
	}
	
	public AccountLogger(UUID uid, String ip) throws InvalidNameException {
		this.uid = uid;
		this.logged_ips.add(ip);
	}

	public static void init() throws SQLException {
		System.out.println("================ ACCOUNT_LOGGER INIT ================");
		if (!SQL.hasTable(TABLE_NAME)) {
			String table_create = "CREATE TABLE " + TABLE_NAME+" (" +
					"`id` int(11) unsigned NOT NULL auto_increment," +
					"`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN',"+
					"`logged_ips` mediumtext DEFAULT NULL," +
					"PRIMARY KEY (`id`)" + ")";
			SQL.makeTable(table_create);
			CivLog.info("Created "+TABLE_NAME+" Table");
		} else {
			if (!SQL.hasColumn(TABLE_NAME, "uuid")) {
				CivLog.info("\tCouldn't find `uuid` for resident.");
				SQL.addColumn(TABLE_NAME, "`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN'");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "logged_ips")) {
				CivLog.info("\tCouldn't find `logged_ips` for resident.");
				SQL.addColumn(TABLE_NAME, "`logged_ips` mediumtext DEFAULT NULL,");
			}
			
			CivLog.info(TABLE_NAME+" table OK!");
		}		
		System.out.println("=====================================================");
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException, InvalidNameException {
		this.setId(rs.getInt("id"));
		if (rs.getString("uuid").equalsIgnoreCase("UNKNOWN")) this.uid = null;
		else this.uid = UUID.fromString(rs.getString("uuid"));
		
		if (rs.getString("logged_ips") != null) this.setIPsFromString(rs.getString("logged_ips"));
	}
	
	@Override
	public void save() {
		SQLUpdate.add(this);
	}
	
	@Override
	public void saveNow() throws SQLException {
		HashMap<String, Object> hashmap = new HashMap<String, Object>();
		hashmap.put("uuid", this.getUUIDString());
		if (this.getIPsFromString() != null) {
			String finalips = "";
			for (String s : this.getIPsFromString()) {
				CivMessage.global(s);
				if (s != null && !finalips.contains(s)) {
					finalips += s+",";
				}
			}
			hashmap.put("logged_ips", finalips);
		}
		SQL.updateNamedObject(this, hashmap, TABLE_NAME);
	}
	
	@Override
	public void delete() throws SQLException {
		SQL.deleteByName(this.getName(), TABLE_NAME);
	}
	
	public UUID getUUID() {
		return uid;
	}
	
	public String getUUIDString() {
		return uid.toString();
	}
	
	public void setUUID(UUID uid) {
		this.uid = uid;
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(UUID.fromString(getUUIDString()));
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(UUID.fromString(getUUIDString()));
	}
	
	public Resident getResident() {
		return CivGlobal.getResidentViaUUID(uid);
	}
	
	private void setIPsFromString(String ip) {
		String[] split = ip.split(",");
		for (String str : split) {
			synchronized (str) {
				if (str == null || str.equals("")) continue;
				this.logged_ips.add(str);
			}
		}
	}
	
	public void addIPFromString(String ip) {
		this.logged_ips.add(ip);
	}
	
	public ArrayList<String> getIPsFromString() {
		return this.logged_ips;
	}
}
