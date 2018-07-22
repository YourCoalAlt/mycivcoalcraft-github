package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.components.NonMemberFeeComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGrocerLevel;
import com.avrgaming.civcraft.config.ConfigMarketItem;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.mobs.CivVillager;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Market extends Structure {
	
	public static int BULK_AMOUNT = 64;
	
	private int grocer_level = 0;
	private NonMemberFeeComponent nonMemberFeeComponent; 
	
	protected Market(Location center, String id, Town town) throws CivException {
		super(center, id, town);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onSave();
		CivGlobal.addMarket(this);
	}
	
	public Market(ResultSet rs) throws SQLException, CivException {
		super(rs);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onLoad();
		CivGlobal.addMarket(this);
	}
	
	public double getNonResidentFee() {
		return nonMemberFeeComponent.getFeeRate();
	}
	
	public void setNonResidentFee(double nonResidentFee) {
		this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
	}
	
	private String getNonResidentFeeString() {
		return "Non-Member Fee: "+((int)(getNonResidentFee()*100) + "%").toString();		
	}
	
	@Override
	public String getDynmapDescription() {
		String out = "<u><b>Market</u></b><br/>";
		out += "<b>Grocer Level "+grocer_level+"</b><br/>";
		return out;
	}
	
	@Override
	public void delete() throws SQLException {
		super.delete();
		CivGlobal.removeMarket(this);
	}
	
	public void processBuy(Player player, Resident resident, int bulkCount, ConfigMarketItem item) {
		item.buy(resident, player, bulkCount);
	}
	
	public void processSell(Player player, Resident resident, int bulkCount, ConfigMarketItem item) {
		item.sell(resident, player, bulkCount);
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		switch (sb.command.toLowerCase().trim()) {
		case "/villager":
			spawnMarketVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		case "/grocer":
			spawnGrocerVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		}
	}
	
	// XXX Villager Information
	
	public void spawnMarketVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		CivVillager.onSpawn(v, vLoc, "Market Tradesman", false, Profession.FARMER);
		
		String vilKey = this.getTown().getName()+":"+v.getCustomName()+":"+v.getLocation().toString();
		if (CivGlobal.getCivVillager(vilKey) != null) {
			v.setHealth(0); v.remove();
		} else {
			CivGlobal.addCivVillager(vilKey, v);
		}
	}
	
	public void openMainMarketMenu(Player p) {
		if (!Buildable.validatePlayerGUI(p, this, false, false, false, false)) return;
		Inventory inv = Bukkit.createInventory(null, 9*6, "Global Market Menu");
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", CivItem.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Global Market menu. You can buy",
				CivColor.RESET+"and sell various types of blocks within this",
				CivColor.RESET+"GUI.",
				CivColor.RESET+" ",
				CivColor.RESET+" "
				));
		
		for (ConfigMarketItem mat : CivSettings.marketItems.values()) {
			if (mat.custom_id == null) {
				ItemStack is = CivItem.newStack(mat.type_id, 1, mat.data);
				ItemStack iis = LoreGuiItem.buildWithStack(CivData.getStackName(is), is, CivColor.Gray+" « Click for More » ");
				inv.addItem(iis);
				continue;
			}
			String cid = mat.custom_id.replace("mat_", "").replaceAll("_", " ");
			ItemStack iis = LoreGuiItem.build(cid, 7, 0, CivColor.Gray+" « Click for More » ");
			inv.addItem(iis);
		}
		p.openInventory(inv);
	}
	
	public String getItemColor(ConfigMarketItem m) {
		switch (m.lastaction) {
			case BUY: return CivColor.LightGreen;
			case SELL: return CivColor.Rose;
			default: return CivColor.DarkGray;
		}
	}
	
	public void openItemGUI(Player p, ConfigMarketItem m) {
		String name = CivData.getStackName(m.type_id, m.data);
		if (m.custom_id != null) name = m.custom_id.replace("mat_", "").replaceAll("_", " ");
		
		Inventory inv = Bukkit.createInventory(null, 9*1, "Market Trade "+name);
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", CivItem.getId(Material.PAPER), 0, 
				CivColor.RESET+"Prices may change if you keep this",
				CivColor.RESET+"inventory opened for too long!",
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" "
				));
		
		inv = getMarketItemOpened(p, inv, m);
		
		p.openInventory(inv);
	}
	
	public Inventory getMarketItemOpened(Player p, Inventory inv, ConfigMarketItem m) {
		String name = CivData.getStackName(m.type_id, m.data);
		if (m.custom_id != null) name = m.custom_id.replace("mat_", "").replaceAll("_", " ");
		
		if (m.custom_id == null) {
			ItemStack is = CivItem.newStack(m.type_id, 1, m.data);
			ItemStack iis = LoreGuiItem.buildWithStack(CivData.getStackName(is), is, CivColor.Gray+" « Click for More » ");
			inv.setItem(1, iis);
		} else {
			ItemStack is = LoreGuiItem.build(name, 7, 0, CivColor.LightPurpleBold+"» » »");
			inv.setItem(1, is);
		}
		
		int buyFull = 0;
		int sellFull = 0;
		for (int i = 0; i < 36; i++) {
			ItemStack is = p.getInventory().getItem(i);
			if (is == null || is.getType() == Material.AIR) {
				buyFull += 64;
				continue;
			}
			if (CivItem.getId(is) == m.type_id && CivItem.getData(is) == m.data) {
				sellFull += is.getAmount();
				continue;
			}
		}
		
		ItemStack sell = LoreGuiItem.build("Sell", m.type_id, m.data,
				CivColor.Gray+"1 for "+getItemColor(m)+m.getSellCostForAmount(1)+CivColor.Gray+" Coins",
				CivColor.Gray+"64 for "+getItemColor(m)+m.getSellCostForAmount(BULK_AMOUNT)+CivColor.Gray+" Coins",
				CivColor.Gray+"Inventory ("+sellFull+") for "+getItemColor(m)+m.getSellCostForAmount(sellFull)+CivColor.Gray+" Coins",
				CivColor.Gray+" « Left Click for 1 » ",
				CivColor.Gray+" « Shift + Left Click for 64 » ",
				CivColor.Gray+" « Middle Click for Inventory ("+sellFull+") » ");
		inv.setItem(3, sell);
		
		ItemStack buy = LoreGuiItem.build("Buy", m.type_id, m.data,
				CivColor.Gray+"1 for "+getItemColor(m)+m.getBuyCostForAmount(1)+CivColor.Gray+" Coins",
				CivColor.Gray+"64 for "+getItemColor(m)+m.getBuyCostForAmount(BULK_AMOUNT)+CivColor.Gray+" Coins",
				CivColor.Gray+"Inventory ("+buyFull+") for "+getItemColor(m)+m.getBuyCostForAmount(buyFull)+CivColor.Gray+" Coins",
				CivColor.Gray+" « Left Click for 1 » ",
				CivColor.Gray+" « Shift + Left Click for 64 » ",
				CivColor.Gray+" « Middle Click for Inventory ("+buyFull+") » ");
		inv.setItem(5, buy);
		
		ItemStack back = LoreGuiItem.build("Back", CivItem.getId(Material.MAP), 0, "Back to Market Menu");
		inv.setItem(8, back);
		
		return inv;
	}
	
	// XXX Grocer Villager
	
	public void spawnGrocerVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		CivVillager.onSpawn(v, vLoc, "Grocer Salesman", false, Profession.BUTCHER);
		
		String vilKey = this.getTown().getName()+":"+v.getCustomName()+":"+v.getLocation().toString();
		if (CivGlobal.getCivVillager(vilKey) != null) {
			v.setHealth(0); v.remove();
		} else {
			CivGlobal.addCivVillager(vilKey, v);
		}
	}
	
	public void openMainGrocerMenu(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9*6, "Grocer Menu");
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", CivItem.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Grocer menu. You can buy food",
				CivColor.RESET+"items inside of this GUI.",
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" "
				));
		
		int buyFull = 0;
		for (int i = 0; i < 36; i++) {
			ItemStack is = p.getInventory().getItem(i);
			if (is == null || is.getType() == Material.AIR) {
				buyFull += 64;
				continue;
			}
		}
		
		for (ConfigGrocerLevel gl : CivSettings.grocerLevels.values()) {
			if (gl.level <= this.getGrocerLevel()) {
				ItemStack is = CivItem.newStack(gl.type_id, 1, gl.data);
				ItemStack iis = LoreGuiItem.buildWithStack(CivData.getStackName(is), is,
						CivColor.Gray+"1 for "+CivColor.Yellow+(gl.price)+CivColor.Gray+" Coins",
						CivColor.Gray+"64 for "+CivColor.Yellow+(gl.price*64)+CivColor.Gray+" Coins",
						CivColor.Gray+"Inventory ("+buyFull+") for "+CivColor.Yellow+(gl.price*buyFull)+CivColor.Gray+" Coins",
						CivColor.Gray+" « Left Click for 1 » ",
						CivColor.Gray+" « Shift + Left Click for 64 » ",
						CivColor.Gray+" « Middle Click for Inventory ("+buyFull+") » ",
						getNonResidentFeeString());
				inv.addItem(iis);
			} else {
				ItemStack is = LoreGuiItem.build("Locked", Material.BEDROCK, 0,
						CivColor.Gray+" « Requires Level "+gl.level+" » ",
						CivColor.Gray+" « Currently Level "+this.getGrocerLevel()+" » ");
				inv.addItem(is);
			}
		}
		p.openInventory(inv);
	}
	
	public void buy_grocer_material(Player player, String itemName, int id, byte data, int amount, double price) {
		Resident resident = CivGlobal.getResident(player.getName());
		double payToTown = Math.round(price*this.getNonResidentFee());
		try {
			Town t = resident.getTown();
			if (t == this.getTown()) {
				// Pay no taxes! You're a member.
				resident.buyItem(itemName, id, data, price, amount);
				CivMessage.send(player, CivColor.LightGreen + "Bought "+amount+" "+itemName+" for "+price+ " coins.");
			} else {
				// Pay non-resident taxes
				resident.buyItem(itemName, id, data, price + payToTown, amount);
				this.getTown().deposit(payToTown);
				CivMessage.send(player, CivColor.LightGreen + "Bought "+amount+" "+itemName+" for "+price+ " coins.");
				CivMessage.send(player, CivColor.Yellow + "Paid "+ payToTown+" coins in non-resident taxes.");
				}
			} catch (CivException e) {
				CivMessage.send(player, CivColor.Rose+e.getMessage());
			}
		return;
	}
	
	public int getGrocerLevel() {
		return grocer_level;
	}
	
	public void setGrocerLevel(int level) {
		this.grocer_level = level;
	}
}
