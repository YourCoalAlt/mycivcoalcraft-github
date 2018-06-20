package com.avrgaming.civcraft.listener.civcraft;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGranaryLevel;
import com.avrgaming.civcraft.config.ConfigGranaryTask;
import com.avrgaming.civcraft.config.ConfigGrocerLevel;
import com.avrgaming.civcraft.config.ConfigLabTask;
import com.avrgaming.civcraft.config.ConfigMarketItem;
import com.avrgaming.civcraft.config.ConfigMineTask;
import com.avrgaming.civcraft.config.ConfigMission;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.interactive.InteractiveSpyMission;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.structure.Bank;
import com.avrgaming.civcraft.structure.Barracks;
import com.avrgaming.civcraft.structure.Blacksmith;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.Granary;
import com.avrgaming.civcraft.structure.Lab;
import com.avrgaming.civcraft.structure.Market;
import com.avrgaming.civcraft.structure.Mine;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.structure.Warehouse;
import com.avrgaming.civcraft.structure.Windmill;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.war.War;

import net.md_5.bungee.api.ChatColor;

public class InventoryDisplaysListener implements Listener {
	
	//XXX Inventroy Clicking
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		Resident res = CivGlobal.getResident(p);
		if (!res.hasTown() || res == null) return;
		
		if (event.getInventory().getName().contains(res.getName()+" Mail Menu [R]")) this.clickResMainMailMenu(p, event);
		if (event.getInventory().getName().contains(res.getName()+" View Mail [R]")) this.clickResViewMailMenu(p, event);
		if (event.getInventory().getName().contains("[RR] Mail ")) this.clickResReadMailMenu(p, event);
		
		if (event.getInventory().getName().contains("Spy Mission Menu")) this.clickSpyMissionMenu(p, event);
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Quest Viewer")) this.clickTownQuestViewer(p, event);
		
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Town Info")) this.clickTownInfoViewer(p, event);
		if (event.getInventory().getName().contains("Stat Information")  || event.getInventory().getName().contains("Town-Applied Buffs")  ||
				event.getInventory().getName().contains("Building Support")) this.clickTownStatRegister(p, event);
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Building Support")) this.clickBuildSupport(p, event);
		
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Barracks Unit Upgrade Menu")) this.clickUnitUpgrade(p, event);
		if (event.getInventory().getName().contains("Repair Master")) this.clickBarracksRepair(p, event);
		
		if (event.getInventory().getName().contains("Global Market Menu")) this.clickMarketMenu(p, event);
		if (event.getInventory().getName().contains("Market Trade ")) this.clickMarketItem(p, event);
		if (event.getInventory().getName().contains("Grocer Menu")) this.clickGrocerItem(p, event);
		
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Warehouse Guide")) this.clickWarehouseToggle(p, event);
		
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Smelter Operator"))	this.clickBlacksmithSmelter(p, event);
		
		if (event.getInventory().getName().contains("Windmill Menu")) this.clickWindmillPlant(p, event);
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Food Storage")) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
				return;
			}
			
			if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
				event.setCancelled(true);
			}
			
			switch (event.getCurrentItem().getType()) {
			case BREAD:
			case CARROT_ITEM:
			case POTATO_ITEM:
			case BEETROOT:
			case SUGAR:
			case PUMPKIN_PIE:
				ItemStack i = event.getCurrentItem();
				if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
					String matN = i.getItemMeta().getDisplayName().toLowerCase();
					String mat = ChatColor.stripColor(matN);
					mat = mat.toString().substring(0, 1).toUpperCase()+mat.toString().substring(1).toLowerCase();
					
					Granary granary = null;
					Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
					if (buildable instanceof Granary) {
						granary = (Granary) buildable;
					} else {
						CivMessage.sendError(p, "Granary you are trying to access is null? Contact an admin if this continues.");
						return;
					}
					granary.openStorageItemMenuGUI(p, res.getTown(), mat);
				}
				break;
			case AIR:
				break;
			default:
				break;
			}
		}
		
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Granary Tasks")) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
				return;
			}
			
			if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
				event.setCancelled(true);
			}
			
			switch (event.getCurrentItem().getType()) {
			case LIME_SHULKER_BOX:
				if (!event.getCurrentItem().hasItemMeta()) {
					CivMessage.sendError(p, "This task had an error, try again or contact an admin.");
					p.closeInventory();
				}
				
				ItemMeta meta = event.getCurrentItem().getItemMeta();
				String taskName = ChatColor.stripColor(meta.getDisplayName()).replace("[Available] Task ", "");
				int task = Integer.parseInt(taskName);
				
				Granary granary = null;
				Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
				if (buildable instanceof Granary) {
					granary = (Granary) buildable;
				} else {
					CivMessage.sendError(p, "Granary you are trying to access is null? Contact an admin if this continues.");
					return;
				}
				granary.openTaskCompleterGUI(p, res.getTown(), task);
				break;
			case RED_SHULKER_BOX:
				CivMessage.sendError(p, "You already completed this task, it will be re-opened at upkeep!");
				p.closeInventory();
				break;
			case BLACK_SHULKER_BOX:
				CivMessage.sendError(p, "This task is currently locked, upgrade your town to unlock it!");
				p.closeInventory();
				break;
			case AIR:
				break;
			default:
				break;
			}
		}
		
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Mine Tasks"))	this.clickMineTask1(p, event);
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Lab Tasks"))	this.clickLabTask1(p, event);
	}
	
	public void clickTownInfoViewer(Player p, InventoryClickEvent event) {
		Resident res = CivGlobal.getResident(p);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
//		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
//			event.setCancelled(true);
//		}
		
		TownHall th = (TownHall) res.getTown().getStructureByType("s_townhall");
		if (th == null) {
			th = (TownHall) res.getTown().getStructureByType("s_capitol");
			if (th == null) {
				CivMessage.sendError(p, "Town Hall null? Contact an admin if this is wrong!"); p.closeInventory();
			}
		}
		switch (event.getCurrentItem().getType()) {
		case WHEAT:
			th.openGrowthStatsGUI(p, th.getTown());
			break;
		case COBBLE_WALL:
			th.openHammerStatsGUI(p, th.getTown());
			break;
		case GLASS_BOTTLE:
			th.openBeakerStatsGUI(p, th.getTown());
			break;
		case NETHER_STALK:
			th.openCultureStatsGUI(p, th.getTown());
			break;
		case ITEM_FRAME:
			th.openTownBuffsListGUI(p, th.getTown());
			break;
		case BEACON:
			th.openSupportDepositGUI(p, th.getTown());
			break;
		default:
			//CivMessage.global(event.getCurrentItem().getType().toString());
			break;
		}
	}
	
	public void clickTownQuestViewer(Player p, InventoryClickEvent event) {
		event.setCancelled(true);
		Resident res = CivGlobal.getResident(p);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		TownHall th = (TownHall) res.getTown().getStructureByType("s_townhall");
		if (th == null) {
			th = (TownHall) res.getTown().getStructureByType("s_capitol");
			if (th == null) {
				CivMessage.sendError(p, "Town Hall null? Contact an admin if this is wrong!"); p.closeInventory();
			}
		}
		
		switch (event.getCurrentItem().getType()) {
		case STONE_PICKAXE:
			Mine mine = (Mine) th.getTown().getStructureByType("ti_mine");
			mine.openToolGUI(p, th.getTown());
			break;
		case GLASS_BOTTLE:
			Lab lab = (Lab) th.getTown().getStructureByType("ti_lab");
			lab.openToolGUI(p, th.getTown());
			break;
		case SKULL:
//			Monument monument = (Monument) th.getTown().getStructureByType("ti_monument");
//			monument.openToolGUI(p, th.getTown());
			CivMessage.sendError(p, "Culture quests coming soon!");
			break;
		case REDSTONE_LAMP_OFF:
//			Cottage cottage = (Cottage) th.getTown().getStructureByType("ti_cottage");
//			cottage.openToolGUI(p, th.getTown());
			CivMessage.sendError(p, "Coin quests coming soon!");
			break;
		default:
			break;
		}
	}
	
	public void clickTownStatRegister(Player p, InventoryClickEvent event) {
		Resident res = CivGlobal.getResident(p);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		TownHall th = (TownHall) res.getTown().getStructureByType("s_townhall");
		if (th == null) {
			th = (TownHall) res.getTown().getStructureByType("s_capitol");
			if (th == null) {
				CivMessage.sendError(p, "Town Hall null? Contact an admin if this is wrong!"); p.closeInventory();
			}
		}
		switch (event.getCurrentItem().getType()) {
		case MAP:
			th.openMainInfoGUI(p, th.getTown());
			break;
		default:
			break;
		}
	}
	
	public void clickBuildSupport(Player p, InventoryClickEvent event) {
		Resident res = CivGlobal.getResident(p);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		TownHall th = (TownHall) res.getTown().getStructureByType("s_townhall");
		if (th == null) {
			th = (TownHall) res.getTown().getStructureByType("s_capitol");
			if (th == null) {
				CivMessage.sendError(p, "Town Hall null? Contact an admin if this is wrong!"); p.closeInventory();
			}
		}
		switch (event.getCurrentItem().getType()) {
		case BARRIER:
			th.getTown().addSupportDeposit(10000);
			CivMessage.sendTown(th.getTown(), "Added 10,000 support blocks from Admin OP.");
			break;
		default:
			break;
		}
	}
	
	// XXX Working
	public void clickBarracksRepair(Player p, InventoryClickEvent event) {
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		if (buildable instanceof Barracks) {
			Barracks b = (Barracks) buildable;
			Resident res = CivGlobal.getResident(p);
			if (event.getClickedInventory() != null && event.getClickedInventory().getName().contains("Repair Master")) {
				switch (event.getCurrentItem().getType()) {
				case COMMAND:
					Inventory opened = b.validateRepairItemGUI(p, res.getTown(), event.getInventory());
					event.getInventory().setContents(opened.getContents());
					break;
				default:
					break;
				}
			}
		} else {
			event.setCancelled(true);
			CivMessage.sendError(p, "Barracks you are trying to access is null? Contact an admin if this continues.");
			p.closeInventory();
		}
	}
	
	public void clickUnitUpgrade(Player p, InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(1).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		if (buildable instanceof Barracks) {
			Barracks b = (Barracks) buildable;
			switch (event.getCurrentItem().getType()) {
			case ANVIL:
				b.upgradeUnit(p, Unit.getPlayerUnitStack(p), event.getInventory().getItem(2));
				p.closeInventory();
				break;
			default:
				break;
			}
		} else {
			event.setCancelled(true);
			CivMessage.sendError(p, "Barracks you are trying to access is null? Contact an admin if this continues.");
			p.closeInventory();
			return;
		}
	}
	
	public void clickMarketMenu(Player p, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) event.setCancelled(true);
		
		Resident res = CivGlobal.getResident(p);
		Market s = (Market) res.getTown().getStructureByType("s_market");
		if (s == null) { CivMessage.sendError(p, "Your town does not have a market... cannot use one until then!"); event.setCancelled(true); }
		for (ConfigMarketItem m : CivSettings.marketItems.values()) {
			if (ItemManager.getId(event.getCurrentItem()) == m.type_id && ItemManager.getData(event.getCurrentItem()) == m.data &&
					event.getInventory().getName().contains("Global Market Menu")) s.openItemGUI(p, m);
		}
	}
	
	public void clickMarketItem(Player p, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) event.setCancelled(true);
		
		Resident res = CivGlobal.getResident(p);
		Market s = (Market) res.getTown().getStructureByType("s_market");
		if (s == null) { CivMessage.sendError(p, "Your town does not have a market... cannot use one until then!"); event.setCancelled(true); }
		
		ConfigMarketItem m = null;
		ItemStack mi = event.getInventory().getItem(1);
		int id = ItemManager.getId(mi);
		int data = ItemManager.getData(mi);
		for (ConfigMarketItem ma : CivSettings.marketItems.values()) {
			if (id == ma.type_id && data == ma.data) {
				m = ma;
			}
		}
		
		if (m == null) return;
		
		String c = event.getCurrentItem().getItemMeta().getDisplayName();
		if (c != null) {
			ClickType ct = event.getClick();
			int buyFull = 0;
			int sellFull = 0;
			for (int i = 0; i < 36; i++) {
				ItemStack is = p.getInventory().getItem(i);
				if (is == null || is.getType() == Material.AIR) {
					buyFull += 64;
					continue;
				}
				if (ItemManager.getId(is) == m.type_id && ItemManager.getData(is) == m.data) {
					sellFull += is.getAmount();
					continue;
				}
			}
			
			if (c.contains("Sell")) {
				if (ct == ClickType.LEFT) s.processSell(p, res, 1, m);
				else if (ct == ClickType.SHIFT_LEFT) s.processSell(p, res, Market.BULK_AMOUNT, m);
				else if (ct == ClickType.MIDDLE) s.processSell(p, res, sellFull, m);
			} else if (c.contains("Buy")) {
				if (ct == ClickType.LEFT) s.processBuy(p, res, 1, m);
				else if (ct == ClickType.SHIFT_LEFT) s.processBuy(p, res, Market.BULK_AMOUNT, m);
				else if (ct == ClickType.MIDDLE) s.processBuy(p, res, buyFull, m);
			} else if (c.contains("Back")) {
				s.openMainMarketMenu(p);
			}
		}
		
		Inventory opened = s.getMarketItemOpened(p, event.getInventory(), m);
		event.getInventory().setContents(opened.getContents());
	}
	
	public void clickGrocerItem(Player p, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) event.setCancelled(true);
		
		Resident res = CivGlobal.getResident(p);
		Market s = (Market) res.getTown().getStructureByType("s_market");
		if (s == null) { CivMessage.sendError(p, "Your town does not have a Market... cannot use one until then!"); event.setCancelled(true); }
		
		ConfigGrocerLevel g = null;
		ItemStack mi = event.getInventory().getItem(1);
		int id = ItemManager.getId(mi);
		int data = ItemManager.getData(mi);
		for (ConfigGrocerLevel gl : CivSettings.grocerLevels.values()) {
			if (id == gl.itemId && data == gl.itemData) {
				g = gl;
			}
		}
		
		if (g == null) return;
		
		String c = event.getCurrentItem().getItemMeta().getDisplayName();
		if (c != null) {
			ClickType ct = event.getClick();
			int buyFull = 0;
			for (int i = 0; i < 36; i++) {
				ItemStack is = p.getInventory().getItem(i);
				if (is == null || is.getType() == Material.AIR) {
					buyFull += 64;
					continue;
				}
			}
			
			if (c.contains(g.itemName)) {
				if (ct == ClickType.LEFT) s.buy_grocer_material(p, g.itemName, g.itemId, (byte)g.itemData, 1, g.price);
				else if (ct == ClickType.SHIFT_LEFT) s.buy_grocer_material(p, g.itemName, g.itemId, (byte)g.itemData, 64, 64*g.price);
				else if (ct == ClickType.MIDDLE) s.buy_grocer_material(p, g.itemName, g.itemId, (byte)g.itemData, buyFull, buyFull*g.price);
			}
		}
		s.openMainGrocerMenu(p);
	}
	
	public void clickWindmillPlant(Player p, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) event.setCancelled(true);
		
		Resident res = CivGlobal.getResident(p);
		Windmill w;
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		if (buildable instanceof Windmill) {
			w = (Windmill) buildable;
			if (w.getTown() != res.getTown()) {
				CivMessage.sendError(p, "Cannot use a Windmill not in your town.");
				event.setCancelled(true);
				return;
			}
		} else {
			CivMessage.sendError(p, "No Windmill found? If this error persists, contact an admin.");
			event.setCancelled(true);
			return;
		}
		
		boolean meax = false;
		boolean melt = false;
		boolean fakeclick = false;
		String n = event.getCurrentItem().getItemMeta().getDisplayName();
		if (n.contains("Wheat")) {
			if (event.getClick() == ClickType.LEFT) {
				if (w.getSumPlantSettings() < w.getMaxPlantAmount()) w.wheat_sel++; else meax = true;
			} else if (event.getClick() == ClickType.RIGHT) {
				if (w.wheat_sel > 0) w.wheat_sel--; else melt = true;
			} else {
				fakeclick = true;
			}
		} else if (n.contains("Carrot")) {
			if (event.getClick() == ClickType.LEFT) {
				if (w.getSumPlantSettings() < w.getMaxPlantAmount()) w.carrot_sel++; else meax = true;
			} else if (event.getClick() == ClickType.RIGHT) {
				if (w.carrot_sel > 0) w.carrot_sel--; else melt = true;
			} else {
				fakeclick = true;
			}
		} else if (n.contains("Potato")) {
			if (event.getClick() == ClickType.LEFT) {
				if (w.getSumPlantSettings() < w.getMaxPlantAmount()) w.potato_sel++; else meax = true;
			} else if (event.getClick() == ClickType.RIGHT) {
				if (w.potato_sel > 0) w.potato_sel--; else melt = true;
			} else {
				fakeclick = true;
			}
		} else if (n.contains("Beetroot")) {
			if (event.getClick() == ClickType.LEFT) {
				if (w.getSumPlantSettings() < w.getMaxPlantAmount()) w.beetroot_sel++; else meax = true;
			} else if (event.getClick() == ClickType.RIGHT) {
				if (w.beetroot_sel > 0) w.beetroot_sel--; else melt = true;
			} else {
				fakeclick = true;
			}
		} else {
			return;
		}
		
		if (meax) {
			CivMessage.sendError(p, "Please reduce another seed's priority to raise this one."); return;
		}
		if (melt) {
			CivMessage.sendError(p, "You cannot have negative planting set."); return;
		}
		if (fakeclick) {
			CivMessage.sendError(p, "Only Left or Right click is valid on this item."); return;
		}
		
		String key = "WINDPLANT_"+w.getConfigId()+"_"+w.getCorner().toString();
		ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
		if (entry != null && !entry.isEmpty()) {
			SessionEntry se = entry.get(0);
			String nk = w.getSaveKey(w);
			CivGlobal.getSessionDB().update(se.request_id, se.key, nk);
		}
		
		CivMessage.sendSuccess(p, "Successfully changed value!");
		w.openPlantSettingsGUI(p, res.getTown());
	}
	
	public void clickSpyMissionMenu(Player p, InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		for (ConfigMission mission : CivSettings.missions.values()) {
			if (event.getCurrentItem().getItemMeta().getDisplayName().contains(mission.name)) {
				boolean canPerform = checkPerformPlayer(p);
				if (canPerform) {
					Resident res = CivGlobal.getResident(p);
					ChunkCoord coord = new ChunkCoord(p.getLocation());
					CultureChunk cc = CivGlobal.getCultureChunk(coord);
					res.setInteractiveMode(new InteractiveSpyMission(mission, p.getName(), p.getLocation(), cc.getTown()));
				}
				p.closeInventory();
			}
		}
	}
	
	private boolean checkPerformPlayer(Player p) {
		if (War.isWarTime()) {
			CivMessage.sendError(p, "Cannot use spy missions during war time.");
			return false;
		}
		
		Resident resident = CivGlobal.getResident(p);
		if (resident == null || !resident.hasTown()) {
			CivMessage.sendError(p, "Only residents of towns can perform spy missions.");
			return false;
		}
		
		if (!p.isOp()) { 
			try {
				Date now = new Date();
				int spyRegisterTime = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.spy_register_time");
				int spyOnlineTime = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.spy_online_time");
				long expire = resident.getRegistered() + (spyRegisterTime*60*1000);
				if (now.getTime() <= expire) {
					CivMessage.sendError(p, "You cannot use a spy yet, you must play CivCraft a bit longer before you can use it.");
					return false;
				}
				expire = resident.getLastOnline() + (spyOnlineTime*60*1000);
				if (now.getTime() <= expire) {
					CivMessage.sendError(p, "You must be online longer before you can use a spy.");
					return false;
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
			}
		}
		
		ConfigUnit unit = Unit.getPlayerUnit(p);
		if (unit == null || !unit.id.equals("u_spy")) {
			CivMessage.sendError(p, "Only spies can do missions.");
			return false;
		}
		
		ChunkCoord coord = new ChunkCoord(p.getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		TownChunk tc = CivGlobal.getTownChunk(coord);
		if (cc == null || cc.getCiv() == resident.getCiv()) {
			CivMessage.sendError(p, "You must be in a civilization's culture that's not your own to spy on them.");
			return false;
		}
		
		if ((cc != null && cc.getCiv().isAdminCiv()) || (tc != null && tc.getTown().getCiv().isAdminCiv())) {
			CivMessage.sendError(p, "You cannot spy on an admin civ.");
			return false;
		}
		
		if (CivGlobal.isCasualMode()) {
			if (!cc.getCiv().getDiplomacyManager().isHostileWith(resident.getCiv()) &&
				!cc.getCiv().getDiplomacyManager().atWarWith(resident.getCiv())) {
				CivMessage.sendError(p, "You must be hostile or at war with "+cc.getCiv().getName()+" in order to perform spy missions in casual mode.");
				return false;
			}
		}
		return true;
	}
	
	public void clickBlacksmithSmelter(Player p, InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		if (buildable instanceof Blacksmith) {
			Blacksmith bs = (Blacksmith) buildable;
			switch (event.getCurrentItem().getType()) {
			case MAGMA:
				bs.withdrawSmelts(p);
				p.closeInventory();
				break;
			default:
				break;
			}
		} else {
			event.setCancelled(true);
			CivMessage.sendError(p, "Blacksmith you are trying to access is null? Contact an admin if this continues.");
			p.closeInventory();
			return;
		}
	}
	
	public void clickWarehouseToggle(Player p, InventoryClickEvent event) {
		Resident res = CivGlobal.getResident(p);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		if (buildable instanceof Warehouse) {
			Warehouse wh = (Warehouse) buildable;
			switch (event.getCurrentItem().getType()) {
			case HOPPER:
				if (event.getCurrentItem().getItemMeta().getDisplayName().contains("(Input)")) {
					wh.toggleGotoTrommel();
					if (wh.canGotoTrommel()) {
						CivMessage.sendTown(res.getTown(), p.getName()+" has allowed Warehouse transfering to Trommels.");
					} else {
						CivMessage.sendTown(res.getTown(), p.getName()+" has disabed Warehouse transfering to Trommels.");
					}
					
					String keyTrommel = wh.getInfoKey(wh, "trommel", "input");
					ArrayList<SessionEntry> entryTrommel = CivGlobal.getSessionDB().lookup(keyTrommel);
					if (entryTrommel != null && !entryTrommel.isEmpty()) {
						SessionEntry se = entryTrommel.get(0);
						CivGlobal.getSessionDB().update(se.request_id, se.key, String.valueOf(wh.canGotoTrommel()));
					} else {
						wh.sessionAdd(wh.getInfoKey(wh, "trommel", "input"), String.valueOf(wh.canGotoTrommel()));
					}
				} else {
					wh.toggleCollectTrommel();
					if (wh.canCollectTrommel()) {
						CivMessage.sendTown(res.getTown(), p.getName()+" has changed Warehouse collection for Trommels to Warehouse.");
					} else {
						CivMessage.sendTown(res.getTown(), p.getName()+" has changed Warehouse collection for Trommels to Itself.");
					}
					
					String keyTrommel = wh.getInfoKey(wh, "trommel", "output");
					ArrayList<SessionEntry> entryTrommel = CivGlobal.getSessionDB().lookup(keyTrommel);
					if (entryTrommel != null && !entryTrommel.isEmpty()) {
						SessionEntry se = entryTrommel.get(0);
						CivGlobal.getSessionDB().update(se.request_id, se.key, String.valueOf(wh.canCollectTrommel()));
					} else {
						wh.sessionAdd(wh.getInfoKey(wh, "trommel", "output"), String.valueOf(wh.canCollectTrommel()));
					}
				}
				p.closeInventory();
				break;
			case COBBLE_WALL:
				wh.toggleCollectQuarry();
				if (wh.getQuarryCollector() == 2) {
					CivMessage.sendTown(res.getTown(), p.getName()+" has changed Warehouse collection for Quarries to Trommels.");
				} else if (wh.getQuarryCollector() == 1) {
					CivMessage.sendTown(res.getTown(), p.getName()+" has changed Warehouse collection for Quarries to Warehouse.");
				} else if (wh.getQuarryCollector() == 0) {
					CivMessage.sendTown(res.getTown(), p.getName()+" has changed Warehouse collection for Quarries to Itself.");
				}
				
				String keyQuarry = wh.getInfoKey(wh, "quarry", "output");
				ArrayList<SessionEntry> entryQuarry = CivGlobal.getSessionDB().lookup(keyQuarry);
				if (entryQuarry != null && !entryQuarry.isEmpty()) {
					SessionEntry se = entryQuarry.get(0);
					CivGlobal.getSessionDB().update(se.request_id, se.key, String.valueOf(wh.getQuarryCollector()));
				} else {
					wh.sessionAdd(wh.getInfoKey(wh, "quarry", "output"), String.valueOf(wh.getQuarryCollector()));
				}
				p.closeInventory();
				break;
			default:
				break;
			}
		} else {
			event.setCancelled(true);
			CivMessage.sendError(p, "Warehouse you are trying to access is null? Contact an admin if this continues.");
			p.closeInventory();
			return;
		}
	}
	
	public void clickMineTask1(Player p, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		switch (event.getCurrentItem().getType()) {
		case LIME_SHULKER_BOX:
			if (!event.getCurrentItem().hasItemMeta()) {
				CivMessage.sendError(p, "This task had an error, try again or contact an admin.");
				p.closeInventory();
			}
			
			ItemMeta meta = event.getCurrentItem().getItemMeta();
			String taskName = ChatColor.stripColor(meta.getDisplayName()).replace("[Available] Task ", "");
			int task = Integer.parseInt(taskName);
			
			Resident res = CivGlobal.getResident(p);
			Mine mine = (Mine) res.getTown().getStructureByType("ti_mine");
			mine.openTaskCompleterGUI(p, res.getTown(), task);
			break;
		case RED_SHULKER_BOX:
			CivMessage.sendError(p, "You already completed this task, it will be re-opened at upkeep!");
			p.closeInventory();
			break;
		case BLACK_SHULKER_BOX:
			CivMessage.sendError(p, "This task is currently locked, upgrade your town to unlock it!");
			p.closeInventory();
			break;
		case AIR:
			break;
		default:
			break;
		}
	}
	
	public void clickLabTask1(Player p, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
			event.setCancelled(true);
		}
		
		switch (event.getCurrentItem().getType()) {
		case LIME_SHULKER_BOX:
			if (!event.getCurrentItem().hasItemMeta()) {
				CivMessage.sendError(p, "This task had an error, try again or contact an admin.");
				p.closeInventory();
			}
			
			ItemMeta meta = event.getCurrentItem().getItemMeta();
			String taskName = ChatColor.stripColor(meta.getDisplayName()).replace("[Available] Task ", "");
			int task = Integer.parseInt(taskName);
			
			Resident res = CivGlobal.getResident(p);
			Lab lab = (Lab) res.getTown().getStructureByType("ti_lab");
			lab.openTaskCompleterGUI(p, res.getTown(), task);
			break;
		case RED_SHULKER_BOX:
			CivMessage.sendError(p, "You already completed this task, it will be re-opened at upkeep!");
			p.closeInventory();
			break;
		case BLACK_SHULKER_BOX:
			CivMessage.sendError(p, "This task is currently locked, upgrade your town to unlock it!");
			p.closeInventory();
			break;
		case AIR:
			break;
		default:
			break;
		}
	}
	
	// XXX Inventory Closing
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) throws SQLException {
		Inventory inv = event.getInventory();
		Player p = (Player) event.getPlayer();
		Resident res = CivGlobal.getResident(p);
		if (!res.hasTown() || res == null) {
			return;
		}
		
		if (inv.getName().contains("[R] Mail ")) {
			this.closeResOpenMailPackage(p, inv);
		}
		
		//Town Hall Inv (Building Support)
		if (inv.getName().contains("'s Building Support")) {
			this.checkTownBuildingSupport(p, inv);
		}		
		
		//Bank Inv
		if (inv.getName().contains("'s Bank Desk")) {
			this.sellBankItems(p, inv);
		}
		
		//Blacksmith Smelt Inv
		if (inv.getName().contains("'s Smelter Operator")) {
			this.smeltBlacksmithItems(p, inv);
		}
		
		//Barracks Repair Inv
		if (inv.getName().contains("'s Repair Master")) {
			this.repairItem(p, inv, event);
		}
		
		//Mine Inv2
		if (inv.getName().contains(res.getTown().getName()+" Mine Task ")) {
			this.completeMineTask(p, inv);
		}
		
		//Mine Inv2
		if (inv.getName().contains(res.getTown().getName()+" Lab Task ")) {
			this.completeLabTask(p, inv);
		}
		
		//Task Inv1 (Granary, Mine, )
		if (event.getInventory().getName().contains(res.getTown().getName()+"'s Granary Tasks") || event.getInventory().getName().contains(res.getTown().getName()+"'s Mine Tasks") ||
				event.getInventory().getName().contains(res.getTown().getName()+"'s Lab Tasks")) {
			this.closeTaskInv1(p, inv);
		}
		
		//Market Inv1
		if (event.getInventory().getName().contains("Global Market Menu")) {
			this.closeMarketInv1(p, inv);
		}
		
		//Storage Inv2
		if (event.getInventory().getName().contains(res.getTown().getName()+" Storage (")) {
			this.collectGranaryStorage(p, inv);
		}
		
		//Granary Inv2
		if (inv.getName().contains(res.getTown().getName()+" Granary Task ")) {
			Granary granary = null;
			Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
			if (buildable instanceof Granary) {
				granary = (Granary) buildable;
			} else {
				CivMessage.sendError(p, "Granary you are trying to access is null? Contact an admin if this continues.");
			}
			
			if (granary == null || granary.getTown() != res.getTown()) {
				CivMessage.sendError(p, "Granary you are trying to access is null? Contact an admin if this continues.");
				return;
			}
			
			Town t = granary.getTown();
			String taskName = ChatColor.stripColor(inv.getName()).replace(t.getName()+" Granary Task ", "");
			int task = Integer.parseInt(taskName);
			
			ConfigGranaryTask gtasks = CivSettings.granaryTasks.get(task);
			boolean addedNotRequiredItems = false;
			int breadGiven = 0;
			int breadRequired = gtasks.required;
			double cultureReward = gtasks.reward;
			
			for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
				if (stack == null || stack.getType() == Material.AIR) {
				continue;
				}
				
//				if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) {
//					event.setCancelled(true);
//				}
				
				if (stack.hasItemMeta() && stack.getItemMeta().getDisplayName().contains("Requirements")) {
					inv.removeItem(stack);
					continue;
				}
				
				LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
				if (stack != null && stack.getType() == Material.BREAD) { //Collect all the bread found in the GUI
					breadGiven += stack.getAmount();
					inv.removeItem(stack);
				} else if (craftMat != null) { //Allow custom items to be dropped
					ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
					newMat.setData(stack.getData());
					ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
					addedNotRequiredItems = true;
				} else if (craftMat == null) { //Drop any vanilla items in the inventory
					ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
					addedNotRequiredItems = true;
				}
			}
			
			if (breadGiven >= breadRequired) { //Drop extra bread collected
				int breadToDrop = breadGiven - breadRequired;
				t.addAccumulatedCulture(cultureReward);
				CivMessage.sendTown(t, "Our town has completed granary task "+task+" and earned "+cultureReward+" culture!");
				granary.sessionAdd(granary.getTaskKey(granary, "task"+task), "complete");
				
				if (breadToDrop > 0) {
					for (int i = 0; i < breadToDrop; i++) {
						ItemStack newMat = new ItemStack(Material.BREAD);
						ItemManager.givePlayerItem(p, newMat, p.getEyeLocation(), newMat.getItemMeta().getDisplayName(), newMat.getAmount(), false);
						addedNotRequiredItems = true;
					}
				}
			} else if (breadGiven < breadRequired && breadGiven > 0) { //Drop anything that cannot complete the task
				CivMessage.sendError(p, "Not enough bread to complete the task! Dropping what you deposited back on the ground.");
				for (int i = 0; i < breadGiven; i++) {
					ItemStack newMat = new ItemStack(Material.BREAD);
					ItemManager.givePlayerItem(p, newMat, p.getEyeLocation(), newMat.getItemMeta().getDisplayName(), newMat.getAmount(), false);
					addedNotRequiredItems = true;
				}
			} else if (breadGiven <= 0) {
				CivMessage.sendError(p, "You need to deposit bread in order to do a granary task!");
			}
			
			if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
		}
	}
	
	public void closeMarketInv1(Player p, Inventory inv) {
		for (ItemStack stack : inv.getContents().clone()) {
			inv.remove(stack);
		}
	}
	
	public void closeTaskInv1(Player p, Inventory inv) {
		boolean addedNotRequiredItems = false;
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) {
				continue;
			}
			
			if (stack.hasItemMeta() && stack.getItemMeta().getDisplayName().contains("Information")) {
				inv.removeItem(stack);
				continue;
			}
			
			if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().contains("[Available] Task ") ||
					stack.hasItemMeta() && stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().contains("[Completed] Task ") ||
					stack.hasItemMeta() && stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().contains("[Locked] Task ")) {
				inv.removeItem(stack);
				continue;
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			} else if (craftMat == null) { //Drop any vanilla items in the inventory
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			}
			if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
		}
	}
	
	public void collectGranaryStorage(Player p, Inventory inv) {
		Granary granary = null;
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		if (buildable instanceof Granary) {
			granary = (Granary) buildable;
		} else {
			CivMessage.sendError(p, "Granary you are trying to access is null? Contact an admin if this continues.");
		}
		
		Town t = granary.getTown();
		String material = ChatColor.stripColor(inv.getName()).replace(t.getName()+" Storage (" , "").replace(")", "").replaceAll(" ", "_").toLowerCase();
		if (material.contains("carrot") || material.contains("potato")) material += "_item";
		Material mat = ItemManager.getMaterial(material.toUpperCase());
		boolean addedNotRequiredItems = false;
		int matGiven = 0;
		
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			if (stack.hasItemMeta()) {
				ItemMeta m = stack.getItemMeta();
				if (m.hasDisplayName()) {
					String d = m.getDisplayName();
					if (d.contains("Information")) {
						inv.removeItem(stack);
						continue;
					}
				}
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (stack.getType() == mat) {
				matGiven += stack.getAmount();
			} else if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			} else if (craftMat == null) { //Drop any vanilla items in the inventory
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			}
		}
		
		
		if (matGiven > 0) {
			String key = granary.getStorageKey(granary, material);
			ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
			if (entry != null && !entry.isEmpty()) {
				SessionEntry se = entry.get(0);
				ConfigGranaryLevel gl = CivSettings.granaryLevels.get(granary.getLevel());
				
				if (matGiven > 0) {
					int toAdd = 0;
					int toDrop = 0;
					boolean isMaxed = false;
					int value = Integer.valueOf(se.value);
					
					int adding = (value + matGiven);
					if (value >= gl.max_storage) {
						isMaxed = true;
						toDrop = matGiven;
					} else if (adding > gl.max_storage) {
						//CivMessage.global("PreTotal "+adding);
						int tdrop = adding - gl.max_storage;
						//CivMessage.global("To Drop "+tdrop);
						toDrop = tdrop;
						matGiven -= toDrop;
						toAdd = matGiven;
						//CivMessage.global("To Add "+toAdd);
						CivMessage.send(p, "You just filled up this item's reserved inventory.");
					} else {
						toAdd = matGiven;
					}
					
					if (toAdd > 0) {
						String nv = ""+(value + toAdd);
						CivGlobal.getSessionDB().update(se.request_id, se.key, nv);
						String mn = material.toString().substring(0, 1).toUpperCase()+material.toString().substring(1).toLowerCase().replace("_", " ").replace(" item", "");
						CivMessage.sendTown(t, p.getName()+" has deposited "+toAdd+" "+mn+" into the town granary! There is currently "+(gl.max_storage-Integer.valueOf(se.value))+" items of room left for this type.");
					}
					
					if (toDrop > 0) {
						for (int i = 0; i < toDrop; i++) {
							ItemStack stack = new ItemStack(mat);
							ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
							addedNotRequiredItems = true;
						}
					}
					
					if (isMaxed) {
						CivMessage.sendError(p, "The granary is maxed, cannot add anymore items to it!");
					}
				} else {
					CivLog.warning("Error trying to process request for "+granary.getCenterLocation().toString()+" in town "+t.getName());
					CivMessage.sendError(p, "Error trying to process request, contact an admin.");
				}
			} else {
				CivLog.warning("Error trying to insert "+material+" for key "+key+" ... for granary "+granary.getCenterLocation().toString()+" in town "+t.getName());
				CivMessage.sendError(p, "Error trying to insert "+material+" for key "+key+", contact an admin.");
			}
		} else {
//			CivMessage.send(p, material);
			CivMessage.sendError(p, "Please insert items in order to store them.");
		}
		
/*		if (matGiven > 0) {
			String key = granary.getStorageKey(granary, material);
			ArrayList<SessionEntry> entry = CivGlobal.getSessionDB().lookup(key);
			if (entry != null && !entry.isEmpty()) {
				SessionEntry se = entry.get(0);
				String nv = ""+(Integer.valueOf(se.value) + matGiven);
				CivGlobal.getSessionDB().update(se.request_id, se.key, nv);
				String mn = material.toString().substring(0, 1).toUpperCase()+material.toString().substring(1).toLowerCase().replace("_", " ").replace(" item", "");
				CivMessage.sendTown(t, p.getName()+" has deposited "+matGiven+" "+mn+" into the town granary!");
			} else {
				CivLog.warning("Error trying to insert "+material+" for key "+key+" ... for granary "+granary.getCenterLocation().toString()+" in town "+t.getName());
				CivMessage.sendError(p, "Error trying to insert "+material+" for key "+key);
			}
		} else {
			CivMessage.send(p, material);
			CivMessage.sendError(p, "Please insert items in order to store them.");
		}*/
		
		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
	
	public void checkTownBuildingSupport(Player p, Inventory inv) {
		Resident res = CivGlobal.getResident(p);
		TownHall th = (TownHall) res.getTown().getStructureByType("s_townhall");
		if (th == null) {
			th = (TownHall) res.getTown().getStructureByType("s_capitol");
			if (th == null) {
				CivMessage.sendError(p, "Town Hall null? Contact an admin if this is wrong!"); p.closeInventory();
			}
		}
		
		boolean addedNotRequiredItems = false;
		int dirt = 0;
		int gravel = 0;
		
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			if (stack.hasItemMeta()) {
				ItemMeta m = stack.getItemMeta();
				if (m.hasDisplayName()) {
					String d = m.getDisplayName();
					if ((stack.getType() == Material.PAPER && (d.contains("Information") || d.contains("Back"))) ||
							(stack.getType() == Material.BARRIER && d.contains("[Admin]"))) {
						inv.removeItem(stack); continue;
					}
				}
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (stack.getType() == Material.DIRT && craftMat == null) {
				dirt += stack.getAmount();
				inv.removeItem(stack);
			} else if (stack.getType() == Material.GRAVEL && craftMat == null) {
				gravel += stack.getAmount();
				inv.removeItem(stack);
			} else if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			} else if (craftMat == null) { //Drop any vanilla items in the inventory
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			}
		}
		
		Town t = th.getTown();
		if (dirt > 0) {
			t.addSupportDeposit(dirt);
			t.save();
			CivMessage.sendTown(t, "Added "+dirt+" dirt to town, thanks to "+p.getName()+"! We now have a total of "+t.getSupportDeposit()+" blocks stored.");
		}
		
		if (gravel > 0) {
			t.addSupportDeposit(gravel);
			t.save();
			CivMessage.sendTown(t, "Added "+gravel+" gravel to town, thanks to "+p.getName()+"! We now have a total of "+t.getSupportDeposit()+" blocks stored.");
		}
		
		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
	
	public void sellBankItems(Player p, Inventory inv) {
		Resident res = CivGlobal.getResident(p);
		Bank bank = (Bank) res.getTown().getStructureByType("s_bank");
		if (bank == null) {
			CivMessage.sendError(p, "Bank null? Contact an admin if this is wrong!"); p.closeInventory();
		}
		
		Town bTown = bank.getTown();
		boolean addedNotRequiredItems = false;
		Map<String, Integer> mats = new HashMap<String, Integer>();
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			if (stack.hasItemMeta()) {
				ItemMeta m = stack.getItemMeta();
				if (m.hasDisplayName()) {
					String d = m.getDisplayName();
					if ((stack.getType() == Material.PAPER && (d.contains("Information") || d.contains("Sell Values"))) || 
							(stack.getType() == Material.STAINED_GLASS_PANE && d.contains("Inventory Border"))) {
						inv.removeItem(stack); continue;
					}
				}
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			} else { //Drop any vanilla items in the inventory after check
				if (stack.getType() == Material.IRON_INGOT || stack.getType() == Material.GOLD_INGOT || stack.getType() == Material.DIAMOND || stack.getType() == Material.EMERALD) {
					if (mats.get(stack.getType().toString()) != null) mats.put(stack.getType().toString(), mats.get(stack.getType().toString())+stack.getAmount());
					else mats.put(stack.getType().toString(), stack.getAmount());
					inv.removeItem(stack);
				} else {
					ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
					addedNotRequiredItems = true;
				}
			}
		}
		
		bank.updateExchangeRate();
		for (String g : mats.keySet()) {
			for (String b : bank.rates.keySet()) {
				if (b.contains(g)) {
					String name = CivData.getDisplayName(ItemManager.getId(ItemManager.getMaterial(g)), 0);
					double sold = mats.get(g)*bank.rates.get(b);
					if (res.getTown() == bTown) { //Resident in this town, no fee
						res.getTreasury().deposit(sold);
						CivMessage.send(res, CivColor.LightGreen + "Exchanged "+mats.get(g)+" "+name+" for "+sold+ " coins at "+bank.getExchangeRateString()+" rate.");
					} else { // non-resident must pay the town's non-resident tax
						int giveToPlayer = (int)(mats.get(g)*(sold));
						int giveToTown = (int)(giveToPlayer*bank.getNonResidentFee());
						giveToPlayer -= giveToTown;
						
						bTown.deposit(giveToTown);
						res.getTreasury().deposit(giveToPlayer);
						CivMessage.send(res, CivColor.LightGreen + "Exchanged "+mats.get(g)+" "+name+" for "+ giveToPlayer+ " coins at "+bank.getExchangeRateString()+" rate.");
						CivMessage.send(res,CivColor.Yellow+" Paid "+giveToTown+" coins in non-resident taxes.");
					}
				}
			}
		}
		
		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
	
	
	public void smeltBlacksmithItems(Player p, Inventory inv) {
		Blacksmith bs = null;
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		if (buildable instanceof Blacksmith) {
			bs = (Blacksmith) buildable;
		} else {
			CivMessage.sendError(p, "Blacksmith you are trying to access is null? Contact an admin if this continues.");
			return;
		}
		
		boolean addedNotRequiredItems = false;
		int ioGiven = 0;
		int goGiven = 0;
		int loGiven = 0;
		int roGiven = 0;
		int sandGiven = 0;
		int cactusGiven = 0;
		
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			if (stack.hasItemMeta()) {
				ItemMeta m = stack.getItemMeta();
				if (m.hasDisplayName()) {
					String d = m.getDisplayName();
					if ((stack.getType() == Material.PAPER && (d.contains("Information"))) || (stack.getType() == Material.MAGMA && (d.contains("Current Smelts"))) || 
							(stack.getType() == Material.STAINED_GLASS_PANE && d.contains("Inventory Border"))) {
						inv.removeItem(stack); continue;
					}
				}
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (stack.getType() == Material.IRON_ORE && craftMat == null) { //Collect all iron ore found in the GUI
				ioGiven += stack.getAmount();
				inv.removeItem(stack);
			} else if (stack.getType() == Material.GOLD_ORE && craftMat == null) { //Collect all gold ore found in the GUI
				goGiven += stack.getAmount();
				inv.removeItem(stack);
			} else if (stack.getType() == Material.LAPIS_ORE && craftMat == null) { //Collect all lapis ore found in the GUI
				loGiven += stack.getAmount();
				inv.removeItem(stack);
			} else if (stack.getType() == Material.REDSTONE_ORE && craftMat == null) { //Collect all redstone ore found in the GUI
				roGiven += stack.getAmount();
				inv.removeItem(stack);
			} else if (stack.getType() == Material.SAND && craftMat == null) { //Collect all redstone ore found in the GUI
				sandGiven += stack.getAmount();
				inv.removeItem(stack);
			} else if (stack.getType() == Material.CACTUS && craftMat == null) { //Collect all redstone ore found in the GUI
				cactusGiven += stack.getAmount();
				inv.removeItem(stack);
			} else if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			} else { //Drop any vanilla items in the inventory
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			}
		}
		
		if (ioGiven > 0) {
			bs.depositSmelt(p, Material.IRON_ORE, ioGiven, 0);
		}
		
		if (goGiven > 0) {
			bs.depositSmelt(p, Material.GOLD_ORE, goGiven, 0);
		}
		
		if (loGiven > 0) {
			int loSmelt = loGiven*4;
			bs.depositSmelt(p, Material.LAPIS_ORE, loSmelt, 4);
		}
		
		if (roGiven > 0) {
			int roSmelt = roGiven*4;
			bs.depositSmelt(p, Material.REDSTONE_ORE, roSmelt, 0);
		}
		
		if (sandGiven > 0) {
			bs.depositSmelt(p, Material.SAND, sandGiven, 0);
		}
		
		if (cactusGiven > 0) {
			bs.depositSmelt(p, Material.CACTUS, cactusGiven, 2);
		}
		
		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
	
	
	public void repairItem(Player p, Inventory inv, InventoryCloseEvent event) {
		Resident res = CivGlobal.getResident(p);
		Barracks b = (Barracks) res.getTown().getStructureByType("s_barracks");
		if (b == null) {
			CivMessage.sendError(p, "Barracks null? Contact an admin if this is wrong!"); p.closeInventory();
		}
		
		boolean addedNotRequiredItems = false;
		ItemStack repair = null;
		
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			if (stack.hasItemMeta()) {
				ItemMeta m = stack.getItemMeta();
				if (m.hasDisplayName()) {
					String d = m.getDisplayName();
					if ((stack.getType() == Material.PAPER && (d.contains("Information"))) || (stack.getType() == Material.WORKBENCH && (d.contains("Repairable Items"))) || 
							(stack.getType() == Material.BARRIER && d.contains("�--")) || (stack.getType() == Material.ANVIL && d.contains("Close Inventory To Repair"))) {
						inv.removeItem(stack); continue;
					}
				}
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (b.canRepairItem(stack)) { // Repair items that can be repaired
				repair = stack;
				inv.removeItem(stack);
			} else {
				if (craftMat != null) { // Allow custom items to be dropped
					ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
					newMat.setData(stack.getData());
					ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
					addedNotRequiredItems = true;
				} else { // Drop any vanilla items in the inventory
					ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
					addedNotRequiredItems = true;
				}
			}
		}
		
		if (repair != null) {
			b.repairItemCalculate(p, res, repair);
		} else {
			CivMessage.sendError(p, "Please insert an item to repair! (If an item did not repair, re-check the requirements of repairing.)");
		}
		
		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
	
	
	public void completeMineTask(Player p, Inventory inv) {
		Resident res = CivGlobal.getResident(p);
		Mine mine = (Mine) res.getTown().getStructureByType("ti_mine");
		
		Town t = mine.getTown();
		String taskName = ChatColor.stripColor(inv.getName()).replace(t.getName()+" Mine Task ", "");
		int task = Integer.parseInt(taskName);
		
		ConfigMineTask mtask = CivSettings.mineTasks.get(task);
		boolean addedNotRequiredItems = false;
		double reward = mtask.reward;
		
		Map<ArrayList<String>, Integer> given = new HashMap<ArrayList<String>, Integer>();
		Map<ArrayList<String>, Integer> required = new HashMap<ArrayList<String>, Integer>();
		Map<ArrayList<String>, Integer> returning = new HashMap<ArrayList<String>, Integer>();
		Map<ArrayList<String>, Integer> dropping = new HashMap<ArrayList<String>, Integer>();
		
		for (ArrayList<String> item : mtask.required.keySet()) {
//			for (String s : item) {
//				String[] split = s.split(";");
				required.put(item, mtask.required.get(item).intValue()); given.put(item, 0);
//				CivMessage.global("Required: id"+Integer.valueOf(split[0])+" amt"+mtask.required.get(item).intValue()+" data"+Integer.valueOf(split[1]));
//			}
		}
		
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			
			if (stack.hasItemMeta() && stack.getItemMeta().getDisplayName().contains("Requirements")) {
				inv.removeItem(stack); continue;
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			ArrayList<String> al = new ArrayList<String>();
			al.add(ItemManager.getId(stack)+";"+stack.getDurability());
			if (required.containsKey(al) && craftMat == null) {
				inv.removeItem(stack);
				int h = given.get(al).intValue();
				h += stack.getAmount();
				given.put(al, h);
//				CivMessage.global("Deposit Add: id"+ItemManager.getId(stack)+" data"+stack.getDurability()+" amt"+stack.getAmount()+" newamt"+h);
			} else if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				CivMessage.sendError(p, craftMat.getName()+","+stack.getAmount());
				addedNotRequiredItems = true;
			} else if (craftMat == null) { //Drop any vanilla items in the inventory
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			}
		}
		
		inv.clear();
		boolean canComplete = true;
		for (ArrayList<String> r : required.keySet()) {
			for (String s1 : r) {
				String[] rsplit = s1.split(";");
				int rid = Integer.valueOf(rsplit[0]);
				int rdata = Integer.valueOf(rsplit[1]);
				for (ArrayList<String> g : given.keySet()) {
					for (String s2 : g) {
						String[] gsplit = s2.split(";");
						int gid = Integer.valueOf(gsplit[0]);
						int gdata = Integer.valueOf(gsplit[1]);
//						CivMessage.global("Reading "+gid+","+gdata+" v "+rid+","+rdata);
						if (gid == rid && gdata == rdata) {
							int ramt = required.get(r).intValue();
							int gamt = given.get(g).intValue();
							if (gamt >= ramt) {
//								CivMessage.global("Yes Complete: "+rid+","+rdata+" - "+gamt+" >= "+ramt);
								int td = gamt-ramt;
								if (td > 0) {
//									CivMessage.global("Yes Dropping: "+td);
									dropping.put(r, td);
								}
							} else {
//								CivMessage.global("Yes Return: "+rid+","+rdata+" - "+gamt+" < "+ramt);
								int amt = ramt - (ramt-gamt);
								returning.put(r, amt);
								canComplete = false;
							}
						}
					}
				}
			}
		}
		
		if (canComplete) { // Give Rewards, state rewards
			for (int i = 0; i < reward; i++) {
				ItemStack newMat = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hammers"));
				ItemManager.givePlayerItem(p, newMat, p.getEyeLocation(), newMat.getItemMeta().getDisplayName(), newMat.getAmount(), false);
			}
			
			CivMessage.sendTown(t, p.getName()+" has completed mine task "+task+" and earned "+reward+" hammers!");
			mine.sessionAdd(mine.getKey(mine, "task"+task), "complete");
			for (ArrayList<String> d : dropping.keySet()) {
				for (String s : d) {
					String[] split = s.split(";");
					int id = Integer.valueOf(split[0]);
					int data = Integer.valueOf(split[1]);
					int amt = dropping.get(d).intValue();
					addedNotRequiredItems = true;
					for (int i = 0; i < amt; i++) {
						ItemStack drop = new ItemStack(ItemManager.getMaterial(id), 1, (short) data);
						ItemManager.givePlayerItem(p, drop, p.getEyeLocation(), drop.getItemMeta().getDisplayName(), drop.getAmount(), false);
					}
				}
			}
		} else { // Give items back, state missing items
			for (ArrayList<String> d : dropping.keySet()) {
				for (String s1 : d) {
					String[] dsplit = s1.split(";");
					int did = Integer.valueOf(dsplit[0]);
					int ddata = Integer.valueOf(dsplit[1]);
					int damt = dropping.get(d).intValue();
					for (ArrayList<String> r : required.keySet()) {
						for (String s2 : r) {
							String[] rsplit = s2.split(";");
							int rid = Integer.valueOf(rsplit[0]);
							int rdata = Integer.valueOf(rsplit[1]);
							int ramt = required.get(r).intValue();
							if (rid == did && rdata == ddata) {
								damt += ramt;
							}
						}
					}
					
					for (int i = 0; i < damt; i++) {
						ItemStack drop = new ItemStack(ItemManager.getMaterial(did), 1, (short) ddata);
						ItemManager.givePlayerItem(p, drop, p.getEyeLocation(), drop.getItemMeta().getDisplayName(), drop.getAmount(), false);
					}
				}
			}
			
			String itemsDropping = "";
			for (ArrayList<String> r : returning.keySet()) {
				for (String s1 : r) {
					String[] rsplit = s1.split(";");
					int rid = Integer.valueOf(rsplit[0]);
					int rdata = Integer.valueOf(rsplit[1]);
					int ramt = returning.get(r).intValue();
					int missing = 0;
					for (ArrayList<String> q : required.keySet()) {
						for (String s2 : q) {
							String[] qsplit = s2.split(";");
							int qid = Integer.valueOf(qsplit[0]);
							int qdata = Integer.valueOf(qsplit[1]);
							int qamt = required.get(q).intValue();
							
							if (qid == rid && qdata == rdata) {
								missing = qamt - ramt;
							}
						}
					}
					
					itemsDropping += missing+" "+CivData.getDisplayName(rid, rdata)+", ";
					for (int i = 0; i < ramt; i++) {
						ItemStack miss = new ItemStack(ItemManager.getMaterial(rid), 1, (short) rdata);
						ItemManager.givePlayerItem(p, miss, p.getEyeLocation(), miss.getItemMeta().getDisplayName(), miss.getAmount(), false);
					}
				}
			}
			CivMessage.sendError(p, "Cannot complete task, you were missing the following items: "+itemsDropping+"... Dropping these items back on the ground.");
		}
		
		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
	
	
	public void completeLabTask(Player p, Inventory inv) {
		Resident res = CivGlobal.getResident(p);
		Lab lab = (Lab) res.getTown().getStructureByType("ti_lab");
		
		Town t = lab.getTown();
		String taskName = ChatColor.stripColor(inv.getName()).replace(t.getName()+" Lab Task ", "");
		int task = Integer.parseInt(taskName);
		
		ConfigLabTask ltask = CivSettings.labTasks.get(task);
		boolean addedNotRequiredItems = false;
		double reward = ltask.reward;
		
		Map<ArrayList<String>, Integer> given = new HashMap<ArrayList<String>, Integer>();
		Map<ArrayList<String>, Integer> required = new HashMap<ArrayList<String>, Integer>();
		Map<ArrayList<String>, Integer> returning = new HashMap<ArrayList<String>, Integer>();
		Map<ArrayList<String>, Integer> dropping = new HashMap<ArrayList<String>, Integer>();
		
		for (ArrayList<String> item : ltask.required.keySet()) {
//			for (String s : item) {
//				String[] split = s.split(";");
				required.put(item, ltask.required.get(item).intValue()); given.put(item, 0);
//				CivMessage.global("Required: id"+Integer.valueOf(split[0])+" amt"+ltask.required.get(item).intValue()+" data"+Integer.valueOf(split[1]));
//			}
		}
		
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			
			if (stack.hasItemMeta() && stack.getItemMeta().getDisplayName().contains("Requirements")) {
				inv.removeItem(stack); continue;
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			ArrayList<String> al = new ArrayList<String>();
			al.add(ItemManager.getId(stack)+";"+stack.getDurability());
			if (required.containsKey(al) && craftMat == null) {
				inv.removeItem(stack);
				int h = given.get(al).intValue();
				h += stack.getAmount();
				given.put(al, h);
//				CivMessage.global("Deposit Add: id"+ItemManager.getId(stack)+" data"+stack.getDurability()+" amt"+stack.getAmount()+" newamt"+h);
			} else if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				CivMessage.sendError(p, craftMat.getName()+","+stack.getAmount());
				addedNotRequiredItems = true;
			} else if (craftMat == null) { //Drop any vanilla items in the inventory
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			}
		}
		
		inv.clear();
		boolean canComplete = true;
		for (ArrayList<String> r : required.keySet()) {
			for (String s1 : r) {
				String[] rsplit = s1.split(";");
				int rid = Integer.valueOf(rsplit[0]);
				int rdata = Integer.valueOf(rsplit[1]);
				for (ArrayList<String> g : given.keySet()) {
					for (String s2 : g) {
						String[] gsplit = s2.split(";");
						int gid = Integer.valueOf(gsplit[0]);
						int gdata = Integer.valueOf(gsplit[1]);
//						CivMessage.global("Reading "+gid+","+gdata+" v "+rid+","+rdata);
						if (gid == rid && gdata == rdata) {
							int ramt = required.get(r).intValue();
							int gamt = given.get(g).intValue();
							if (gamt >= ramt) {
//								CivMessage.global("Yes Complete: "+rid+","+rdata+" - "+gamt+" >= "+ramt);
								int td = gamt-ramt;
								if (td > 0) {
//									CivMessage.global("Yes Dropping: "+td);
									dropping.put(r, td);
								}
							} else {
//								CivMessage.global("Yes Return: "+rid+","+rdata+" - "+gamt+" < "+ramt);
								int amt = ramt - (ramt-gamt);
								returning.put(r, amt);
								canComplete = false;
							}
						}
					}
				}
			}
		}
		
		if (canComplete) { // Give Rewards, state rewards
			for (int i = 0; i < reward; i++) {
				ItemStack newMat = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_beakers"));
				ItemManager.givePlayerItem(p, newMat, p.getEyeLocation(), newMat.getItemMeta().getDisplayName(), newMat.getAmount(), false);
			}
			
			CivMessage.sendTown(t, p.getName()+" has completed lab task "+task+" and earned "+reward+" beakers!");
			lab.sessionAdd(lab.getKey(lab, "task"+task), "complete");
			for (ArrayList<String> d : dropping.keySet()) {
				for (String s : d) {
					String[] split = s.split(";");
					int id = Integer.valueOf(split[0]);
					int data = Integer.valueOf(split[1]);
					int amt = dropping.get(d).intValue();
					addedNotRequiredItems = true;
					for (int i = 0; i < amt; i++) {
						ItemStack drop = new ItemStack(ItemManager.getMaterial(id), 1, (short) data);
						ItemManager.givePlayerItem(p, drop, p.getEyeLocation(), drop.getItemMeta().getDisplayName(), drop.getAmount(), false);
					}
				}
			}
		} else { // Give items back, state missing items
			for (ArrayList<String> d : dropping.keySet()) {
				for (String s1 : d) {
					String[] dsplit = s1.split(";");
					int did = Integer.valueOf(dsplit[0]);
					int ddata = Integer.valueOf(dsplit[1]);
					int damt = dropping.get(d).intValue();
					for (ArrayList<String> r : required.keySet()) {
						for (String s2 : r) {
							String[] rsplit = s2.split(";");
							int rid = Integer.valueOf(rsplit[0]);
							int rdata = Integer.valueOf(rsplit[1]);
							int ramt = required.get(r).intValue();
							if (rid == did && rdata == ddata) {
								damt += ramt;
							}
						}
					}
					
					for (int i = 0; i < damt; i++) {
						ItemStack drop = new ItemStack(ItemManager.getMaterial(did), 1, (short) ddata);
						ItemManager.givePlayerItem(p, drop, p.getEyeLocation(), drop.getItemMeta().getDisplayName(), drop.getAmount(), false);
					}
				}
			}
			
			String itemsDropping = "";
			for (ArrayList<String> r : returning.keySet()) {
				for (String s1 : r) {
					String[] rsplit = s1.split(";");
					int rid = Integer.valueOf(rsplit[0]);
					int rdata = Integer.valueOf(rsplit[1]);
					int ramt = returning.get(r).intValue();
					int missing = 0;
					for (ArrayList<String> q : required.keySet()) {
						for (String s2 : q) {
							String[] qsplit = s2.split(";");
							int qid = Integer.valueOf(qsplit[0]);
							int qdata = Integer.valueOf(qsplit[1]);
							int qamt = required.get(q).intValue();
							
							if (qid == rid && qdata == rdata) {
								missing = qamt - ramt;
							}
						}
					}
					
					itemsDropping += missing+" "+CivData.getDisplayName(rid, rdata)+", ";
					for (int i = 0; i < ramt; i++) {
						ItemStack miss = new ItemStack(ItemManager.getMaterial(rid), 1, (short) rdata);
						ItemManager.givePlayerItem(p, miss, p.getEyeLocation(), miss.getItemMeta().getDisplayName(), miss.getAmount(), false);
					}
				}
			}
			CivMessage.sendError(p, "Cannot complete task, you were missing the following items: "+itemsDropping+"... Dropping these items back on the ground.");
		}
		
		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
	
	
	
	// Resident Mail System
	
	public void clickResMainMailMenu(Player p, InventoryClickEvent event) {
		if (event.getClickedInventory() == null) return;
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getCurrentItem().getType() == Material.PAPER && event.getInventory().getItem(0).getType() == Material.PAPER) event.setCancelled(true);
		
		Resident res = CivGlobal.getResident(p);
		switch (event.getCurrentItem().getType()) {
		case WHITE_SHULKER_BOX:
			res.openViewMailMenu(p, res, 0);
			break;
		default:
			break;
		}
	}
	
	public void clickResViewMailMenu(Player p, InventoryClickEvent event) {
		if (event.getClickedInventory() == null) return;
		ItemStack is = event.getCurrentItem();
		Resident res = CivGlobal.getResident(p);
		if (is.hasItemMeta()) {
			ItemMeta im = is.getItemMeta();
			if (im.hasDisplayName() && im.hasLore()) {
				String mail_code = im.getDisplayName()+"&MAILCODE@"+im.getLore().get(0).replace("Mail ID: ", "");
				mail_code = ChatColor.stripColor(mail_code);
				if (res.getMails().containsKey(mail_code)) {
					res.openMailPackage(p, res, mail_code);
					res.view_mail = mail_code;
				}
			}
		}
		
/*		switch (event.getCurrentItem().getType()) {
		case WHITE_SHULKER_BOX:
			res.openViewMailMenu(p, res, 0);
			break;
		default:
			break;
		}*/
	}
	
	public void clickResReadMailMenu(Player p, InventoryClickEvent event) {
		if (event.getClickedInventory() == null) return;
		Resident res = CivGlobal.getResident(p);
		ItemStack is = event.getCurrentItem();
		if (is.hasItemMeta()) {
			ItemMeta im = is.getItemMeta();
			if (im.hasDisplayName() && im.hasLore()) {
				if (im.getDisplayName().contains("Collect Mail")) {
					String mail_code = res.view_mail;
					Inventory inv = res.getMail(mail_code);
					res.removeMail(mail_code);
					res.view_mail = null;
					for (ItemStack items : inv.getContents()) {
						if (items == null || items.getType() == Material.AIR || 
								(items.getType() == Material.STAINED_GLASS_PANE && items.getDurability() == 7 && items.getItemMeta().getDisplayName().contains("Inventory Border")) ||
								(items.getType() == Material.PAPER && items.getItemMeta().getDisplayName().contains("Information")) ||
								(items.getType() == Material.PAPER && items.getItemMeta().getDisplayName().contains("Message")) ||
								(items.getType() == Material.CAULDRON_ITEM && items.getItemMeta().getDisplayName().contains("Collect Mail")) ||
								(items.getType() == Material.MINECART && items.getItemMeta().getDisplayName().contains("Forward Mail"))) {
							continue;
						} else {
							ItemManager.givePlayerItem(p, items, p.getEyeLocation(), items.getItemMeta().getDisplayName(), items.getAmount(), false);
						}
					}
					p.closeInventory();
					CivMessage.sendSuccess(p, "Mail package recieved!");
				}
			}
		}
	}
	
	public void closeResOpenMailPackage(Player p, Inventory inv) {
//		boolean addedNotRequiredItems = false;
		
//		int inv_slot = 0;
		Resident res = CivGlobal.getResident(p);
		res.view_mail = null;
		for (ItemStack stack : inv.getContents().clone()) { //Grab the items the player put in the inventory
			if (stack == null || stack.getType() == Material.AIR) continue;
			else inv.removeItem(stack);
/*			if (stack.getType() == Material.STAINED_GLASS_PANE && ((inv_slot > 0 && inv_slot < 9) || (inv_slot > 36 && inv_slot < 45))) {
				inv.removeItem(stack);
				continue;
			}
			
			if (stack.hasItemMeta()) {
				if (stack.getItemMeta().getDisplayName().contains("Collect Mail") || stack.getItemMeta().getDisplayName().contains("Forward Mail")) {
					inv.removeItem(stack);
					continue;
				}
			}
			
			// No drops, we use "Collect Mail" button click to get items out
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat != null) { //Allow custom items to be dropped
				ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
				newMat.setData(stack.getData());
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			} else { //Drop any vanilla items in the inventory
				ItemManager.givePlayerItem(p, stack, p.getEyeLocation(), stack.getItemMeta().getDisplayName(), stack.getAmount(), false);
				addedNotRequiredItems = true;
			}*/
		}
		
//		if (addedNotRequiredItems == true) CivMessage.send(p, CivColor.LightGrayItalic+"We dropped non-required items back on the ground.");
	}
}

