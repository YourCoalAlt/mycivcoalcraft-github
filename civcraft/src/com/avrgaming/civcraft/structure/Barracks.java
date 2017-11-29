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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTech;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.interactive.InteractiveRepairItem;
import com.avrgaming.civcraft.items.components.RepairCost;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.listener.civcraft.HolographicDisplaysListener;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementUnitGainAttack;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.UnitSaveAsyncTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

import gpl.AttributeUtil;

public class Barracks extends Structure {
	
	private static final long SAVE_INTERVAL = 60*1000;
	
	private ConfigUnit trainingUnit = null;
	private double currentHammers = 0.0;
	
	private TreeMap<Integer, StructureSign> progressBar = new TreeMap<Integer, StructureSign>();
	private Date lastSave = null;
	
	protected Barracks(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public Barracks(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public void train(Resident r, String id) {
		ConfigUnit unit = CivSettings.units.get(id);
		if (unit == null) {
			CivMessage.sendError(r, "Unknown unit type "+id+".");
			return;
		}
		
		if (unit.limit != 0 && unit.limit < getTown().getUnitTypeCount(unit.id)) {
			CivMessage.sendError(r, "We've reached the maximum number of "+unit.name+" units we can have.");
			return;
		}
		
		if (!unit.isAvailable(getTown())) {
			CivMessage.sendError(r, "This unit is unavailable.");
			return;
		}
		
		if (!getTown().getTreasury().hasEnough(unit.cost)) {
			CivMessage.sendError(r, "Not enough coins to train unit. We require "+unit.cost+" coins.");
			return;
		}
		
		if (this.trainingUnit != null) {
			CivMessage.sendError(r, "Already training a "+this.trainingUnit.name+".");
			return;
		}
		
		if (unit.id.equals("u_settler")) {
			if (!this.getCiv().getLeaderGroup().hasMember(r) && !this.getCiv().getAdviserGroup().hasMember(r)) {
				CivMessage.sendError(r, "You must be an adivser to the civilization in order to build a Settler.");
				return;
			}
		}
		
		getTown().getTreasury().withdraw(unit.cost);
		this.setCurrentHammers(0.0);
		this.setTrainingUnit(unit);
		CivMessage.sendTown(getTown(), "We've begun training a "+unit.name+"!");
		this.updateTraining();
	}
	
	public boolean canRepairItem(ItemStack repair) {
		if (repair == null || repair.getType().equals(Material.AIR)) return false;
		if (repair.getType().getMaxDurability() == 0) return false;
		if (repair.getDurability() == 0) return false;
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(repair);
		if (craftMat == null) return false;
		
		for (String ritems : CivSettings.repairableItems) {
			String[] split = ritems.split(":");
			try {
				Integer type = Integer.valueOf(split[0]);
				int data = Integer.valueOf(split[1]);
				if (type == ItemManager.getId(repair) && data == ItemManager.getData(repair)) return true;
			} catch (NumberFormatException e) {
				String custMatID = split[0];
				if (craftMat == null || craftMat.getConfigId() != custMatID) continue;
				return true;
			}
		}
		return true;
	}
	
	public void repairItemCalculate(Player player, Resident res, ItemStack repair) {
		if (!this.canRepairItem(repair)) {
			player.getInventory().addItem(repair);
			CivMessage.sendError(player, "Cannot repair item, failed repair check?");
			return;
		}
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(repair);
		try {
			int totalCost;
			if (craftMat.hasComponent("RepairCost")) {
				RepairCost getRepairCost = (RepairCost)craftMat.getComponent("RepairCost");
				int repairCost = getRepairCost.getInt("value");
				
				double duraCost = 0;
				if (!LoreEnhancement.isWeaponOrArmor(repair)) {
					double itemDura = repair.getDurability();
					double maxDura = repair.getType().getMaxDurability();
					duraCost = Math.pow(itemDura, (maxDura-Math.abs(0.001*(itemDura-maxDura)))/maxDura);
				}
				int subTotal = (int) (repairCost + duraCost);
				totalCost = subTotal;
			} else {
				double baseTierRepair = CivSettings.getDouble(CivSettings.structureConfig, "barracks.base_tier_repair");
				
				double tierDamp = CivSettings.getDouble(CivSettings.structureConfig, "barracks.tier_damp");
				double tierCost = Math.pow((craftMat.getConfigMaterial().tier), tierDamp);
				double fromTier = Math.pow(baseTierRepair, tierCost);
				
				double duraCost = 0;
				if (!LoreEnhancement.isWeaponOrArmor(repair)) {
					double itemDura = repair.getDurability();
					double maxDura = repair.getType().getMaxDurability();
					duraCost = Math.pow(itemDura, (maxDura-Math.abs(0.001*(itemDura-maxDura)))/maxDura);
				}
				double subTotal = (fromTier + duraCost);
				totalCost = (int) subTotal;
			}
			
			InteractiveRepairItem repairItem = new InteractiveRepairItem(totalCost, player.getName(), repair);
			repairItem.displayMessage();
			res.setInteractiveMode(repairItem);
			return;
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			CivMessage.sendError(player, "Internal configuration error");
		}
	}
	
	public static void repairItem(int cost, String playerName, ItemStack stack) {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			CivMessage.sendError(playerName, "Cannot find player: "+playerName);
			return;
		}
		
		Resident resident = CivGlobal.getResident(player);
		if (!resident.getTreasury().hasEnough(cost)) {
			CivMessage.sendError(player, "Sorry, but you don't have the required "+cost+" coins.");
			return;
		}
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		ItemStack newStack = LoreMaterial.spawn(craftMat);
		AttributeUtil attr = new AttributeUtil(stack); newStack = attr.getStack();
		newStack.setDurability((short)0);
		player.getInventory().addItem(newStack);
		resident.getTreasury().withdraw(cost);
		CivMessage.sendSuccess(player, "Repaired "+craftMat.getName()+" for "+cost+" coins.");
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		StructureSign structSign;
		switch (sb.command) {
		case "/trainer":
			spawnTrainingVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		case "/upgrader":
			spawnUnitUpgradeVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		case "/progress":
			ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
			ItemManager.setData(absCoord.getBlock(), sb.getData());
			structSign = new StructureSign(absCoord, this);
			structSign.setText("");
			structSign.setDirection(sb.getData());
			structSign.setAction("");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
			this.progressBar.put(Integer.valueOf(sb.keyvalues.get("id")), structSign);
			break;
		case "/repair":
			spawnRepairVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		}
	}
	
	public ConfigUnit getTrainingUnit() {
		return trainingUnit;
	}
	
	public void setTrainingUnit(ConfigUnit trainingUnit) {
		this.trainingUnit = trainingUnit;
	}
	
	public double getCurrentHammers() {
		return currentHammers;
	}
	
	public void setCurrentHammers(double currentHammers) {
		this.currentHammers = currentHammers;
	}
	
	public void createUnit(ConfigUnit unit) {
		ArrayList<StructureChest> chests = this.getAllChestsById(0);
		if (chests.size() == 0) return;
		Chest chest = (Chest)chests.get(0).getCoord().getBlock().getState();
		try {
			Class<?> c = Class.forName(unit.class_name);
			Method m = c.getMethod("spawn", Inventory.class, Town.class);
			m.invoke(null, chest.getInventory(), this.getTown());
			
			CivMessage.sendTown(this.getTown(), "Completed a "+unit.name+"!");
			this.trainingUnit = null;
			this.currentHammers = 0.0;
			
			CivGlobal.getSessionDB().delete_all(getSessionKey());
		} catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
			this.trainingUnit = null;
			this.currentHammers = 0.0;
			CivMessage.sendTown(getTown(), CivColor.Red+"ERROR couldn't find class?:"+e.getMessage());
		} catch (InvocationTargetException e) {
			CivMessage.sendTown(getTown(), CivColor.Rose+e.getCause().getMessage());
			this.currentHammers -= 20.0;
			if (this.currentHammers < 0.0) {
				this.currentHammers = 0.0;
			}
			//e.printStackTrace();
		//	CivMessage.sendTown(getTown(), CivColor.Rose+e.getMessage());
		}
	}
	
	public void updateProgressBar() {
		double percentageDone = this.currentHammers / this.trainingUnit.hammer_cost;
		DecimalFormat df = new DecimalFormat("#.#");
		double sipe = (percentageDone*100);
		String per = df.format(sipe)+"%";
		
		int size = this.progressBar.size();
		int textCount = (int)(size*16*percentageDone);
		int textIndex = 0;
		
		for (int i = 0; i < size; i++) {
			StructureSign structSign = this.progressBar.get(i);
			String[] text = new String[4];
			text[0] = "";
			text[1] = "";
			text[2] = "";
			text[3] = "";
			for (int j = 0; j < 16; j++) {
				if (textIndex == 0) {
					text[2] += "[";
				} else if (textIndex == ((size*15)+3)) {
					text[2] += "]";
				} else if (textIndex < textCount) {
					text[2] += "=";
				} else {
					text[2] += "_";
				}
				textIndex++;
			}
			
			if (i == (size/2)) {
				text[0] = CivColor.GreenBold+this.trainingUnit.name;
				text[1] = per;
			}
			structSign.setText(text);
			structSign.update();
		}
		StructureChest sc = this.getAChestById(0);
		Location loc = new Location(Bukkit.getWorld("world"), sc.getCoord().getX()+0.5, sc.getCoord().getY()+2.0, sc.getCoord().getZ()+0.5);
		String title = CivColor.GoldBold+"Training: "+CivColor.LightGreen+this.trainingUnit.name;
		HolographicDisplaysListener.updateBarracksHolo(loc, title, per);
	}
	
	public String getSessionKey() {
		return this.getTown().getName()+":"+"barracks"+":"+this.getId();
	}
	
	public void saveProgress() {
		if (this.getTrainingUnit() != null) {
			String key = getSessionKey();
			String value = this.getTrainingUnit().id+":"+this.currentHammers; 
			ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
			if (entries.size() > 0) {
				SessionEntry entry = entries.get(0);
				CivGlobal.getSessionDB().update(entry.request_id, key, value);
				
				/* delete any bad extra entries. */
				for (int i = 1; i < entries.size(); i++) {
					SessionEntry bad_entry = entries.get(i);
					CivGlobal.getSessionDB().delete(bad_entry.request_id, key);
				}
			} else {
				this.sessionAdd(key, value);
			}
			lastSave = new Date();
		}	
	}
	
	@Override
	public void onUnload() {
		saveProgress();
	}
	
	@Override
	public void onLoad() {
		String key = getSessionKey();
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
		if (entries.size() > 0) {
			SessionEntry entry = entries.get(0);
			String[] values = entry.value.split(":");
			this.trainingUnit = CivSettings.units.get(values[0]);
			if (trainingUnit == null) {
				CivLog.error("Couldn't find in-progress unit id:"+values[0]+" for town "+this.getTown().getName());
				return;
			}
			
			this.currentHammers = Double.valueOf(values[1]);
			/* delete any bad extra entries. */
			for (int i = 1; i < entries.size(); i++) {
				SessionEntry bad_entry = entries.get(i);
				CivGlobal.getSessionDB().delete(bad_entry.request_id, key);
			}
		} 
	}
	
	public void updateTraining() {
		if (this.trainingUnit != null) {
			// Hammers are per hour, this runs per min. We need to adjust the hammers we add.
			double addedHammers = (getTown().getHammers().total / 60) / 60;
			this.currentHammers += addedHammers;
			
			this.updateProgressBar();
			Date now = new Date();
			
			if (lastSave == null || ((lastSave.getTime() + SAVE_INTERVAL) < now.getTime())) {
				TaskMaster.asyncTask(new UnitSaveAsyncTask(this), 0);
			}
			
			if (this.currentHammers >= this.trainingUnit.hammer_cost) {
				this.currentHammers = this.trainingUnit.hammer_cost;
				this.createUnit(this.trainingUnit);
			}
		}
	}
	
	// XXX Villager Information
	
	public void spawnTrainingVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Barracks Trainer");
		v.setProfession(Profession.LIBRARIAN);
		for (Villager vg : CivGlobal.structureVillagers.keySet()) {
			if (vg.getLocation().equals(v.getLocation())) {
				CivGlobal.removeStructureVillager(v);
				v.remove();
			}
		}
		CivGlobal.addStructureVillager(v);
	}
	
	public void openUnitTrainGUI(Player p, Town t) {
		Inventory inv = Bukkit.createInventory(null, 9*6, t.getName()+"'s Barracks Unit Train Menu");
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Barracks menu. Here, you can",
				CivColor.RESET+"use it to create units, ranging from war to",
				CivColor.RESET+"settlers for new towns.",
				CivColor.RESET+"",
				CivColor.RESET+""
				));
		
		Resident res = CivGlobal.getResident(p);
		for (ConfigUnit u : CivSettings.units.values()) {
			try { @SuppressWarnings("unused")
			Class<?> ctest = Class.forName(u.class_name);
			} catch (ClassNotFoundException e) {
				ItemStack is = LoreGuiItem.build(u.name, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+" « Coming Soon / Invalid » ");
				inv.setItem(u.position, is);
				continue;
			}
			
			if (!res.hasTown()) {
				ItemStack is = LoreGuiItem.build(u.name, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must belong to a town build a structure.");
				inv.setItem(u.position, is);
			} else if (!res.getTown().isMayor(res) && !res.getTown().isAssistant(res)) {
				ItemStack is = LoreGuiItem.build(u.name, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must be town mayor/assistant to build structures.");
				inv.setItem(u.position, is);
			} else {
				String out = "";
				
				if (t.hasEnough(u.cost)) out += CivColor.GreenBold+"Cost: "+CivColor.LightGreen+u.cost+" Coins;";
				else out += CivColor.RedBold+"Cost: "+CivColor.Rose+u.cost+" Coins;";
				
				out += CivColor.LightGreen+"       "+u.hammer_cost+" Hammers;";
				
				ConfigTech tech = CivSettings.techs.get(u.require_tech);
				if (tech == null) out += CivColor.GreenBold+"Required Tech: "+CivColor.YellowItalic+"None;";
				else
					if (!res.getCiv().hasTechnology(tech.id)) out += CivColor.Red+"Required Tech: "+CivColor.Rose+tech.name+";";
					else out += CivColor.GreenBold+"Required Tech: "+CivColor.LightGreen+tech.name+";";
				
				out += CivColor.GreenBold+"On Death Destroy Chance: "+CivColor.LightGreen+u.destroy_chance+"%;";
				
				out += CivColor.GreenBold+"Description:;";
				List<String> des = u.description;
				for (String s : des) out += CivColor.LightGreen+CivColor.colorize(s)+";";
				
				out += CivColor.GoldBold+"<Click To Train>";
				
				ItemStack si = LoreGuiItem.build(u.name, u.item_id, u.item_data, out.split(";"));
				si = LoreGuiItem.setAction(si, "UnitTrainBarracks");
				si = LoreGuiItem.setActionData(si, "unitid", u.id);
				inv.setItem(u.position, si);
			}
		}
		p.openInventory(inv);
	}
	
	// Item Repair Villager
	
	public void spawnRepairVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Barracks Repair Master");
		v.setProfession(Profession.BLACKSMITH);
		CivGlobal.addStructureVillager(v);
	}
	
	public void openRepairGUI(Player p, Town t) {
		Inventory inv = Bukkit.createInventory(null, 9*1, t.getName()+"'s Repair Master");
		for (int i = 0; i < 9*1; i++) {
			ItemStack is = LoreGuiItem.build("«--", ItemManager.getId(Material.BARRIER), 0);
			inv.setItem(i, is);
		}
		
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Barracks Repair Menu. In here,",
				CivColor.RESET+"you can put an item inside the inventory to",
				CivColor.RESET+"repair, in exchange for hammers.",
				CivColor.RESET+"",
				CivColor.RESET+""
				));
		
		inv.setItem(1, LoreGuiItem.build(CivColor.LightPurpleBold+"Repairable Items", ItemManager.getId(Material.WORKBENCH), 0, 
				CivColor.RESET+"Must meet these standards to repair items:",
				CivColor.RESET+"  » Custom Material",
				CivColor.RESET+"  » Is Repairable Material",
				CivColor.RESET+"  » Item Takes Durability Damage",
				CivColor.RESET+"  » Item Has Durability Damage",
				CivColor.RESET+""
				));
		
		ItemStack tmp = new ItemStack(Material.BEDROCK);
		inv.setItem(2, tmp);
		inv.removeItem(tmp);
		inv.setItem(8, LoreGuiItem.build(CivColor.LightGreenBold+"Close Inventory To Repair", ItemManager.getId(Material.ANVIL), 0));
		
		p.openInventory(inv);
	}
	
	// Unit Upgrade Villager
	
	public void spawnUnitUpgradeVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Barracks Unit Upgrader");
		v.setProfession(Profession.BLACKSMITH);
		CivGlobal.addStructureVillager(v);
	}
	
	public void openUnitUpgradeGUI_DISABLED_UNTIL_CODED(Player p, Town t) {
		Inventory inv = Bukkit.createInventory(null, 9*1, t.getName()+"'s Barracks Unit Upgrade Menu");
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Barracks Unit Upgrade Menu.",
				CivColor.RESET+"You can use it to upgrade units that you",
				CivColor.RESET+"have already created. Only certain upgrades",
				CivColor.RESET+"work with certain units, so be sure you check",
				CivColor.RESET+"them before you upgrade them!"
				));
		
		inv.addItem(LoreGuiItem.build(CivColor.LightPurpleBold+"Help", ItemManager.getId(Material.WORKBENCH), 0, 
				CivColor.RESET+"in the first empty slot, put the upgrade you",
				CivColor.RESET+"want to use to upgrade the unit with. Once",
				CivColor.RESET+"done, we will automatically grab the unit out",
				CivColor.RESET+"of your inventory and perform the upgrade.",
				CivColor.RESET+""
				));
		
		inv.setItem(7, LoreGuiItem.build(CivColor.LightGreenBold+"Click to Upgrade", ItemManager.getId(Material.ANVIL), 0));
		
		p.openInventory(inv);
	}
	
	public void upgradeUnit(Player p, ItemStack unit, ItemStack upgrade) {
		if (unit == null) {
			CivMessage.sendError(p,"No unit found. Please insert a unit in the slot closest to the workbench to upgrade it.");
			return;
		}
		
		ConfigUnit u = Unit.getUnit(unit);
		AttributeUtil attr = new AttributeUtil(unit);
		p.getInventory().removeItem(Unit.getPlayerUnitStack(p));
		if (u.id.equals("u_warrior")) {
			if (attr.hasEnhancement("LoreEnhancementUnitGainAttack")) {
				int lvl = 1;
				for (LoreEnhancement enh : attr.getEnhancements()) {
					if (enh instanceof LoreEnhancementUnitGainAttack) lvl += enh.getLevel(attr);
				}
				
				attr.setEnhancementData("LoreEnhancementUnitGainAttack", "level", String.valueOf(lvl));
				attr.addLore(CivColor.LightGreen+"Upgrade Level "+lvl);
				attr.addLore(CivColor.LightGrayItalic+"   +5% Attack Damage");
				p.getInventory().addItem(attr.getStack());
				CivMessage.sendSuccess(p, "Unit upgraded to level "+lvl+"!");
			} else {
				attr.addEnhancement("LoreEnhancementUnitGainAttack", "level", "1");
				attr.addLore(CivColor.LightGreen+"Upgrade Level 1");
				attr.addLore(CivColor.LightGrayItalic+"   +5% Attack Damage");
				p.getInventory().addItem(attr.getStack());
				CivMessage.sendSuccess(p, "Unit upgraded to level 1!");
			}
		} else {
			p.getInventory().addItem(attr.getStack());
			CivMessage.sendError(p, "No unit found, or this unit cannot be upgraded. Please try again.");
		}
	}
}
