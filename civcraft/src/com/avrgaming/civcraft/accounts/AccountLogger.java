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
	
	private boolean banned = false;
	private String banMessage = "null";
	private long banLength = 0;
	
	private boolean muted = false;
	private String muteMessage = "null";
	private long muteLength = 0;
	
//	private boolean ipBanned = false;
//	private String ipBanMessage = "null";
//	private long ipBanLength = 0;
	
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
					"`banned` bool NOT NULL DEFAULT '0'," +
					"`bannedMessage` mediumtext DEFAULT NULL,"+
					"`bannedLength` BIGINT DEFAULT '0'," + 
					"`muted` bool NOT NULL DEFAULT '0'," +
					"`mutedMessage` mediumtext DEFAULT NULL,"+
					"`mutedLength` BIGINT DEFAULT '0'," + 
					"PRIMARY KEY (`id`)" + ")";
			SQL.makeTable(table_create);
			CivLog.info("Created "+TABLE_NAME+" Table");
		} else {
			if (!SQL.hasColumn(TABLE_NAME, "uuid")) {
				CivLog.info("\tCouldn't find `uuid` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN'");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "logged_ips")) {
				CivLog.info("\tCouldn't find `logged_ips` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`logged_ips` mediumtext DEFAULT NULL,");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "banned")) {
				CivLog.info("\tCouldn't find `banned` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`banned` bool default 0");
			}			
			
			if (!SQL.hasColumn(TABLE_NAME, "bannedMessage")) {
				CivLog.info("\tCouldn't find `bannedMessage` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`bannedMessage` mediumtext default null");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "bannedLength")) {
				CivLog.info("\tCouldn't find `bannedLength` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`bannedLength` bigint default 0");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "muted")) {
				CivLog.info("\tCouldn't find `muted` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`muted` bool default 0");
			}			
			
			if (!SQL.hasColumn(TABLE_NAME, "mutedMessage")) {
				CivLog.info("\tCouldn't find `mutedMessage` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`mutedMessage` mediumtext default null");
			}
			
			if (!SQL.hasColumn(TABLE_NAME, "mutedLength")) {
				CivLog.info("\tCouldn't find `mutedLength` for account_logger.");
				SQL.addColumn(TABLE_NAME, "`mutedLength` bigint default 0");
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
		
		this.banned = rs.getBoolean("banned");
		this.banMessage = rs.getString("bannedMessage");
		this.banLength = rs.getLong("bannedLength");
		
		this.muted = rs.getBoolean("muted");
		this.muteMessage = rs.getString("mutedMessage");
		this.muteLength = rs.getLong("mutedLength");
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
		
		hashmap.put("banned", this.isBanned());
		hashmap.put("bannedMessage", this.getBanMessage());
		hashmap.put("bannedLength", this.getBanLength());
		
		hashmap.put("muted", this.isMuted());
		hashmap.put("mutedMessage", this.getMuteMessage());
		hashmap.put("mutedLength", this.getMuteLength());
		
		SQL.updateNamedObject(this, hashmap, TABLE_NAME);
	}
	
	@Override
	public void delete() throws SQLException {
		SQL.deleteByName(this.getName(), TABLE_NAME);
	}
	
	//UUID/Player
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
	
	//IP Addresses
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
	
	//Muting
	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}
	
	public void setMuteMessage(String msg) {
		this.muteMessage = msg;
	}
	
	public String getMuteMessage() {
		return muteMessage;
	}
	
	public void setMuteLength(int sec, int min, int hours) {
		this.muteLength = System.currentTimeMillis() + (hours*60*60*1000) + (min*60*1000) + (sec*1000);
	}
	
	public void resetMuteLength() {
		this.muteLength = 0;
	}
	
	public Long getMuteLength() {
		return muteLength;
	}
	
	// Banning
	public boolean isBanned() {
		return banned;
	}

	public void setBanned(boolean banned) {
		this.banned = banned;
	}
	
	public void setBanMessage(String msg) {
		this.banMessage = msg;
	}
	
	public String getBanMessage() {
		return banMessage;
	}
	
	public void setBanLength(int sec, int min, int hours) {
		this.banLength = System.currentTimeMillis() + (hours*60*60*1000) + (min*60*1000) + (sec*1000);
	}
	
	public void resetBanLength() {
		this.banLength = 0;
	}
	
	public Long getBanLength() {
		return banLength;
	}
}
