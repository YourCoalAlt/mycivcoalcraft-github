package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.components.ConsumeLevelComponent;
import com.avrgaming.civcraft.components.ConsumeLevelComponent.Result;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCottageLevel;
import com.avrgaming.civcraft.config.ConfigGranaryFood;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class Cottage extends Structure {
	
	private ConsumeLevelComponent consumeComp = null;
	
	protected Cottage(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public Cottage(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public ConsumeLevelComponent getConsumeComponent() {
		if (consumeComp == null) {
			consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
		}
		return consumeComp;
	}
	
	@Override
	public void loadSettings() {
		super.loadSettings();
	}
	
	@Override
	public String getDynmapDescription() {
		if (getConsumeComponent() == null) return "";
		
		String out = "";
		out += "Level: "+getConsumeComponent().getLevel()+" "+getConsumeComponent().getCountString();
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "house";
	}
	
	public String getkey() {
		return this.getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString(); 
	}
	
	// Returns true if the granary has been poisoned, false otherwise.
	public boolean processPoison(MultiInventory inv) {
		//Check to make sure the granary has not been poisoned!
		String key = "posiongranary:"+getTown().getName();
		ArrayList<SessionEntry> entries;
		entries = CivGlobal.getSessionDB().lookup(key);
		int max_poison_ticks = -1;
		for (SessionEntry entry : entries) {
			int next = Integer.valueOf(entry.value);
			if (next > max_poison_ticks) {
				max_poison_ticks = next;
			} 
		}
		
		if (max_poison_ticks > 0) {
			CivGlobal.getSessionDB().delete_all(key);
			max_poison_ticks--;
			
			if (max_poison_ticks > 0)
				CivGlobal.getSessionDB().add(key, ""+max_poison_ticks, this.getTown().getCiv().getId(), this.getTown().getId(), this.getId());
	
			// Add some rotten flesh to the chest lol
			CivMessage.sendTown(this.getTown(), CivColor.GoldItalic+"Our granaries are currently poisoned!!");
			// TODO Make chest at capitol/town hall for this since we no longer have Granary chests, the items will just be voided
//			inv.addItem(ItemManager.createItemStack(CivData.ROTTEN_FLESH, 4));
			return true;
		}
		return false;
	}
	
	public void generateCoins(CivAsyncTask task) {
		Granary granary = (Granary) this.getTown().getStructureByType("s_granary");
		if (granary == null) return;
		if (!this.isActive()) return;
		
		double consume_mod = 1.0;
		if (this.getTown().getBuffManager().hasBuff(Buff.REDUCE_CONSUME)) {
			consume_mod *= this.getTown().getBuffManager().getEffectiveDouble(Buff.REDUCE_CONSUME);
		}
		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_consume")) {
			consume_mod *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_consume");
		}
		
		if (this.getTown().getBuffManager().hasBuff(Buff.FISHING)) {
			// XXX change this to config var after testing...
			int breadPerFish = this.getTown().getBuffManager().getEffectiveInt(Buff.FISHING);
			getConsumeComponent().addEquivExchange(CivData.BREAD, CivData.RAW_FISH, breadPerFish);
		}
		
		// Calculate how much money we made.
		ConfigCottageLevel lvl = CivSettings.cottageLevels.get(getConsumeComponent().getLevel());
		
		if ((this.getCount()+1) >= lvl.count) { // In order to level up, must set lvl to new level
			if (lvl.level != CivSettings.getCottageMaxLevel()) {
				lvl = CivSettings.cottageLevels.get(getConsumeComponent().getLevel()+1);
			}
		}
		
		// Get all required items first
		HashMap<Integer, Integer> required = new HashMap<Integer, Integer>();
		for (Integer id : lvl.consumes.keySet()) {
			int req_amt = (int) (lvl.consumes.get(id) * consume_mod);
			required.put(id, req_amt);
		}
		
		// Get all stored items from Granary next
		Map<Integer, Integer> stored = new HashMap<Integer, Integer>();
		for (ConfigGranaryFood f : CivSettings.granaryFood.values()) {
			if (required.containsKey(f.food)) { // Only add required items
				Material m = ItemManager.getMaterial(f.food);
				String key = granary.getStorageKey(granary, m.toString().toLowerCase());
				ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
				if (entry != null && !entry.isEmpty()) {
					SessionEntry se = entry.get(0);
					stored.put(f.food, Integer.valueOf(se.value));
				} else {
					if (entry == null) {
						granary.sessionAdd(granary.getStorageKey(this, m.toString().toLowerCase()), "0");
					} else {
						stored.put(f.food, 0);
					}
				}
			}
		}
		
		boolean canGrow = true;
		Inventory temp_inv = Bukkit.createInventory(null, ((required.values().size() + 64) / 64) * 9, granary.getTown().getName()+" temp granary");
		// Check if we have enough items to consume
		for (Integer r : required.keySet()) {
			for (Integer s : stored.keySet()) {
				if (r.intValue() == s.intValue()) {
					if (stored.get(s) < required.get(r)) {
						canGrow = false;
						break;
					}
				}
			}
		}
		
		// Create inventory for cottages to consume from
		MultiInventory multiInv = new MultiInventory();
		multiInv.addInventory(temp_inv);
		getConsumeComponent().setSource(multiInv);
		
		// If we got all required items, put into multiInv and remove from database
		// NOTE It will only change database values if there is a success, otherwise the multiInv will de-count the cottage.
		if (canGrow) {
			for (Integer r : required.keySet()) {
				Material mat = ItemManager.getMaterial(r);
				ItemStack is = new ItemStack(mat, required.get(r));
				temp_inv.addItem(is);
				for (Integer s : stored.keySet()) {
					if (r.intValue() == s.intValue()) {
						String key = granary.getStorageKey(granary, mat.toString().toLowerCase());
						ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
						if (entry != null && !entry.isEmpty()) {
							SessionEntry se = entry.get(0);
							String nv = ""+(Integer.valueOf(se.value) - required.get(r));
							CivGlobal.getSessionDB().update(se.request_id, se.key, nv);
						} // No need for an else here, check above. If it didn't work, well sucks to suck!
					}
				}
			}
		}
		
		getConsumeComponent().setConsumeRate(consume_mod);
		Result result = getConsumeComponent().processConsumption();
		getConsumeComponent().onSave();
		getConsumeComponent().clearEquivExchanges();
		
		if (processPoison(multiInv)) return;
		
		// XXX leveling down doesnt generate coins, so we don't have to check it here.
		if (result == Result.LEVELUP) {
			lvl = CivSettings.cottageLevels.get(getConsumeComponent().getLevel()-1);	
		}
		
		double total_coins = getTotalCoinsGenerated(lvl.level, lvl.coins);
		double taxesPaid = total_coins*this.getTown().getDepositCiv().getIncomeTaxRate();
		this.getTown().getTreasury().deposit(total_coins - taxesPaid);
		this.getTown().getDepositCiv().taxPayment(this.getTown(), taxesPaid);
		
		boolean bail = false;
		boolean pay = false;
		String resultMsg = "";
		switch (result) {
		// Do not generate money from no consumptions
		case STARVE:
			bail = true;
			resultMsg = CivColor.Rose+"starved "+getConsumeComponent().getCountString();
			break;
		case LEVELDOWN:
			bail = true;
			resultMsg = CivColor.Red+"leveled-down "+getConsumeComponent().getCountString();
			break;
		case STAGNATE:
			bail = true;
			resultMsg = CivColor.Gold+"stagnated "+getConsumeComponent().getCountString();
			break;
		case UNKNOWN:
			bail = true;
			resultMsg = CivColor.LightPurple+"unknown "+getConsumeComponent().getCountString();
			break;
		// Only money for these results
		case GROW:
			pay = true;
			resultMsg = CivColor.Green+"grew "+getConsumeComponent().getCountString();
			break;
		case LEVELUP:
			pay = true;
			resultMsg = CivColor.Green+"leveled up"+getConsumeComponent().getCountString();
			break;
		case MAXED:
			pay = true;
			resultMsg = CivColor.Green+"is maxed "+getConsumeComponent().getCountString();
			break;
		default:
			break;
		}
		
		if (bail) {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" Cottage "+resultMsg+CivColor.Yellow+" +0 Coins");
		} else if (pay) {
			if (taxesPaid > 0) {
				CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" Cottage "+resultMsg+CivColor.LightGreen+" +"+total_coins+" Coins"+
						CivColor.Yellow+" (Paid "+taxesPaid+" in taxes to "+this.getTown().getDepositCiv().getName()+")");
			} else {
				CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" Cottage "+resultMsg+CivColor.LightGreen+" +"+total_coins+" Coins");
			}
		} else {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" Cottage "+CivColor.Purple+"No fail/success?"+CivColor.Rose+" +0 Coins");
			CivMessage.sendTown(this.getTown(), CivColor.Rose+"Contact an Admin!");
		}
	}
	
	public int getLevel() {
		return getConsumeComponent().getLevel();
	}
	
	public Result getLastResult() {
		return getConsumeComponent().getLastResult();
	}
	
	public int getCount() {
		return getConsumeComponent().getCount();
	}
	
	public int getMaxCount() {
		int level = getLevel();
		ConfigCottageLevel lvl = CivSettings.cottageLevels.get(level);
		return lvl.count;
	}
	
	public double getTotalCoinsGenerated(int level, double lvl_coins) {
		double total_coins = Math.round(lvl_coins*this.getTown().getCottageRate());
		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_bonus")) {
			total_coins *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_bonus");
		}
		
		if (this.getCiv().hasTechnology("tech_taxation")) {
			try {
				double taxation_bonus = CivSettings.getDouble(CivSettings.techsConfig, "taxation_cottage_buff");
				total_coins *= taxation_bonus;
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
			}
		}
		return total_coins;
	}
	
	public void delevelSpy() {
		if (getLevel() > 1) {
			getConsumeComponent().setLevel(getLevel()-1);
			getConsumeComponent().setCount(0);
			getConsumeComponent().onSave();
		}
	}
	
	@Override
	public void delete() throws SQLException {
		super.delete();
		if (getConsumeComponent() != null) {
			getConsumeComponent().onDelete();
		}
	}
	
	public void onDestroy() {
		super.onDestroy(null);
		getConsumeComponent().setLevel(1);
		getConsumeComponent().setCount(0);
		getConsumeComponent().onSave();
	}
}
