package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.avrgaming.civcraft.components.ConsumeLevelComponent;
import com.avrgaming.civcraft.components.ConsumeLevelComponent.Result;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigLabLevel;
import com.avrgaming.civcraft.config.ConfigLabTask;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
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
		if (getConsumeComponent() == null) {
			return null;
		}
		
		String out = "";
		out += "Level "+getConsumeComponent().getLevel();
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "lightbulb";
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		switch (sb.command) {
		case "/sign":
			Integer id = Integer.valueOf(sb.keyvalues.get("id"));
			int rid = id+1;
			if (this.getLevel() >= rid) {
				ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
				ItemManager.setData(absCoord.getBlock(), sb.getData());
				Sign sign = (Sign)absCoord.getBlock().getState();
				sign.setLine(0, "");
				sign.setLine(1, "Lab "+rid);
				sign.setLine(2, CivColor.Green+"Useable");
				sign.setLine(3, "");
				sign.update();
			} else {
				ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
				ItemManager.setData(absCoord.getBlock(), sb.getData());
				Sign sign = (Sign)absCoord.getBlock().getState();
				sign.setLine(0, "");
				sign.setLine(1, "Lab "+rid);
				sign.setLine(2, CivColor.Red+"Locked");
				sign.setLine(3, "");
				sign.update();
			}
			this.addStructureBlock(absCoord, false);
			break;
		}
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		Resident res = CivGlobal.getResident(player);
		if (res == null) return;
		switch (sign.getAction()) {
		case "sign":
			if (res.isSBPermOverride()) {
				sign.getCoord().getBlock().setType(Material.AIR);
			}
			break;
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
					CivColor.Rose+"fell "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+".");
			break;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+(getConsumeComponent().getLevel()+1)+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"de-leveled "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". It is now level "+
					CivColor.Green+getConsumeComponent().getLevel()+CivColor.LightGreen+".");
			break;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Rose+"stagnated "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". ");
			break;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.Green+"rose "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". ");
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+(getConsumeComponent().getLevel()-1)+" "+getDisplayName()+" consumption "+
					CivColor.Green+"leveled up "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". It is now level "+
					getConsumeComponent().getLevel()+". ");
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
					CivColor.LightPurple+"is maxed "+CivColor.Green+getConsumeComponent().getCountString()+CivColor.LightGreen+". ");
			break;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" "+getDisplayName()+" consumption "+
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
		ConfigLabLevel lvl = CivSettings.labLevels.get(getLevel());
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
		
		ConfigLabLevel lvl = CivSettings.labLevels.get(getLevel());
		int total_production = (int) (lvl.beakers*this.getTown().getLabRate().total);
//		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_bonus")) {
//			total_production *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_bonus");
//		}
		
		double buff = 1.0 + this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
		total_production *= buff;
		if (this.getCiv().hasTechnology("tech_resource_efficiency")) {
			double tech_bonus;
			try {
				tech_bonus = CivSettings.getDouble(CivSettings.techsConfig, "tech_lab_buff");
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
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+"'s Lab Tasks");
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Lab Quest Chest. You can use it",
				CivColor.RESET+"to complete tasks to recieve beakers (items)",
				CivColor.RESET+"for upgrading. You can get more tasks by having",
				CivColor.RESET+"a higher town level, or through upgrades.",
				CivColor.RESET+"Upgrades can also increase beaker output!"
				));
		
		for (ConfigLabTask m : CivSettings.labTasks.values()) {
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
				lore.add(CivColor.Green+"Will Reward: "+CivColor.Rose+m.reward+" Beakers");
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
					lore.add(CivColor.Green+"Rewards: "+CivColor.Yellow+m.reward+" Beakers");
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
					lore.add(CivColor.Green+"Rewarded: "+CivColor.Rose+m.reward+" Beakers");
					meta.setLore(lore);
					item.setItemMeta(meta);
					inv.addItem(item);
				}
			}
		}
		p.openInventory(inv);
	}
	
	public void openTaskCompleterGUI(Player p, Town town, int task) {
		ConfigLabTask mtask = CivSettings.labTasks.get(task);
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
		loreReq += CivColor.Green+"Rewards: "+CivColor.Yellow+mtask.reward+" Beakers";
		
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+" Lab Task "+task);
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Requirements", ItemManager.getId(Material.PAPER), 0, 
				loreReq.split(";")
				));
		p.openInventory(inv);
	}
	
	public void resetTasks() {
		for (ConfigLabTask t : CivSettings.labTasks.values()) {
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
		for (ConfigLabTask t : CivSettings.labTasks.values()) {
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
