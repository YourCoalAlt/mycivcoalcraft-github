package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.avrgaming.civcraft.components.NonMemberFeeComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBankLevel;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Town;
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
	
	public double getBankExchangeRate() {
		double rate = 0.0;
		ConfigBankLevel cbl = CivSettings.bankLevels.get(level);
		if (cbl != null) {
			rate = cbl.exchange_rate;
		} else {
			rate = 0.4;
			CivLog.warning("null exchange rate .:. cannot find level: "+level+" for town "+this.getTown().getName());
		}
		
		rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.BARTER);
		return rate;
	}
	
	private String getExchangeRateString() {
		return ((int)(getBankExchangeRate()*100) + "%").toString();		
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
		
		int newCoins = (int)(principal*effectiveInterestRate);
		if (newCoins != 0) {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+"Our town earned "+newCoins+" coins from an interest rate of "
					+effectiveInterestRate+"% and on a principal of "+principal+" coins.");
			this.getTown().getTreasury().deposit(newCoins);
		}
		//Update the principal with the new value.
		this.getTown().getTreasury().setPrincipalAmount(this.getTown().getTreasury().getBalance());
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		this.level = getTown().saved_bank_level;
		this.interestRate = getTown().saved_bank_interest_amount;
	}
	
	public NonMemberFeeComponent getNonMemberFeeComponent() {
		return nonMemberFeeComponent;
	}
	
	public void setNonMemberFeeComponent(NonMemberFeeComponent nonMemberFeeComponent) {
		this.nonMemberFeeComponent = nonMemberFeeComponent;
	}
	
	public void openToolGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*3, town.getName()+"'s Bank Desk");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Bank Desk. You can use it to sell",
				CivColor.RESET+"different items for a set amount of price as",
				CivColor.RESET+"listed in the GUI. Hover over them to see the",
				CivColor.RESET+"sell price and non-member fee. If upgraded,",
				CivColor.RESET+"you can earn more money per item sold!"
				));
		
		
		inv.addItem(LoreGuiItem.build(CivColor.WhiteBold+"[D] Iron Ingots", ItemManager.getId(Material.IRON_INGOT), 0, 
				CivColor.LightGreen+"Rate: "+CivColor.Yellow+getExchangeRateString(),
				CivColor.LightGreen+"Non-Resident Fee: "+CivColor.Yellow+getNonResidentFeeString(),
				CivColor.Yellow+(this.IRON_INGOT_RATE*getBankExchangeRate())+CivColor.LightGreen+" Coins/Ingot"
				));
		
		inv.addItem(LoreGuiItem.build(CivColor.WhiteBold+"[D] Gold Ingots", ItemManager.getId(Material.GOLD_INGOT), 0, 
				CivColor.LightGreen+"Rate: "+CivColor.Yellow+getExchangeRateString(),
				CivColor.LightGreen+"Non-Resident Fee: "+CivColor.Yellow+getNonResidentFeeString(),
				CivColor.Yellow+(this.GOLD_INGOT_RATE*getBankExchangeRate())+CivColor.LightGreen+" Coins/Ingot"
				));
		
		inv.addItem(LoreGuiItem.build(CivColor.WhiteBold+"[D] Diamonds", ItemManager.getId(Material.DIAMOND), 0, 
				CivColor.LightGreen+"Rate: "+CivColor.Yellow+getExchangeRateString(),
				CivColor.LightGreen+"Non-Resident Fee: "+CivColor.Yellow+getNonResidentFeeString(),
				CivColor.Yellow+(this.DIAMOND_RATE*getBankExchangeRate())+CivColor.LightGreen+" Coins/Ingot"
				));
		
		inv.addItem(LoreGuiItem.build(CivColor.WhiteBold+"[D] Emeralds", ItemManager.getId(Material.EMERALD), 0, 
				CivColor.LightGreen+"Rate: "+CivColor.Yellow+getExchangeRateString(),
				CivColor.LightGreen+"Non-Resident Fee: "+CivColor.Yellow+getNonResidentFeeString(),
				CivColor.Yellow+(this.EMERALD_RATE*getBankExchangeRate())+CivColor.LightGreen+" Coins/Ingot"
				));
		
		p.openInventory(inv);
	}
}
