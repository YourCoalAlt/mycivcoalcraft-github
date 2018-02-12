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

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMarketItem;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Market extends Structure {
	
	public static int BULK_AMOUNT = 64;
	
	protected Market(Location center, String id, Town town) throws CivException {
		super(center, id, town);
		CivGlobal.addMarket(this);
	}
	
	public Market(ResultSet rs) throws SQLException, CivException {
		super(rs);
		CivGlobal.addMarket(this);
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
			spawnTradesmanVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		}
	}
	
	// XXX Villager Information
	
	public void spawnTradesmanVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Market Tradesman");
		v.setProfession(Profession.FARMER);
		
		String vilKey = this.getTown().getName()+":"+v.getCustomName()+":"+v.getLocation().toString();
		if (CivGlobal.getStructureVillager(vilKey) != null) {
			v.setHealth(0); v.remove();
		} else {
			CivGlobal.addStructureVillager(vilKey, v);
		}
	}
	
	public void openMainMenu(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9*6, "Global Market Menu");
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Global Market menu. You can buy",
				CivColor.RESET+"and sell various types of blocks within this",
				CivColor.RESET+"GUI.",
				CivColor.RESET+" ",
				CivColor.RESET+" "
				));
		
		for (ConfigMarketItem mat : CivSettings.marketItems.values()) {
			if (mat.custom_id == null) {
				ItemStack is = LoreGuiItem.build(CivData.getDisplayName(mat.type_id, mat.data), mat.type_id, mat.data, CivColor.LightGray+" « Click for Options » ");
				inv.addItem(is);
			} else {
				String cid = mat.custom_id.replace("mat_", "").replaceAll("_", " ");
				ItemStack is = LoreGuiItem.build(cid, 7, 0, CivColor.LightGray+" « Click for Options » ");
				inv.addItem(is);
			}
		}
		p.openInventory(inv);
	}
	
	public String ge1tPriceText(ConfigMarketItem item) {
		String out = "";
		String itemColor = getItemColor(item);
		
		out += CivColor.LightGray+"Sell "+BULK_AMOUNT+" for "+itemColor+item.getSellCostForAmount(BULK_AMOUNT)+CivColor.LightGray+" Coins;";
		out += CivColor.LightGray+"Sell 1 for "+itemColor+item.getSellCostForAmount(1)+CivColor.LightGray+" Coins;";
		out += CivColor.LightGray+"Buy 1 for "+itemColor+item.getBuyCostForAmount(1)+CivColor.LightGray+" Coins;";
		out += CivColor.LightGray+"Buy "+BULK_AMOUNT+" for "+itemColor+item.getBuyCostForAmount(BULK_AMOUNT)+CivColor.LightGray+" Coins";
		return out;
	}
	
	public String getItemColor(ConfigMarketItem m) {
		switch (m.lastaction) {
			case BUY: return CivColor.LightGreen;
			case SELL: return CivColor.Rose;
			default: return CivColor.Gray;
		}
	}
	
	public void openItemGUI(Player p, ConfigMarketItem m) {
		String name = CivData.getDisplayName(m.type_id, m.data);
		if (m.custom_id != null) name = m.custom_id.replace("mat_", "").replaceAll("_", " ");
		
		Inventory inv = Bukkit.createInventory(null, 9*1, "Market Trade "+name);
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" "
				));
		
		inv = getMarketItemOpened(inv, m);
		
		p.openInventory(inv);
	}
	
	public Inventory getMarketItemOpened(Inventory inv, ConfigMarketItem m) {
		String name = CivData.getDisplayName(m.type_id, m.data);
		if (m.custom_id != null) name = m.custom_id.replace("mat_", "").replaceAll("_", " ");
		
		if (m.custom_id == null) {
			ItemStack is = LoreGuiItem.build(CivData.getDisplayName(m.type_id, m.data), m.type_id, m.data, "» » »");
			inv.setItem(1, is);
		} else {
			ItemStack is = LoreGuiItem.build(name, 7, 0, "» » »");
			inv.setItem(1, is);
		}
		
		ItemStack sellbulk = LoreGuiItem.build("Sell "+BULK_AMOUNT, m.type_id, m.data,
				getItemColor(m)+m.getSellCostForAmount(BULK_AMOUNT)+CivColor.LightGray+" Coins", CivColor.LightGray+" « Click to Activate » ");
		inv.setItem(2, sellbulk);
		
		ItemStack sell = LoreGuiItem.build("Sell 1", m.type_id, m.data,
				getItemColor(m)+m.getSellCostForAmount(1)+CivColor.LightGray+" Coins", CivColor.LightGray+" « Click to Activate » ");
		inv.setItem(3, sell);
		
		ItemStack buy = LoreGuiItem.build("Buy 1", m.type_id, m.data,
				getItemColor(m)+m.getBuyCostForAmount(1)+CivColor.LightGray+" Coins", CivColor.LightGray+" « Click to Activate » ");
		inv.setItem(4, buy);
		
		ItemStack buybulk = LoreGuiItem.build("Buy "+BULK_AMOUNT, m.type_id, m.data,
				getItemColor(m)+m.getBuyCostForAmount(BULK_AMOUNT)+CivColor.LightGray+" Coins", CivColor.LightGray+" « Click to Activate » ");
		inv.setItem(5, buybulk);
		return inv;
	}
}
