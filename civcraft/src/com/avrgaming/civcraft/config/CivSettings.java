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
package com.avrgaming.civcraft.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import com.avrgaming.civcraft.endgame.ConfigEndCondition;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.camp.Camp;
import com.avrgaming.civcraft.randomevents.ConfigRandomEvent;
import com.avrgaming.civcraft.structure.Wall;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.war.WarListener;
import com.avrgaming.global.perks.Perk;

public class CivSettings {
	
	public static CivCraft plugin;
	
	//TODO make this configurable.
	public static final int GRACE_DAYS = 3; 
	
	public static final int CIV_DEBT_GRACE_DAYS = 14;
	public static final int CIV_DEBT_SELL_DAYS = 21;
	public static final int CIV_DEBT_TOWN_SELL_DAYS = 28;
	
	public static final int TOWN_DEBT_GRACE_DAYS = 14;
	public static final int TOWN_DEBT_SELL_DAYS = 21;
	
	// cached for faster access.
	public static float normal_speed;
	public static float T1_leather_speed;
	public static float T2_leather_speed;
	public static float T3_leather_speed;
	public static float T4_leather_speed;
	public static float T5_leather_speed;
	public static float T2_metal_speed;
	public static float T3_metal_speed;
	public static float T4_metal_speed;
	public static float T5_metal_speed;
	
	public static FileConfiguration townConfig; /* town.yml */
	public static Map<Integer, ConfigTownLevel> townLevels = new HashMap<Integer, ConfigTownLevel>();
	public static Map<String, ConfigTownUpgrade> townUpgrades = new TreeMap<String, ConfigTownUpgrade>();
	
	public static FileConfiguration campConfig; /* camp.yml */
	public static Map<Integer, ConfigCampLonghouseLevel> longhouseLevels = new HashMap<Integer, ConfigCampLonghouseLevel>();
	public static Map<String, ConfigCampUpgrade> campUpgrades = new HashMap<String, ConfigCampUpgrade>();
	
	public static FileConfiguration civConfig; /* civ.yml */
	public static Map<String, ConfigEndCondition> endConditions = new HashMap<String, ConfigEndCondition>();
	
	public static FileConfiguration cultureConfig; /* culture.yml */
	public static Map<Integer, ConfigCultureLevel> cultureLevels = new HashMap<Integer, ConfigCultureLevel>();
	private static Map<String, ConfigCultureBiomeInfo> cultureBiomes = new HashMap<String, ConfigCultureBiomeInfo>();
	
	public static FileConfiguration gameConfig; /* game.yml */
	public static Map<Integer, ConfigNewspaper> newspapers = new HashMap<Integer, ConfigNewspaper>();
	public static Map<String, ConfigEXPMining> resxpMiningBlocks = new HashMap<String, ConfigEXPMining>();
	
	public static FileConfiguration mobConfig; /* mobs.yml */
	public static Map<String, ConfigMobsCustom> customMobs = new HashMap<String, ConfigMobsCustom>();
	
	public static FileConfiguration structureConfig; /* structures.yml */
	public static Map<String, ConfigBuildableInfo> structures = new HashMap<String, ConfigBuildableInfo>();
	public static Map<Integer, ConfigGrocerLevel> grocerLevels = new HashMap<Integer, ConfigGrocerLevel>();
	public static ArrayList<ConfigTempleSacrifice> templeSacrifices = new ArrayList<ConfigTempleSacrifice>();
	
	public static FileConfiguration structuredataConfig; /* structuredata.yml */
	public static ArrayList<String> repairableItems = new ArrayList<String>();
	public static Map<Integer, ConfigBankLevel> bankLevels = new HashMap<Integer, ConfigBankLevel>();
	public static Map<Integer, ConfigLumberMillItem> lumbermillItems = new HashMap<Integer, ConfigLumberMillItem>();
	public static ArrayList<ConfigLumberMill> lumbermillDrops = new ArrayList<ConfigLumberMill>();
	public static Map<Integer, ConfigQuarryItem> quarryItems = new HashMap<Integer, ConfigQuarryItem>();
	public static ArrayList<ConfigQuarry> quarryDrops = new ArrayList<ConfigQuarry>();
	public static Map<String, ConfigTrommelItem> trommelItems = new HashMap<String, ConfigTrommelItem>();
	public static ArrayList<ConfigTrommel> trommelDrops = new ArrayList<ConfigTrommel>();
	public static Map<Integer, ConfigCottageLevel> cottageLevels = new HashMap<Integer, ConfigCottageLevel>();
	public static Map<Integer, ConfigGranaryLevel> granaryLevels = new HashMap<Integer, ConfigGranaryLevel>();
	public static Map<Integer, ConfigGranaryTask> granaryTasks = new HashMap<Integer, ConfigGranaryTask>();
	public static Map<Integer, ConfigGranaryFood> granaryFood = new HashMap<Integer, ConfigGranaryFood>();
	public static Map<Integer, ConfigMineLevel> mineLevels = new HashMap<Integer, ConfigMineLevel>();
	public static Map<Integer, ConfigMineTask> mineTasks = new HashMap<Integer, ConfigMineTask>();
	public static Map<Integer, ConfigLabLevel> labLevels = new HashMap<Integer, ConfigLabLevel>();
	public static Map<Integer, ConfigLabTask> labTasks = new HashMap<Integer, ConfigLabTask>();
	
	public static FileConfiguration wonderConfig; /* wonders.yml */
	public static Map<String, ConfigBuildableInfo> wonders = new HashMap<String, ConfigBuildableInfo>();
	public static Map<String, ConfigWonderBuff> wonderBuffs = new HashMap<String, ConfigWonderBuff>();
	
	public static FileConfiguration techsConfig; /* techs.yml */
	public static Map<String, ConfigTech> techs = new HashMap<String, ConfigTech>();
	public static Map<Integer, ConfigTechPotion> techPotions = new HashMap<Integer, ConfigTechPotion>();

	public static FileConfiguration goodsConfig; /* goods.yml */
	public static Map<String, ConfigTradeGood> goods = new HashMap<String, ConfigTradeGood>();
	public static Map<String, ConfigTradeGood> landGoods = new HashMap<String, ConfigTradeGood>();
	public static Map<String, ConfigTradeGood> waterGoods = new HashMap<String, ConfigTradeGood>();
	public static Map<String, ConfigHemisphere> hemispheres = new HashMap<String, ConfigHemisphere>();

	public static FileConfiguration buffConfig;
	public static Map<String, ConfigBuff> buffs = new HashMap<String, ConfigBuff>();
	
	public static FileConfiguration unitConfig;
	public static Map<String, ConfigUnit> units = new HashMap<String, ConfigUnit>();
	
	public static FileConfiguration espionageConfig;
	public static Map<String, ConfigMission> missions = new HashMap<String, ConfigMission>();
	
	public static FileConfiguration governmentConfig; /* governments.yml */
	public static Map<String, ConfigGovernment> governments = new HashMap<String, ConfigGovernment>();
	
	public static HashSet<Integer> switchItems = new HashSet<Integer>();
	public static Map<Material, Integer> restrictedItems = new HashMap<Material, Integer>();
	public static Map<Material, Integer> blockPlaceExceptions =  new HashMap<Material, Integer>();
	public static ArrayList<EntityType> restrictedSpawns = new ArrayList<EntityType>();
	public static ArrayList<EntityType> vanillaHostileMobs = new ArrayList<EntityType>();
	public static HashSet<EntityType> playerEntityWeapons = new HashSet<EntityType>();
	public static HashSet<Integer> alwaysCrumble = new HashSet<Integer>();
	
	public static FileConfiguration warConfig; /* war.yml */
	
	public static FileConfiguration scoreConfig; /* score.yml */
	
	public static FileConfiguration perkConfig; /* perks.yml */
	public static Map<String, ConfigPerk> perks = new HashMap<String, ConfigPerk>();

	public static FileConfiguration enchantConfig; /* enchantments.yml */
	public static Map<String, ConfigEnchant> enchants = new HashMap<String, ConfigEnchant>();
	
	public static FileConfiguration marketConfig; /* market.yml */
	public static Map<Integer, ConfigMarketItem> marketItems = new HashMap<Integer, ConfigMarketItem>();
	
	public static Set<ConfigStableItem> stableItems = new HashSet<ConfigStableItem>();
	public static HashMap<Integer, ConfigStableHorse> horses = new HashMap<Integer, ConfigStableHorse>();
	
	public static FileConfiguration happinessConfig; /* happiness.yml */
	public static HashMap<Integer, ConfigTownHappinessLevel> townHappinessLevels = new HashMap<Integer, ConfigTownHappinessLevel>();
	public static HashMap<Integer, ConfigHappinessState> happinessStates = new HashMap<Integer, ConfigHappinessState>();
	
	public static FileConfiguration materialsConfig; /* materials.yml */
	public static HashMap<String, ConfigMaterial> materials = new HashMap<String, ConfigMaterial>();
	
	public static FileConfiguration randomEventsConfig; /* randomevents.yml */
	public static HashMap<String, ConfigRandomEvent> randomEvents = new HashMap<String, ConfigRandomEvent>();
	public static ArrayList<String> randomEventIDs = new ArrayList<String>();
	
	public static FileConfiguration nocheatConfig; /* nocheatConfig.yml */
	
	public static FileConfiguration fishingConfig; /* fishing.yml */
	public static ArrayList<ConfigFishing> fishingDrops = new ArrayList<ConfigFishing>();
		
	public static int iron_rate;
	public static int gold_rate;
	public static int diamond_rate;
	public static int emerald_rate;
	public static Double startingCoins;
	
	public static ArrayList<String> kitItems = new ArrayList<String>();
	public static HashMap<Integer, ConfigRemovedRecipes> removedRecipies = new HashMap<Integer, ConfigRemovedRecipes>();
	public static HashSet<Material> restrictedUndoBlocks = new HashSet<Material>();
	
//	public static final String ADMIN = "civ.admin";
//	public static final String MINI_ADMIN = "civ.mini_admin";
	public static final String MODERATOR = "civ.moderator";
	public static final String HELPER = "civ.helper";
	public static final String FREE_PERKS = "civ.freeperks";
	public static final String ECON = "civ.econ";
	
	public static void init(JavaPlugin plugin) throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.plugin = (CivCraft)plugin;
		
		// Check for required data folder, if it's not there export it.
		CivSettings.validateFiles();
		
		initRestrictedItems();
		initRestrictedUndoBlocks();
		initSwitchItems();
		initRestrictedSpawns();
		initVanillaHostileMobs();
		initBlockPlaceExceptions();
		initPlayerEntityWeapons();
		
		loadConfigFiles();
		loadConfigObjects();
		
		Perk.init();
		Unit.init();
		
		CivSettings.T1_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T1_leather_speed");
		CivSettings.T2_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T2_leather_speed");
		CivSettings.T3_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T3_leather_speed");
		CivSettings.T4_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T4_leather_speed");
		CivSettings.T5_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T5_leather_speed");
		
		CivSettings.T2_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T2_metal_speed");
		CivSettings.T3_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T3_metal_speed");
		CivSettings.T4_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T4_metal_speed");
		CivSettings.T5_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T5_metal_speed");
		
		CivSettings.normal_speed = 0.2f;
		
		for (Object obj : civConfig.getList("global.start_kit")) {
			if (obj instanceof String) {
				kitItems.add((String)obj);
			}
		}
		
		for (Object obj : structuredataConfig.getList("repairable_items")) {
			if (obj instanceof String) {
				repairableItems.add((String)obj);
			}
		}
		
		
		CivGlobal.banWords.add("fuck");
		CivGlobal.banWords.add("shit");
		CivGlobal.banWords.add("nigger");
		CivGlobal.banWords.add("faggot");
		CivGlobal.banWords.add("gay");
		CivGlobal.banWords.add("rape");
		CivGlobal.banWords.add("http");
		CivGlobal.banWords.add("cunt");
		
		iron_rate = CivSettings.getInteger(civConfig, "ore_rates.iron");
		gold_rate = CivSettings.getInteger(civConfig, "ore_rates.gold");
		diamond_rate = CivSettings.getInteger(civConfig, "ore_rates.diamond");
		emerald_rate = CivSettings.getInteger(civConfig, "ore_rates.emerald");
		startingCoins = CivSettings.getDouble(civConfig, "global.starting_coins");
		
		alwaysCrumble.add(CivData.BEDROCK);
		alwaysCrumble.add(CivData.GOLD_ORE);
		alwaysCrumble.add(CivData.IRON_ORE);
		alwaysCrumble.add(CivData.COAL_ORE);
		alwaysCrumble.add(CivData.LAPIS_BLOCK);
		alwaysCrumble.add(CivData.LAPIS_ORE);
		alwaysCrumble.add(CivData.GOLD_BLOCK);
		alwaysCrumble.add(CivData.IRON_BLOCK);
		alwaysCrumble.add(CivData.MOB_SPAWNER);
		alwaysCrumble.add(CivData.DIAMOND_ORE);
		alwaysCrumble.add(CivData.DIAMOND_BLOCK);
		alwaysCrumble.add(CivData.REDSTONE_ORE);
		alwaysCrumble.add(CivData.REDSTONE_ORE_GLOW);
		alwaysCrumble.add(CivData.EMERALD_ORE);
		alwaysCrumble.add(CivData.EMERALD_BLOCK);
		alwaysCrumble.add(CivData.COMMAND_BLOCK);
		alwaysCrumble.add(CivData.BEACON);
		alwaysCrumble.add(CivData.REDSTONE_BLOCK);
		alwaysCrumble.add(CivData.QUARTZ_ORE);
		alwaysCrumble.add(CivData.COAL_BLOCK);
		
		LoreEnhancement.init();
		LoreCraftableMaterial.buildStaticMaterials();
		LoreCraftableMaterial.buildRecipes();
		Template.initAttachableTypes();
		
		WarListener.addPlaceables();
		WarListener.AddFllowingBlocks();
	}
	
	private static void initRestrictedUndoBlocks() {
		restrictedUndoBlocks.add(Material.SAPLING);
		restrictedUndoBlocks.add(Material.POWERED_RAIL);
		restrictedUndoBlocks.add(Material.DETECTOR_RAIL);
		restrictedUndoBlocks.add(Material.LONG_GRASS);
		restrictedUndoBlocks.add(Material.DEAD_BUSH);
		restrictedUndoBlocks.add(Material.YELLOW_FLOWER);
		restrictedUndoBlocks.add(Material.RED_ROSE);
		restrictedUndoBlocks.add(Material.BROWN_MUSHROOM);
		restrictedUndoBlocks.add(Material.RED_MUSHROOM);
		restrictedUndoBlocks.add(Material.TORCH);
		restrictedUndoBlocks.add(Material.REDSTONE_WIRE);
		restrictedUndoBlocks.add(Material.CROPS);
		restrictedUndoBlocks.add(Material.SIGN_POST);
		restrictedUndoBlocks.add(Material.WOODEN_DOOR);
		restrictedUndoBlocks.add(Material.LADDER);
		restrictedUndoBlocks.add(Material.RAILS);
		restrictedUndoBlocks.add(Material.LEVER);
		restrictedUndoBlocks.add(Material.STONE_PLATE);
		restrictedUndoBlocks.add(Material.WALL_SIGN);
		restrictedUndoBlocks.add(Material.IRON_DOOR_BLOCK);
		restrictedUndoBlocks.add(Material.WOOD_PLATE);
		restrictedUndoBlocks.add(Material.REDSTONE_TORCH_OFF);
		restrictedUndoBlocks.add(Material.REDSTONE_TORCH_ON);
		restrictedUndoBlocks.add(Material.STONE_BUTTON);
		restrictedUndoBlocks.add(Material.CACTUS);
		restrictedUndoBlocks.add(Material.SUGAR_CANE_BLOCK);
		restrictedUndoBlocks.add(Material.DIODE_BLOCK_OFF);
		restrictedUndoBlocks.add(Material.DIODE_BLOCK_ON);
		restrictedUndoBlocks.add(Material.TRAP_DOOR);
		restrictedUndoBlocks.add(Material.PUMPKIN_STEM);
		restrictedUndoBlocks.add(Material.MELON_STEM);
		restrictedUndoBlocks.add(Material.COCOA);
		restrictedUndoBlocks.add(Material.TRIPWIRE_HOOK);
		restrictedUndoBlocks.add(Material.TRIPWIRE);
		restrictedUndoBlocks.add(Material.FLOWER_POT);
		restrictedUndoBlocks.add(Material.CARROT);
		restrictedUndoBlocks.add(Material.POTATO);
		restrictedUndoBlocks.add(Material.WOOD_BUTTON);
		restrictedUndoBlocks.add(Material.SKULL);
		restrictedUndoBlocks.add(Material.GOLD_PLATE);
		restrictedUndoBlocks.add(Material.IRON_PLATE);
		restrictedUndoBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
		restrictedUndoBlocks.add(Material.REDSTONE_COMPARATOR_ON);
		restrictedUndoBlocks.add(Material.ACTIVATOR_RAIL);
		restrictedUndoBlocks.add(Material.IRON_TRAPDOOR);
		restrictedUndoBlocks.add(Material.CARPET);
		restrictedUndoBlocks.add(Material.DOUBLE_PLANT);
		restrictedUndoBlocks.add(Material.SPRUCE_DOOR);
		restrictedUndoBlocks.add(Material.BIRCH_DOOR);
		restrictedUndoBlocks.add(Material.JUNGLE_DOOR);
		restrictedUndoBlocks.add(Material.ACACIA_DOOR);
		restrictedUndoBlocks.add(Material.DARK_OAK_DOOR);
		restrictedUndoBlocks.add(Material.END_ROD);
		restrictedUndoBlocks.add(Material.CHORUS_PLANT);
		restrictedUndoBlocks.add(Material.CHORUS_FLOWER);
		restrictedUndoBlocks.add(Material.BEETROOT_BLOCK);
		
		restrictedUndoBlocks.add(Material.PISTON_BASE);
		restrictedUndoBlocks.add(Material.PISTON_EXTENSION);
		restrictedUndoBlocks.add(Material.PISTON_MOVING_PIECE);
		restrictedUndoBlocks.add(Material.PISTON_STICKY_BASE);
		
	}

	private static void initPlayerEntityWeapons() {
		playerEntityWeapons.add(EntityType.PLAYER);
		playerEntityWeapons.add(EntityType.ARROW);
		playerEntityWeapons.add(EntityType.SPECTRAL_ARROW);
		playerEntityWeapons.add(EntityType.TIPPED_ARROW);
		playerEntityWeapons.add(EntityType.EGG);
		playerEntityWeapons.add(EntityType.ENDER_PEARL);
		playerEntityWeapons.add(EntityType.SNOWBALL);
		playerEntityWeapons.add(EntityType.SPLASH_POTION);
		playerEntityWeapons.add(EntityType.LINGERING_POTION);
		playerEntityWeapons.add(EntityType.FISHING_HOOK);
	}
	
	public static void validateFiles() {
		File data = new File(plugin.getDataFolder().getPath()+"/data");
		if (!data.exists()) {
			data.mkdirs();
		}
	}
	
	public static void streamResourceToDisk(String filepath) throws IOException {
		URL inputUrl = plugin.getClass().getResource(filepath);
		File dest = new File(plugin.getDataFolder().getPath()+filepath);
		if (inputUrl == null) {
			CivLog.error("Destination is null: "+filepath);
		} else {
			FileUtils.copyURLToFile(inputUrl, dest);
		}
	}

	public static FileConfiguration loadCivConfig(String filepath) throws FileNotFoundException, IOException, InvalidConfigurationException {
		File file = new File(plugin.getDataFolder().getPath()+"/data/"+filepath);
		if (!file.exists()) {
			CivLog.warning("Configuration file:"+filepath+" was missing. Streaming to disk from Jar.");
			streamResourceToDisk("/data/"+filepath);
		}
		
		CivLog.info("Loading Configuration file:"+filepath);
		// read the config.yml into memory
		YamlConfiguration cfg = new YamlConfiguration(); 
		cfg.load(file);
		return cfg;
	}
	
	public static void reloadGovConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.governments.clear();
		governmentConfig = loadCivConfig("governments.yml");
		ConfigGovernment.loadConfig(governmentConfig, governments);
	}
	
	public static void reloadMobConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.customMobs.clear();
		mobConfig = loadCivConfig("mobs.yml");
		ConfigMobsCustom.loadConfig(mobConfig, customMobs);
	}
	
	public static void reloadNewspaperConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.newspapers.clear();
		gameConfig = loadCivConfig("game.yml");
		ConfigNewspaper.loadConfig(gameConfig, newspapers);
	}
	
	public static void reloadMaterialConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.materials.clear();
		CivSettings.removedRecipies.clear();
		materialsConfig = loadCivConfig("materials.yml");
		ConfigMaterial.loadConfig(materialsConfig, materials);
		ConfigRemovedRecipes.removeRecipes(materialsConfig, removedRecipies);
	}
	
	public static void reloadTechConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.techs.clear();
		CivSettings.techPotions.clear();
		techsConfig = loadCivConfig("techs.yml");
		ConfigTech.loadConfig(techsConfig, techs);
		ConfigTechPotion.loadConfig(techsConfig, techPotions);
	}
	
	public static void reloadStructureConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.structures.clear();
		CivSettings.wonders.clear();
		CivSettings.grocerLevels.clear();
		CivSettings.templeSacrifices.clear();
		CivSettings.stableItems.clear();
		CivSettings.horses.clear();
		structureConfig = loadCivConfig("structures.yml");
		wonderConfig = loadCivConfig("wonders.yml");
		ConfigBuildableInfo.loadConfig(structureConfig, "structures", structures, false);
		ConfigBuildableInfo.loadConfig(wonderConfig, "wonders", wonders, true);
		ConfigGrocerLevel.loadConfig(structureConfig, grocerLevels);
		ConfigTempleSacrifice.loadConfig(structureConfig, templeSacrifices);
		ConfigStableItem.loadConfig(structureConfig, stableItems);
		ConfigStableHorse.loadConfig(structureConfig, horses);
		
		CivSettings.mineTasks.clear();
		CivSettings.mineLevels.clear();
		CivSettings.labTasks.clear();
		CivSettings.labLevels.clear();
		CivSettings.bankLevels.clear();
		CivSettings.lumbermillItems.clear();
		CivSettings.lumbermillDrops.clear();
		CivSettings.quarryItems.clear();
		CivSettings.quarryDrops.clear();
		CivSettings.trommelItems.clear();
		CivSettings.trommelDrops.clear();
		CivSettings.granaryFood.clear();
		CivSettings.granaryTasks.clear();
		CivSettings.granaryLevels.clear();
		structuredataConfig = loadCivConfig("structuredata.yml");
		ConfigMineTask.loadConfig(structuredataConfig, mineTasks);
		ConfigMineLevel.loadConfig(structuredataConfig, mineLevels);
		ConfigLabTask.loadConfig(structuredataConfig, labTasks);
		ConfigLabLevel.loadConfig(structuredataConfig, labLevels);
		ConfigBankLevel.loadConfig(structuredataConfig, bankLevels);
		ConfigLumberMillItem.loadConfig(structuredataConfig, lumbermillItems);
		ConfigLumberMill.loadConfig(structuredataConfig, lumbermillDrops);
		ConfigQuarryItem.loadConfig(structuredataConfig, quarryItems);
		ConfigQuarry.loadConfig(structuredataConfig, quarryDrops);
		ConfigTrommelItem.loadConfig(structuredataConfig, trommelItems);
		ConfigTrommel.loadConfig(structuredataConfig, trommelDrops);
		ConfigGranaryFood.loadConfig(structuredataConfig, granaryFood);
		ConfigGranaryTask.loadConfig(structuredataConfig, granaryTasks);
		ConfigGranaryLevel.loadConfig(structuredataConfig, granaryLevels);
	}
	
	private static void loadConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException {
		campConfig = loadCivConfig("camp.yml");
		civConfig = loadCivConfig("civ.yml");
		cultureConfig = loadCivConfig("culture.yml");
		gameConfig = loadCivConfig("game.yml");
		mobConfig = loadCivConfig("mobs.yml");
		structureConfig = loadCivConfig("structures.yml");
		structuredataConfig = loadCivConfig("structuredata.yml");
		techsConfig = loadCivConfig("techs.yml");
		townConfig = loadCivConfig("town.yml");
		goodsConfig = loadCivConfig("goods.yml");
		buffConfig = loadCivConfig("buffs.yml");
		governmentConfig = loadCivConfig("governments.yml");
		warConfig = loadCivConfig("war.yml");
		wonderConfig = loadCivConfig("wonders.yml");
		unitConfig = loadCivConfig("units.yml");
		espionageConfig = loadCivConfig("espionage.yml");
		scoreConfig = loadCivConfig("score.yml");
		perkConfig = loadCivConfig("perks.yml");
		enchantConfig = loadCivConfig("enchantments.yml");
		marketConfig = loadCivConfig("market.yml");
		happinessConfig = loadCivConfig("happiness.yml");
		materialsConfig = loadCivConfig("materials.yml");
		randomEventsConfig = loadCivConfig("randomevents.yml");
		nocheatConfig = loadCivConfig("nocheat.yml");
		fishingConfig = loadCivConfig("fishing.yml");
	}

	private static void loadConfigObjects() throws InvalidConfiguration {
		ConfigNewspaper.loadConfig(gameConfig, newspapers);
		ConfigEXPMining.loadConfig(gameConfig, resxpMiningBlocks);
		
		ConfigMobsCustom.loadConfig(mobConfig, customMobs);
		
		ConfigMineTask.loadConfig(structuredataConfig, mineTasks);
		ConfigMineLevel.loadConfig(structuredataConfig, mineLevels);
		ConfigLabTask.loadConfig(structuredataConfig, labTasks);
		ConfigLabLevel.loadConfig(structuredataConfig, labLevels);
		ConfigBankLevel.loadConfig(structuredataConfig, bankLevels);
		ConfigLumberMillItem.loadConfig(structuredataConfig, lumbermillItems);
		ConfigLumberMill.loadConfig(structuredataConfig, lumbermillDrops);
		ConfigQuarryItem.loadConfig(structuredataConfig, quarryItems);
		ConfigQuarry.loadConfig(structuredataConfig, quarryDrops);
		ConfigTrommelItem.loadConfig(structuredataConfig, trommelItems);
		ConfigTrommel.loadConfig(structuredataConfig, trommelDrops);
		ConfigGranaryFood.loadConfig(structuredataConfig, granaryFood);
		ConfigGranaryTask.loadConfig(structuredataConfig, granaryTasks);
		ConfigGranaryLevel.loadConfig(structuredataConfig, granaryLevels);
		
		ConfigTownLevel.loadConfig(townConfig, townLevels);
		ConfigTownUpgrade.loadConfig(townConfig, townUpgrades);
		
		ConfigCampLonghouseLevel.loadConfig(campConfig, longhouseLevels);
		ConfigCampUpgrade.loadConfig(campConfig, campUpgrades);
		
		ConfigCultureLevel.loadConfig(cultureConfig, cultureLevels);
		ConfigBuildableInfo.loadConfig(structureConfig, "structures", structures, false);
		ConfigBuildableInfo.loadConfig(wonderConfig, "wonders", wonders, true);
		ConfigTech.loadConfig(techsConfig, techs);
		ConfigTechPotion.loadConfig(techsConfig, techPotions);
		ConfigHemisphere.loadConfig(goodsConfig, hemispheres);
		ConfigBuff.loadConfig(buffConfig, buffs);
		ConfigWonderBuff.loadConfig(wonderConfig, wonderBuffs);
		ConfigTradeGood.loadConfig(goodsConfig, goods, landGoods, waterGoods);
		ConfigGrocerLevel.loadConfig(structureConfig, grocerLevels);
		ConfigCottageLevel.loadConfig(structuredataConfig, cottageLevels);
		ConfigTempleSacrifice.loadConfig(structureConfig, templeSacrifices);
		ConfigGovernment.loadConfig(governmentConfig, governments);
		ConfigEnchant.loadConfig(enchantConfig, enchants);
		ConfigUnit.loadConfig(unitConfig, units);
		ConfigMission.loadConfig(espionageConfig, missions);
		ConfigPerk.loadConfig(perkConfig, perks);
		ConfigMarketItem.loadConfig(marketConfig, marketItems);
		ConfigStableItem.loadConfig(structureConfig, stableItems);
		ConfigStableHorse.loadConfig(structureConfig, horses);
		ConfigTownHappinessLevel.loadConfig(happinessConfig, townHappinessLevels);
		ConfigHappinessState.loadConfig(happinessConfig, happinessStates);
		ConfigCultureBiomeInfo.loadConfig(cultureConfig, cultureBiomes);
		ConfigMaterial.loadConfig(materialsConfig, materials);
		ConfigRandomEvent.loadConfig(randomEventsConfig, randomEvents, randomEventIDs);
		ConfigEndCondition.loadConfig(civConfig, endConditions);
		ConfigFishing.loadConfig(fishingConfig, fishingDrops);
		ConfigRemovedRecipes.removeRecipes(materialsConfig, removedRecipies);
		CivGlobal.preGenerator.preGenerate();
		Wall.init_settings();
	}

	private static void initRestrictedSpawns() {
		restrictedSpawns.add(EntityType.BAT);
		restrictedSpawns.add(EntityType.BLAZE);
		restrictedSpawns.add(EntityType.CAVE_SPIDER);
		restrictedSpawns.add(EntityType.ELDER_GUARDIAN);
		restrictedSpawns.add(EntityType.ENDERMAN);
		restrictedSpawns.add(EntityType.EVOKER);
		restrictedSpawns.add(EntityType.EVOKER_FANGS);
		restrictedSpawns.add(EntityType.GHAST);
		restrictedSpawns.add(EntityType.ILLUSIONER);
		restrictedSpawns.add(EntityType.MAGMA_CUBE);
		restrictedSpawns.add(EntityType.PIG_ZOMBIE);
		restrictedSpawns.add(EntityType.SHULKER);
		restrictedSpawns.add(EntityType.SILVERFISH);
		restrictedSpawns.add(EntityType.VEX);
		restrictedSpawns.add(EntityType.VINDICATOR);
		restrictedSpawns.add(EntityType.WITCH);
		restrictedSpawns.add(EntityType.WITHER_SKELETON);
	}
	
	private static void initVanillaHostileMobs() {
		vanillaHostileMobs.add(EntityType.CREEPER);
		vanillaHostileMobs.add(EntityType.ENDERMITE);
		vanillaHostileMobs.add(EntityType.GUARDIAN);
		vanillaHostileMobs.add(EntityType.HUSK);
		vanillaHostileMobs.add(EntityType.SKELETON);
		vanillaHostileMobs.add(EntityType.SPIDER);
		vanillaHostileMobs.add(EntityType.STRAY);
		vanillaHostileMobs.add(EntityType.ZOMBIE);
		vanillaHostileMobs.add(EntityType.ZOMBIE_VILLAGER);
	}
	
	private static void initRestrictedItems() {
		// TODO make this configurable? 
		restrictedItems.put(Material.FLINT_AND_STEEL, 0);
		restrictedItems.put(Material.BUCKET, 0);
		restrictedItems.put(Material.WATER_BUCKET, 0);
		restrictedItems.put(Material.LAVA_BUCKET, 0);
		restrictedItems.put(Material.CAKE_BLOCK, 0);
		restrictedItems.put(Material.CAULDRON, 0);
		restrictedItems.put(Material.DIODE, 0);
		restrictedItems.put(Material.INK_SACK, 0);
		restrictedItems.put(Material.ITEM_FRAME, 0);
		restrictedItems.put(Material.PAINTING, 0);
		restrictedItems.put(Material.SHEARS, 0);
		restrictedItems.put(Material.STATIONARY_LAVA, 0);
		restrictedItems.put(Material.STATIONARY_WATER, 0);
		restrictedItems.put(Material.TNT, 0);
	}

	private static void initSwitchItems() {
		switchItems.add(CivData.SAPLING);
		switchItems.add(CivData.LEAF);
		switchItems.add(CivData.GLASS);
		switchItems.add(CivData.DISPENSER);
		switchItems.add(CivData.BED_BLOCK);
		switchItems.add(CivData.RAIL_POWERED);
		switchItems.add(CivData.RAIL_DETECTOR);
		switchItems.add(CivData.COBWEB);
		switchItems.add(CivData.TALL_GRASS);
		switchItems.add(CivData.DEAD_BUSH);
		switchItems.add(CivData.DANDELION);
		switchItems.add(CivData.OTHER_FLOWERS);
		switchItems.add(CivData.BROWN_MUSHROOM);
		switchItems.add(CivData.RED_MUSHROOM);
		switchItems.add(CivData.TNT);
		switchItems.add(CivData.TORCH);
		switchItems.add(CivData.CHEST);
		switchItems.add(CivData.REDSTONE_WIRE);
		switchItems.add(CivData.WORKBENCH);
		switchItems.add(CivData.WHEAT_CROP);
		switchItems.add(CivData.FURNACE);
		switchItems.add(CivData.FURNACE_LIT);
		switchItems.add(CivData.SIGN);
		switchItems.add(CivData.WOOD_DOOR);
		switchItems.add(CivData.LADDER);
		switchItems.add(CivData.RAIL);
		switchItems.add(CivData.WALL_SIGN);
		switchItems.add(CivData.LEVER);
		switchItems.add(CivData.STONE_PLATE);
		switchItems.add(CivData.IRON_DOOR);
		switchItems.add(CivData.WOOD_PLATE);
		switchItems.add(CivData.REDSTONE_TORCH_OFF);
		switchItems.add(CivData.REDSTONE_TORCH_ON);
		switchItems.add(CivData.STONE_BUTTON);
		switchItems.add(CivData.CACTUS);
		switchItems.add(CivData.SUGARCANE_BLOCK);
		switchItems.add(CivData.CAKE_BLOCK);
		switchItems.add(CivData.REPEATER_OFF);
		switchItems.add(CivData.REPEATER_ON);
		switchItems.add(CivData.TRAPDOOR);
		switchItems.add(CivData.PUMPKIN_STEM);
		switchItems.add(CivData.MELON_STEM);
		switchItems.add(CivData.VINE);
		switchItems.add(CivData.OAK_GATE);
		switchItems.add(CivData.LILY_PAD);
		switchItems.add(CivData.NETHERWART_CROP);
		switchItems.add(CivData.ENCHANTMENT_TABLE);
		switchItems.add(CivData.BREWING_STAND_BLOCK);
		switchItems.add(CivData.CAULDRON_BLOCK);
		switchItems.add(CivData.COCOA_CROP);
		switchItems.add(CivData.ENDER_CHEST);
		switchItems.add(CivData.TRIPWIRE_HOOK);
		switchItems.add(CivData.BEACON);
		switchItems.add(CivData.FLOWER_POT);
		switchItems.add(CivData.CARROT_CROP);
		switchItems.add(CivData.POTATO_CROP);
		switchItems.add(CivData.WOOD_BUTTON);
		switchItems.add(CivData.ANVIL);
		switchItems.add(CivData.TRAPPED_CHEST);
		switchItems.add(CivData.GOLD_PLATE);
		switchItems.add(CivData.IRON_PLATE);
		switchItems.add(CivData.COMPARATOR_OFF);
		switchItems.add(CivData.COMPARATOR_ON);
		switchItems.add(CivData.HOPPER);
		switchItems.add(CivData.RAIL_ACTIVATOR);
		switchItems.add(CivData.DROPPER);
		switchItems.add(CivData.LEAF2);
		switchItems.add(CivData.IRON_TRAPDOOR);
		switchItems.add(CivData.CARPET);
		switchItems.add(CivData.DOUBLE_FLOWER);
		switchItems.add(CivData.SPRUCE_GATE);
		switchItems.add(CivData.BIRCH_GATE);
		switchItems.add(CivData.JUNGLE_GATE);
		switchItems.add(CivData.DARK_OAK_GATE);
		switchItems.add(CivData.ACACIA_GATE);
		switchItems.add(CivData.SPRUCE_DOOR);
		switchItems.add(CivData.BIRCH_DOOR);
		switchItems.add(CivData.JUNGLE_DOOR);
		switchItems.add(CivData.ACACIA_DOOR);
		switchItems.add(CivData.DARK_OAK_DOOR);
		switchItems.add(CivData.BEETROOT_CROP);
	}
	
	private static void initBlockPlaceExceptions() {
		/* These blocks can be placed regardless of permissions.
		 * this is currently used only for blocks that are generated
		 * by specific events such as portal or fire creation.
		 */
		blockPlaceExceptions.put(Material.FIRE, 0);
		blockPlaceExceptions.put(Material.PORTAL, 0);
	}
	
	public static String getStringBase(String path) throws InvalidConfiguration {
		return getString(plugin.getConfig(), path);
	}
	
	public static double getDoubleTown(String path) throws InvalidConfiguration {
		return getDouble(townConfig, path);
	}
	
	public static double getDoubleCiv(String path) throws InvalidConfiguration {
		return getDouble(civConfig, path);
	}
	
	public static void saveGenID(String gen_id) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plugins/CivCraft/genid.data")));
			writer.write(gen_id);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getGenID() {
		String genid = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader("plugins/CivCraft/genid.data"));
			genid = br.readLine();
			br.close();
		} catch (IOException e) {
		}
		return genid;
	}
	
	public static Double getDoubleStructure(String path) {
		Double ret;
		try {
			ret = getDouble(structureConfig, path);
		} catch (InvalidConfiguration e) {
			ret = 0.0;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static int getIntegerGameConfig(String path) {
		Integer ret;
		try {
			ret = getInteger(gameConfig, path);
		} catch (InvalidConfiguration e) {
			ret = 0;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static int getIntegerStructure(String path) {
		Integer ret;
		try {
			ret = getInteger(structureConfig, path);
		} catch (InvalidConfiguration e) {
			ret = 0;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Integer getIntegerGovernment(String path) {
		Integer ret;
		try {
			ret = getInteger(governmentConfig, path);
		} catch (InvalidConfiguration e) {
			ret = 0;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Integer getInteger(FileConfiguration cfg, String path) throws InvalidConfiguration {
		if (!cfg.contains(path)) {
			throw new InvalidConfiguration("Could not get configuration integer "+path);
		}
		return cfg.getInt(path);
	}
	
	public static double getDouble(FileConfiguration cfg, String path) throws InvalidConfiguration {
		if (!cfg.contains(path)) {
			throw new InvalidConfiguration("Could not get configuration double "+path);
		}
		return cfg.getDouble(path);
	}
	
	public static String getString(FileConfiguration cfg, String path) throws InvalidConfiguration {
		if (!cfg.contains(path)) {
			throw new InvalidConfiguration("Could not get configuration string "+path);
		}
		return cfg.getString(path);
	}
	
	public static boolean getBoolean(FileConfiguration cfg, String path) throws InvalidConfiguration {
		if (!cfg.contains(path)) {
			throw new InvalidConfiguration("Could not get configuration boolean "+path);
		}
		return cfg.getBoolean(path);
	}
	
	public static int getMaxNameLength() {
		// TODO make this configurable?
		return 32;
	}
	
	public static ConfigTownUpgrade getUpgradeByName(String name) {
		for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
			if (upgrade.name.equalsIgnoreCase(name)) {
				return upgrade;
			}
		}
		return null;
	}
	
	public static ConfigHappinessState getHappinessState(int amount) {
		ConfigHappinessState closestState = happinessStates.get(0);
		
		for (int i = 0; i < happinessStates.size(); i++) {
			ConfigHappinessState state = happinessStates.get(i);
			if (amount >= state.amount) {
				closestState = state;
			}
		}
		
		return closestState;
	}
	
	public static ConfigTownUpgrade getUpgradeByNameRegex(Town town, String name) throws CivException {
		ConfigTownUpgrade returnUpgrade = null;
		for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
			if (!upgrade.isAvailable(town)) {
				continue;
			}
			
			if (name.equalsIgnoreCase(upgrade.name)) {
				return upgrade;
			}
			
			String loweredUpgradeName = upgrade.name.toLowerCase();
			String loweredName = name.toLowerCase();
			
			if (loweredUpgradeName.contains(loweredName)) {
				if (returnUpgrade == null) {
					returnUpgrade = upgrade;
				} else {
					throw new CivException(name+" is not specific enough to single out only one upgrade.");
				}
			}
		}
		return returnUpgrade;
	}
	
	public static ConfigTownUpgrade getUpgradeByNameRegexSpecial(Town town, String name) throws CivException {
		ConfigTownUpgrade returnUpgrade = null;
		for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
			if (name.equalsIgnoreCase(upgrade.name)) {
				return upgrade;
			}
			
			String loweredUpgradeName = upgrade.name.toLowerCase();
			String loweredName = name.toLowerCase();
			
			if (loweredUpgradeName.contains(loweredName)) {
				if (returnUpgrade == null) {
					returnUpgrade = upgrade;
				} else {
					throw new CivException(name+" is not specific enough to single out only one upgrade.");
				}
			}
		}
		return returnUpgrade;
	}
	
	public static ConfigCampUpgrade getCampUpgradeByNameRegex(Camp camp, String name) throws CivException {
		ConfigCampUpgrade returnUpgrade = null;
		for (ConfigCampUpgrade upgrade : campUpgrades.values()) {
			if (!upgrade.isAvailable(camp)) {
				continue;
			}
			
			if (name.equalsIgnoreCase(upgrade.name)) {
				return upgrade;
			}
			
			String loweredUpgradeName = upgrade.name.toLowerCase();
			String loweredName = name.toLowerCase();
			
			if (loweredUpgradeName.contains(loweredName)) {
				if (returnUpgrade == null) {
					returnUpgrade = upgrade;
				} else {
					throw new CivException(name+" is not specific enough to single out only one upgrade.");
				}
			}
		}
		return returnUpgrade;
	}
	
	public static ConfigBuildableInfo getBuildableInfoByName(String fullArgs) {
		for (ConfigBuildableInfo sinfo : structures.values()) {
			if (sinfo.displayName.equalsIgnoreCase(fullArgs)) {
				return sinfo;
			}
		}
		
		for (ConfigBuildableInfo sinfo : wonders.values()) {
			if (sinfo.displayName.equalsIgnoreCase(fullArgs)) {
				return sinfo;
			}
		}
		
		return null;
	}

	public static ConfigTech getTechByName(String techname) {
		for (ConfigTech tech : techs.values()) {
			if (tech.name.equalsIgnoreCase(techname)) {
				return tech;
			}
		}
		return null;
	}
	
	public static ConfigTech getTechByID(String tech_id) {
		for (ConfigTech tech : techs.values()) {
			if (tech.id.equalsIgnoreCase(tech_id)) {
				return tech;
			}
		}
		return null;
	}
	
	public static int getCottageMaxLevel() {
		int returnLevel = 0;
		for (Integer level : cottageLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		return returnLevel;
	}
	
	public static int getMineMaxLevel() {
		int returnLevel = 0;
		for (Integer level : mineLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		return returnLevel;
	}
	
	public static int getMaxCultureLevel() {
		int returnLevel = 0;
		for (Integer level : cultureLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		return returnLevel;
	}
	
	public static ConfigCultureBiomeInfo getCultureBiome(String name) {
		ConfigCultureBiomeInfo biomeInfo = cultureBiomes.get(name);
		if (biomeInfo == null) {
			biomeInfo = cultureBiomes.get("UNKNOWN");
		}
		
		return biomeInfo;
	}

	
	
	
}
