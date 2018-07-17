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
package com.avrgaming.civcraft.main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.anticheat.AC_Manager;
import com.avrgaming.civcraft.command.AcceptCommand;
import com.avrgaming.civcraft.command.BackpackCommand;
import com.avrgaming.civcraft.command.BuildCommand;
import com.avrgaming.civcraft.command.CMatCommand;
import com.avrgaming.civcraft.command.DenyCommand;
import com.avrgaming.civcraft.command.EconCommand;
import com.avrgaming.civcraft.command.HereCommand;
import com.avrgaming.civcraft.command.KillCommand;
import com.avrgaming.civcraft.command.PayCommand;
import com.avrgaming.civcraft.command.RebootCommand;
import com.avrgaming.civcraft.command.ReportCommand;
import com.avrgaming.civcraft.command.SelectCommand;
import com.avrgaming.civcraft.command.VoteCommand;
import com.avrgaming.civcraft.command.admin.AdminCommand;
import com.avrgaming.civcraft.command.admin.AdminGUICommand;
import com.avrgaming.civcraft.command.admin.AdminTestCommand;
import com.avrgaming.civcraft.command.camp.CampCommand;
import com.avrgaming.civcraft.command.civ.CivChatCommand;
import com.avrgaming.civcraft.command.civ.CivCommand;
import com.avrgaming.civcraft.command.debug.DebugCommand;
import com.avrgaming.civcraft.command.market.MarketCommand;
import com.avrgaming.civcraft.command.moderator.ModeratorCommand;
import com.avrgaming.civcraft.command.plot.PlotCommand;
import com.avrgaming.civcraft.command.resident.ResidentCommand;
import com.avrgaming.civcraft.command.town.TownChatCommand;
import com.avrgaming.civcraft.command.town.TownCommand;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.database.SQL;
import com.avrgaming.civcraft.database.SQLUpdate;
import com.avrgaming.civcraft.database.session.SessionDBAsyncTimer;
import com.avrgaming.civcraft.endgame.EndConditionNotificationTask;
import com.avrgaming.civcraft.event.EventTimerTask;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.listener.BlockListener;
import com.avrgaming.civcraft.listener.BonusGoodieManager;
import com.avrgaming.civcraft.listener.ChatListener;
import com.avrgaming.civcraft.listener.CustomItemManager;
import com.avrgaming.civcraft.listener.DisableXPListener;
import com.avrgaming.civcraft.listener.FishingListener;
import com.avrgaming.civcraft.listener.HeroChatListener;
import com.avrgaming.civcraft.listener.MarkerPlacementManager;
import com.avrgaming.civcraft.listener.PlayerListener;
import com.avrgaming.civcraft.listener.TagAPIListener;
import com.avrgaming.civcraft.listener.civcraft.BrewingListener;
import com.avrgaming.civcraft.listener.civcraft.HolographicDisplaysListener;
import com.avrgaming.civcraft.listener.civcraft.InventoryDisplaysListener;
import com.avrgaming.civcraft.listener.civcraft.MinecraftListener;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterialListener;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.mobs.MobListener;
import com.avrgaming.civcraft.mobs.MobSpawner;
import com.avrgaming.civcraft.mobs.MobSpawnerTimer;
import com.avrgaming.civcraft.nocheat.NoCheatPlusSurvialFlyHandler;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.ProtectedBlock;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.object.TradeGood;
import com.avrgaming.civcraft.object.camp.Camp;
import com.avrgaming.civcraft.populators.TradeGoodPopulator;
import com.avrgaming.civcraft.randomevents.RandomEventSweeper;
import com.avrgaming.civcraft.siege.CannonListener;
import com.avrgaming.civcraft.structure.Cottage;
import com.avrgaming.civcraft.structure.Farm;
import com.avrgaming.civcraft.structure.Lab;
import com.avrgaming.civcraft.structure.Mine;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.farm.FarmGrowthSyncTask;
import com.avrgaming.civcraft.structure.farm.FarmPreCachePopulateTimer;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.structurevalidation.StructureValidationChecker;
import com.avrgaming.civcraft.structurevalidation.StructureValidationPunisher;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.sync.SyncBuildUpdateTask;
import com.avrgaming.civcraft.threading.sync.SyncGetChestInventory;
import com.avrgaming.civcraft.threading.sync.SyncGrowTask;
import com.avrgaming.civcraft.threading.sync.SyncLoadChunk;
import com.avrgaming.civcraft.threading.sync.SyncUpdateInventory;
import com.avrgaming.civcraft.threading.tasks.ArrowProjectileTask;
import com.avrgaming.civcraft.threading.tasks.BuildUndoTask;
import com.avrgaming.civcraft.threading.tasks.ScoutTowerTask;
import com.avrgaming.civcraft.threading.timers.ActionBarUpdateTimer;
import com.avrgaming.civcraft.threading.timers.AnnouncementTimer;
import com.avrgaming.civcraft.threading.timers.BeakerTimer;
import com.avrgaming.civcraft.threading.timers.CalculateScoreTimer;
import com.avrgaming.civcraft.threading.timers.ChangeGovernmentTimer;
import com.avrgaming.civcraft.threading.timers.ParticleEffectTimer;
import com.avrgaming.civcraft.threading.timers.PlayerLocationCacheUpdate;
import com.avrgaming.civcraft.threading.timers.PlayerProximityComponentTimer;
import com.avrgaming.civcraft.threading.timers.PlayerTagUpdateTimer;
import com.avrgaming.civcraft.threading.timers.ProjectileComponentTimer;
import com.avrgaming.civcraft.threading.timers.ReduceExposureTimer;
import com.avrgaming.civcraft.threading.timers.RegenTimer;
import com.avrgaming.civcraft.threading.timers.StructureProcessTimer;
import com.avrgaming.civcraft.threading.timers.UnitTrainTimer;
import com.avrgaming.civcraft.threading.timers.UpdateEventTimer;
import com.avrgaming.civcraft.threading.timers.WindmillTimer;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.BukkitObjects;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.TimeTools;
import com.avrgaming.civcraft.war.WarListener;
import com.avrgaming.global.perks.PlatinumManager;

import pvptimer.PvPListener;
import pvptimer.PvPTimer;

public final class CivCraft extends JavaPlugin {
	
	private boolean isError = false;
	private static JavaPlugin plugin;
	public static boolean isDisable = false;
	public static boolean isStarted = false;
	public static boolean isRestarting = false;
	
	public static String worldName;
	public static Integer structure_process;
	public static final String server_name = CivColor.Gray+"["+CivColor.Red+"Coal"+CivColor.LightBlue+"CivCraft"+CivColor.Gray+"] "+CivColor.RESET;
	
	private void startTimers() {
		TaskMaster.asyncTask("SQLUpdate", new SQLUpdate(), 0);
		
		// Sync Timers
		TaskMaster.syncTimer(SyncBuildUpdateTask.class.getName(), new SyncBuildUpdateTask(), 0, 1);
		TaskMaster.syncTimer(SyncLoadChunk.class.getName(), new SyncLoadChunk(), 0, 1);
		TaskMaster.syncTimer(SyncGetChestInventory.class.getName(), new SyncGetChestInventory(), 0, 1);
		TaskMaster.syncTimer(SyncUpdateInventory.class.getName(), new SyncUpdateInventory(), 0, 1);
		TaskMaster.syncTimer(SyncGrowTask.class.getName(), new SyncGrowTask(), 0, 1);
		TaskMaster.syncTimer(PlayerLocationCacheUpdate.class.getName(), new PlayerLocationCacheUpdate(), 0, 10);
		TaskMaster.asyncTimer("RandomEventSweeper", new RandomEventSweeper(), 0, TimeTools.toTicks(10));
		
		// BeakerTimer now runs task to check if player should be unbanned/unmuted.
//		TaskMaster.asyncTimer("CountdownTimer", new CountdownTimer(), TimeTools.toTicks(1));
		TaskMaster.asyncTimer("ActionBarUpdateTimer", new ActionBarUpdateTimer(), TimeTools.toTicks(1));
		
		// Structure event timers
		TaskMaster.asyncTimer("ParticleEffectTimer", new ParticleEffectTimer(), TimeTools.toTicks(1, 7));
		TaskMaster.asyncTimer("RegenTimer", new RegenTimer(), TimeTools.toTicks(5));
		
		TaskMaster.asyncTimer("BeakerTimer", new BeakerTimer(60), TimeTools.toTicks(60));
		TaskMaster.syncTimer("UnitTrainTimer", new UnitTrainTimer(), TimeTools.toTicks(1));
		
		TaskMaster.asyncTimer("UpdateEventTimer", new UpdateEventTimer(), TimeTools.toTicks(1));
		
		try {
			structure_process = CivSettings.getInteger(CivSettings.gameConfig, "timers.structure_process");
			TaskMaster.asyncTimer("StructureProcessTimer", new StructureProcessTimer(), TimeTools.toTicks(structure_process));
			
			int exposure_time = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.reduce_time");
			TaskMaster.asyncTimer("ReduceExposureTimer", new ReduceExposureTimer(), 0, TimeTools.toTicks(exposure_time));
			
			int tips_timer = CivSettings.getIntegerGameConfig("tips.amount");
			int tips_cooldown = CivSettings.getIntegerGameConfig("tips.cooldown");
			TaskMaster.asyncTimer("announcer", new AnnouncementTimer("civ_tips.txt"), 0, TimeTools.toTicks(tips_cooldown*tips_timer));
			
			double arrow_firerate = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.fire_rate");
			TaskMaster.syncTimer("arrowTower", new ProjectileComponentTimer(), (int)(arrow_firerate*20));	
			TaskMaster.asyncTimer("ScoutTowerTask", new ScoutTowerTask(), TimeTools.toTicks(1));
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return;
		}
		TaskMaster.syncTimer("arrowhomingtask", new ArrowProjectileTask(), 5);
			
		// Global Event timers		
		TaskMaster.syncTimer("FarmCropCache", new FarmPreCachePopulateTimer(), TimeTools.toTicks(30));
	
		TaskMaster.asyncTimer("FarmGrowthTimer", new FarmGrowthSyncTask(), TimeTools.toTicks(Farm.GROW_TICK_RATE));
		
		TaskMaster.asyncTimer("ChangeGovernmentTimer", new ChangeGovernmentTimer(), TimeTools.toTicks(60));
		TaskMaster.asyncTimer("CalculateScoreTimer", new CalculateScoreTimer(), 0, TimeTools.toTicks(60));
		
		TaskMaster.asyncTimer(PlayerProximityComponentTimer.class.getName(), new PlayerProximityComponentTimer(), TimeTools.toTicks(1));
		
		TaskMaster.asyncTimer(EventTimerTask.class.getName(), new EventTimerTask(), TimeTools.toTicks(5));

		if (PlatinumManager.isEnabled()) {
			TaskMaster.asyncTimer(PlatinumManager.class.getName(), new PlatinumManager(), TimeTools.toTicks(5));
		}
		
		TaskMaster.syncTimer("WindmillTimer", new WindmillTimer(), TimeTools.toTicks(30));
		TaskMaster.asyncTimer("EndGameNotification", new EndConditionNotificationTask(), TimeTools.toTicks(3600));
		
		TaskMaster.asyncTask(new StructureValidationChecker(), TimeTools.toTicks(120));
		TaskMaster.asyncTimer("StructureValidationPunisher", new StructureValidationPunisher(), TimeTools.toTicks(3600));
		TaskMaster.asyncTimer("SessionDBAsyncTimer", new SessionDBAsyncTimer(), 5);
		TaskMaster.asyncTimer("PvPTimer", new PvPTimer(), TimeTools.toTicks(30));
		
		TaskMaster.syncTimer("MobSpawner", new MobSpawnerTimer(), TimeTools.toTicks(20));

	}
	
	private void registerEvents() {
		final PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new BlockListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new BonusGoodieManager(), this);
		pluginManager.registerEvents(new MarkerPlacementManager(), this);
		pluginManager.registerEvents(new CustomItemManager(), this);
		pluginManager.registerEvents(new PlayerListener(), this);		
		pluginManager.registerEvents(new LoreCraftableMaterialListener(), this);
		pluginManager.registerEvents(new LoreGuiItemListener(), this);
		pluginManager.registerEvents(new DisableXPListener(), this);
		pluginManager.registerEvents(new CannonListener(), this);
		pluginManager.registerEvents(new WarListener(), this);
		pluginManager.registerEvents(new FishingListener(), this);	
		pluginManager.registerEvents(new PvPListener(), this);
		
		pluginManager.registerEvents(new MobListener(), this);
		pluginManager.registerEvents(new MinecraftListener(), this);
		pluginManager.registerEvents(new BrewingListener(), this);
		
		//Registered GUIs
		pluginManager.registerEvents(new InventoryDisplaysListener(), this);
		pluginManager.registerEvents(new AdminGUICommand(), this);
		pluginManager.registerEvents(new AdminTestCommand(), this);
		
		if (hasPlugin("TagAPI")) {
			pluginManager.registerEvents(new TagAPIListener(), this);
		}
		
		if (hasPlugin("HeroChat")) {
			pluginManager.registerEvents(new HeroChatListener(), this);
		}
	}
	
	private void registerNPCHooks() {
		NoCheatPlusSurvialFlyHandler.init();
	}
	
	@Override
	public void onEnable() {
		CivLog.init(this);
		isDisable = false; isStarted = false; isRestarting = false;
		setPlugin(this);
		this.saveDefaultConfig();
		
		BukkitObjects.initialize(this);
		worldName = BukkitObjects.getWorlds().get(0).getName();
		
		//Load World Populators
		BukkitObjects.getWorlds().get(0).getPopulators().add(new TradeGoodPopulator());
		
		try {
			CivSettings.init(this);
			SQL.initialize();
			SQL.initCivObjectTables();
			ChunkCoord.buildWorldList();
			CivGlobal.loadGlobals();
		} catch (InvalidConfiguration | SQLException | IOException | InvalidConfigurationException | CivException | ClassNotFoundException e) {
			e.printStackTrace();
			setError(true);
			return;
		}
		
		// Init commands
		getCommand("cmat").setExecutor(new CMatCommand());
		getCommand("backpack").setExecutor(new BackpackCommand());
		getCommand("town").setExecutor(new TownCommand());
		getCommand("resident").setExecutor(new ResidentCommand());
		getCommand("dbg").setExecutor(new DebugCommand());
		getCommand("plot").setExecutor(new PlotCommand());
		getCommand("accept").setExecutor(new AcceptCommand());
		getCommand("deny").setExecutor(new DenyCommand());
		getCommand("camp").setExecutor(new CampCommand());
		getCommand("civ").setExecutor(new CivCommand());
		getCommand("tc").setExecutor(new TownChatCommand());
		getCommand("cc").setExecutor(new CivChatCommand());
		getCommand("ad").setExecutor(new AdminCommand());
		getCommand("mod").setExecutor(new ModeratorCommand());
		getCommand("econ").setExecutor(new EconCommand());
		getCommand("pay").setExecutor(new PayCommand());
		getCommand("build").setExecutor(new BuildCommand());
		getCommand("market").setExecutor(new MarketCommand());
		getCommand("select").setExecutor(new SelectCommand());
		getCommand("here").setExecutor(new HereCommand());
		getCommand("report").setExecutor(new ReportCommand());
		getCommand("vote").setExecutor(new VoteCommand());
		getCommand("kill").setExecutor(new KillCommand());
		getCommand("reboot").setExecutor(new RebootCommand());
	
		registerEvents();
		acManager = new AC_Manager(this);
		
		if (hasPlugin("NoCheatPlus")) {
			registerNPCHooks();
		} else {
			CivLog.warning("NoCheatPlus not found, not registering NCP hooks. It is fine if you're not using NCP.");
		}
		
		startTimers();
		CivCraft.addFurnaceRecipes();
		MinecraftListener.setupFoodValues();
		
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				isStarted = true;
				try {
					worldName = CivSettings.getString(CivSettings.gameConfig, "world.name");
				} catch (InvalidConfiguration e) {
					worldName = BukkitObjects.getWorlds().get(0).getName();
					CivLog.warning("Cannot find 'world_name' in fle 'game.yml'. Defaulting to '"+worldName+"'");
				}
				
				if (hasPlugin("HolographicDisplays")) {
					HolographicDisplaysListener.generateTradeGoodHolograms();
				} else {
					CivLog.warning("HolographicDisplays not found, not registering listener. It is fine if you're not using Holographic Displays.");
				}
				
				CivLog.info("Force despawning mobs before we start...");
				MobSpawner.despawnMobs(null, true, true, true, true, true, true, true);
				CivCraft.updateStructureArrays();
				BuildUndoTask.resumeUndoTasks();
			}
		});
	}
	
	public boolean hasPlugin(String name) {
		Plugin p = getServer().getPluginManager().getPlugin(name);
		return (p != null);
	}
	
	@Override
	public void onDisable() {
		CivMessage.global("The server is being stopped, saving data...");
		TaskMaster.stopAll();
		CivGlobal.resetGlobalVillagers();
		MobSpawner.despawnMobs(null, true, true, true, false, false, true, true);
		
		isDisable = true;
		SQLUpdate.save();
		disableCivGlobal();
		
		super.onDisable();
	}
	
	private static AC_Manager acManager;
	public static AC_Manager getACManager() {
		return acManager;
	}
	
	public boolean isError() {
		return isError;
	}
	
	public void setError(boolean isError) {
		this.isError = isError;
	}
	
	public static JavaPlugin getPlugin() {
		return plugin;
	}
	
	public static void setPlugin(JavaPlugin plugin) {
		CivCraft.plugin = plugin;
	}
	
	public static void playerTagUpdate() {
		TaskMaster.asyncTask(new PlayerTagUpdateTimer(), 1);
	}
	
	public static void addFurnaceRecipes() {
		MaterialData lap_ore = new MaterialData(Material.LAPIS_ORE);
		FurnaceRecipe recipe1 = new FurnaceRecipe(new ItemStack(Material.INK_SACK, 4, (short) 4), lap_ore, 0.85f);
		Bukkit.addRecipe(recipe1);
		plugin.getServer().addRecipe(recipe1);
		
		MaterialData red_ore = new MaterialData(Material.REDSTONE_ORE);
		FurnaceRecipe recipe2 = new FurnaceRecipe(new ItemStack(Material.REDSTONE, 4), red_ore, 0.7f);
		Bukkit.addRecipe(recipe2);
		plugin.getServer().addRecipe(recipe2);
		
		MaterialData rot_stk = new MaterialData(Material.ROTTEN_FLESH);
		FurnaceRecipe recipe3 = new FurnaceRecipe(new ItemStack(Material.COOKED_BEEF, 1), rot_stk, 0.35f);
		Bukkit.addRecipe(recipe3);
		plugin.getServer().addRecipe(recipe3);
		
//		ItemStack cio = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_crushed_iron_chunk"), 1);
//		MaterialData crushed_iron = new MaterialData(Material.IRON_ORE);
//		FurnaceRecipe recipe4 = new FurnaceRecipe(cio, crushed_iron, 0.5f);
//		Bukkit.addRecipe(recipe4);
//		plugin.getServer().addRecipe(recipe4);
	}
	
	public static void updateStructureArrays() {
		CivGlobal.cottages.clear();
		CivGlobal.mines.clear();
		CivGlobal.labs.clear();
		
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		while (iter.hasNext()) {
			Structure s = iter.next().getValue();
			if (s instanceof Cottage) {
				CivGlobal.cottages.add((Cottage) s);
				continue;
			}
			if (s instanceof Mine) {
				CivGlobal.mines.add((Mine) s);
				continue;
			}
			if (s instanceof Lab) {
				CivGlobal.labs.add((Lab) s);
				continue;
			}
		}
	}
	
	public void disableCivGlobal() {
		for (Location loc : ParticleEffectTimer.externalParticleBlocks.keySet()) {
			ParticleEffectTimer.externalParticleBlocks.remove(loc);
		}
		
		for (Camp ca : CivGlobal.getCamps()) {
			ca.save();
			CivGlobal.removeCamp(ca);
		}
		
		for (AccountLogger al : CivGlobal.getAccounts()) {
			al.save();
			CivGlobal.removeAccount(al);
		}
		
		for (Civilization civ : CivGlobal.getCivs()) {
			civ.save();
			CivGlobal.removeCiv(civ);
		}
		
		for (Town town : CivGlobal.getTowns()) {
			town.save();
			CivGlobal.removeTown(town);
		}
		
		for (Resident resident : CivGlobal.getResidents()) {
			resident.save();
			CivGlobal.removeResident(resident);
		}
		
		for (TownChunk tc : CivGlobal.getTownChunks()) {
			tc.save();
			CivGlobal.removeTownChunk(tc);
		}
		
		for (CultureChunk cc : CivGlobal.getCultureChunks()) {
			CivGlobal.removeCultureChunk(cc);
		}
		
		for (Structure structure : CivGlobal.getStructures()) {
			structure.save();
			CivGlobal.removeStructure(structure);
		}
		
		for (Wonder wonder : CivGlobal.getWonders()) {
			wonder.save();
			CivGlobal.removeWonder(wonder);
		}
		
		for (TradeGood tg : CivGlobal.getTradeGoods()) {
			tg.save();
			CivGlobal.removeTradeGood(tg);
		}
		
		for (ProtectedBlock pg : CivGlobal.getProtectedBlocks()) {
			pg.save();
			CivGlobal.removeProtectedBlock(pg);
		}
	}
}
