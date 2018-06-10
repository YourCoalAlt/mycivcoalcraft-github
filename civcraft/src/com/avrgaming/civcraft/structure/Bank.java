package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;

import com.avrgaming.civcraft.components.NonMemberFeeComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBankLevel;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Bank extends Structure {
	
	private int level = 1;
	private double interestRate = 0;
	
	private NonMemberFeeComponent nonMemberFeeComponent;
	
	public double IRON_INGOT_RATE = CivSettings.iron_rate;
	public double GOLD_INGOT_RATE = CivSettings.gold_rate;
	public double DIAMOND_RATE = CivSettings.diamond_rate;
	public double EMERALD_RATE = CivSettings.emerald_rate;
	public Map<String, Double> rates = new HashMap<String, Double>();
	
	protected Bank(Location center, String id, Town town) throws CivException {
		super(center, id, town);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onSave();
	}
	
	public Bank(ResultSet rs) throws SQLException, CivException {
		super(rs);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onLoad();
	}
	
	public double getExchangeRate() {
		double rate = 0.4;
		ConfigBankLevel cbl = CivSettings.bankLevels.get(level);
		if (cbl != null) {
			rate = cbl.exchange_rate;
		} else {
			CivLog.warning("null exchange rate .:. cannot find level: "+level+" for town "+this.getTown().getName());
			CivMessage.sendTown(this.getTown(), "null exchange rate .:. cannot find level: "+level+" for your bank. Contact an admin.");
		}
		
		rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.BARTER);
		return rate;
	}
	
	public String getExchangeRateString() {
		return ((int)(getExchangeRate()*100) + "%").toString();		
	}
	
	public void updateExchangeRate() {
		double update_exchange = this.getExchangeRate();
		rates.clear();
		rates.put("IRON_INGOT_RATE", IRON_INGOT_RATE*update_exchange);
		rates.put("GOLD_INGOT_RATE", GOLD_INGOT_RATE*update_exchange);
		rates.put("DIAMOND_RATE", DIAMOND_RATE*update_exchange);
		rates.put("EMERALD_RATE", EMERALD_RATE*update_exchange);
	}
	
	private String getNonResidentFeeString() {
		return ((int)(this.nonMemberFeeComponent.getFeeRate()*100)+"%").toString();		
	}
	
	@Override
	public String getDynmapDescription() {
		String out = "<u><b>Bank</u></b><br/>";
		out += "Level: "+this.level;
		out += "Non-Resident Fee: "+this.getNonResidentFeeString();
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "bank";
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.getExchangeRate();
		this.level = level;
	}
	
	public double getNonResidentFee() {
		return this.nonMemberFeeComponent.getFeeRate();
	}
	
	public void setNonResidentFee(double nonResidentFee) {
		this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
	}
	
	public double getInterestRate() {
		return interestRate;
	}
	
	public void setInterestRate(double interestRate) {
		this.interestRate = interestRate;
	}
	
	@Override
	public void onLoad() {
		if (interestRate == 0.0) {
			this.getTown().getTreasury().setPrincipalAmount(0);
		} else {
			this.getTown().getTreasury().setPrincipalAmount(this.getTown().getTreasury().getBalance());
		}
	}
	
	@Override
	public void onDailyEvent() {
		double effectiveInterestRate = interestRate;
		if (effectiveInterestRate == 0.0) {
			this.getTown().getTreasury().setPrincipalAmount(0);
			return;
		}
		
		double principal = this.getTown().getTreasury().getPrincipalAmount();
		
		if (this.getTown().getBuffManager().hasBuff("buff_greed")) {
			double increase = this.getTown().getBuffManager().getEffectiveDouble("buff_greed");
			effectiveInterestRate += increase;
			CivMessage.sendTown(this.getTown(), CivColor.LightGray+"Your goodie buff 'Greed' has increased the interest our town generated."
					+CivColor.ITALIC+"(With this buff, our interest rate went from "+interestRate+"% to "+effectiveInterestRate+"%.)");
		}
		
		double newCoins = principal*effectiveInterestRate;
		if (newCoins != 0) {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Our town earned "+newCoins+" coins from an interest rate of "
					+effectiveInterestRate+"% and on a principal of "+principal+" coins.");
			this.getTown().getTreasury().deposit(newCoins);
		}
		//Update the principal with the new value.
		this.getTown().getTreasury().setPrincipalAmount(this.getTown().getTreasury().getBalance());
	}
	
	public NonMemberFeeComponent getNonMemberFeeComponent() {
		return nonMemberFeeComponent;
	}
	
	public void setNonMemberFeeComponent(NonMemberFeeComponent nonMemberFeeComponent) {
		this.nonMemberFeeComponent = nonMemberFeeComponent;
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		this.level = getTown().saved_bank_level;
		this.interestRate = getTown().saved_bank_interest_amount;
		
		switch (sb.command) {
		case "/banker":
			spawnBankerVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		}
	}
	
	// XXX Villager stuff
	
	public void spawnBankerVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName(this.getTown().getName()+"'s Bank Teller");
		v.setProfession(Profession.LIBRARIAN);
		
		String vilKey = this.getTown().getName()+":"+v.getCustomName()+":"+v.getLocation().toString();
		if (CivGlobal.getStructureVillager(vilKey) != null) {
			v.setHealth(0); v.remove();
		} else {
			CivGlobal.addStructureVillager(vilKey, v);
		}
	}
	
	public void openToolGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*5, town.getName()+"'s Bank Desk");
		for (int i = 0; i <= 8; i++) inv.setItem(i, LoreGuiItem.build(CivColor.Gray+"Inventory Border", CivData.STAINED_GLASS_PANE, 7));
		for (int i = 36; i <= 44; i++) inv.setItem(i, LoreGuiItem.build(CivColor.Gray+"Inventory Border", CivData.STAINED_GLASS_PANE, 7));
		
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Bank Menu. You can use it to sell",
				CivColor.RESET+"different items for a set amount of price as",
				CivColor.RESET+"listed in the GUI. If upgraded, you can earn",
				CivColor.RESET+"more money per item sold!",
				CivColor.RESET+""
				));
		
		inv.setItem(2, LoreGuiItem.build(CivColor.LightBlueBold+"Sell Values", ItemManager.getId(Material.PAPER), 0, 
				CivColor.LightGreen+"Bank Rate: "+CivColor.Yellow+getExchangeRateString(),
				CivColor.LightGreen+"Non-Resident Fee: "+CivColor.Yellow+getNonResidentFeeString(),
				CivColor.Yellow+(this.IRON_INGOT_RATE*getExchangeRate())+CivColor.LightGreen+" Coins per Iron Ingot",
				CivColor.Yellow+(this.GOLD_INGOT_RATE*getExchangeRate())+CivColor.LightGreen+" Coins per Gold Ingot",
				CivColor.Yellow+(this.DIAMOND_RATE*getExchangeRate())+CivColor.LightGreen+" Coins per Diamond",
				CivColor.Yellow+(this.EMERALD_RATE*getExchangeRate())+CivColor.LightGreen+" Coins per Emerald"
				));
		
		p.openInventory(inv);
	}
}
