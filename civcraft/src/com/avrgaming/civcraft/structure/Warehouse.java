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

import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Warehouse extends Structure {
	
	private int level = 1;
	
	private boolean gotoTrommel = false;
	
	private boolean collectTrommel = false;
	private int collectQuarry = 0;
	
	protected Warehouse(Location center, String id, Town town) throws CivException {
		super(center, id, town);	
	}
	
	public Warehouse(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	public String getDynmapDescription() {
		String out = "<u><b>Warehouse</u></b><br/>";
		out += "Level: "+this.level;
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "key";
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		this.level = getTown().saved_warehouse_level;
		
		String keyTrommelIn = getInfoKey(this, "trommel", "input");
		ArrayList<SessionEntry> entryTrommelIn = CivGlobal.getSessionDB().lookup(keyTrommelIn);
		if (entryTrommelIn != null && !entryTrommelIn.isEmpty()) {
			SessionEntry se = entryTrommelIn.get(0);
			this.gotoTrommel = Boolean.valueOf(se.value);
		} else {
			this.sessionAdd(this.getInfoKey(this, "trommel", "input"), "false");
		}
		
		String keyTrommelOut = getInfoKey(this, "trommel", "output");
		ArrayList<SessionEntry> entryTrommelOut = CivGlobal.getSessionDB().lookup(keyTrommelOut);
		if (entryTrommelOut != null && !entryTrommelOut.isEmpty()) {
			SessionEntry se = entryTrommelOut.get(0);
			this.collectTrommel = Boolean.valueOf(se.value);
		} else {
			this.sessionAdd(this.getInfoKey(this, "trommel", "output"), "false");
		}
		
		String keyQuarry = getInfoKey(this, "quarry", "output");
		ArrayList<SessionEntry> entryQuarry = CivGlobal.getSessionDB().lookup(keyQuarry);
		if (entryQuarry != null && !entryQuarry.isEmpty()) {
			SessionEntry se = entryQuarry.get(0);
			this.collectQuarry = Integer.valueOf(se.value);
		} else {
			this.sessionAdd(this.getInfoKey(this, "quarry", "output"), "0");
		}
		
//		this.collectTrommel = getTown().warehouse_collect_trommel;
//		this.collectQuarry = getTown().warehouse_collect_quarry;
	}
	
	public String getInfoKey(Structure struct, String type, String direction) {
		return struct.getTown().getName().toLowerCase()+"_"+struct.getConfigId()+"_"+type+":"+direction;
	}
	
	public int getLevel() { return level; }
	public void setLevel(int level) { this.level = level; }
	
	public boolean canGotoTrommel() { return gotoTrommel; }
	public void toggleGotoTrommel() {
		if (this.gotoTrommel) { this.gotoTrommel = false; }
							else { this.gotoTrommel = true; }
	}
	
	public boolean canCollectTrommel() { return collectTrommel; }
	public void toggleCollectTrommel() {
		if (this.collectTrommel) { this.collectTrommel = false; }
							else { this.collectTrommel = true; }
	}
	
	public Integer getQuarryCollector() { return collectQuarry; }
	public void toggleCollectQuarry() {
		if (this.collectQuarry == 0) { this.collectQuarry = 1; }
		else if (this.collectQuarry == 1) { this.collectQuarry = 2; }
								else { this.collectQuarry = 0; }
	}
	
	// XXX Villager stuff
	
	public void spawnToggleVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Warehouse Guide");
		v.setProfession(Profession.LIBRARIAN);
		CivGlobal.addStructureVillager(v);
	}
	
	public void openToggleGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+"'s Warehouse Guide");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Warehouse Guide Menu. You can use",
				CivColor.RESET+"it to toggle whether structures will send their",
				CivColor.RESET+"goods here or, by default, to their own chests.",
				CivColor.RESET+"Upgrading the Warehouse will allow for more",
				CivColor.RESET+"chests to be able to fill within the Warehouse."
				));
		
		// Inputs
		
		ItemStack iTrommelIn = new ItemStack(Material.HOPPER, 1);
		ItemMeta mTrommelIn = iTrommelIn.getItemMeta();
		mTrommelIn.setDisplayName(CivColor.WhiteBold+"Trommel (Input)");
		List<String> loreTrommelIn = new ArrayList<>();
		if (this.canGotoTrommel()) {
			loreTrommelIn.add(CivColor.LightGreen+"Its input is the Warehouse.");
		} else {
			loreTrommelIn.add(CivColor.LightGreen+"Its input is its structure.");
		}
		loreTrommelIn.add(CivColor.Gold+"<Click to Toggle>");
		mTrommelIn.setLore(loreTrommelIn);
		iTrommelIn.setItemMeta(mTrommelIn);
		inv.setItem(9, iTrommelIn);
		
		// Outputs
		
		ItemStack iTrommelOut = new ItemStack(Material.HOPPER, 1);
		ItemMeta mTrommelOut = iTrommelOut.getItemMeta();
		mTrommelOut.setDisplayName(CivColor.WhiteBold+"Trommel (Output)");
		List<String> loreTrommelOut = new ArrayList<>();
		if (this.canCollectTrommel()) {
			loreTrommelOut.add(CivColor.LightGreen+"Its output comes to the Warehouse.");
		} else {
			loreTrommelOut.add(CivColor.LightGreen+"Its output stays at its structure.");
		}
		loreTrommelOut.add(CivColor.Gold+"<Click to Toggle>");
		mTrommelOut.setLore(loreTrommelOut);
		iTrommelOut.setItemMeta(mTrommelOut);
		inv.setItem(18, iTrommelOut);
		
		ItemStack iQuarryOut = new ItemStack(Material.COBBLE_WALL, 1);
		ItemMeta mQuarryOut = iQuarryOut.getItemMeta();
		mQuarryOut.setDisplayName(CivColor.WhiteBold+"Quarry (Output)");
		List<String> loreQuarry = new ArrayList<>();
		if (this.getQuarryCollector() == 2) {
			loreQuarry.add(CivColor.LightGreen+"Its output goes to the Trommel.");
			loreQuarry.add(CivColor.Yellow+"(Only stone outputs, other drops stay at Quarry.)");
		} else if (this.getQuarryCollector() == 1) {
			loreQuarry.add(CivColor.LightGreen+"Its output comes to the Warehouse.");
		} else {
			loreQuarry.add(CivColor.LightGreen+"Its output stays at its structure.");
		}
		loreQuarry.add(CivColor.Gold+"<Click to Toggle>");
		mQuarryOut.setLore(loreQuarry);
		iQuarryOut.setItemMeta(mQuarryOut);
		inv.setItem(19, iQuarryOut);
		
		p.openInventory(inv);
	}
}
