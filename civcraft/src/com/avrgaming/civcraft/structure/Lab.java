package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.avrgaming.civcraft.components.ConsumeLevelComponent;
import com.avrgaming.civcraft.components.ConsumeLevelComponent.Result;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigLabLevel;
import com.avrgaming.civcraft.config.ConfigMineLevel;
import com.avrgaming.civcraft.config.ConfigMineTask;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Lab extends Structure {
	
	private ConsumeLevelComponent consumeComp = null;
	
	protected Lab(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public Lab(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	public void loadSettings() {
		super.loadSettings();
	}
	
	public String getkey() {
		return getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString(); 
	}
	
	@Override
	public String getDynmapDescription() {
		return null;
	}
	
	@Override
	public String getMarkerIconName() {
		return "lightbulb";
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		switch (sb.command) {
		case "/task":
			spawnTaskVillager(absCoord.getLocation(), (byte)sb.getData());
		}
	}
	
	public ConsumeLevelComponent getConsumeComponent() {
		if (consumeComp == null) {
			consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
		}
		return consumeComp;
	}
	
	public Result consume(CivAsyncTask task) throws InterruptedException {
		if (this.getChests().size() == 0) return Result.STAGNATE;	

		MultiInventory multiInv = new MultiInventory();
		ArrayList<StructureChest> chests = this.getAllChestsById(0);
		
		// Make sure the chest is loaded and add it to the multi inv.
		for (StructureChest c : chests) {
			task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
			Inventory tmp;
			try {
				tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), true);
			} catch (CivTaskAbortException e) {
				return Result.STAGNATE;
			}
			multiInv.addInventory(tmp);
		}
		getConsumeComponent().setSource(multiInv);
		getConsumeComponent().setConsumeRate(1.0);
		Result result = getConsumeComponent().processConsumption();
		getConsumeComponent().onSave();		
		return result;
	}
	
	public void process_consume(CivAsyncTask task) throws InterruptedException {	
		Result result = this.consume(task);
		switch (result) {
		case STARVE:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"fell"+CivColor.Green+"."+getConsumeComponent().getCountString());
			break;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+(getConsumeComponent().getLevel()+1)+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"de-leveled"+CivColor.LightGreen+". It is now level "+CivColor.Green+getConsumeComponent().getLevel()+" "+CivColor.Green+getConsumeComponent().getCountString());
			break;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"stagnated "+CivColor.LightGreen+". "+CivColor.Green+getConsumeComponent().getCountString());
			break;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Green+"rose"+CivColor.LightGreen+". "+CivColor.Green+getConsumeComponent().getCountString());
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+(getConsumeComponent().getLevel()-1)+" "+getDisplayName()+" consumption "+
					CivColor.Green+"leveled up"+CivColor.LightGreen+". It is now level "+getConsumeComponent().getLevel()+". "+CivColor.Green+getConsumeComponent().getCountString());
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.LightPurple+"is maxed"+CivColor.LightGreen+". "+CivColor.Green+getConsumeComponent().getCountString());
			break;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.GrayBold+"UNKNOWN"+CivColor.LightGreen+". "+CivColor.Green+getConsumeComponent().getCountString());
			break;
		default:
			break;
		}
	}
	
	public int getLevel() {
		return this.getConsumeComponent().getLevel();
	}
	
	public int getCount() {
		return this.getConsumeComponent().getCount();
	}
	
	public int getMaxCount() {
		int level = getLevel();
		ConfigMineLevel lvl = CivSettings.mineLevels.get(level);
		return lvl.count;	
	}
	
	public Result getLastResult() {
		return this.getConsumeComponent().getLastResult();
	}
	
	public double getBonusBeakers() {
		if (!this.isComplete()) {
			return 0.0;
		}
		
		if (getConsumeComponent().getLevel() <= 0) {
			return 0.0;
		}
		
		int level = getLevel(); 
		ConfigLabLevel lvl = CivSettings.labLevels.get(level);
		int total_production = (int) (lvl.beakers*this.getTown().getLabRate().total);
		//TODO make a new buff that works for mines/labs
//		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_bonus")) {
//			total_production *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_bonus");
//		}
		
//		total_production *= this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
//		if (this.getCiv().hasTechnology("tech_taxation")) {
//			double tech_bonus;
//			try {
//				tech_bonus = CivSettings.getDouble(CivSettings.techsConfig, "taxation_mine_buff");
//				total_production *= tech_bonus;
//			} catch (InvalidConfiguration e) {
//				e.printStackTrace();
//			}
//		}
		return total_production;
	}
	
	
	public String getKey(Structure struct, String tag) {
		return struct.getConfigId()+"_"+struct.getCorner().toString()+"_"+tag; 
	}
	
	public void spawnTaskVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Lab Tasks");
		v.setProfession(Profession.LIBRARIAN);
		CivGlobal.addStructureVillager(v);
	}
	
	public void openToolGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+"'s Lab Tasks");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Lab Quest Villager. You can use it",
				CivColor.RESET+"to complete tasks to recieve beakers (items)",
				CivColor.RESET+"for upgrading. You can get more tasks by having",
				CivColor.RESET+"a higher town level, or through upgrades.",
				CivColor.RESET+"Upgrades can also increase beaker output!"
				));
		
		for (ConfigMineTask m : CivSettings.mineTasks.values()) {
			Map<Integer, Integer> consumptions = m.required;
			List<String> loreRequired = new ArrayList<>();
			
			if (this.getTown().getLevel() < m.task) {
				for (Integer typeID : consumptions.keySet()) {
					int imat = typeID;
					Material mat = ItemManager.getMaterial(imat);
					int amt = consumptions.get(typeID);
					loreRequired.add(CivColor.LightGrayBold+" » "+CivColor.Rose+amt+" "+mat.toString().substring(0, 1).toUpperCase()+mat.toString().substring(1).toLowerCase().replace("_", " "));
				}
				
				ItemStack item = new ItemStack(Material.BLACK_SHULKER_BOX, 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(CivColor.WhiteBold+CivColor.ITALIC+"[Locked] Task "+m.task);
				List<String> lore = new ArrayList<>();
				lore.add(CivColor.LightGreen+"Will Require: ");
				lore.addAll(loreRequired);
				lore.add(CivColor.LightGreen+"Will Reward: "+CivColor.Rose+m.reward+" Hammers");
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			} else {
				for (Integer typeID : consumptions.keySet()) {
					int imat = typeID;
					Material mat = ItemManager.getMaterial(imat);
					int amt = consumptions.get(typeID);
					loreRequired.add(CivColor.LightGrayBold+" » "+CivColor.Yellow+amt+" "+mat.toString().substring(0, 1).toUpperCase()+mat.toString().substring(1).toLowerCase().replace("_", " "));
				}
				
				String key = getKey(this, "task"+m.task);
				ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
				if (entry == null || entry.isEmpty()) {
					ItemStack item = new ItemStack(Material.LIME_SHULKER_BOX, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CivColor.WhiteBold+"[Available] Task "+m.task);
					List<String> lore = new ArrayList<>();
					lore.add(CivColor.LightGreen+"Requires: ");
					lore.addAll(loreRequired);
					lore.add(CivColor.LightGreen+"Rewards: "+CivColor.Yellow+m.reward+" Hammers");
					meta.setLore(lore);
					item.setItemMeta(meta);
					inv.addItem(item);
				} else {
					ItemStack item = new ItemStack(Material.RED_SHULKER_BOX, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CivColor.WhiteBold+CivColor.ITALIC+"[Completed] Task "+m.task);
					List<String> lore = new ArrayList<>();
					lore.add(CivColor.LightGreen+"Consumed: ");
					lore.addAll(loreRequired);
					lore.add(CivColor.LightGreen+"Rewarded: "+CivColor.Rose+m.reward+" Hammers");
					meta.setLore(lore);
					item.setItemMeta(meta);
					inv.addItem(item);
				}
			}
		}
		p.openInventory(inv);
	}
	
	public void openTaskCompleterGUI(Player p, Town town, int task) {
		ConfigMineTask mtasks = CivSettings.mineTasks.get(task);
		
		List<String> lr = new ArrayList<>();
		for (Integer typeID : mtasks.required.keySet()) {
			int imat = typeID;
			Material mat = ItemManager.getMaterial(imat);
			int amt = mtasks.required.get(typeID);
			lr.add(CivColor.LightGrayBold+" » "+CivColor.Yellow+amt+" "+mat.toString().substring(0, 1).toUpperCase()+mat.toString().substring(1).toLowerCase().replace("_", " "));
		}
		
		String loreReq = CivColor.LightGreen+"Required:;";
		for (String s : lr) {
			loreReq += s+" ;";
		}
		loreReq += CivColor.LightGreen+"Rewards:;";
		loreReq += CivColor.Yellow+mtasks.reward+" Hammers";
		
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+" Mine Task "+task);
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Requirements", ItemManager.getId(Material.PAPER), 0, 
				loreReq.split(";")
				));
		p.openInventory(inv);
	}
	
	public void resetTasks() {
		for (ConfigMineTask t : CivSettings.mineTasks.values()) {
			String key = getKey(this, "task"+t.task);
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
		for (ConfigMineTask t : CivSettings.mineTasks.values()) {
			String key = getKey(this, "task"+t.task);
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
