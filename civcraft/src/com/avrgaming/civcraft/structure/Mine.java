package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.avrgaming.civcraft.components.ConsumeLevelComponent;
import com.avrgaming.civcraft.components.ConsumeLevelComponent.Result;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMineLevel;
import com.avrgaming.civcraft.config.ConfigMineTask;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class Mine extends Structure {
	
	private ConsumeLevelComponent consumeComp = null;
	
	protected Mine(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public Mine(ResultSet rs) throws SQLException, CivException {
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
		if (getConsumeComponent() == null) {
			return null;
		}
		
		String out = "";
		out += "Level "+getConsumeComponent().getLevel();
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "hammer";
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
		
		for (StructureChest c : chests) {
			task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
			try {
				Inventory tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), true);
				multiInv.addInventory(tmp);
			} catch (CivTaskAbortException e) {
				CivLog.warning("Mine Inv: "+e.getMessage());
			}
		}
		
		if (multiInv.getInventories().size() < 1) {
			return Result.STAGNATE;
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
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"fell "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+".");
			break;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+(getLevel()+1)+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"de-leveled "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". It is now level "+
					CivColor.Green+getConsumeComponent().getLevel()+CivColor.LightGreen+".");
			break;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"stagnated "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". ");
			break;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Green+"rose "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". ");
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+(getLevel()-1)+" "+getDisplayName()+" consumption "+
					CivColor.Green+"leveled up "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". It is now level "+
					getConsumeComponent().getLevel()+". ");
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.LightPurple+"is maxed "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". ");
			break;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.GrayBold+"UNKNOWN "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". ");
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
		ConfigMineLevel lvl = CivSettings.mineLevels.get(getLevel());
		return lvl.count;	
	}
	
	public Result getLastResult() {
		return this.getConsumeComponent().getLastResult();
	}
	
	public double getBonusHammers() {
		if (!this.isComplete()) return 0.0;
		if (getConsumeComponent().getLevel() < 1) return 0.0;
		
		ConfigMineLevel lvl = CivSettings.mineLevels.get(getLevel());
		int total_production = (int) (lvl.hammers*this.getTown().getMineRate().total);
//		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_bonus")) {
//			total_production *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_bonus");
//		}
		
		double buff = 0.0;
		buff += this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
		total_production *= buff;
		
		if (this.getCiv().hasTechnology("tech_resource_efficiency")) {
			double tech_bonus;
			try {
				tech_bonus = CivSettings.getDouble(CivSettings.techsConfig, "tech_mine_buff");
				total_production *= tech_bonus;
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
			}
		}
		return total_production;
	}
	
	// XXX Quests
	public String getKey(Structure struct, String tag) {
		return struct.getConfigId()+"_"+struct.getCorner().toString()+"_"+tag; 
	}
	
	public void openToolGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+"'s Mine Tasks");
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Mine Quest Chest. You can use it",
				CivColor.RESET+"to complete tasks to recieve hammers (items)",
				CivColor.RESET+"for upgrading. You can get more tasks by having",
				CivColor.RESET+"a higher town level, or through upgrades.",
				CivColor.RESET+"Upgrades can also increase hammer output!"
				));
		
		for (ConfigMineTask m : CivSettings.mineTasks.values()) {
			List<String> loreRequired = new ArrayList<>();
			if (this.getTown().getLevel() < m.task) {
				for (ArrayList<String> item : m.required.keySet()) {
					for (String s : item) {
						String[] split = s.split(";");
						int imat = Integer.valueOf(split[0]);
						int data = Integer.valueOf(split[1]);
						loreRequired.add(CivColor.LightGrayBold+" » "+CivColor.Rose+m.required.get(item).intValue()+" "+CivData.getDisplayName(imat, data));
					}
				}
				ItemStack item = new ItemStack(Material.BLACK_SHULKER_BOX, 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(CivColor.WhiteBold+CivColor.ITALIC+"[Locked] Task "+m.task);
				List<String> lore = new ArrayList<>();
				lore.add(CivColor.Green+"Will Require: ");
				lore.addAll(loreRequired);
				lore.add(CivColor.Green+"Will Reward: "+CivColor.Rose+m.reward+" Hammers");
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			} else {
				String key = getKey(this, "task"+m.task);
				ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
				if (entry == null || entry.isEmpty()) {
					for (ArrayList<String> item : m.required.keySet()) {
						for (String s : item) {
							String[] split = s.split(";");
							int imat = Integer.valueOf(split[0]);
							int data = Integer.valueOf(split[1]);
							loreRequired.add(CivColor.LightGrayBold+" » "+CivColor.LightGreen+m.required.get(item).intValue()+" "+CivData.getDisplayName(imat, data));
						}
					}
					ItemStack item = new ItemStack(Material.LIME_SHULKER_BOX, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CivColor.WhiteBold+"[Available] Task "+m.task);
					List<String> lore = new ArrayList<>();
					lore.add(CivColor.Green+"Requires: ");
					lore.addAll(loreRequired);
					lore.add(CivColor.Green+"Rewards: "+CivColor.Yellow+m.reward+" Hammers");
					meta.setLore(lore);
					item.setItemMeta(meta);
					inv.addItem(item);
				} else {
					for (ArrayList<String> item : m.required.keySet()) {
						for (String s : item) {
							String[] split = s.split(";");
							int imat = Integer.valueOf(split[0]);
							int data = Integer.valueOf(split[1]);
							loreRequired.add(CivColor.LightGrayBold+" » "+CivColor.Yellow+m.required.get(item).intValue()+" "+CivData.getDisplayName(imat, data));
						}
					}
					ItemStack item = new ItemStack(Material.RED_SHULKER_BOX, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CivColor.WhiteBold+CivColor.ITALIC+"[Completed] Task "+m.task);
					List<String> lore = new ArrayList<>();
					lore.add(CivColor.Green+"Consumed: ");
					lore.addAll(loreRequired);
					lore.add(CivColor.Green+"Rewarded: "+CivColor.Rose+m.reward+" Hammers");
					meta.setLore(lore);
					item.setItemMeta(meta);
					inv.addItem(item);
				}
			}
		}
		p.openInventory(inv);
	}
	
	public void openTaskCompleterGUI(Player p, Town town, int task) {
		ConfigMineTask mtask = CivSettings.mineTasks.get(task);
		List<String> lr = new ArrayList<>();
		for (ArrayList<String> item : mtask.required.keySet()) {
			for (String s : item) {
				String[] split = s.split(";");
				int imat = Integer.valueOf(split[0]);
				int data = Integer.valueOf(split[1]);
				lr.add(CivColor.LightGrayBold+" » "+CivColor.LightGreen+mtask.required.get(item).intValue()+" "+CivData.getDisplayName(imat, data));
			}
		}
		
		String loreReq = CivColor.Green+"Required: ;";
		for (String s : lr) loreReq += s+" ;";
		loreReq += CivColor.Green+"Rewards: "+CivColor.Yellow+mtask.reward+" Hammers";
		
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
