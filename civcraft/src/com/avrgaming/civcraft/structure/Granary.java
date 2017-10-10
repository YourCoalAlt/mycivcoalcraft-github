package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGranaryFood;
import com.avrgaming.civcraft.config.ConfigGranaryLevel;
import com.avrgaming.civcraft.config.ConfigGranaryTask;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Granary extends Structure {
	
	private int level = 1;
	
	protected Granary(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public Granary(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	public String getDynmapDescription() {
		String out = "<u><b>Granary</u></b><br/>";
		out += "Level: "+this.level;
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "chest";
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		this.level = getTown().saved_granary_level;
		
		for (ConfigGranaryFood f : CivSettings.granaryFood.values()) {
			Material m = ItemManager.getMaterial(f.food);
			String key = getStorageKey(this, m.toString().toLowerCase());
			ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
			if (entry != null && !entry.isEmpty()) {
			} else {
				this.sessionAdd(this.getStorageKey(this, m.toString().toLowerCase()), "0");
			}
		}
		
		switch (sb.command) {
		case "/task":
			spawnTaskVillager(absCoord.getLocation(), (byte)sb.getData());
		case "/storage":
			spawnStorageVillager(absCoord.getLocation(), (byte)sb.getData());
		}
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	// Storage
	
	public String getStorageKey(Structure struct, String type) {
		return struct.getConfigId()+"_"+struct.getCorner().toString()+"_"+type;
	}
	
	public void spawnStorageVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Granary Food Storage");
		v.setProfession(Profession.NITWIT);
		for (Villager vg : CivGlobal.structureVillagers.keySet()) {
			if (vg.getLocation().equals(v.getLocation())) {
				CivGlobal.removeStructureVillager(v);
				v.remove();
			}
		}
		CivGlobal.addStructureVillager(v);
	}
	
	public void openStorageGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+"'s Food Storage");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Granary Storage Menu. You can use",
				CivColor.RESET+"it to increase the food needed to feed the",
				CivColor.RESET+"cottages within the town. If there is no food",
				CivColor.RESET+"inside, then cottages will starve! "+CivColor.STRIKETHROUGH+"Upgrade the",
				CivColor.RESET+CivColor.STRIKETHROUGH+"granary to increase the amount of food to store."
				));
		
		ConfigGranaryLevel gl = CivSettings.granaryLevels.get(this.getLevel());
		for (ConfigGranaryFood f : CivSettings.granaryFood.values()) {
			Material m = ItemManager.getMaterial(f.food);
			String key = getStorageKey(this, m.toString().toLowerCase());
			ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
			if (entry != null && !entry.isEmpty()) {
				SessionEntry se = entry.get(0);
				ItemStack item = new ItemStack(m, 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(CivColor.WhiteBold+m.toString().substring(0, 1).toUpperCase()+m.toString().substring(1).toLowerCase().replace("_", " ").replace(" item", ""));
				List<String> lore = new ArrayList<>();
				lore.add(CivColor.LightGreen+"Count:  "+CivColor.Yellow+se.value+" / "+gl.max_storage);
				lore.add(CivColor.Purple+"<Click to Deposit>");
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			} else {
				CivMessage.sendError(p, "SessionError key: "+key);
				
				this.sessionAdd(this.getStorageKey(this, m.toString().toLowerCase()), "0");
				ItemStack item = new ItemStack(m, 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(CivColor.WhiteBold+m.toString().substring(0, 1).toUpperCase()+m.toString().substring(1).toLowerCase().replace("_", " "));
				List<String> lore = new ArrayList<>();
				lore.add(CivColor.LightGreen+"Count:  0 / UNLIMITED");
				lore.add(CivColor.Purple+"<Click to Deposit>");
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			}
		}
		p.openInventory(inv);
	}
	
	public void openStorageItemMenuGUI(Player p, Town town, String mat) {
		int amt = 0;
		String key = getStorageKey(this, mat);
		ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
		if (entry != null && !entry.isEmpty()) {
			SessionEntry se = entry.get(0);
			amt = Integer.valueOf(se.value);
		}
		
		String dismat = mat.toString().substring(0, 1).toUpperCase()+mat.toString().substring(1).toLowerCase();
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+" Storage ("+mat+")");
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.LightGreen+"Currently Storing: "+CivColor.Yellow+amt+" "+dismat
				));
		p.openInventory(inv);
	}
	
	// Tasks
	
	public String getTaskKey(Structure struct, String tag) {
		return struct.getConfigId()+"_"+struct.getCorner().toString()+"_"+tag;
	}
	
	public void spawnTaskVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Granary Tasks");
		v.setProfession(Profession.NITWIT);
		for (Villager vg : CivGlobal.structureVillagers.keySet()) {
			if (vg.getLocation().equals(v.getLocation())) {
				CivGlobal.removeStructureVillager(v);
				v.remove();
			}
		}
		CivGlobal.addStructureVillager(v);
	}
	
	public void openTaskGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+"'s Granary Tasks");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Granary Quest Menu. You can use",
				CivColor.RESET+"it to complete tasks to level your town up",
				CivColor.RESET+"with culture. You can get more tasks by having",
				CivColor.RESET+"a higher town level, or through upgrades.",
				CivColor.RESET+"Upgrades can also increase culture output!"
				));
		
		for (ConfigGranaryTask t : CivSettings.granaryTasks.values()) {
			if (this.getTown().getLevel() < t.task) {
				ItemStack item = new ItemStack(Material.BLACK_SHULKER_BOX, 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(CivColor.WhiteBold+CivColor.ITALIC+"[Locked] Task "+t.task);
				List<String> lore = new ArrayList<>();
				lore.add(CivColor.LightGreen+"Will Require: "+CivColor.Rose+t.required+" Bread");
				lore.add(CivColor.LightGreen+"Will Reward: "+CivColor.Rose+t.reward+" Culture");
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			} else {
				String key = getTaskKey(this, "task"+t.task);
				ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
				if (entry == null || entry.isEmpty()) {
					ItemStack item = new ItemStack(Material.LIME_SHULKER_BOX, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CivColor.WhiteBold+"[Available] Task "+t.task);
					List<String> lore = new ArrayList<>();
					lore.add(CivColor.LightGreen+"Requires: "+CivColor.Yellow+t.required+" Bread");
					lore.add(CivColor.LightGreen+"Rewards: "+CivColor.Yellow+t.reward+" Culture");
					meta.setLore(lore);
					item.setItemMeta(meta);
					inv.addItem(item);
				} else {
					ItemStack item = new ItemStack(Material.RED_SHULKER_BOX, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CivColor.WhiteBold+CivColor.ITALIC+"[Completed] Task "+t.task);
					List<String> lore = new ArrayList<>();
					lore.add(CivColor.LightGreen+"Consumed: "+CivColor.Rose+t.required+" Bread");
					lore.add(CivColor.LightGreen+"Rewarded: "+CivColor.Rose+t.reward+" Culture");
					meta.setLore(lore);
					item.setItemMeta(meta);
					inv.addItem(item);
				}
			}
		}
		p.openInventory(inv);
	}
	
	public void openTaskCompleterGUI(Player p, Town town, int task) {
		ConfigGranaryTask gtasks = CivSettings.granaryTasks.get(task);
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+" Granary Task "+task);
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Requirements", ItemManager.getId(Material.PAPER), 0, 
				CivColor.LightGreen+"Requires: "+CivColor.Yellow+gtasks.required+" Bread",
				CivColor.LightGreen+"Rewards: "+CivColor.Yellow+gtasks.reward+" Culture"
				));
		p.openInventory(inv);
	}
	
	public void resetTasks() {
		for (ConfigGranaryTask t : CivSettings.granaryTasks.values()) {
			String key = getTaskKey(this, "task"+t.task);
			ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
			synchronized (entry) {
				if (entry != null && !entry.isEmpty()) {
					SessionEntry se = entry.get(0);
					CivGlobal.getSessionDB().delete(se.request_id, se.key);
				}
			}
		}
	}
	
	@Override
	public void delete() throws SQLException {
		super.delete();
		for (ConfigGranaryTask t : CivSettings.granaryTasks.values()) {
			String key = getTaskKey(this, "task"+t.task);
			ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
			synchronized (entry) {
				if (entry != null && !entry.isEmpty()) {
					SessionEntry se = entry.get(0);
					CivGlobal.getSessionDB().delete(se.request_id, se.key);
				}
			}
		}
	}
}
