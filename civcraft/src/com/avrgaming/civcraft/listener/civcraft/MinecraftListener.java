package com.avrgaming.civcraft.listener.civcraft;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Stray;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigEXPMining;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.object.ResidentExperience.EXPSlots;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;

/* https://github.com/gvlfm78/BukkitOldCombatMechanics */

public class MinecraftListener implements Listener {
	
	//XXX Player-Bound Aspect
	
	private static HashMap<Material, ArrayList<Integer>> food_values = new HashMap<Material, ArrayList<Integer>>();
	
	public static void setupFoodValues() {
		ArrayList<Integer> raw_fish = new ArrayList<Integer>();
		raw_fish.add(2); raw_fish.add(7); raw_fish.add(0);
		food_values.put(Material.RAW_FISH, raw_fish);
		
		ArrayList<Integer> raw_salmon = new ArrayList<Integer>();
		raw_salmon.add(2); raw_salmon.add(8); raw_salmon.add(1);
		food_values.put(Material.RAW_FISH, raw_salmon);
		
		ArrayList<Integer> cooked_fish = new ArrayList<Integer>();
		cooked_fish.add(5); cooked_fish.add(65);cooked_fish.add(0);
		food_values.put(Material.COOKED_FISH, cooked_fish);
		
		ArrayList<Integer> cooked_salmon = new ArrayList<Integer>();
		cooked_salmon.add(6); cooked_salmon.add(90); cooked_salmon.add(1);
		food_values.put(Material.COOKED_FISH, cooked_salmon);
		
		ArrayList<Integer> melon = new ArrayList<Integer>();
		melon.add(2); melon.add(15); melon.add(0);
		food_values.put(Material.MELON, melon);
		
		ArrayList<Integer> steak = new ArrayList<Integer>();
		steak.add(8); steak.add(128); steak.add(0);
		food_values.put(Material.COOKED_BEEF, steak);
		
		ArrayList<Integer> gold_carrot = new ArrayList<Integer>();
		gold_carrot.add(7); gold_carrot.add(164); gold_carrot.add(0);
		food_values.put(Material.GOLDEN_CARROT, gold_carrot);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerConsumeItem(PlayerItemConsumeEvent event) throws CivException {
		ItemStack stack = event.getItem();
		if (food_values.containsKey(stack.getType())) {
			ArrayList<Integer> food = food_values.get(stack.getType());
			if (stack.getDurability() != food.get(2)) return;
			
			Player p = event.getPlayer();
			event.setCancelled(true);
			
			if (p.getInventory().getItemInMainHand().getType() == stack.getType()) {
				p.getInventory().getItemInMainHand().setAmount(stack.getAmount()-1);
			} else if (p.getInventory().getItemInOffHand().getType() == stack.getType()) {
				p.getInventory().getItemInOffHand().setAmount(stack.getAmount()-1);
			} else {
				CivMessage.send(p, "How do you eat? Like, you broke the game here lad!");
			}
			
			Integer foodAmt = p.getFoodLevel()+food.get(0);
			float SaturateAmt = (float) (p.getSaturation()+((double)food.get(1)/10));
			if (foodAmt >= 20) {
				int toSaturate = foodAmt - 20;
				SaturateAmt += toSaturate;
				foodAmt = 20;
			}
			p.setFoodLevel((int) foodAmt);
			p.setSaturation(SaturateAmt);
		}
	}
	
	private final BlockFace[] faces = new BlockFace[] {
			BlockFace.SELF, BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.EAST,
			BlockFace.SOUTH, BlockFace.WEST
	 };

	public BlockCoord generatesCobble(int id, Block b) {
		int mirrorID1 = (id == CivData.WATER_RUNNING || id == CivData.WATER_STILL ? CivData.LAVA_RUNNING : CivData.WATER_RUNNING);
		int mirrorID2 = (id == CivData.WATER_RUNNING || id == CivData.WATER_STILL ? CivData.LAVA_STILL : CivData.WATER_STILL);
		int mirrorID3 = (id == CivData.WATER_RUNNING || id == CivData.WATER_STILL ? CivData.LAVA_RUNNING : CivData.WATER_STILL);
		int mirrorID4 = (id == CivData.WATER_RUNNING || id == CivData.WATER_STILL ? CivData.LAVA_STILL : CivData.WATER_RUNNING);
		for (BlockFace face : faces) {
			Block r = b.getRelative(face, 1);
			if(ItemManager.getId(r) == mirrorID1 || ItemManager.getId(r) == mirrorID2 || ItemManager.getId(r) == mirrorID3 || ItemManager.getId(r) == mirrorID4) {
				return new BlockCoord(r);
			}
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnBlockFromToEvent(BlockFromToEvent event) {
		// Disable cobblestone generators
		int id = ItemManager.getId(event.getBlock());
		if(id >= CivData.WATER_STILL && id <= CivData.LAVA_STILL) {
			Block b = event.getToBlock();
			int toid = ItemManager.getId(b);
			if (toid == CivData.COBBLESTONE || toid == CivData.OBSIDIAN) {
				BlockCoord other = generatesCobble(id, b);
				if(other != null && other.getBlock().getType() != Material.AIR) {
					other.getBlock().setType(Material.NETHERRACK);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnBlockFormEvent(BlockFormEvent event) {
		// Disable cobblestone generators
		if (ItemManager.getId(event.getNewState()) == CivData.COBBLESTONE || ItemManager.getId(event.getNewState()) == CivData.OBSIDIAN) {
			ItemManager.setTypeId(event.getNewState(), CivData.NETHERRACK);
			return;
		}
	}
	
	// Crops:
	// https://github.com/YourCoal/Project/commit/546c1b127f3097e3555836a9d70b31d1144e176b#diff-b2e10f17867f39d39fd9c7e456535a04
	// https://github.com/YourCoal/Project/commit/447b4145b4f087fe7c78e2ea39bac957ccab7a9f#diff-b2e10f17867f39d39fd9c7e456535a04
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreakSpawnItems(BlockBreakEvent event) throws CivException {
		Player p = event.getPlayer();
		Random rand = new Random();
		Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
		
		// Quest XP First, then check later for custom drops.
		for (ConfigEXPMining m : CivSettings.resxpMiningBlocks.values()) {
			if (ItemManager.getId(event.getBlock().getType()) == m.id) {
				if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
				ResidentExperience re = CivGlobal.getResidentE(p);
				double mod = (re.getEXPLevel(EXPSlots.MINING)+1) / 2;
				int eEXP = (int) (event.getExpToDrop()*mod) / 2;
				if (eEXP >= 1) ItemManager.dropPlayerEXP(p, dropLoc, eEXP);
				re.addResEXP(EXPSlots.MINING, m.resxp);
			}
		}
		
		// Custom Drops check now.
		
		if (event.getBlock().getType().equals(Material.COAL_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				int level = 0;
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Coal Drops
				int min_coal = CivSettings.getInteger(CivSettings.gameConfig, "coal.min_drop");
				int max_coal = CivSettings.getInteger(CivSettings.gameConfig, "coal.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "coal.max_drop_fortune") * level);
				
				if (max_coal < min_coal) max_coal = min_coal;
				int rand_coal = rand.nextInt(max_coal)+1;
				if (rand_coal < min_coal) rand_coal = min_coal;
				
				ItemStack stack_coal = new ItemStack(Material.COAL);
				ItemManager.givePlayerItem(p, stack_coal, dropLoc, stack_coal.getItemMeta().getDisplayName(), rand_coal, true);
				
				// Hammer Drops
				int min_coalHam = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.min_drop");
				int max_coalHam = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.max_drop_fortune") * level);
				
				if (max_coalHam < min_coalHam) max_coalHam = min_coalHam;
				int rand_ham = rand.nextInt(max_coalHam)+1;
				if (rand_ham < min_coalHam) rand_ham = min_coalHam;
				// Just to make getting hammers a little harder
					int newRand = rand.nextInt(3);
					if (newRand != 0) rand_ham = 0;
				
				ItemStack stack_ham = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hammers"));
				ItemManager.givePlayerItem(p, stack_ham, dropLoc, stack_ham.getItemMeta().getDisplayName(), rand_ham, true);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.REDSTONE_ORE) || event.getBlock().getType().equals(Material.GLOWING_REDSTONE_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				int level = 0;
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Redstone Drops
				int min_red = CivSettings.getInteger(CivSettings.gameConfig, "redstone.min_drop");
				int max_red = CivSettings.getInteger(CivSettings.gameConfig, "redstone.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "redstone.max_drop_fortune") * level);
				
				if (max_red < min_red) max_red = min_red;
				int rand_red = rand.nextInt(max_red)+1;
				if (rand_red < min_red) rand_red = min_red;
				
				ItemStack stack_red = new ItemStack(Material.REDSTONE);
				ItemManager.givePlayerItem(p, stack_red, dropLoc, stack_red.getItemMeta().getDisplayName(), rand_red, true);
				
				// Hammer Drops
				int min_redBek = CivSettings.getInteger(CivSettings.gameConfig, "redstone_beakers.min_drop");
				int max_redBek = CivSettings.getInteger(CivSettings.gameConfig, "redstone_beakers.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "redstone_beakers.max_drop_fortune") * level);
				
				if (max_redBek < min_redBek) max_redBek = min_redBek;
				int rand_bek = rand.nextInt(max_redBek)+1;
				if (rand_bek < min_redBek) rand_bek = min_redBek;
				// Just to make getting beakers a little harder
					int newRand = rand.nextInt(3);
					if (newRand != 0) rand_bek = 0;
				
				ItemStack stack_bek = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_beakers"));
				ItemManager.givePlayerItem(p, stack_bek, dropLoc, stack_bek.getItemMeta().getDisplayName(), rand_bek, true);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.LAPIS_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				int level = 0;
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Lapis Drops
				int min_lap = CivSettings.getInteger(CivSettings.gameConfig, "lapis.min_drop");
				int max_lap = CivSettings.getInteger(CivSettings.gameConfig, "lapis.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "lapis.max_drop_fortune") * level);
				
				if (max_lap < min_lap) max_lap = min_lap;
				int rand_lap = rand.nextInt(max_lap)+1;
				if (rand_lap < min_lap) rand_lap = min_lap;
				
				ItemStack stack_lap = new ItemStack(Material.INK_SACK, 1, (short)4);
				ItemManager.givePlayerItem(p, stack_lap, dropLoc, stack_lap.getItemMeta().getDisplayName(), rand_lap, true);
				
				// Beaker Drops
				int min_lapBek = CivSettings.getInteger(CivSettings.gameConfig, "lapis_beakers.min_drop");
				int max_lapBek = CivSettings.getInteger(CivSettings.gameConfig, "lapis_beakers.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "lapis_beakers.max_drop_fortune") * level);
				
				if (max_lapBek < min_lapBek) max_lapBek = min_lapBek;
				int rand_bek = rand.nextInt(max_lapBek)+1;
				if (rand_bek < min_lapBek) rand_bek = min_lapBek;
				// Just to make getting beakers a little harder
					int newRand = rand.nextInt(3);
					if (newRand != 0) rand_bek = 0;
				
				ItemStack stack_bek = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_beakers"));
				ItemManager.givePlayerItem(p, stack_bek, dropLoc, stack_bek.getItemMeta().getDisplayName(), rand_bek, true);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.DIAMOND_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				int level = 0;
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Diamond Drops
				int min_dia = CivSettings.getInteger(CivSettings.gameConfig, "diamond.min_drop");
				int max_dia = CivSettings.getInteger(CivSettings.gameConfig, "diamond.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "diamond.max_drop_fortune") * level);
				
				if (max_dia < min_dia) max_dia = min_dia;
				int rand_dia = rand.nextInt(max_dia)+1;
				if (rand_dia < min_dia) rand_dia = min_dia;
				
				ItemStack stack_dia = new ItemStack(Material.DIAMOND);
				ItemManager.givePlayerItem(p, stack_dia, dropLoc, stack_dia.getItemMeta().getDisplayName(), rand_dia, true);
				
				// Hammer Drops
				int min_diaHam = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.min_drop");
				int max_diaHam = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.max_drop_fortune") * level);
				
				if (max_diaHam < min_diaHam) max_diaHam = min_diaHam;
				int rand_ham = rand.nextInt(max_diaHam)+1;
				if (rand_ham < min_diaHam) rand_ham = min_diaHam;
				// Just to make getting hammers a little harder
					int newRand = rand.nextInt(3);
					if (newRand != 0) rand_ham = 0;
				
				ItemStack stack_ham = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hammers"));
				ItemManager.givePlayerItem(p, stack_ham, dropLoc, stack_ham.getItemMeta().getDisplayName(), rand_ham, true);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		// Farming stuff
		
		if (event.getBlock().getType().equals(Material.CROPS)) {
			if (event.isCancelled()) return;
			Crops crops = (Crops) event.getBlock().getState().getData();
			if (crops.getState() != CropState.RIPE) return;
			event.setDropItems(false);
			try {
				int level = 0;
				int fortune_level_difference = CivSettings.getInteger(CivSettings.gameConfig, "wheat_hand.fortune_level_difference");
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Crop Drops
				int min_crop = CivSettings.getInteger(CivSettings.gameConfig, "wheat_hand.min_drop");
				int max_crop = CivSettings.getInteger(CivSettings.gameConfig, "wheat_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "wheat_hand.max_drop_fortune") * (level/fortune_level_difference));
				
				if (max_crop < min_crop) max_crop = min_crop;
				int rand_crop = rand.nextInt(max_crop)+1;
				if (rand_crop < min_crop) rand_crop = min_crop;
				
				ItemStack stack_crop = new ItemStack(Material.WHEAT);
				ItemManager.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
				
				// Seed Drops
				int min_seed = CivSettings.getInteger(CivSettings.gameConfig, "wheat_seed_hand.min_drop");
				int max_seed = CivSettings.getInteger(CivSettings.gameConfig, "wheat_seed_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "wheat_seed_hand.max_drop_fortune") * level);
				
				if (max_seed < min_seed) max_seed = min_seed;
				int rand_seed = rand.nextInt(max_seed)+1;
				if (rand_seed < min_seed) rand_seed = min_seed;
				
				ItemStack stack_seed = new ItemStack(Material.SEEDS);
				ItemManager.givePlayerItem(p, stack_seed, dropLoc, stack_seed.getItemMeta().getDisplayName(), rand_seed, false);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.BEETROOT_BLOCK)) {
			if (event.isCancelled()) return;
			Crops crops = (Crops) event.getBlock().getState().getData();
			if (crops.getState() != CropState.RIPE) return;
			event.setDropItems(false);
			try {
				int level = 0;
				int fortune_level_difference = CivSettings.getInteger(CivSettings.gameConfig, "beetroot_hand.fortune_level_difference");
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Crop Drops
				int min_crop = CivSettings.getInteger(CivSettings.gameConfig, "beetroot_hand.min_drop");
				int max_crop = CivSettings.getInteger(CivSettings.gameConfig, "beetroot_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "beetroot_hand.max_drop_fortune") * (level/fortune_level_difference));
				
				if (max_crop < min_crop) max_crop = min_crop;
				int rand_crop = rand.nextInt(max_crop)+1;
				if (rand_crop < min_crop) rand_crop = min_crop;
				
				ItemStack stack_crop = new ItemStack(Material.BEETROOT);
				ItemManager.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
				
				// Seed Drops
				int min_seed = CivSettings.getInteger(CivSettings.gameConfig, "beetroot_seed_hand.min_drop");
				int max_seed = CivSettings.getInteger(CivSettings.gameConfig, "beetroot_seed_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "beetroot_seed_hand.max_drop_fortune") * level);
				
				if (max_seed < min_seed) max_seed = min_seed;
				int rand_seed = rand.nextInt(max_seed)+1;
				if (rand_seed < min_seed) rand_seed = min_seed;
				
				ItemStack stack_seed = new ItemStack(Material.BEETROOT_SEEDS);
				ItemManager.givePlayerItem(p, stack_seed, dropLoc, stack_seed.getItemMeta().getDisplayName(), rand_seed, false);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.CARROT)) {
			event.setDropItems(false);
			Crops crops = (Crops) event.getBlock().getState().getData();
			if (crops.getState() != CropState.RIPE) return;
			event.setDropItems(false);
			try {
				int level = 0;
				int fortune_level_difference = CivSettings.getInteger(CivSettings.gameConfig, "carrot_hand.fortune_level_difference");
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Crop Drops
				int min_crop = CivSettings.getInteger(CivSettings.gameConfig, "carrot_hand.min_drop");
				int max_crop = CivSettings.getInteger(CivSettings.gameConfig, "carrot_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "carrot_hand.max_drop_fortune") * (level/fortune_level_difference));
				
				if (max_crop < min_crop) max_crop = min_crop;
				int rand_crop = rand.nextInt(max_crop)+1;
				if (rand_crop < min_crop) rand_crop = min_crop;
				
				ItemStack stack_crop = new ItemStack(Material.CARROT_ITEM);
				ItemManager.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.POTATO)) {
			event.setDropItems(false);
			Crops crops = (Crops) event.getBlock().getState().getData();
			if (crops.getState() != CropState.RIPE) return;
			event.setDropItems(false);
			try {
				int level = 0;
				int fortune_level_difference = CivSettings.getInteger(CivSettings.gameConfig, "potato_hand.fortune_level_difference");
				Map<Enchantment, Integer> enchants = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
				
				// Crop Drops
				int min_crop = CivSettings.getInteger(CivSettings.gameConfig, "potato_hand.min_drop");
				int max_crop = CivSettings.getInteger(CivSettings.gameConfig, "potato_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "potato_hand.max_drop_fortune") * (level/fortune_level_difference));
				
				if (max_crop < min_crop) max_crop = min_crop;
				int rand_crop = rand.nextInt(max_crop)+1;
				if (rand_crop < min_crop) rand_crop = min_crop;
				
				ItemStack stack_crop = new ItemStack(Material.POTATO_ITEM);
				ItemManager.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	// https://minecraft.gamepedia.com/Health#Death_messages 
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event) throws CivException {
		String name = ((CraftLivingEntity) event.getEntity()).getHandle().getScoreboardDisplayName().getText()+CivColor.RESET;
		Player p = event.getEntity();
		Resident res = CivGlobal.getResident(p);
		if (res.isSuicidal) {
			res.isSuicidal = false;
			event.setDeathMessage(name+" commited suicide");
			return;
		}
		
		EntityDamageEvent ed = p.getLastDamageCause();
		DamageCause dc = ed.getCause();
		if (dc == DamageCause.SUICIDE) {
			event.setDeathMessage(name+" commited suicide");
			return;
		}
		
		if (p.getLastDamageCause() == null) {
			event.setDeathMessage(name+" had a mysterious death");
			return;
		}
		
		Entity ec = null;
		Player pc = null;
		if (ed instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) ed;
			if (ede.getDamager() != null) ec = ede.getDamager();
		}
		if (p.getKiller() != null) pc = p.getKiller();
		
		if (dc == DamageCause.FALL) {
			if (pc != null) {
				event.setDeathMessage(name+" fell to their doom trying to escape "+pc.getDisplayName());
			} else if (ec != null) {
				event.setDeathMessage(name+" fell to their doom trying to escape "+ec.getName());
			} else {
				event.setDeathMessage(name+" fell to their doom");
			}
		} else if (dc == DamageCause.DROWNING) {
			if (pc != null) {
				event.setDeathMessage(name+" drowned trying to escape "+pc.getDisplayName());
			} else if (ec != null) {
				event.setDeathMessage(name+" drowned trying to escape "+ec.getName());
			} else {
				event.setDeathMessage(name+" does not have gills like a fish");
			}
		} else if (dc == DamageCause.LAVA) {
			if (pc != null) {
				event.setDeathMessage(name+" swam in lava trying to escape "+pc.getDisplayName());
			} else if (ec != null) {
				event.setDeathMessage(name+" swam in lava trying to escape "+ec.getName());
			} else {
				event.setDeathMessage(name+" got stuck in lava");
			}
		} else if (dc == DamageCause.FIRE) {
			if (pc != null) {
				event.setDeathMessage(name+" was roasted like a marshmallow in flames trying to escape "+pc.getDisplayName());
			} else if (ec != null) {
				event.setDeathMessage(name+" was roasted like a marshmallow in flames trying to escape "+ec.getName());
			} else {
				event.setDeathMessage(name+" was roasted like a marshmallow in flames");
			}
		} else if (dc == DamageCause.FIRE_TICK) {
			if (pc != null) {
				event.setDeathMessage(name+" could not stop, drop, and roll in time trying to escape "+pc.getDisplayName());
			} else if (ec != null) {
				event.setDeathMessage(name+" could not stop, drop, and roll in time trying to escape "+ec.getName());
			} else {
				event.setDeathMessage(name+" could not stop, drop, and roll in time");
			}
		} else if (dc == DamageCause.STARVATION) {
			event.setDeathMessage(name+" missed too many meals and couldn't find a McDonalds");
		} else if (dc == DamageCause.PROJECTILE) {
			if (ec != null) {
				if (ec instanceof Arrow) {
					Arrow arw = (Arrow) ec;
					if (arw.getShooter() instanceof Skeleton) {
						Skeleton atk = (Skeleton) arw.getShooter();
						if (atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName() != null) {
							event.setDeathMessage(name+" was shot to death by Skeleton using "+atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName());
						} else if (atk.getEquipment().getItemInOffHand().getItemMeta().getDisplayName() != null) {
							event.setDeathMessage(name+" was shot to death by Skeleton using "+atk.getEquipment().getItemInOffHand().getItemMeta().getDisplayName());
						} else {
							event.setDeathMessage(name+" was shot to death by Skeleton");
						}
					}
					if (arw.getShooter() instanceof Stray) {
						Stray atk = (Stray) arw.getShooter();
						if (atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName() != null) {
							event.setDeathMessage(name+" was shot to death by Stray using "+atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName());
						} else if (atk.getEquipment().getItemInOffHand().getItemMeta().getDisplayName() != null) {
							event.setDeathMessage(name+" was shot to death by Stray using "+atk.getEquipment().getItemInOffHand().getItemMeta().getDisplayName());
						} else {
							event.setDeathMessage(name+" was shot to death by Stray");
						}
					}
				}
			}
		} else if (dc == DamageCause.ENTITY_ATTACK) {
			if (ec instanceof Wolf) {
				Wolf w = (Wolf) ec;
				if (w.getOwner() != null) {
					event.setDeathMessage(name+" was bit to death by "+w.getOwner().getName()+"'s Wolf");
				} else {
					event.setDeathMessage(name+" was bit to death by Wild Wolf");
				}
			}
		} else if (dc == DamageCause.LIGHTNING) {
			if (p.getWorld().hasStorm() || p.getWorld().isThundering()) {
				event.setDeathMessage(name+" was struck by Lightning in a thunderstorm");
			} else {
				event.setDeathMessage(name+" was not liked by Zeus");
			}
		} else if (dc == DamageCause.MAGIC) {
			if (pc != null) {
				event.setDeathMessage(name+" was killed using Magic from "+pc.getDisplayName());
			} else if (ec != null) {
				if (ec instanceof ThrownPotion) {
					ThrownPotion pot = (ThrownPotion) ec;
					if (pot.getShooter() != null) {
						Entity etp = (Entity) pot.getShooter();
						event.setDeathMessage(name+" was killed using Magic "+ec.getName()+" by "+etp.getName());
					} else {
						event.setDeathMessage(name+" was killed using Magic "+ec.getName());
					}
				} else {
					event.setDeathMessage(name+" was killed with Magic by "+ec.getName());
				}
			} else {
				event.setDeathMessage(name+" was killed using Magic by Unknown Source");
			}
		}
		return;
	}
	
	public static void randomTeleport(Player p) {
		Resident res = CivGlobal.getResident(p);
		if (res == null || res.isTPing) {
			CivLog.warning("Tried to teleport "+p.getName()+" while task was already running!");
			return;
		}
		
		res.isTPing = true;
		Location tpLoc = null;
		boolean isOnLand = false;
		boolean isClearAbove = false;
		
		int rX = 5000;
		int rZ = 5000;
		try {
			rX = CivSettings.getInteger(CivSettings.gameConfig, "world.radius_x");
			rZ = CivSettings.getInteger(CivSettings.gameConfig, "world.radius_z");
		} catch (InvalidConfiguration e) {
			CivLog.error("-- Error on Reciving Setting --");
			CivLog.error("Could not load either world.radius_x AND/OR world.radius_z when trying to teleport player "+p.getName());
			e.printStackTrace();
		}
		
		Random rand = new Random();
		int x = -(rX/2) + (rand.nextInt(rX)) - (rX/10);
		int y = 64;
		int z = -(rZ/2) + (rand.nextInt(rZ)) - (rZ/10);
		
		while (!isOnLand) {
			tpLoc = new Location(p.getWorld(), x, y, z);
			Location teleportLocationBelow = new Location(p.getWorld(), x, y-1, z);
			Location teleportLocationBelow2 = new Location(p.getWorld(), x, y-2, z);
			if (tpLoc.getBlock().getType() == Material.AIR &&
					teleportLocationBelow.getBlock().getType() == Material.AIR &&
					teleportLocationBelow2.getBlock().getType().isSolid() &&
					
					tpLoc.getBlock().getBiome() != Biome.DEEP_OCEAN && tpLoc.getBlock().getBiome() != Biome.FROZEN_OCEAN &&
					tpLoc.getBlock().getBiome() != Biome.OCEAN && tpLoc.getBlock().getBiome() != Biome.FROZEN_RIVER &&
					tpLoc.getBlock().getBiome() != Biome.RIVER && tpLoc.getBlock().getBiome() != Biome.BEACHES &&
					tpLoc.getBlock().getBiome() != Biome.COLD_BEACH && tpLoc.getBlock().getBiome() != Biome.STONE_BEACH) {
				isOnLand = true;
			} else {
				if (x <= rX && x > 0) {
					x += rand.nextInt(3)+1;
				} else if (x >= (-rX) && x < 0) {
					x += -(rand.nextInt(3)+1);
				} else {
					x = -(rX/2) + (rand.nextInt(rX)) - (rX/10);
				}
				
				if (z <= rZ && x > 0) {
					z += rand.nextInt(3)+1;
				} else if (z >= (-rZ) && x < 0) {
					z += -(rand.nextInt(3)+1);
				} else {
					z = -(rZ/2) + (rand.nextInt(rZ)) - (rZ/10);
				}
			}
		}
		
		while (!isClearAbove) {
			Location tpLocAbove1 = new Location(p.getWorld(), x, y+1, z);
			Location tpLocAbove2 = new Location(p.getWorld(), x, y+2, z);
			if (tpLocAbove1.getBlock().getType() == Material.AIR && tpLocAbove2.getBlock().getType() == Material.AIR) {
				isClearAbove = true;
			} else {
				isOnLand = false;
				if (x <= rX && x > 0) {
					x += rand.nextInt(3)+1;
				} else if (x >= (-rX) && x < 0) {
					x += -(rand.nextInt(3)+1);
				} else {
					x = -(rX/2) + (rand.nextInt(rX)) - (rX/10);
				}
				
				if (z <= rZ && x > 0) {
					z += rand.nextInt(3)+1;
				} else if (z >= (-rZ) && x < 0) {
					z += -(rand.nextInt(3)+1);
				} else {
					z = -(rZ/2) + (rand.nextInt(rZ)) - (rZ/10);
				}
			}
		}
		
		ChunkCoord cc = new ChunkCoord(tpLoc);
		if (CivGlobal.getCultureChunk(cc) != null && cc.getChunk() == tpLoc.getChunk()) {
			CivMessage.sendError(p, "We accidently tried teleporting you to a civilization's culture borders. Recalculating new placement...");
			res.isTPing = false;
			randomTeleport(p);
			return;
		}
		
		if (CivSettings.hasWorldBorder) {
			BorderData border = Config.Border(CivCraft.worldName);
			if (border != null) {
				if(!border.insideBorder(tpLoc.getX(), tpLoc.getZ(), Config.ShapeRound())) {
					CivMessage.sendError(p, "We accidently tried teleporting you outside the world border. Recalculating new placement...");
					res.isTPing = false;
					randomTeleport(p);
					return;
				}
			}
		} else {
			World w = Bukkit.getWorld(CivCraft.worldName);
			double bs = w.getWorldBorder().getSize() / 2;
			if (Math.abs(tpLoc.getX()) >= bs || Math.abs(tpLoc.getZ()) >= bs) {
				CivMessage.sendError(p, "We accidently tried teleporting you outside the world border. Recalculating new placement...");
				res.isTPing = false;
				randomTeleport(p);
				return;
			}
		}
		
		p.setInvulnerable(true);
		p.setSaturation(20f);
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		p.teleport(tpLoc);
		p.setInvulnerable(false);
		res.isTPing = false;
		CivMessage.sendSuccess(p, "You have been randomly teleported to "+x+", "+y+", "+z+"!");
	}
	
	//XXX Mechanical PvP Aspect
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
		boolean enabled = false;
		try {
			enabled = Boolean.valueOf(CivSettings.getString(CivSettings.gameConfig, "inventory.allow_offhand"));
		} catch (InvalidConfiguration e1) {
			e1.printStackTrace();
		}
		
		if (enabled == false) {
			CivMessage.sendError(e.getPlayer(), "You cannot switch items to off-hand!");
			e.setCancelled(true);
		}
	}
	
/*	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e){
		if (!e.getInventory().getType().equals(InventoryType.CRAFTING)) {
			return; //Making sure it's a survival player's inventory
		}
		
		if (e.getSlot() != 40) {
			return; // If they didn't click into the offhand slot, return
		}
		
		if (!e.getCurrentItem().getType().equals(Material.AIR) && e.getCursor().getType().equals(Material.AIR)) {
			return; // If the slot is not empty, allow taking the item
		}
		
		if(shouldWeCancel(e.getCursor())){
			e.setResult(Event.Result.DENY);
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryDrag(InventoryDragEvent e){
		if(!e.getInventory().getType().equals(InventoryType.CRAFTING) || !e.getInventorySlots().contains(40)) {
			return;
		}
		
		if(shouldWeCancel(e.getOldCursor())){
			e.setResult(Event.Result.DENY);
			e.setCancelled(true);
		}
	}*/
	
	private static ArrayList<Material> mats = new ArrayList<Material>();
	public boolean shouldWeCancel(ItemStack item) {		
		Material mat = item.getType();
		boolean isContained = mats.contains(mat);
		if(!isContained && !mat.equals(Material.AIR)) {
			return true;
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerJoinEvent e) {
		boolean enabled = true;
		int GAS = 4;
		try {
			GAS = CivSettings.getInteger(CivSettings.gameConfig, "pvp.attack_speed");
			enabled = Boolean.valueOf(CivSettings.getString(CivSettings.gameConfig, "pvp.attack_cooldown_enabled"));
		} catch (InvalidConfiguration e1) {
			e1.printStackTrace();
		}
		
		Player p = e.getPlayer();
		AttributeInstance attribute = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		double baseValue = attribute.getBaseValue();

		if (enabled == false) {
			GAS = 4; //If module is disabled, set attack speed to 1.9 default
			CivLog.debug("[Player Join] game.yml-pvp.attack_speed set to 4");
		}
		
		if (baseValue != GAS) {
			attribute.setBaseValue(GAS);
			CivLog.debug("[Player Join] game.yml-pvp.attack_speed set to "+GAS);
			p.saveData();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e){
		Player player = e.getPlayer();
		AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		double baseValue = attribute.getBaseValue();
		if (baseValue != 4){ //If basevalue is not 1.9 default, set it back
			attribute.setBaseValue(4);
			CivLog.debug("[Player Leave] game.yml-pvp.attack_speed set to 4");
			player.saveData();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldChange(PlayerChangedWorldEvent e) {
		Player player = e.getPlayer();
		boolean enabled = true;
		int GAS = 4;
		try {
			GAS = CivSettings.getInteger(CivSettings.gameConfig, "pvp.attack_speed");
			enabled = Boolean.valueOf(CivSettings.getString(CivSettings.gameConfig, "pvp.attack_cooldown_enabled"));
		} catch (InvalidConfiguration e1) {
			e1.printStackTrace();
		}
		
		AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		double baseValue = attribute.getBaseValue();
		
		if (enabled == false) {
			GAS = 4; //If module is disabled, set attack speed to 1.9 default
			CivLog.debug("[Player World Change] game.yml-pvp.attack_speed set to 4");
		}
		
		if (baseValue!=GAS){
			attribute.setBaseValue(GAS);
			CivLog.debug("[Player World Change] game.yml-pvp.attack_speed set to "+GAS);
			player.saveData();
		}
	}
	
	// XXX Server Aspect
	
	// https://hub.spigotmc.org/javadocs/spigot/index.html?overview-summary.html
	@EventHandler(priority = EventPriority.NORMAL)
	public void onServerListRefresh(ServerListPingEvent event) throws IllegalArgumentException, UnsupportedOperationException, Exception {
		event.setServerIcon(Bukkit.loadServerIcon(new File("mcdiamondsword1.png")));
		int amtPlayers = (int) ((event.getNumPlayers()*2.5)+2);
		if (amtPlayers > CivGlobal.maxPlayers) {
			event.setMaxPlayers(CivGlobal.maxPlayers);
		} else {
			event.setMaxPlayers(amtPlayers);
		}
		String title = CivColor.Red+"Coal"+CivColor.LightBlue+"Civ: "+CivColor.RESET+CivColor.LightGrayItalic;
		Random rand = new Random();
		int msg = rand.nextInt(7);
		if (msg == 0) {
			event.setMotd(title+"Speak softly and carry a big stick; you will go far -Roosevelt");
		} else if (msg == 1) {
			event.setMotd(title+"The two most powerful warriors are patience and time -Tolstoy");
		} else if (msg == 2) {
			event.setMotd(title+"Sometimes by losing a battle you find a new way to win the war -Trump");
		} else if (msg == 3) {
			event.setMotd(title+"We are going to have peace even if we have to fight for it -Eisenhower");
		} else if (msg == 4) {
			event.setMotd(title+"To be prepared for war is the most effective means of peace -Washington");
		} else if (msg == 5) {
			event.setMotd(title+"You mustn't fight too often with an enemy; you'll teach him your art of war -Bonaparte");
		} else {
			event.setMotd(title+"Why play with friends when you can play with communities? -YourCoal");
		}
		
	}
	
}
