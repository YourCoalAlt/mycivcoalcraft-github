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
package com.avrgaming.civcraft.items.units;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.config.ConfigMission;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.interactive.InteractiveSpyMission;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.MissionLogger;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.Cottage;
import com.avrgaming.civcraft.structure.Granary;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.structure.TradeOutpost;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.util.BookUtil;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.war.War;

public class MissionBook extends UnitItemMaterial {
	
	public MissionBook(String id, int minecraftId, short damage) {
		super(id, minecraftId, damage);
	}
	
	public static double getMissionFailChance(ConfigMission mission, Town town) {
		int onlineResidents = town.getOnlineResidents().size();
		double chance = 1 - mission.fail_chance;
		if (mission.intel != 0) {
			double percentIntel = (double)onlineResidents / (double)mission.intel; 
			if (percentIntel > 1.0) {
				percentIntel = 1.0;
			}
			chance *= percentIntel;
		}
		chance = 1 - chance; // Convert to failure chance
		return chance;
	}
	
	public static double getMissionCompromiseChance(ConfigMission mission, Town town) {
		int onlineResidents = town.getOnlineResidents().size();
		double chance = 1 - mission.compromise_chance;
		if (mission.intel != 0) {
			double percentIntel = (double)onlineResidents / (double)mission.intel; 
			if (percentIntel > 1.0) {
				percentIntel = 1.0;
			}
			chance *= percentIntel;
		}
		chance = 1 - chance; // Convert to failure chance
		return chance;
	}
	
	public void setupLore(String id) {
		ConfigMission mission = CivSettings.missions.get(this.getId());
		if (mission == null) {
			CivLog.warning("Couldn't find mission with id:"+id+" to set the lore.");
			return;
		}
		
		for (String str : mission.description) {
			this.addLore(str);
		}
		this.addLore(CivColor.Yellow+mission.cost+" Coins.");
		this.addLore(CivColor.Gold+"Soulbound");
	}
	
	@Override
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		event.setCancelled(true);
	}
	
	@Override
	public void onInteract(PlayerInteractEvent event) {
		try {
			if (War.isWarTime()) {
				throw new CivException("Cannot use spy missions during war time.");
			}
			
			ConfigMission mission = CivSettings.missions.get(this.getId());
			if (mission == null) {
				throw new CivException("Unknown mission "+this.getId());
			}
			
			Resident resident = CivGlobal.getResident(event.getPlayer());
			if (resident == null || !resident.hasTown()) {
				throw new CivException("Only residents of towns can perform spy missions.");
			}
			
			Date now = new Date();
			if (!event.getPlayer().isOp()) { 
				try {
					int spyRegisterTime = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.spy_register_time");
					int spyOnlineTime = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.spy_online_time");
					long expire = resident.getRegistered() + (spyRegisterTime*60*1000);
					if (now.getTime() <= expire) {
						throw new CivException("You cannot use a spy yet, you must play CivCraft a bit longer before you can use it.");
					}
					
					expire = resident.getLastOnline() + (spyOnlineTime*60*1000);
					if (now.getTime() <= expire) {
						throw new CivException("You must be online longer before you can use a spy.");
					}
				} catch (InvalidConfiguration e) {
					e.printStackTrace();
				}
			}
					
			ConfigUnit unit = Unit.getPlayerUnit(event.getPlayer());
			if (unit == null || !unit.id.equals("u_spy")) {
				event.getPlayer().getInventory().remove(event.getItem());
				throw new CivException("Only spies can use mission books.");
			}
			
			ChunkCoord coord = new ChunkCoord(event.getPlayer().getLocation());
			CultureChunk cc = CivGlobal.getCultureChunk(coord);
			TownChunk tc = CivGlobal.getTownChunk(coord);
			if (cc == null || cc.getCiv() == resident.getCiv()) {
				throw new CivException("You must be in a civilization's culture that's not your own to spy on them.");
			}
			
			if ((cc != null && cc.getCiv().isAdminCiv()) || (tc != null && tc.getTown().getCiv().isAdminCiv())) {
				throw new CivException("You cannot spy on an admin civ.");
			}
			
			if (CivGlobal.isCasualMode()) {
				if (!cc.getCiv().getDiplomacyManager().isHostileWith(resident.getCiv()) &&
					!cc.getCiv().getDiplomacyManager().atWarWith(resident.getCiv())) {
					throw new CivException("You must be hostile or at war with "+cc.getCiv().getName()+" in order to perform spy missions in casual mode.");
				}
			}
			resident.setInteractiveMode(new InteractiveSpyMission(mission, event.getPlayer().getName(), event.getPlayer().getLocation(), cc.getTown()));
		} catch (CivException e) {
			CivMessage.sendError(event.getPlayer(), e.getMessage());
		}
	}
	
	public static void performMission(ConfigMission mission, String playerName) {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e1) {
			return;
		}
		
		try {
			Resident resident = CivGlobal.getResident(playerName);
			if (!resident.getTreasury().hasEnough(mission.cost)) {
				throw new CivException("You require "+mission.cost+" coins to perform this mission.");
			}
			
			switch (mission.id) {
			case "spy_subvert_government":
				performSubvertGovernment(player, mission);
				break;
			case "spy_ravage_technology":
				performRavageTechnology(player, mission);
				break;
			case "spy_investigate_town":
				performInvestigateTown(player, mission);
				break;
			case "spy_steal_treasury":
				performStealTreasury(player, mission);
				break;
			case "spy_incite_riots":
				performInciteRiots(player, mission);
				break;
			case "spy_poison_granary":
				performPosionGranary(player, mission);
				break;
			case "spy_pirate":
				performPirate(player, mission);
				break;
			case "spy_sabotage":
				performSabotage(player, mission);
				break;
			}
		} catch (CivException e) {
			CivMessage.sendError(player, e.getMessage());
		}
	}
	
	private static boolean processMissionResult(Player player, Town target, ConfigMission mission) {
		return processMissionResult(player, target, mission, 1.0, 1.0);
	}
	private static boolean processMissionResult(Player player, Town target, ConfigMission mission, double failModifier, double compromiseModifier) {
		double failChance = (int)((MissionBook.getMissionFailChance(mission, target)*failModifier)*100);
		double compChance = (int)((MissionBook.getMissionCompromiseChance(mission, target)*compromiseModifier)*100);
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			return false;
		}
		
		if (!resident.getTreasury().hasEnough(mission.cost)) {
			CivMessage.send(player, CivColor.Rose+"Suddenly, you do not have enough coins to follow through with the mission.");
			return false;
		}
		
		resident.getTreasury().withdraw(mission.cost);
		
		Random rand = new Random();
		String result = "Success";
		int failnext = rand.nextInt(100);
		int next = rand.nextInt(100);
		if (failnext < failChance) {
			result = "Failed";
			if (next < compChance) {
				CivMessage.global(CivColor.Yellow+"INTERNATIONAL INCIDENT!"+CivColor.White+" "+
						player.getName()+" was caught trying to perform (and failed) a "+mission.name+" spy mission in "+
						target.getName()+"!");
				CivMessage.send(player, CivColor.Rose+"You've been compromised! (Rolled "+next+" vs "+compChance+") Spy unit was destroyed!");
				Unit.removeUnit(player);
				result += ", COMPROMISED";
			} else {
				CivMessage.send(player, CivColor.LightGreen+"Not Compromised! (Rolled "+next+" vs "+compChance+")");
			}
			
			MissionLogger.logMission(resident.getTown(), target, resident, mission.name, result);
			CivMessage.send(player, CivColor.Rose+"Mission Failed! (Rolled "+failnext+" vs "+failChance+")");
			return false;
		}
		
		if (next < compChance) {
			CivMessage.global(CivColor.Yellow+"INTERNATIONAL INCIDENT!"+CivColor.White+" "+
					player.getName()+" was caught trying to perform (and succeeded) a "+mission.name+" spy mission in "+
					target.getName()+"!");
			CivMessage.send(player, CivColor.Rose+"You've been compromised! (Rolled "+next+" vs "+compChance+") Spy unit was destroyed!");
			Unit.removeUnit(player);
			result += ", COMPROMISED";
		} else {
			CivMessage.send(player, CivColor.LightGreen+"Not Compromised! (Rolled "+next+" vs "+compChance+")");
		}
		
		CivMessage.send(player, CivColor.LightGreen+"Mission Success! (Rolled "+failnext+" vs "+failChance+")");
		MissionLogger.logMission(resident.getTown(), target, resident, mission.name, result);
		return true;

	}
	
	private static void performSabotage(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		if (cc == null || cc.getCiv() == resident.getTown().getCiv()) {
			throw new CivException("Must be in another civilization's borders.");
		}
		
		// Check that the player is within range of the town hall.
		Buildable buildable = cc.getTown().getNearestBuildable(player.getLocation());
		if (buildable instanceof TownHall) {
			throw new CivException("Nearest structure is a town hall which cannot be destroyed.");
		}
		if (buildable instanceof Wonder) {
			if (buildable.isComplete()) {
				throw new CivException("Cannot sabotage completed wonders.");
			}
		}
		
		double distance = player.getLocation().distance(buildable.getCorner().getLocation());
		if (distance > mission.range) {
			throw new CivException("Too far away the "+buildable.getDisplayName()+" to sabotage it");
		}
		
		if (buildable instanceof Structure) {
			if (!buildable.isComplete()) {
				throw new CivException("Cannot sabotage incomplete structures.");
			}
			
			if (buildable.isDestroyed()) {
				throw new CivException(buildable.getDisplayName()+" is already destroyed.");
			}
		}
		
		if (buildable instanceof Wonder) {
			// Create a new mission and with the penalties.
			mission = CivSettings.missions.get("spy_sabotage_wonder");
		}
		
		double failMod = 1.0;
		if (resident.getTown().getBuffManager().hasBuff("buff_sabotage")) {
			failMod = resident.getTown().getBuffManager().getEffectiveDouble("buff_sabotage");
			CivMessage.send(player, CivColor.LightGray+"Your goodie buff 'Sabotage' will come in handy here.");
		}
		
		if (processMissionResult(player, cc.getTown(), mission, failMod, 1.0)) {
			CivMessage.global(CivColor.Yellow+"DISASTER!"+CivColor.White+" A "+buildable.getDisplayName()+" has been destroyed! Foul play is suspected.");
			buildable.setHitpoints(0);
			buildable.fancyDestroyStructureBlocks();
			buildable.save();
			
			if (buildable instanceof Wonder) {
				Wonder wonder = (Wonder)buildable;
				wonder.unbindStructureComponents();
				try {
					wonder.delete();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	private static void performPirate(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException("Only residents of towns can perform spy missions.");
		}
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		if (cc == null || cc.getCiv() == resident.getTown().getCiv()) {
			throw new CivException("Must be in another civilization's borders.");
		}
		
		// Check that the player is within range of the town hall.
		Structure tradeoutpost = cc.getCiv().getNearestStructureInTowns(player.getLocation());
		if (!(tradeoutpost instanceof TradeOutpost)) {
			throw new CivException("The closest structure to you must be a trade outpost.");
		}
		
		double distance = player.getLocation().distance(((TradeOutpost)tradeoutpost).getTradeOutpostTower().getLocation());
		if (distance > mission.range) {
			throw new CivException("Too far away from the trade outpost to pirate it.");
		}
		
		TradeOutpost outpost = (TradeOutpost)tradeoutpost;
		ItemStack stack = outpost.getItemFrameStore().getItem(); 
		
		if (stack == null || ItemManager.getId(stack) == CivData.AIR) {
			throw new CivException("No trade goodie item at this location.");
		}
		
		if(processMissionResult(player, cc.getTown(), mission)) {
			outpost.getItemFrameStore().clearItem();
			player.getWorld().dropItem(player.getLocation(), stack);
		
			CivMessage.sendSuccess(player, "Arg! Got the booty!");
			CivMessage.sendTown(cc.getTown(), CivColor.Rose+"Avast! Someone stole our trade goodie "+outpost.getGood().getInfo().name+" at "+outpost.getCorner());
		}
	}
	
	private static void performPosionGranary(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException("Only residents of towns can perform spy missions.");
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException("Must be in another civilization's town's borders.");
		}
		
		// Check that the player is within range of the town hall.
		Structure granary = tc.getTown().getNearestStrucutre(player.getLocation());
		if (!(granary instanceof Granary)) {
			throw new CivException("The closest structure to you must be a granary.");
		}
		
		double distance = player.getLocation().distance(granary.getCorner().getLocation());
		if (distance > mission.range) {
			throw new CivException("Too far away from the granary to poison it.");
		}
		
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("posiongranary:"+tc.getTown().getName());
		if (entries != null && entries.size() != 0) {
			throw new CivException("Cannot poison granary, already posioned.");
		}
		
		double failMod = 1.0;
		if (resident.getTown().getBuffManager().hasBuff("buff_espionage")) {
			failMod = resident.getTown().getBuffManager().getEffectiveDouble("buff_espionage");
			CivMessage.send(player, CivColor.LightGray+"Your goodie buff 'Espionage' will come in handy here.");
		}
		
		if (processMissionResult(player, tc.getTown(), mission, failMod, 1.0)) {
			int min;
			int max;
			try {
				min = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.poison_granary_min_ticks");
				max = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.poison_granary_max_ticks");
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				throw new CivException("Invalid configuration error.");
			}
			
			Random rand = new Random();
			int posion_ticks = rand.nextInt((max -min)) + min;
			String value = ""+posion_ticks;
			
			CivGlobal.getSessionDB().add("posiongranary:"+tc.getTown().getName(), value, tc.getTown().getId(), tc.getTown().getId(), granary.getId());
			
			try {
				double famine_chance = CivSettings.getDouble(CivSettings.espionageConfig, "espionage.poison_granary_famine_chance");
				
				if (rand.nextInt(100) < (int)(famine_chance*100)) {
					
					for (Structure struct : tc.getTown().getStructures()) {
						if (struct instanceof Cottage) {
							((Cottage)struct).delevel();
						}
					}
					
					CivMessage.global(CivColor.Yellow+"DISASTER!"+CivColor.White+" The cottages in "+tc.getTown().getName()+
							" have suffered a famine from poison grain! Each cottage loses 1 level.");
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				throw new CivException("Invalid configuration.");
			}
			
			CivMessage.sendSuccess(player, "Poisoned the granary for "+posion_ticks+" hours!");
		}
	}
	
	private static void performStealTreasury(Player player, ConfigMission mission) throws CivException {
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException("Only residents of towns can perform spy missions.");
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException("Must be in another civilization's town's borders.");
		}
		
		// Check that the player is within range of the town hall.
		TownHall townhall = tc.getTown().getTownHall();
		if (townhall == null) {
			throw new CivException("This town doesnt have a town hall... that sucks.");
		}
		
		double distance = player.getLocation().distance(townhall.getCorner().getLocation());
		if (distance > mission.range) {
			throw new CivException("Too far away from town hall to steal treasury.");
		}
		
		double failMod = 1.0;
		if (resident.getTown().getBuffManager().hasBuff("buff_dirty_money")) {
			failMod = resident.getTown().getBuffManager().getEffectiveDouble("buff_dirty_money");
			CivMessage.send(player, CivColor.LightGray+"Your goodie buff 'Dirty Money' will come in handy here.");
		}
		
		if(processMissionResult(player, tc.getTown(), mission, failMod, 1.0)) {
			
			double amount = (int)(tc.getTown().getTreasury().getBalance()*0.2);
			if (amount > 0) {
				tc.getTown().getTreasury().withdraw(amount);
				resident.getTown().getTreasury().deposit(amount);
			}
			
			CivMessage.sendSuccess(player, "Success! Stole "+amount+" coins from "+tc.getTown().getName());
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void performInvestigateTown(Player player, ConfigMission mission) throws CivException {
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException("Only residents of towns can perform spy missions.");
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException("Must be in another civilization's town's borders.");
		}
		
		if(processMissionResult(player, tc.getTown(), mission)) {
			Town t = tc.getTown();
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta meta = (BookMeta) book.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			lore.add("Mission Report");
			
			meta.setAuthor("Mission Reports");
			meta.setTitle("Investigate Town");
			
		//	ArrayList<String> out = new ArrayList<String>();
			String out = "";
			
			out += ChatColor.UNDERLINE+"Town:"+t.getName()+"\n"+ChatColor.RESET;
			out += ChatColor.UNDERLINE+"Civ:"+t.getCiv().getName()+"\n\n"+ChatColor.RESET;
			
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
			out += "Time: "+sdf.format(new Date())+"\n";
			out += ("Treasury: "+t.getTreasury().getBalance()+"\n");
			out += ("Hammers: "+t.getHammers().total+"\n");
			out += ("Culture: "+t.getCulture().total+"\n");
			out += ("Growth: "+t.getGrowth().total+"\n");
			out += ("Beakers(civ): "+t.getBeakers().total+"\n");
			if (tc.getTown().getCiv().getResearchTech() != null) {
				out += ("Researching: "+t.getCiv().getResearchTech().name+"\n");
			} else {
				out += ("Researching:Nothing"+"\n");
			}
			
			BookUtil.paginate(meta, out);
			
			out = ChatColor.UNDERLINE+"Upkeep Info\n\n"+ChatColor.RESET;
			try {
				out += "From Spread:"+t.getSpreadUpkeep()+"\n";
				out += "From Structures:"+t.getStructureUpkeep()+"\n";
				out += "Total:"+t.getTotalUpkeep();
				BookUtil.paginate(meta, out);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				throw new CivException("Internal configuration exception.");
			}
			
			
			meta.setLore(lore);
			book.setItemMeta(meta);
			
			HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(book);
			for (ItemStack stack : leftovers.values()) {
				player.getWorld().dropItem(player.getLocation(), stack);
			}
			
			player.updateInventory();
			
			CivMessage.sendSuccess(player, "Mission Accomplished");
		}
	}
	
	private static void performInciteRiots(Player player, ConfigMission mission) throws CivException {
		throw new CivException("Not implemented.");
	}
	
	
	
	
	
	// Civilization Missions
	
	public static void performSubvertGovernment(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException("Only residents of towns can perform spy missions.");
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException("Must be in another civilization's town's borders.");
		}
		
		if (tc != null && !tc.getTown().isCapitol()) {
			throw new CivException("Must be civilization's capital ("+tc.getTown().getCiv().getCapitolName()+") to perform this mission.");
		}
		
		if(processMissionResult(player, tc.getTown(), mission)) {
			Town t = tc.getTown();
			Civilization civ = t.getCiv();
			
			Random r = new Random();
			ArrayList<String> list = new ArrayList<String>();
			for (ConfigGovernment gov : CivSettings.governments.values()) {
				list.add(gov.id);
			}
			
			String key = "changegov_"+civ.getId();
			ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
			if (entries != null) {
				if (entries.size() > 0) {
					SessionEntry se = entries.get(0);
					civ.setGovernment(se.value);
				}
				CivGlobal.getSessionDB().delete_all(key);
			}
			
			String newKey = "subvertgov_"+civ.getId();
			String value = list.get(r.nextInt(list.size()));
			civ.sessionAdd(newKey, value);
			civ.setGovernment("gov_anarchy");
			CivMessage.global(CivColor.Yellow+"INTERNATIONAL INCIDENT! "+CivColor.White+"The government in "+civ.getName()+" has been overthrown in a spy mission"+
					" and is set to anarchy for the next 24 hours!");
			
			CivMessage.sendSuccess(player, "Mission Accomplished.");
		}
	}
	
	public static void performRavageTechnology(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException("Only residents of towns can perform spy missions.");
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException("Must be in another civilization's town's borders.");
		}
		
		if (tc != null && !tc.getTown().isCapitol()) {
			throw new CivException("Must be civilization's capital ("+tc.getTown().getCiv().getCapitolName()+") to perform this mission.");
		}
		
		Civilization civ = tc.getTown().getCiv();
		if (civ.getResearchTech() == null) {
			throw new CivException("Civilization does not have a technology in progress.");
		}
		
		if (civ.getResearchProgress() >= (civ.getResearchTech().beaker_cost/4)) {
			throw new CivException("Cannot ravage a technology passed 75% completion.");
		}
		
		if(processMissionResult(player, tc.getTown(), mission)) {
			civ.setResearchTech(null);
			civ.setResearchProgress(0);
			civ.save();
			CivMessage.global(CivColor.Yellow+"INTERNATIONAL INCIDENT! "+CivColor.White+"The technology being researched in civ "+civ.getName()+" has been ravaged in a spy mission"+
					" and has lost its progress!");
			
			CivMessage.sendSuccess(player, "Mission Accomplished.");
		}
	}
}