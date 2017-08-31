package com.avrgaming.civcraft.listener.civcraft;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.ItemManager;

/* https://github.com/gvlfm78/BukkitOldCombatMechanics */

public class MinecraftListener implements Listener {
	
	//XXX Player-Bound Aspect
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreakSpawnItems(BlockBreakEvent event) {
		Random rand = new Random();
		if (event.getBlock().getType().equals(Material.LAPIS_ORE)) {
			if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				int min = CivSettings.getInteger(CivSettings.gameConfig, "tungsten_min_drop");
				int max;
				Map<Enchantment, Integer> enchant = event.getPlayer().getInventory().getItemInMainHand().getEnchantments();
				if (enchant.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchant.get(Enchantment.LOOT_BONUS_BLOCKS);
					max = CivSettings.getInteger(CivSettings.gameConfig, "tungsten_max_drop_with_fortune")+(level-1);
				} else {
					max = CivSettings.getInteger(CivSettings.gameConfig, "tungsten_max_drop");
				}
				
				int randAmount = rand.nextInt(min + max)+1;
				randAmount -= min;
				if (randAmount <= 0) randAmount = 1;
				for (int i = 0; i < randAmount; i++) {
					Location dropLoc = new Location(event.getPlayer().getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_tungsten_ore"));
					event.getPlayer().getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.COAL_ORE)) {
			if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				// Coal Drops
				int minC = CivSettings.getInteger(CivSettings.gameConfig, "coal.min_drop");
				int maxC;
				Map<Enchantment, Integer> enchantC = event.getPlayer().getInventory().getItemInMainHand().getEnchantments();
				if (enchantC.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantC.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxC = CivSettings.getInteger(CivSettings.gameConfig, "coal.max_drop_fortune")+(level-1);
				} else {
					maxC = CivSettings.getInteger(CivSettings.gameConfig, "coal.max_drop");
				}
				
				int randAmtC = rand.nextInt(minC + maxC)+1;
				randAmtC -= minC;
				if (randAmtC <= minC) randAmtC = minC;
				for (int i = 0; i < randAmtC; i++) {
					Location dropLoc = new Location(event.getPlayer().getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.COAL);
					event.getPlayer().getWorld().dropItemNaturally(dropLoc, stack);
				}
				
				// Hammer Drops
				int minH = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.min_drop");
				int maxH;
				Map<Enchantment, Integer> enchantH = event.getPlayer().getInventory().getItemInMainHand().getEnchantments();
				if (enchantH.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantH.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxH = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.max_drop_fortune")+(level-1);
				} else {
					maxH = CivSettings.getInteger(CivSettings.gameConfig, "coal_hammers.max_drop");
				}
				
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
					Location dropLoc = new Location(event.getPlayer().getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hammers"));
					event.getPlayer().getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (event.getBlock().getType().equals(Material.DIAMOND_ORE)) {
			if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
			event.setCancelled(true); ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			try {
				// Coal Drops
				int minD = CivSettings.getInteger(CivSettings.gameConfig, "diamond.min_drop");
				int maxD;
				Map<Enchantment, Integer> enchantC = event.getPlayer().getInventory().getItemInMainHand().getEnchantments();
				if (enchantC.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantC.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxD = CivSettings.getInteger(CivSettings.gameConfig, "diamond.max_drop_fortune")+(level-1);
				} else {
					maxD = CivSettings.getInteger(CivSettings.gameConfig, "diamond.max_drop");
				}
				
				int randAmtD = rand.nextInt(minD + maxD)+1;
				randAmtD -= minD;
				if (randAmtD <= minD) randAmtD = minD;
				for (int i = 0; i < randAmtD; i++) {
					Location dropLoc = new Location(event.getPlayer().getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = new ItemStack(Material.DIAMOND);
					event.getPlayer().getWorld().dropItemNaturally(dropLoc, stack);
				}
				
				// Hammer Drops
				int minH = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.min_drop");
				int maxH;
				Map<Enchantment, Integer> enchantH = event.getPlayer().getInventory().getItemInMainHand().getEnchantments();
				if (enchantH.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
					int level = enchantH.get(Enchantment.LOOT_BONUS_BLOCKS);
					maxH = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.max_drop_fortune")+(level-1);
				} else {
					maxH = CivSettings.getInteger(CivSettings.gameConfig, "diamond_hammers.max_drop");
				}
				
				int randAmtH = rand.nextInt(minH + maxH)+1;
				randAmtH -= minH;
				if (randAmtH <= minH) randAmtH = minH;
				for (int i = 0; i < randAmtH; i++) {
					Location dropLoc = new Location(event.getPlayer().getWorld(), event.getBlock().getX(), event.getBlock().getY()+0.5, event.getBlock().getZ());
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hammers"));
					event.getPlayer().getWorld().dropItemNaturally(dropLoc, stack);
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
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
