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
		
		if (event.getBlock().getType().equals(Material.LAPIS_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				int min = CivSettings.getInteger(CivSettings.gameConfig, "tungsten_min_drop");
				int max;
				Map<Enchantment, Integer> enchant = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchant.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchant.get(Enchantment.LOOT_BONUS_BLOCKS);
					max = CivSettings.getInteger(CivSettings.gameConfig, "tungsten_max_drop_with_fortune")+(level-1);
				} else { max = CivSettings.getInteger(CivSettings.gameConfig, "tungsten_max_drop"); }
				
				int randAmount = rand.nextInt(min + max)+1;
				randAmount -= min;
				if (randAmount <= 0) randAmount = 1;
				for (int i = 0; i < randAmount; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_tungsten_ore"));
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.COAL_ORE)) {
			if (event.isCancelled() || p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				// Coal Drops
				int minC = CivSettings.getInteger(CivSettings.gameConfig, "coal.min_drop");
				int maxC;
				Map<Enchantment, Integer> enchantC = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchantC.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantC.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxC = CivSettings.getInteger(CivSettings.gameConfig, "coal.max_drop_fortune")+(level-1);
				} else { maxC = CivSettings.getInteger(CivSettings.gameConfig, "coal.max_drop"); }
				
				int randAmtC = rand.nextInt(minC + maxC)+1;
				randAmtC -= minC;
				if (randAmtC <= minC) randAmtC = minC;
				for (int i = 0; i < randAmtC; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.COAL);
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
				
				// Hammer Drops
				int minH = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.min_drop");
				int maxH;
				Map<Enchantment, Integer> enchantH = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchantH.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantH.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxH = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.max_drop_fortune")+(level-1);
				} else { maxH = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.max_drop"); }
				
				int randAmtH = rand.nextInt(minH + maxH)+1;
				randAmtH -= minH;
				if (randAmtH <= minH) randAmtH = minH;
				if (randAmtH == 1) {
					int newRand = rand.nextInt(3);
					if (newRand == 1) {
						randAmtH = 1;
					} else {
						randAmtH = 0;
					}
				}
				
				if (randAmtH >= randAmtC) randAmtH = randAmtC;
				for (int i = 0; i < randAmtH; i++) {
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
				// Coal Drops
				int minD = CivSettings.getInteger(CivSettings.gameConfig, "redstone.min_drop");
				int maxD;
				Map<Enchantment, Integer> enchantC = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchantC.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantC.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxD = CivSettings.getInteger(CivSettings.gameConfig, "redstone.max_drop_fortune")+(level-1);
				} else { maxD = CivSettings.getInteger(CivSettings.gameConfig, "redstone.max_drop"); }
				
				int randAmtD = rand.nextInt(minD + maxD)+1;
				randAmtD -= minD;
				if (randAmtD <= minD) randAmtD = minD;
				for (int i = 0; i < randAmtD; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.REDSTONE);
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
				
				// Hammer Drops
				int minH = CivSettings.getInteger(CivSettings.gameConfig, "redstone_beakers.min_drop");
				int maxH;
				Map<Enchantment, Integer> enchantH = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchantH.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantH.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxH = CivSettings.getInteger(CivSettings.gameConfig, "redstone_beakers.max_drop_fortune")+(level-1);
				} else { maxH = CivSettings.getInteger(CivSettings.gameConfig, "redstone_beakers.max_drop"); }
				
				int randAmtH = rand.nextInt(minH + maxH)+1;
				randAmtH -= minH;
				if (randAmtH <= minH) randAmtH = minH;
				for (int i = 0; i < randAmtH; i++) {
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
				// Coal Drops
				int minD = CivSettings.getInteger(CivSettings.gameConfig, "diamond.min_drop");
				int maxD;
				Map<Enchantment, Integer> enchantC = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchantC.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantC.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxD = CivSettings.getInteger(CivSettings.gameConfig, "diamond.max_drop_fortune")+(level-1);
				} else { maxD = CivSettings.getInteger(CivSettings.gameConfig, "diamond.max_drop"); }
				
				int randAmtD = rand.nextInt(minD + maxD)+1;
				randAmtD -= minD;
				if (randAmtD <= minD) randAmtD = minD;
				for (int i = 0; i < randAmtD; i++) {
					Location dropLoc = new Location(p.getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.DIAMOND);
					p.getWorld().dropItemNaturally(dropLoc, stack);
				}
				
				// Hammer Drops
				int minH = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.min_drop");
				int maxH;
				Map<Enchantment, Integer> enchantH = p.getInventory().getItemInMainHand().getEnchantments();
				if (enchantH.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantH.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxH = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.max_drop_fortune")+(level-1);
				} else { maxH = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.max_drop"); }
				
				int randAmtH = rand.nextInt(minH + maxH)+1;
				randAmtH -= minH;
				if (randAmtH <= minH) randAmtH = minH;
				for (int i = 0; i < randAmtH; i++) {
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
		
		while (isOnLand == false) {
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
		
		while (isClearAbove == false) {
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
