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

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.command.AcceptCommand;
import com.avrgaming.civcraft.command.BackpackCommand;
import com.avrgaming.civcraft.command.BuildCommand;
import com.avrgaming.civcraft.command.CMatCommand;
import com.avrgaming.civcraft.command.DenyCommand;
import com.avrgaming.civcraft.command.EconCommand;
import com.avrgaming.civcraft.command.HereCommand;
import com.avrgaming.civcraft.command.KillCommand;
import com.avrgaming.civcraft.command.PayCommand;
import com.avrgaming.civcraft.command.ReportCommand;
import com.avrgaming.civcraft.command.SelectCommand;
import com.avrgaming.civcraft.command.TestCommand;
import com.avrgaming.civcraft.command.VoteCommand;
import com.avrgaming.civcraft.command.admin.AdminCommand;
import com.avrgaming.civcraft.command.admin.AdminGUICommand;
import com.avrgaming.civcraft.command.admin.AdminTestCommand;
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
import com.avrgaming.civcraft.listener.DebugListener;
import com.avrgaming.civcraft.listener.DisableXPListener;
import com.avrgaming.civcraft.listener.FishingListener;
import com.avrgaming.civcraft.listener.HeroChatListener;
import com.avrgaming.civcraft.listener.MarkerPlacementManager;
import com.avrgaming.civcraft.listener.PlayerListener;
import com.avrgaming.civcraft.listener.TagAPIListener;
import com.avrgaming.civcraft.listener.civcraft.HolographicDisplaysListener;
import com.avrgaming.civcraft.listener.civcraft.InventoryDisplaysListener;
import com.avrgaming.civcraft.listener.civcraft.MinecraftListener;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterialListener;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.nocheat.NoCheatPlusSurvialFlyHandler;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.ProtectedBlock;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.object.TradeGood;
import com.avrgaming.civcraft.populators.TradeGoodPopulator;
import com.avrgaming.civcraft.randomevents.RandomEventSweeper;
import com.avrgaming.civcraft.siege.CannonListener;
import com.avrgaming.civcraft.structure.Farm;
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
import com.avrgaming.civcraft.threading.timers.CountdownTimer;
import com.avrgaming.civcraft.threading.timers.ParticleEffectTimer;
import com.avrgaming.civcraft.threading.timers.PlayerLocationCacheUpdate;
import com.avrgaming.civcraft.threading.timers.PlayerProximityComponentTimer;
import com.avrgaming.civcraft.threading.timers.PlayerTagUpdateTimer;
import com.avrgaming.civcraft.threading.timers.ProjectileComponentTimer;
import com.avrgaming.civcraft.threading.timers.ReduceExposureTimer;
import com.avrgaming.civcraft.threading.timers.RegenTimer;
import com.avrgaming.civcraft.threading.timers.UnitTrainTimer;
import com.avrgaming.civcraft.threading.timers.UpdateEventTimer;
import com.avrgaming.civcraft.threading.timers.WindmillTimer;
import com.avrgaming.civcraft.util.BukkitObjects;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.TimeTools;
import com.avrgaming.civcraft.war.WarListener;
import com.avrgaming.global.perks.PlatinumManager;
import com.avrgaming.sls.SLSManager;

import pvptimer.PvPListener;
import pvptimer.PvPTimer;

public final class CivCraft extends JavaPlugin {
	
	private boolean isError = false;
	private static JavaPlugin plugin;
	public static boolean isDisable = false;
	public static boolean isStarted = false;
	public static String worldName;
	
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
		
		TaskMaster.asyncTimer("CountdownTimer", new CountdownTimer(), TimeTools.toTicks(1));
		TaskMaster.asyncTimer("PlayerTagUpdateTimer", new PlayerTagUpdateTimer(), TimeTools.toTicks(10));
		TaskMaster.asyncTimer("ActionBarUpdateTimer", new ActionBarUpdateTimer(), TimeTools.toTicks(1));
		
		// Structure event timers
		TaskMaster.asyncTimer("ParticleEffectTimer", new ParticleEffectTimer(), TimeTools.toTicks(1, 7));
		TaskMaster.asyncTimer("UpdateEventTimer", new UpdateEventTimer(), TimeTools.toTicks(1));
		TaskMaster.asyncTimer("RegenTimer", new RegenTimer(), TimeTools.toTicks(5));

		TaskMaster.asyncTimer("BeakerTimer", new BeakerTimer(60), TimeTools.toTicks(60));
		TaskMaster.syncTimer("UnitTrainTimer", new UnitTrainTimer(), TimeTools.toTicks(1));

		try {
			int exposure_time = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.reduce_time");
			TaskMaster.asyncTimer("ReduceExposureTimer", new ReduceExposureTimer(), 0, TimeTools.toTicks(exposure_time));
			
			int tips_timer = CivSettings.getInteger(CivSettings.gameConfig, "tips.amount");
			TaskMaster.asyncTimer("announcer", new AnnouncementTimer("tips.txt"), 0, TimeTools.toTicks(60*(4*tips_timer)));
			
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
		
		TaskMaster.syncTimer("WindmillTimer", new WindmillTimer(), TimeTools.toTicks(45));
		TaskMaster.asyncTimer("EndGameNotification", new EndConditionNotificationTask(), TimeTools.toTicks(3600));
				
		TaskMaster.asyncTask(new StructureValidationChecker(), TimeTools.toTicks(120));
		TaskMaster.asyncTimer("StructureValidationPunisher", new StructureValidationPunisher(), TimeTools.toTicks(3600));
		TaskMaster.asyncTimer("SessionDBAsyncTimer", new SessionDBAsyncTimer(), 5);
		TaskMaster.asyncTimer("pvptimer", new PvPTimer(), TimeTools.toTicks(30));

	}
	
	private void registerEvents() {
		final PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new BlockListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new BonusGoodieManager(), this);
		pluginManager.registerEvents(new MarkerPlacementManager(), this);
		pluginManager.registerEvents(new CustomItemManager(), this);
		pluginManager.registerEvents(new PlayerListener(), this);		
		pluginManager.registerEvents(new DebugListener(), this);
		pluginManager.registerEvents(new LoreCraftableMaterialListener(), this);
		pluginManager.registerEvents(new LoreGuiItemListener(), this);
		pluginManager.registerEvents(new DisableXPListener(), this);
		pluginManager.registerEvents(new CannonListener(), this);
		pluginManager.registerEvents(new WarListener(), this);
		pluginManager.registerEvents(new FishingListener(), this);	
		pluginManager.registerEvents(new PvPListener(), this);
		
		pluginManager.registerEvents(new MinecraftListener(), this);
		
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
		isDisable = false;
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
			SLSManager.init();
		} catch (InvalidConfiguration | SQLException | IOException | InvalidConfigurationException | CivException | ClassNotFoundException e) {
			e.printStackTrace();
			setError(true);
			return;
		}
		
		// Init commands
		getCommand("cmat").setExecutor(new CMatCommand());
		getCommand("backpack").setExecutor(new BackpackCommand());
		getCommand("test").setExecutor(new TestCommand());
		getCommand("town").setExecutor(new TownCommand());
		getCommand("resident").setExecutor(new ResidentCommand());
		getCommand("dbg").setExecutor(new DebugCommand());
		getCommand("plot").setExecutor(new PlotCommand());
		getCommand("accept").setExecutor(new AcceptCommand());
		getCommand("deny").setExecutor(new DenyCommand());
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
	
		registerEvents();
		
		if (hasPlugin("NoCheatPlus")) {
			registerNPCHooks();
		} else {
			CivLog.warning("NoCheatPlus not found, not registering NCP hooks. It is fine if you're not using NCP.");
		}
		
		startTimers();
		CivCraft.addFurnaceRecipes();
		
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
				BuildUndoTask.resumeUndoTasks();
			}
		});
	}
	
	public boolean hasPlugin(String name) {
		Plugin p;
		p = getServer().getPluginManager().getPlugin(name);
		return (p != null);
	}
	
	@Override
	public void onDisable() {
		CivMessage.global("The server is being stopped, saving data...");
		
		int chunksSearched = 0;
		int villagersRemoved = 0;
		for (TownChunk tc : CivGlobal.getTownChunks()) {
			Chunk chunk = tc.getChunkCoord().getChunk();
			if (!chunk.isLoaded()) chunk.load();
			
			for (Entity e : chunk.getEntities()) {
				if (e instanceof Villager) {
					Villager v = (Villager) e; // TODO We will allow regular villagers to exist with HIDDEN name 'civcraft_villager'
					if (v.getCustomName() != null && !v.getCustomName().equalsIgnoreCase("civcraft_villager")) {
						villagersRemoved++;
						v.setHealth(0);
						e.remove();
					}
				}
			}
			
			chunksSearched++;
			chunk.unload();
		}
		CivMessage.global(CivColor.Gold+"Removed "+villagersRemoved+" villagers from "+chunksSearched+" town chunks.");
		
		isDisable = true;
		SQLUpdate.save();
		disableCivGlobal();
		
		super.onDisable();
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
	
	public static void addFurnaceRecipes() {
		FurnaceRecipe recipe1 = new FurnaceRecipe(new ItemStack(Material.INK_SACK, 4, (short) 4), Material.LAPIS_ORE);
		recipe1.setExperience(1.0F);
		Bukkit.addRecipe(recipe1);
		
		FurnaceRecipe recipe2 = new FurnaceRecipe(new ItemStack(Material.REDSTONE, 4), Material.REDSTONE_ORE);
		recipe2.setExperience(1.0F);
		Bukkit.addRecipe(recipe2);
	}
	
	public void disableCivGlobal() {
		for (Location loc : ParticleEffectTimer.externalParticleBlocks.keySet()) {
			ParticleEffectTimer.externalParticleBlocks.remove(loc);
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
