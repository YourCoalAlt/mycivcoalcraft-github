/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
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
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.mobs.CivVillager;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.WindmillStartSyncTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Windmill extends Structure {
	
	public int wheat_sel;
	public int carrot_sel;
	public int potato_sel;
	public int beetroot_sel;
	public int max_plant;
	
	public Windmill(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public Windmill(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		this.sessionAdd(this.getSaveKey(this), "0");
		switch (sb.command) {
		case "/plant":
			spawnPlantVillager(absCoord.getLocation(), (byte)sb.getData());
		}
	}
	
	// XXX Planting Villager
	
	public int getMaxPlantAmount() {
		int plant_max = 8;
		try {
			plant_max = CivSettings.getInteger(CivSettings.structureConfig, "windmill.plant_max");
		} catch (InvalidConfiguration e) {
			CivLog.warning("Could not get structure.yml windmill.plant_max, setting to default: 8");
		}
		
		if (this.getCiv().hasTechnology("tech_machinery")) {
			plant_max *= 2;
		}
		
		return plant_max;
	}
	
	public int getSumPlantSettings() {
		return this.wheat_sel+this.carrot_sel+this.potato_sel+this.beetroot_sel;
	}
	
	public String getSaveKey(Structure struct) {
		return "WINDPLANT_"+struct.getConfigId()+"_"+struct.getCorner().toString()+"_"+this.wheat_sel+"="+this.carrot_sel+"="+this.potato_sel+"="+this.beetroot_sel;
	}
	
	public void spawnPlantVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		CivVillager.onSpawn(v, vLoc, "Windmill Manager", false, Profession.FARMER);
		
		String vilKey = this.getTown().getName()+":"+v.getCustomName()+":"+v.getLocation().toString();
		if (CivGlobal.getStructureVillager(vilKey) != null) {
			v.setHealth(0); v.remove();
		} else {
			CivGlobal.addStructureVillager(vilKey, v);
		}
	}
	
	public void openPlantSettingsGUI(Player plr, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*1, "Windmill Menu");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Windmill Settings Menu. You",
				CivColor.RESET+"can change the amount of what crops to",
				CivColor.RESET+"plant, for when the windmill ticks.",
				CivColor.RESET+" ",
				CivColor.RESET+" "
				));
		
		ItemStack w = new ItemStack(Material.SEEDS, 1);
		ItemMeta wm = w.getItemMeta();
		wm.setDisplayName(CivData.getDisplayName(295, 0));
		List<String> wl = new ArrayList<>();
		wl.add(CivColor.LightGreen+this.wheat_sel);
		wl.add(CivColor.LightGray+" « Left Click +1 » ");
		wl.add(CivColor.LightGray+" « Right Click -1 » ");
		wm.setLore(wl); w.setItemMeta(wm);
		inv.addItem(w);
		
		ItemStack c = new ItemStack(Material.CARROT_ITEM, 1);
		ItemMeta cm = c.getItemMeta();
		cm.setDisplayName(CivData.getDisplayName(391, 0));
		List<String> cl = new ArrayList<>();
		cl.add(CivColor.LightGreen+this.carrot_sel);
		cl.add(CivColor.LightGray+" « Left Click +1 » ");
		cl.add(CivColor.LightGray+" « Right Click -1 » ");
		cm.setLore(cl); c.setItemMeta(cm);
		inv.addItem(c);
		
		ItemStack p = new ItemStack(Material.POTATO_ITEM, 1);
		ItemMeta pm = p.getItemMeta();
		pm.setDisplayName(CivData.getDisplayName(392, 0));
		List<String> pl = new ArrayList<>();
		pl.add(CivColor.LightGreen+this.potato_sel);
		pl.add(CivColor.LightGray+" « Left Click +1 » ");
		pl.add(CivColor.LightGray+" « Right Click -1 » ");
		pm.setLore(pl); p.setItemMeta(pm);
		inv.addItem(p);
		
		ItemStack b = new ItemStack(Material.BEETROOT, 1);
		ItemMeta bm = b.getItemMeta();
		bm.setDisplayName(CivData.getDisplayName(434, 0));
		List<String> bl = new ArrayList<>();
		bl.add(CivColor.LightGreen+this.beetroot_sel);
		bl.add(CivColor.LightGray+" « Left Click +1 » ");
		bl.add(CivColor.LightGray+" « Right Click -1 » ");
		bm.setLore(bl); b.setItemMeta(bm);
		inv.addItem(b);
		
		plr.openInventory(inv);
	}
	
	// XXX Plant Task
	
	public void processWindmill() {
		// Fire a sync task to perform this.
		TaskMaster.syncTask(new WindmillStartSyncTask(this), 0);
	}
}
