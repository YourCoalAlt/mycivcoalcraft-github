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
import com.avrgaming.civcraft.config.ConfigGranaryLevel;
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
	
	protected Cottage(Location center, String id, Town town)
			throws CivException {
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
		
//		attrComp = new AttributeComponent();
//		attrComp.setType(AttributeType.DIRECT);
//		attrComp.setOwnerKey(this.getTown().getName());
//		attrComp.setAttrKey(Attribute.TypeKeys.COINS.name());
//		attrComp.setSource("Cottage("+this.getCorner().toString()+")");
//		attrComp.registerComponent();
	}

	
	@Override
	public String getDynmapDescription() {
		if (getConsumeComponent() == null) {
			return "";
		}
		
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

	/*
	 * Returns true if the granary has been poisoned, false otherwise.
	 */
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
			CivMessage.sendTown(this.getTown(), CivColor.Rose+"Our granaries have been poisoned!!");
			inv.addItem(ItemManager.createItemStack(CivData.ROTTEN_FLESH, 4));
			return true;
		}
		return false;
	}
	
	public void generateCoins(CivAsyncTask task) {
		Granary granary = (Granary) this.getTown().getStructureByType("s_granary");
		if (granary == null) return;
		if (!this.isActive()) return;
		
		// Build a multi-inv from granaries.
		MultiInventory multiInv = new MultiInventory();
		int types = CivSettings.granaryFood.keySet().size();
		ConfigGranaryLevel cgl = CivSettings.granaryLevels.get(this.getTown().saved_structures_default_level);
		
		Inventory inv = Bukkit.createInventory(null, 9*(((int) types*(cgl.max_storage/9))+(9*types)), granary.getTown().getName()+" temp granary");
		for (ConfigGranaryFood f : CivSettings.granaryFood.values()) {
			Material m = ItemManager.getMaterial(f.food);
			String key = granary.getStorageKey(granary, m.toString().toLowerCase());
			ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
			if (entry != null && !entry.isEmpty()) {
				SessionEntry se = entry.get(0);
				String fName = se.key.replace(granary.getConfigId()+"_"+granary.getCorner().toString()+"_", "");
				Material mat = ItemManager.getMaterial(fName.toUpperCase());
				ItemStack is = new ItemStack(mat, Integer.valueOf(se.value));
				inv.addItem(is);
			}
		}
		multiInv.addInventory(inv);
		
		
		getConsumeComponent().setSource(multiInv);
		double cottage_consume_mod = 1.0; //allows buildings and govs to change the totals for cottage consumption.

		if (this.getTown().getBuffManager().hasBuff(Buff.REDUCE_CONSUME)) {
			cottage_consume_mod *= this.getTown().getBuffManager().getEffectiveDouble(Buff.REDUCE_CONSUME);
		}
		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_consume")) {
			cottage_consume_mod *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_consume");
		}
		
		if (this.getTown().getBuffManager().hasBuff(Buff.FISHING)) {
			// XXX change this to config var after testing...
			int breadPerFish = this.getTown().getBuffManager().getEffectiveInt(Buff.FISHING);
			getConsumeComponent().addEquivExchange(CivData.BREAD, CivData.RAW_FISH, breadPerFish);
		}
		
		getConsumeComponent().setConsumeRate(cottage_consume_mod);
		Result result = getConsumeComponent().processConsumption();
		getConsumeComponent().onSave();
		getConsumeComponent().clearEquivExchanges();
		
		
		// Bail early for results that do not generate coins.
		switch (result) {
		case STARVE:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"A level "+getConsumeComponent().getLevel()+" Cottage "+CivColor.Rose+"starved"+
					getConsumeComponent().getCountString()+CivColor.LightGreen+" and generated no coins.");
			return;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"A level "+(getConsumeComponent().getLevel()+1)+" Cottage "+CivColor.Red+"leveled-down"+CivColor.LightGreen+" and generated no coins.");
			return;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+"A level "+getConsumeComponent().getLevel()+" Cottage "+CivColor.Yellow+"stagnated"+getConsumeComponent().getCountString()+CivColor.LightGreen+" and generated no coins.");
			return;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivColor.LightGreen+"Something "+CivColor.Purple+"unknown"+CivColor.LightGreen+" happened to a Cottage. It generates no coins.");
			return;
		default:
			break;
		}
		
		if (processPoison(multiInv)) {
			return;
		}
		
		// Calculate how much money we made.
		// XXX leveling down doesnt generate coins, so we don't have to check it here.
		ConfigCottageLevel lvl = null;
		if (result == Result.LEVELUP) {
			lvl = CivSettings.cottageLevels.get(getConsumeComponent().getLevel()-1);	
		} else {
			lvl = CivSettings.cottageLevels.get(getConsumeComponent().getLevel());
		}
		
		double total_coins = Math.round(lvl.coins*this.getTown().getCottageRate());
		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_bonus")) {
			total_coins *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_bonus");
		}
		
		if (this.getCiv().hasTechnology("tech_taxation")) {
			double taxation_bonus;
			try {
				taxation_bonus = CivSettings.getDouble(CivSettings.techsConfig, "taxation_cottage_buff");
				total_coins *= taxation_bonus;
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
			}
		}
		
		double taxesPaid = total_coins*this.getTown().getDepositCiv().getIncomeTaxRate();
		this.getTown().getTreasury().deposit(total_coins - taxesPaid);
		this.getTown().getDepositCiv().taxPayment(this.getTown(), taxesPaid);
		
		if (result == Result.LEVELUP || result == Result.GROW || result == Result.MAXED) {
			Map<Integer, Integer> required = new HashMap<Integer, Integer>();
			for (Integer i : lvl.consumes.keySet()) {
				required.put(i, lvl.consumes.get(i));
			}
			
			Map<Integer, Integer> stored = new HashMap<Integer, Integer>();
			for (ConfigGranaryFood f : CivSettings.granaryFood.values()) {
				Material m = ItemManager.getMaterial(f.food);
				String key = granary.getStorageKey(granary, m.toString().toLowerCase());
				ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
				if (entry != null && !entry.isEmpty()) {
					SessionEntry se = entry.get(0);
					String fName = se.key.replace(granary.getConfigId()+"_"+granary.getCorner().toString()+"_", "");
					Material mat = ItemManager.getMaterial(fName.toUpperCase());
					int fType = ItemManager.getId(mat);
					stored.put(fType, Integer.valueOf(se.value));
				}
			}
			
			for (Integer r : required.keySet()) {
				for (Integer s : stored.keySet()) {
					if (r.intValue() == s.intValue()) {
						Material mat = ItemManager.getMaterial(r);
						String key = granary.getStorageKey(granary, mat.toString().toLowerCase());
						ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
						if (entry != null && !entry.isEmpty()) {
							SessionEntry se = entry.get(0);
							String nv = ""+(Integer.valueOf(se.value) - required.get(r));
							CivGlobal.getSessionDB().update(se.request_id, se.key, nv);
						}
					}
				}
			}
		}
		
		String stateMessage = "";
		switch (result) {
		case GROW:
			stateMessage = CivColor.Green+"grew "+getConsumeComponent().getCountString();
			break;
		case LEVELUP:
			stateMessage = CivColor.Green+"leveled up ";
			break;
		case MAXED:
			stateMessage = CivColor.Green+"is maxed "+getConsumeComponent().getCountString();
			break;
		default:
			break;
		}
		
		if (taxesPaid > 0) {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" Cottage "+stateMessage+CivColor.LightGreen+". +"+total_coins+" Coins"+
					CivColor.Yellow+" (Paid "+taxesPaid+" in taxes to "+this.getTown().getDepositCiv().getName()+")");
		} else {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Level "+getConsumeComponent().getLevel()+" Cottage "+stateMessage+CivColor.LightGreen+". +"+total_coins+" Coins");
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

	public double getCoinsGenerated() {
		int level = getLevel();
		
		ConfigCottageLevel lvl = CivSettings.cottageLevels.get(level);
		if (lvl == null) {
			return 0;
		}
		return lvl.coins;
	}

	public void delevel() {
		int currentLevel = getLevel();
		
		if (currentLevel > 1) {
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
