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
import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMarketItem;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Market extends Structure {

	public HashMap<Integer, LinkedList<StructureSign>> signIndex = new HashMap<Integer, LinkedList<StructureSign>>();
	
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
	
	public static void globalSignUpdate(int id) {
		for (Market market : CivGlobal.getMarkets()) {
			
			LinkedList<StructureSign> signs = market.signIndex.get(id);
			if (signs == null) {
				continue;
			}
			
			for (StructureSign sign : signs) {			
				ConfigMarketItem item = CivSettings.marketItems.get(id);
				if (item != null) {
					try {
					market.setSignText(sign, item);
					} catch (ClassCastException e) {
						CivLog.error("Can't cast structure sign to sign for market update.");
						continue;
					}
				}
			}
		}
	}
	
	public void processBuy(Player player, Resident resident, int bulkCount, ConfigMarketItem item) throws CivException {
		item.buy(resident, player, bulkCount);
	}
	
	public void processSell(Player player, Resident resident, int bulkCount, ConfigMarketItem item) throws CivException {
		item.sell(resident, player, bulkCount);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) throws CivException {
		
		Integer id = Integer.valueOf(sign.getType());
		ConfigMarketItem item = CivSettings.marketItems.get(id);
		Resident resident = CivGlobal.getResident(player);

		if (resident == null) {
			CivMessage.sendError(player, "You're not registerd?? what??");
			return;
		}
		
		if (item == null) {
			CivMessage.sendError(player, "ERROR: Unknown item. Market ID:"+id);
			return;
		}
		
		switch (sign.getAction().toLowerCase()) {
		case "sellbig":
			processSell(player, resident, BULK_AMOUNT, item);
			break;
		case "sell":
			processSell(player, resident, 1, item);
			break;
		case "buy":
			processBuy(player, resident, 1, item);
			break;
		case "buybig":
			processBuy(player, resident, BULK_AMOUNT, item);
			break;
		}
	
		player.updateInventory();
		Market.globalSignUpdate(id);
	}
	
	public void setSignText(StructureSign sign, ConfigMarketItem item) {
		String itemColor;
		switch (item.lastaction) {
		case BUY:
			itemColor = CivColor.LightGreen;
			break;
		case SELL:
			itemColor = CivColor.Rose;
			break;
		default:
			itemColor = CivColor.Black;
			break;
		}
		
		try {
		Sign s;
		switch (sign.getAction().toLowerCase()) {
		case "sellbig":
			s = (Sign)sign.getCoord().getBlock().getState();
			s.setLine(0, ChatColor.BOLD+"Sell Bulk");
			s.setLine(1, item.name);
			s.setLine(2, itemColor+item.getSellCostForAmount(BULK_AMOUNT)+" Coins");
			s.setLine(3, "Amount "+BULK_AMOUNT);
			s.update();
			break;
		case "sell":
			s = (Sign)sign.getCoord().getBlock().getState();
			s.setLine(0, ChatColor.BOLD+"Sell");
			s.setLine(1, item.name);
			s.setLine(2, itemColor+item.getSellCostForAmount(1)+" Coins");
			s.setLine(3, "Amount 1");
			s.update();
			break;
		case "buy":
			s = (Sign)sign.getCoord().getBlock().getState();
			s.setLine(0, ChatColor.BOLD+"Buy");
			s.setLine(1, item.name);
			s.setLine(2, itemColor+item.getBuyCostForAmount(1)+" Coins");
			s.setLine(3, "Amount 1");
			s.update();
			break;
		case "buybig":
			s = (Sign)sign.getCoord().getBlock().getState();
			s.setLine(0, ChatColor.BOLD+"Buy Bulk");
			s.setLine(1, item.name);
			s.setLine(2, itemColor+item.getBuyCostForAmount(BULK_AMOUNT)+" Coins");
			s.setLine(3, "Amount "+BULK_AMOUNT);
			s.update();
			break;
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void buildSign(String action, Integer id, BlockCoord absCoord, 
			ConfigMarketItem item, SimpleBlock commandBlock) {
		Block b = absCoord.getBlock();
		
		ItemManager.setTypeIdAndData(b, ItemManager.getId(Material.WALL_SIGN), (byte)commandBlock.getData(), false);
		
		StructureSign structSign = CivGlobal.getStructureSign(absCoord);
		if (structSign == null) {
			structSign = new StructureSign(absCoord, this);
		}
		
		structSign.setDirection(ItemManager.getData(b.getState()));
		structSign.setType(""+id);
		structSign.setAction(action);

		structSign.setOwner(this);
		this.addStructureSign(structSign);
		CivGlobal.addStructureSign(structSign);
		
		LinkedList<StructureSign> signs = this.signIndex.get(id);
		if (signs == null) {
			signs = new LinkedList<StructureSign>();
		}
	
		signs.add(structSign);
		this.signIndex.put(id, signs);
		this.setSignText(structSign, item);
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		Integer id;
		ConfigMarketItem item;
		switch (commandBlock.command.toLowerCase().trim()) {
		case "/sellbig":
			spawnTradesmanVillager(absCoord.getLocation(), 0);
			id = Integer.valueOf(commandBlock.keyvalues.get("id"));
			item = CivSettings.marketItems.get(id);
			if (item != null) {
				if (item.isStackable()) {
					buildSign("sellbig", id, absCoord, item, commandBlock);
				}
			}
			break;
		case "/sell":
			id = Integer.valueOf(commandBlock.keyvalues.get("id"));
			
			item = CivSettings.marketItems.get(id);
			if (item != null) {
				buildSign("sell", id, absCoord, item, commandBlock);
			}		
			break;
		case "/buy":
			id = Integer.valueOf(commandBlock.keyvalues.get("id"));
			item = CivSettings.marketItems.get(id);
			if (item != null) {
				buildSign("buy", id, absCoord, item, commandBlock);
			}		
			break;
		case "/buybig":
			id = Integer.valueOf(commandBlock.keyvalues.get("id"));
			item = CivSettings.marketItems.get(id);
			if (item != null) {
				if (item.isStackable()) {
					buildSign("buybig", id, absCoord, item, commandBlock);
				}
			}		
			break;
		case "/tradesman":
			spawnTradesmanVillager(absCoord.getLocation(), 0);
//			spawnTradesmanVillager(absCoord.getLocation(), (byte)sb.getData());
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
		CivGlobal.addStructureVillager(v);
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
		
		for (int id = 0; id < 200; id++) {
			ConfigMarketItem mat = CivSettings.marketItems.get(id);
			if (mat == null) {
				continue;
			}
			
			if (mat.custom_id == null) {
				ItemStack is = LoreGuiItem.build(CivData.getDisplayName(mat.type_id, mat.data), mat.type_id, mat.data, getPriceText(mat).split(";"));
				inv.addItem(is);
			} else {
				String cid = mat.custom_id.replace("mat_", "").replaceAll("_", " ");
				ItemStack is = LoreGuiItem.build(cid, 7, 0, getPriceText(mat).split(";"));
				inv.addItem(is);
			}
		}
		p.openInventory(inv);
	}
	
	public String getPriceText(ConfigMarketItem item) {
		String out = "";
		String itemColor;
		switch (item.lastaction) {
		case BUY:
			itemColor = CivColor.LightGreen;
			break;
		case SELL:
			itemColor = CivColor.Rose;
			break;
		default:
			itemColor = CivColor.Gray;
			break;
		}
		
		String name = CivData.getDisplayName(item.type_id, item.data);
		if (item.custom_id != null) {
			name = item.custom_id.replace("mat_", "").replaceAll("_", " ");
		}
		
		out += CivColor.LightGray+"Sell "+BULK_AMOUNT+" "+name+" for "+itemColor+item.getSellCostForAmount(BULK_AMOUNT)+CivColor.LightGray+" Coins;";
		out += CivColor.LightGray+"Sell 1 "+name+" for "+itemColor+item.getSellCostForAmount(1)+CivColor.LightGray+" Coins;";
		out += CivColor.LightGray+"Buy 1 "+name+" for "+itemColor+item.getBuyCostForAmount(1)+CivColor.LightGray+" Coins;";
		out += CivColor.LightGray+"Buy "+BULK_AMOUNT+" "+name+" for "+itemColor+item.getBuyCostForAmount(BULK_AMOUNT)+CivColor.LightGray+" Coins;";
		out += CivColor.LightGray+" « Click for Options » ";
		return out;
	}
}
