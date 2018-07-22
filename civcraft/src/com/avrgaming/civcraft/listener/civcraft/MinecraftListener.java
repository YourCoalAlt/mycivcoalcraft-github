package com.avrgaming.civcraft.listener.civcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.Event;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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
import com.avrgaming.civcraft.util.CivItem;
import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;

/* https://github.com/gvlfm78/BukkitOldCombatMechanics */

public class MinecraftListener implements Listener {
	
	//XXX Player-Bound Aspect
	
	private static HashMap<ItemStack, ArrayList<Double>> food_values = new HashMap<ItemStack, ArrayList<Double>>();
	
	public static void setupFoodValues() {
		ArrayList<Double> raw_fish = new ArrayList<Double>();
		raw_fish.add(2.0); raw_fish.add(.35);
		food_values.put(CivItem.newStack(Material.RAW_FISH), raw_fish);
		
		ArrayList<Double> raw_salmon = new ArrayList<Double>();
		raw_salmon.add(3.0); raw_salmon.add(.26667);
		food_values.put(CivItem.newStack(Material.RAW_FISH, 1, true), raw_salmon);
		
		ArrayList<Double> cooked_fish = new ArrayList<Double>();
		cooked_fish.add(6.0); cooked_fish.add(1.4);
		food_values.put(CivItem.newStack(Material.COOKED_FISH), cooked_fish);
		
		ArrayList<Double> cooked_salmon = new ArrayList<Double>();
		cooked_salmon.add(7.0); cooked_salmon.add(1.5429);
		food_values.put(CivItem.newStack(Material.COOKED_FISH, 1, true), cooked_salmon);
		
		ArrayList<Double> melon = new ArrayList<Double>();
		melon.add(3.0); melon.add(1.0667);
		food_values.put(CivItem.newStack(Material.MELON), melon);
		
		ArrayList<Double> beetroot_soup = new ArrayList<Double>();
		beetroot_soup.add(6.0); beetroot_soup.add(1.6);
		food_values.put(CivItem.newStack(Material.MELON), beetroot_soup);
		
		ArrayList<Double> gold_carrot = new ArrayList<Double>();
		gold_carrot.add(7.0); gold_carrot.add(2.4);
		food_values.put(CivItem.newStack(Material.GOLDEN_CARROT), gold_carrot);
		
		ArrayList<Double> gold_apple_regular = new ArrayList<Double>();
		gold_apple_regular.add(4.0); gold_apple_regular.add(4.8);
		food_values.put(CivItem.newStack(Material.GOLDEN_APPLE), gold_apple_regular);
		
		ArrayList<Double> gold_apple_enchanted = new ArrayList<Double>();
		gold_apple_enchanted.add(8.0); gold_apple_enchanted.add(3.6);
		food_values.put(CivItem.newStack(Material.GOLDEN_APPLE, 1 , true), gold_apple_enchanted);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerConsumeItem(PlayerItemConsumeEvent event) throws CivException {
		ItemStack is = event.getItem();
		for (ItemStack foods : food_values.keySet()) {
			if (foods.getType() != is.getType()) continue;
			if (foods.getDurability() != is.getDurability()) continue;
			
			ArrayList<Double> food = food_values.get(foods);
			Player p = event.getPlayer();
			event.setCancelled(true);
			
			ItemStack mainHand = p.getInventory().getItemInMainHand();
			ItemStack offHand = p.getInventory().getItemInOffHand();
			if (mainHand.getType() == foods.getType() && mainHand.getDurability() == foods.getDurability()) {
				p.getInventory().getItemInMainHand().setAmount(mainHand.getAmount()-1);
			} else if (offHand.getType() == foods.getType() && offHand.getDurability() == foods.getDurability()) {
				p.getInventory().getItemInMainHand().setAmount(offHand.getAmount()-1);
			} else {
				CivMessage.send(p, "How do you eat? Like, you broke the game here, lad!");
				return;
			}
			
			double fakeFoodAmt = food.get(0);
			int foodAmt = (int) fakeFoodAmt;
			double satRatio = food.get(1)/100;
			float SaturateAmt = (float) (p.getSaturation()+(foodAmt*satRatio));
			int pFood = p.getFoodLevel()+foodAmt;
			if (pFood >= 20) {
				int toSaturate = pFood - 20;
				SaturateAmt += toSaturate;
				pFood = 20;
			}
			p.setFoodLevel(pFood);
			p.setSaturation(SaturateAmt);
			
			if (is.getType() == Material.GOLDEN_APPLE) {
				if (is.getDurability() == 0) {
					new BukkitRunnable() {
						@Override
						public void run() {
							p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*15, 1));
							p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20*120, 0));
							p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*60, 0));
						}
					}.runTask(CivCraft.getPlugin());
				}
				if (is.getDurability() == 1) {
					new BukkitRunnable() {
						@Override
						public void run() {
							p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20*5, 0));
							p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*30, 1));
							p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20*120, 1));
							p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*90, 2));
							p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*300, 0));
							p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20*300, 0));
						}
					}.runTask(CivCraft.getPlugin());
				}
			}
			
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
			if(CivItem.getId(r) == mirrorID1 || CivItem.getId(r) == mirrorID2 || CivItem.getId(r) == mirrorID3 || CivItem.getId(r) == mirrorID4) {
				return new BlockCoord(r);
			}
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnBlockFromToEvent(BlockFromToEvent event) {
		// Disable cobblestone generators
		int id = CivItem.getId(event.getBlock());
		if(id >= CivData.WATER_STILL && id <= CivData.LAVA_STILL) {
			Block b = event.getToBlock();
			int toid = CivItem.getId(b);
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
		if (CivItem.getId(event.getNewState()) == CivData.COBBLESTONE || CivItem.getId(event.getNewState()) == CivData.OBSIDIAN) {
			CivItem.setTypeId(event.getNewState(), CivData.NETHERRACK);
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
			if (CivItem.getId(event.getBlock().getType()) == m.id) {
				if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
				ResidentExperience re = CivGlobal.getResidentE(p);
				double mod = (re.getEXPLevel(EXPSlots.MINING)+1) / 2;
				int eEXP = (int) (event.getExpToDrop()*mod) / 2;
				if (eEXP >= 1) CivItem.dropPlayerEXP(p, dropLoc, eEXP);
				re.addResEXP(EXPSlots.MINING, m.resxp);
			}
		}
		
		// Custom Drops check now.
		
		if (event.getBlock().getType().equals(Material.COAL_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); CivItem.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true); reduceItemDurability(p ,p.getInventory().getItemInMainHand());
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
				CivItem.givePlayerItem(p, stack_coal, dropLoc, stack_coal.getItemMeta().getDisplayName(), rand_coal, true);
				
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
				CivItem.givePlayerItem(p, stack_ham, dropLoc, stack_ham.getItemMeta().getDisplayName(), rand_ham, true);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.REDSTONE_ORE) || event.getBlock().getType().equals(Material.GLOWING_REDSTONE_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); CivItem.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true); reduceItemDurability(p ,p.getInventory().getItemInMainHand());
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
				CivItem.givePlayerItem(p, stack_red, dropLoc, stack_red.getItemMeta().getDisplayName(), rand_red, true);
				
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
				CivItem.givePlayerItem(p, stack_bek, dropLoc, stack_bek.getItemMeta().getDisplayName(), rand_bek, true);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.LAPIS_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); CivItem.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true); reduceItemDurability(p ,p.getInventory().getItemInMainHand());
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
				CivItem.givePlayerItem(p, stack_lap, dropLoc, stack_lap.getItemMeta().getDisplayName(), rand_lap, true);
				
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
				CivItem.givePlayerItem(p, stack_bek, dropLoc, stack_bek.getItemMeta().getDisplayName(), rand_bek, true);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.DIAMOND_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setDropItems(false); CivItem.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true); reduceItemDurability(p ,p.getInventory().getItemInMainHand());
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
				CivItem.givePlayerItem(p, stack_dia, dropLoc, stack_dia.getItemMeta().getDisplayName(), rand_dia, true);
				
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
				CivItem.givePlayerItem(p, stack_ham, dropLoc, stack_ham.getItemMeta().getDisplayName(), rand_ham, true);
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
				CivItem.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
				
				// Seed Drops
				int min_seed = CivSettings.getInteger(CivSettings.gameConfig, "wheat_seed_hand.min_drop");
				int max_seed = CivSettings.getInteger(CivSettings.gameConfig, "wheat_seed_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "wheat_seed_hand.max_drop_fortune") * level);
				
				if (max_seed < min_seed) max_seed = min_seed;
				int rand_seed = rand.nextInt(max_seed)+1;
				if (rand_seed < min_seed) rand_seed = min_seed;
				
				ItemStack stack_seed = new ItemStack(Material.SEEDS);
				CivItem.givePlayerItem(p, stack_seed, dropLoc, stack_seed.getItemMeta().getDisplayName(), rand_seed, false);
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
				CivItem.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
				
				// Seed Drops
				int min_seed = CivSettings.getInteger(CivSettings.gameConfig, "beetroot_seed_hand.min_drop");
				int max_seed = CivSettings.getInteger(CivSettings.gameConfig, "beetroot_seed_hand.max_drop")
						+ (CivSettings.getInteger(CivSettings.gameConfig, "beetroot_seed_hand.max_drop_fortune") * level);
				
				if (max_seed < min_seed) max_seed = min_seed;
				int rand_seed = rand.nextInt(max_seed)+1;
				if (rand_seed < min_seed) rand_seed = min_seed;
				
				ItemStack stack_seed = new ItemStack(Material.BEETROOT_SEEDS);
				CivItem.givePlayerItem(p, stack_seed, dropLoc, stack_seed.getItemMeta().getDisplayName(), rand_seed, false);
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
				CivItem.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
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
				CivItem.givePlayerItem(p, stack_crop, dropLoc, stack_crop.getItemMeta().getDisplayName(), rand_crop, false);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public ItemStack reduceItemDurability(Player p, ItemStack is) {
		short newDura = (short) (is.getDurability()+1);
		if (newDura < 0) {
			is = new ItemStack(Material.AIR);
			p.playSound(p.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
		} else {
			is.setDurability(newDura);
		}
		return is;
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
						if (atk.getEquipment().getItemInMainHand() != null && atk.getEquipment().getItemInMainHand().getType() != Material.AIR &&
								atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName() != null) {
							event.setDeathMessage(name+" was shot to death by Skeleton using "+atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName());
						} else if (atk.getEquipment().getItemInOffHand() != null && atk.getEquipment().getItemInOffHand().getType() != Material.AIR &&
								atk.getEquipment().getItemInOffHand().getItemMeta().getDisplayName() != null) {
							event.setDeathMessage(name+" was shot to death by Skeleton using "+atk.getEquipment().getItemInOffHand().getItemMeta().getDisplayName());
						} else {
							event.setDeathMessage(name+" was shot to death by Skeleton");
						}
					}
					if (arw.getShooter() instanceof Stray) {
						Stray atk = (Stray) arw.getShooter();
						if (atk.getEquipment().getItemInMainHand() != null && atk.getEquipment().getItemInMainHand().getType() != Material.AIR &&
								atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName() != null) {
							event.setDeathMessage(name+" was shot to death by Stray using "+atk.getEquipment().getItemInMainHand().getItemMeta().getDisplayName());
						} else if (atk.getEquipment().getItemInOffHand() != null && atk.getEquipment().getItemInOffHand().getType() != Material.AIR &&
								atk.getEquipment().getItemInOffHand().getItemMeta().getDisplayName() != null) {
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
	public void onSwapHandItems(PlayerSwapHandItemsEvent ev) {
		if (!CivCraft.allow_offhand) {
			CivMessage.sendError(ev.getPlayer(), "You cannot have items in your off-hand!");
			ev.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent ev) {
		if (!CivCraft.allow_offhand) {
			// Check if inventory exists
			if (ev.getClickedInventory() == null) return;
			
			// Check if this inventory has offhand
			if (!(ev.getClickedInventory().getType() == InventoryType.PLAYER) && !(ev.getClickedInventory().getType() == InventoryType.CRAFTING)) return;
			
			// Check if offhand slot had interaction
			if (ev.getSlot() != 40) return;
			
			// If the slot is not empty, allow taking the item
			if (!ev.getCurrentItem().getType().equals(Material.AIR) && ev.getCursor().getType().equals(Material.AIR)) return;
			
			// Cancel the offhand
			CivMessage.sendError(ev.getWhoClicked(), "You cannot have items in your off-hand!");
			ev.setResult(Event.Result.DENY);
			ev.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryDrag(InventoryDragEvent ev) {
		if (!CivCraft.allow_offhand) {
			// Check if this inventory has offhand
			if (!(ev.getInventory().getType() == InventoryType.PLAYER) && !(ev.getInventory().getType() == InventoryType.CRAFTING)) return;
			
			// Check if offhand slot had interaction
			if (!ev.getInventorySlots().contains(40)) return;
			
			// Cancel the offhand
			CivMessage.sendError(ev.getWhoClicked(), "You cannot have items in your off-hand!");
			ev.setResult(Event.Result.DENY);
			ev.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerJoinEvent e) {
		double GAS = 4; // default
		try {
			GAS = CivSettings.getDouble(CivSettings.gameConfig, "pvp.attack_speed");
		} catch (InvalidConfiguration e1) {
			CivLog.warning("game.yml pvp.attack_speed was not able to be found, setting to Minecraft's default 4.");
		}
		
		Player p = e.getPlayer();
		AttributeInstance atrb = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		double attrValue = atrb.getBaseValue();
		
		if (attrValue != GAS) {
			CivLog.debug("[Player Join] game.yml-pvp.attack_speed set from "+attrValue+" to "+GAS);
			atrb.setBaseValue(GAS);
			p.saveData();
		}
	}
	
	// XXX Server Aspect
	
	// https://hub.spigotmc.org/javadocs/spigot/index.html?overview-summary.html
	@EventHandler(priority = EventPriority.NORMAL)
	public void onServerListRefresh(ServerListPingEvent event) throws IllegalArgumentException, UnsupportedOperationException, Exception {
		if (CivCraft.isRestarting) {
			event.setMaxPlayers(0);
			event.setMotd(CivCraft.server_name+" -- Server Rebooting!");
		} else {
			ArrayList<String> motd = new ArrayList<String>();
			motd.add("Speak softly and carry a big stick; you will go far -Roosevelt");
			motd.add("The two most powerful warriors are patience and time -Tolstoy");
			motd.add("Sometimes by losing a battle you find a new way to win the war -Trump");
			motd.add("We are going to have peace even if we have to fight for it -Eisenhower");
			motd.add("To be prepared for war is the most effective means of peace -Washington");
			motd.add("You mustn't fight too often with an enemy; you'll teach him your art of war -Bonaparte");
			motd.add("Why play with friends when you can play with communities? -YourCoal");
			
			Random rand = new Random();
			int msg = rand.nextInt(motd.size());
			event.setMotd(CivCraft.server_name+CivColor.GrayItalic+motd.get(msg));
		}
	}
	
}
