package com.avrgaming.civcraft.listener.civcraft;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigEXPMining;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.util.ItemManager;

/* https://github.com/gvlfm78/BukkitOldCombatMechanics */

public class MinecraftListener implements Listener {
	
	//XXX Player-Bound Aspect
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreakSpawnItems(BlockBreakEvent event) throws CivException {
		Player p = event.getPlayer();
		Random rand = new Random();
		
		// Quest XP First, then check later for custom drops.
		for (ConfigEXPMining m : CivSettings.resxpMiningBlocks.values()) {
			if (ItemManager.getId(event.getBlock().getType()) == m.id) {
				if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
				ResidentExperience re = CivGlobal.getResidentE(p);
				DecimalFormat df = new DecimalFormat("0.00");
				double mod = re.getMiningLevel() + 1; mod /= 2;
				
				int eEXP = (int) (event.getExpToDrop()*mod) / 2;
				event.setExpToDrop(eEXP);
				
				double genrf = m.resxp*mod;
				double rfEXP = Double.valueOf(df.format(genrf));
				re.addMiningEXP(rfEXP);
			}
		}
		
		// Custom Drops check now.
		
		if (event.getBlock().getType().equals(Material.COAL_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
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
				for (int i = 0; i < rand_coal; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.COAL);
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
				
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
				
				for (int i = 0; i < rand_ham; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hammers"));
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.REDSTONE_ORE) || event.getBlock().getType().equals(Material.GLOWING_REDSTONE_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
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
				for (int i = 0; i < rand_red; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.REDSTONE);
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
				
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
				
				for (int i = 0; i < rand_bek; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_beakers"));
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.LAPIS_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
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
				for (int i = 0; i < rand_lap; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.INK_SACK, 1, (short)4);
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
				
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
				
				for (int i = 0; i < rand_bek; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_beakers"));
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.DIAMOND_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
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
				for (int i = 0; i < rand_dia; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.DIAMOND);
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
				
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
				
				for (int i = 0; i < rand_ham; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hammers"));
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void randomTeleport(Player p) {
		Location teleportLocation = null;
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
			teleportLocation = new Location(p.getWorld(), x, y, z);
			Location teleportLocationBelow = new Location(p.getWorld(), x, y-1, z);
			Location teleportLocationBelow2 = new Location(p.getWorld(), x, y-2, z);
			if (teleportLocation.getBlock().getType() == Material.AIR &&
					teleportLocationBelow.getBlock().getType() == Material.AIR &&
					teleportLocationBelow2.getBlock().getType().isSolid() &&
					
					teleportLocation.getBlock().getBiome() != Biome.DEEP_OCEAN && teleportLocation.getBlock().getBiome() != Biome.FROZEN_OCEAN &&
					teleportLocation.getBlock().getBiome() != Biome.OCEAN && teleportLocation.getBlock().getBiome() != Biome.FROZEN_RIVER &&
					teleportLocation.getBlock().getBiome() != Biome.RIVER && teleportLocation.getBlock().getBiome() != Biome.BEACHES &&
					teleportLocation.getBlock().getBiome() != Biome.COLD_BEACH && teleportLocation.getBlock().getBiome() != Biome.STONE_BEACH) {
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
		
		for (CultureChunk cc : CivGlobal.getCultureChunks()) {
			if (cc.getChunkCoord().getChunk() == teleportLocation.getChunk()) {
				CivMessage.sendError(p, "We accidently tried teleporting you to a civilization's culture borders. Recalculating new placement...");
				x = -(rX/2) + (rand.nextInt(rX)) - (rand.nextInt(rX)/10);
				z = -(rZ/2) + (rand.nextInt(rZ)) - (rand.nextInt(rZ)/10);
				isOnLand = false;
				isClearAbove = false;
			}
		}
		
		p.setInvulnerable(true);
		p.setSaturation(20f);
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		p.teleport(teleportLocation);
		p.setInvulnerable(false);
		CivMessage.sendSuccess(p, "You have been randomly teleported to "+x+", "+y+", "+z+"!");
	}
	
	//XXX Mechanical PvP Aspect
	
	public static ItemStack getArrowStack(Player player) {
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack != null && stack.getType() == Material.ARROW) {
				return stack;
			}
		}
		return null;
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onEntityShootBow(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			ItemStack a = getArrowStack(p);
			if (a != null && a.hasItemMeta() && a.getItemMeta().hasDisplayName()) {
				Resident r = CivGlobal.getResident(p);
				r.lastShotArrow = a.getItemMeta().getDisplayName();
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
		boolean enabled = false;
		try {
			if (CivSettings.getString(CivSettings.gameConfig, "inventory.allow_offhand") != "false") {
				enabled = true;
			}
		} catch (InvalidConfiguration e1) {
			e1.printStackTrace();
		}
		
		if (enabled == false) {
			CivMessage.sendError(e.getPlayer(), "You cannot switch items to off-hand!");
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
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
	}
	
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
			if (CivSettings.getString(CivSettings.gameConfig, "pvp.attack_cooldown_enabled") != "true") {
				enabled = false;
			}
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
			if (CivSettings.getString(CivSettings.gameConfig, "pvp.attack_cooldown_enabled") != "true") {
				enabled = false;
			}
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
}
