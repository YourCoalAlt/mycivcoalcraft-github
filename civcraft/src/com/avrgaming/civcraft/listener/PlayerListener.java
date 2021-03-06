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
package com.avrgaming.civcraft.listener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.command.admin.AdminCommand;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTechPotion;
import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.exception.InvalidNameException;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.items.units.UnitItemMaterial;
import com.avrgaming.civcraft.items.units.UnitMaterial;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.mobs.MobSpawnerTimer;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.road.Road;
import com.avrgaming.civcraft.structure.Capitol;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.PlayerChunkNotifyAsyncTask;
import com.avrgaming.civcraft.threading.tasks.PlayerKickBan;
import com.avrgaming.civcraft.threading.tasks.PlayerLoginAsyncTask;
import com.avrgaming.civcraft.threading.timers.CountdownTimer;
import com.avrgaming.civcraft.threading.timers.PlayerLocationCacheUpdate;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;
import com.avrgaming.civcraft.war.WarStats;
import com.codingforcookies.armorequip.ArmorEquipEvent;

import gpl.AttributeUtil;

public class PlayerListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPickup(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			Resident res = CivGlobal.getResident(p);
			
			// Item Pickup Messages
			if (res.getItemMode().equals("none")) return;
			ItemStack is = event.getItem().getItemStack();
			String name = CivData.getStackName(is);
			if (res.getItemMode().equals("all")) {
				CivMessage.send(p, CivColor.LightGreen+"You've picked up "+CivColor.LightPurple+is.getAmount()+" "+name);
			} else if (is.getItemMeta().hasDisplayName() && res.getItemMode().equals("rare")) {
				name = is.getItemMeta().getDisplayName();
				CivMessage.send(p, CivColor.LightGreen+"You've picked up "+CivColor.LightPurple+is.getAmount()+" "+name);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemMend(PlayerItemMendEvent event) {
		if (LoreEnhancement.isWeaponOrArmor(event.getItem())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerShootBow(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player) {
//			ItemStack bow = event.getBow();
			
/*			Projectile j = (Projectile) event.getProjectile();
			Random rand = new Random();
			if (rand.nextInt(3) < 1) {
				CivMessage.global("Changed");
				Vector v = new Vector(j.getVelocity().getX()*1.75, j.getVelocity().getY()*1.4, j.getVelocity().getZ()*1.75);
				j.setVelocity(v);
				event.setProjectile(j);
			} else {
				CivMessage.global("Normal");
			}*/
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) throws InvalidNameException {
		TaskMaster.asyncTask("CountdownTimer", new CountdownTimer(), 0);
		Player p = event.getPlayer();
		Resident res = CivGlobal.getResident(p);
		if (CivCraft.isRestarting) {
			p.kickPlayer(" �b�l� CivilizationCraft �"+"\n"+
					" "+"\n"+
					"�c�lKicked By �r�8�� �d�o"+"CONSOLE"+"\n"+
					"�c�lReason �r�8�� �f"+"Server Locked -- Rebooting"+"\n"+
					" "+"\n"+
					" "+"\n"+
					"�7�o[Please wait to re-join the server.]"+"\n");
			return;
		}
		
		CivLog.info("Scheduling Player Login Task for:"+p.getName());
		if (res == null) {
			CivLog.info("No resident found. Creating for "+p.getName());
			try {
				res = new Resident(p.getUniqueId(), p.getName());
			} catch (InvalidNameException e) {
				TaskMaster.syncTask(new PlayerKickBan(p.getName(), true, false, "You have an invalid name? Sorry, contact an admin."));
				return;
			}
			CivGlobal.addResident(res);
		}
		
		String ip = event.getAddress().getHostAddress();
		if (ip == null) {
			String msg = (" �b�l� CivilizationCraft �"+"\n"+
					" "+"\n"+
					"�c�lKicked By �r�8�� �4�oCONSOLE-PreLogin\n"+
					"�c�lReason �r�8�� �fKicked: Your IP is invalid? Contact an admin if this continues.\n");
			event.disallow(Result.KICK_OTHER, msg);
			CivLog.error("Null IP? Kicking player for potential problems.");
		} else {
			AccountLogger track = CivGlobal.getAccount(p.getUniqueId().toString());
			if (track == null) {
				track = new AccountLogger(p.getUniqueId(), ip);
				CivGlobal.addAccount(track);
				track.save();
				event.disallow(Result.KICK_OTHER, "An error occured. Please re-join the server.");
			}
			
			boolean isSaved = false;
			for (String trackIP : track.getIPsFromString()) {
				if (trackIP.equals(ip) && trackIP != null && ip != null) isSaved = true;
			}
			if (!isSaved) {
				track.addIPFromString(ip); track.save();
			}
			
			for (AccountLogger al : CivGlobal.getAccounts()) {
				if (!al.getUUID().equals(p.getUniqueId())) {
					for (String ipal : al.getIPsFromString()) {
						if (ipal.equals(ip)) {
							Resident found_al = al.getResident();
							found_al.addAlt(res.getUUIDString()); found_al.save();
							res.addAlt(found_al.getUUIDString()); res.save();
							
							if (!al.getIPsFromString().contains(ip)) {
								al.addIPFromString(ip); al.save();
							}
							if (!track.getIPsFromString().contains(ip)) {
								track.addIPFromString(ip); track.save();
							}
						}
					}
				}
			}
		}
		
		AccountLogger al = CivGlobal.getAccount(p.getUniqueId().toString());
		if (al.isBanned()) {
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
			sdf.setTimeZone(TimeZone.getTimeZone(res.getTimezone()));
			Date date = new Date(al.getBanLength());
			String msg = (" �b�l� CivilizationCraft �"+"\n"+
					" "+"\n"+
					"�c�lKicked By �r�8�� �4�oCONSOLE\n"+
					"�c�lReason �r�8�� �fBanned: "+al.getBanMessage()+"\n"+
					"�c�lUnbanned At �r�8�� �f"+sdf.format(date)+"\n"+
					" "+"\n"+
					" "+"\n"+
					"�e�lAppeal at �r�8�� �6http://coalcivcraft.enjin.com/forum"+"\n"+
					"�7�o[You are banned, cannot rejoin server.]"+"\n");
			event.disallow(Result.KICK_OTHER, msg);
			CivLog.info("Denied Player Join Task for:"+p.getName()+" (Banned)");
		}
		
		if (AdminCommand.isLockdown() && !p.isOp() && !CivPerms.isMiniAdmin(p)) {
			String msg = (" �b�l� CivilizationCraft �"+"\n"+
					" "+"\n"+
					"�c�lKicked By �r�8�� �4�oCONSOLE-PreLogin\n"+
					"�c�lReason �r�8�� �fKicked: The server is currently on lockdown... Try again in a few minutes.\n");
			event.disallow(Result.KICK_OTHER, msg);
			CivLog.info("Denied Player Join Task for:"+p.getName()+" (Server Lockdown)");
		}
		
		if (War.isWarTime() && War.isOnlyWarriors()) {
			if (p.isOp() || CivPerms.isMod(p)) {
				//Allowed to connect since player is OP or mini admin.
			} else if (!res.hasTown() || !res.getTown().getCiv().getDiplomacyManager().isAtWar()) {
				SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
				sdf.setTimeZone(TimeZone.getTimeZone(res.getTimezone()));
				Date date = new Date(War.getEnd().getTime());
				String msg = (" �b�l� CivilizationCraft �"+"\n"+
						" "+"\n"+
						"�c�lKicked By �r�8�� �4�oCONSOLE-PreLogin\n"+
						"�c�lReason �r�8�� �fKicked: Currently WarTime, only players at-war can join.\n"+
						"�c�lWar Ends At �r�8�� �f"+sdf.format(date)+"\n");
				event.disallow(Result.KICK_OTHER, msg);
				CivLog.info("Denied Player Join Task for:"+p.getName()+" (Not War Participant)");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		CivLog.info("Scheduling Player Join Task for:"+p.getName());
		TaskMaster.asyncTask("onPlayerLogin-"+event.getPlayer().getName(), new PlayerLoginAsyncTask(p.getUniqueId()), 0);
		PlayerLocationCacheUpdate.playerQueue.add(p.getName());
		MobSpawnerTimer.playerQueue.add(p.getName());
		setModifiedMovementSpeed(p);
		CivCraft.playerTagUpdate();
		
//		Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), "minecraft:recipe give "+event.getPlayer().getName()+" *");
		
		// TODO Make this a /res toggle serverpack?
		// Send the player the resource pack
/*		Bukkit.getScheduler().runTaskLater(CivCraft.getPlugin(), () -> {
			event.getPlayer().setResourcePack("https://github.com/YourCoal/YourCoal-Resource-Pack/files/2087472/YourCoal_Simple.zip");
		}, 30L); // Give time for the player to join*/
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Resident resident = CivGlobal.getResident(event.getPlayer());
		if (resident != null) {
			if (resident.previewUndo != null) {
				resident.previewUndo.clear();
			}
			resident.clearInteractiveMode();
		}
		resident.setLastOnline(System.currentTimeMillis());
		MobSpawnerTimer.playerQueue.remove(event.getPlayer().getName());
		PlayerLocationCacheUpdate.playerQueue.remove(event.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		TaskMaster.asyncTask(PlayerChunkNotifyAsyncTask.class.getSimpleName(), new PlayerChunkNotifyAsyncTask(event.getFrom(), event.getTo(), event.getPlayer().getName()), 0);
		if (event.getCause().equals(TeleportCause.COMMAND) || event.getCause().equals(TeleportCause.PLUGIN)) {
			CivLog.info("[TELEPORT] "+event.getPlayer().getName()+" to:"+event.getTo().getBlockX()+","+event.getTo().getBlockY()+","+event.getTo().getBlockZ()+
					" from:"+event.getFrom().getBlockX()+","+event.getFrom().getBlockY()+","+event.getFrom().getBlockZ());
		}
	}
		
	private void setModifiedMovementSpeed(Player player) {
		// Get player's current speed. Will be grabbing armor with it.
		double speed = CivSettings.normal_speed;
		
		// T5 Tungsten
		speed += Unit.speedWearingAnyDiamond(player, (CivSettings.T5_metal_speed / 4));
		// T4 Gold
		speed += Unit.speedWearingAnyGold(player, (CivSettings.T4_metal_speed / 4));
		// T3 Iron
		speed += Unit.speedWearingAnyIron(player, (CivSettings.T3_metal_speed / 4));
		// T2 Aluminium
		speed += Unit.speedWearingAnyChain(player, (CivSettings.T2_metal_speed / 4));
		
		// TODO Add all leather options
		
		// T1 Leather
//		speed += Unit.speedWearingBasicLeather(player, (CivSettings.T1_leather_speed / 4));
		
		// Add any attribute speed set on it
		for (ItemStack stack : player.getInventory().getContents()) {
			if (LoreEnhancement.isWeaponOrArmor(stack)) {
				AttributeUtil util = new AttributeUtil(stack);
				if (util.getCivCraftProperty("modified_speed") != null) {
					speed += Integer.valueOf(util.getCivCraftProperty("modified_speed"));
				}
			}
		}
		
		Resident resident = CivGlobal.getResident(player);
		if (resident != null && resident.isOnRoad()) {
			if (player.getVehicle() != null && player.getVehicle().getType().equals(EntityType.HORSE)) {
				Vector vec = player.getVehicle().getVelocity();
				double yComp = vec.getY();
				
				vec.multiply(Road.ROAD_HORSE_SPEED);
				vec.setY(yComp); // Do not multiply y velocity.
				player.getVehicle().setVelocity(vec);
			} else {
				speed *= Road.ROAD_PLAYER_SPEED;
			}
		}
		
		speed = Double.valueOf(new DecimalFormat("0.00000").format(speed));
		player.setWalkSpeed((float) Math.min(1.0f, speed));
	}
	
	@EventHandler
	public void onArmorWear(ArmorEquipEvent event) {
		class SyncTask implements Runnable {
			Player p;
				
			public SyncTask(Player p) {
				this.p = p;
			}
			
			@Override
			public void run() {
				setModifiedMovementSpeed(p);
			}
		}
		TaskMaster.syncTask(new SyncTask(event.getPlayer()), 5);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerVelocity(VehicleCreateEvent event) {
		if (event.getVehicle() instanceof Minecart) {
			Minecart mc = (Minecart) event.getVehicle();
			// Default speed is 0.4
			mc.setMaxSpeed(0.45);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerMove(PlayerMoveEvent event) {
		// Abort if we havn't really moved
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) return;
		
		ChunkCoord fromChunk = new ChunkCoord(event.getFrom());
		ChunkCoord toChunk = new ChunkCoord(event.getTo());
		if (fromChunk.equals(toChunk)) return; // Haven't moved chunks.
		
		TaskMaster.asyncTask(PlayerChunkNotifyAsyncTask.class.getSimpleName(), new PlayerChunkNotifyAsyncTask(event.getFrom(), event.getTo(), event.getPlayer().getName()), 0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) return;
		
		if (War.isWarTime()) {
			if (resident.getTown().getCiv().getDiplomacyManager().isAtWar()) {
				//TownHall townhall = resident.getTown().getTownHall();
				Capitol capitol = resident.getCiv().getCapitolStructure();
				if (capitol != null) {
					BlockCoord respawn = capitol.getRandomRespawnPoint();
					if (respawn != null) {
						//PlayerReviveTask reviveTask = new PlayerReviveTask(player, townhall.getRespawnTime(), townhall, event.getRespawnLocation());
						resident.setLastKilledTime(new Date());
						event.setRespawnLocation(respawn.getCenteredLocation());
						CivMessage.send(player, CivColor.Gray+"You've respawned in the War Room since it's WarTime and you're at war.");
						
						//TaskMaster.asyncTask("", reviveTask, 0);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (War.isWarTime()) {
			Player d = event.getEntity();
			WarStats.incrementPlayerDeaths(d.getName());
			if (d.getKiller() != null) {
				WarStats.incrementPlayerKills(d.getKiller().getName());
			}
		}
		
		//Unit.removeUnit(((Player)event.getEntity()));
		Boolean keepInventory = Boolean.valueOf(Bukkit.getWorld("world").getGameRuleValue("keepInventory"));
		if (!keepInventory) {
			ArrayList<ItemStack> stacksToRemove = new ArrayList<ItemStack>();
			for (ItemStack stack : event.getDrops()) {
				if (stack != null) {
					//CustomItemStack is = new CustomItemStack(stack);
					LoreMaterial material = LoreMaterial.getMaterial(stack);
					if (material != null) {
						material.onPlayerDeath(event, stack);
						if (material instanceof UnitMaterial) {
							stacksToRemove.add(stack); continue;
						}
						
						if (material instanceof UnitItemMaterial) {
							stacksToRemove.add(stack); continue;
						}
					}
				}
			}
			
			for (ItemStack stack : stacksToRemove) {
				event.getDrops().remove(stack);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onPortalCreate(PortalCreateEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPortalEvent(PlayerPortalEvent event) {
		if(event.getCause().equals(TeleportCause.END_PORTAL)) {
			event.setCancelled(true);
			CivMessage.sendErrorNoRepeat(event.getPlayer(), "The End portal is disabled on this server.");
			return;
		}
		
		if (event.getCause().equals(TeleportCause.NETHER_PORTAL)) {
			event.setCancelled(true);
			CivMessage.sendErrorNoRepeat(event.getPlayer(), "The Nether is disabled on this server.");
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
		Resident resident = CivGlobal.getResident(event.getPlayer());
	
		if (resident == null) {
			event.setCancelled(true);
			return;
		}

		ChunkCoord coord = new ChunkCoord(event.getBlockClicked().getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		if (cc != null) {
			if (event.getBucket().equals(Material.LAVA_BUCKET) || event.getBucket().equals(Material.LAVA)) {
				
				if (!resident.hasTown() || (resident.getTown().getCiv() != cc.getCiv())) {
					CivMessage.sendError(event.getPlayer(), "You cannot place lava inside another civ's culture.");
					event.setCancelled(true);
					return;
				}
				
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void OnBrewEvent(BrewEvent event) {
		/* Hardcoded disables based on ingredients used. */
		if (event.getContents().getItem(3).getType() == Material.BLAZE_POWDER) {
			event.setCancelled(true);
			return;
		}
		
		if (event.getContents().contains(Material.SPIDER_EYE) ||
				event.getContents().contains(Material.GOLDEN_CARROT) ||
				event.getContents().contains(Material.GHAST_TEAR) ||
				event.getContents().contains(Material.FERMENTED_SPIDER_EYE) ||
				event.getContents().contains(Material.SULPHUR)) {
				event.setCancelled(true);
			}
		
		if (event.getContents().contains(Material.POTION)) {
			ItemStack potion = event.getContents().getItem(event.getContents().first(Material.POTION));
			
			if (potion.getDurability() == CivData.MUNDANE_POTION_DATA || 
				potion.getDurability() == CivData.MUNDANE_POTION_EXT_DATA ||
				potion.getDurability() == CivData.THICK_POTION_DATA) {
				event.setCancelled(true);
			}
		}
	}
	
	private boolean isPotionDisabled(PotionEffect type) {
		if (type.getType().equals(PotionEffectType.SPEED) ||
			type.getType().equals(PotionEffectType.FIRE_RESISTANCE) ||
			type.getType().equals(PotionEffectType.HEAL)) {
			return false;
		}
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.LOW) 
	public void onPotionSplash(PotionSplashEvent event) {
		for (PotionEffect effect : event.getPotion().getEffects()) {
			if (isPotionDisabled(effect)) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW) 
	public void onConsume(PlayerItemConsumeEvent event) {
		Player p = event.getPlayer();
/*		if (CivItem.getId(event.getItem()) == CivData.GOLDEN_APPLE) {
			CivMessage.sendError(pl, "You cannot use golden apples.");
			event.setCancelled(true);
			return;
		}*/
		
		ItemStack is = event.getItem();
		if (is.getType().equals(Material.POTION)) {
			int effect = event.getItem().getDurability() & 0x000F;
			if (effect == 0xE) {
				event.setCancelled(true);
				CivMessage.sendError(p, "You cannot use invisibility potions for now... Sorry.");
				return;
			}
			
			ConfigTechPotion pot = CivSettings.techPotions.get(Integer.valueOf(is.getDurability()));
			if (pot != null) {
				if (!pot.hasTechnology(p)) {
					CivMessage.sendError(p, "You cannot use "+pot.name+" potions. You do not have the technology yet.");
					event.setCancelled(true);
					return;
				} else {
					event.setCancelled(false);
				}
			} else {
				CivMessage.sendError(p, "You cannot use this type of potion.");
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		if (event.getInventory() instanceof DoubleChestInventory) {
			DoubleChestInventory doubleInv = (DoubleChestInventory)event.getInventory();
						
			Chest leftChest = (Chest)doubleInv.getHolder().getLeftSide();			
			/*Generate a new player 'switch' event for the left and right chests. */
			PlayerInteractEvent interactLeft = new PlayerInteractEvent((Player)event.getPlayer(), Action.RIGHT_CLICK_BLOCK, null, leftChest.getBlock(), null);
			BlockListener.OnPlayerSwitchEvent(interactLeft);
			
			if (interactLeft.isCancelled()) {
				event.setCancelled(true);
				return;
			}
			
			Chest rightChest = (Chest)doubleInv.getHolder().getRightSide();
			PlayerInteractEvent interactRight = new PlayerInteractEvent((Player)event.getPlayer(), Action.RIGHT_CLICK_BLOCK, null, rightChest.getBlock(), null);
			BlockListener.OnPlayerSwitchEvent(interactRight);
			
			if (interactRight.isCancelled()) {
				event.setCancelled(true);
				return;
			}			
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageByEntityMonitor(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		
		Player attacker;
		Player defender;
		String damage;
		
		if (event.getEntity() instanceof Player) {
			defender = (Player)event.getEntity();
		} else {
			defender = null;
		}
		
		if (event.getDamager() instanceof Player) {
			attacker = (Player)event.getDamager();
		} else if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				attacker = (Player)arrow.getShooter();
			} else {
				attacker = null;
			}
		} else if (event.getDamager() instanceof Snowball) {
			Snowball sb = (Snowball)event.getDamager();
			if (sb.getShooter() instanceof Player) {
				attacker = (Player)sb.getShooter();
				if (sb.getCustomName().equals("bullet")) {
					// TODO Fix, this does TRUE damage and ignores armor. I mean, it could since it is a gun, but... meh.
					if (sb.getMetadata("damage") != null) event.setDamage(sb.getMetadata("damage").get(0).asDouble());
				}
			} else {
				attacker = null;
			}
		} else {
			attacker = null;
		}
		
		if (attacker == null && defender == null) {
			return;
		}
		
		damage = new DecimalFormat("#.#").format(event.getDamage());
		
		if (defender != null) {
			Resident defenderResident = CivGlobal.getResident(defender);
			if (defenderResident.isCombatInfo()) {	
				if (attacker != null) {
					CivMessage.send(defender, CivColor.Gray+"  [Combat] Took "+CivColor.Rose+damage+" damage "+CivColor.Gray+" from "+CivColor.LightPurple+attacker.getName());				
				} else {
					String entityName = null;
					if (event.getDamager() instanceof LivingEntity) {
						entityName = ((LivingEntity)event.getDamager()).getCustomName();
					}
					
					if (entityName == null) {
						entityName = event.getDamager().getType().toString();
					}
					
					if (entityName.equalsIgnoreCase("SNOWBALL") && event.getDamage() >= 1.0) entityName = "Bullet";
					
					CivMessage.send(defender, CivColor.Gray+"  [Combat] Took "+CivColor.Rose+damage+" damage "+CivColor.Gray+" from a "+entityName);
				}
			}
		}
		
		if (attacker != null) {
			Resident attackerResident = CivGlobal.getResident(attacker);
			if (attackerResident.isCombatInfo()) {
				if (defender != null) {
					CivMessage.send(attacker, CivColor.Gray+"    [Combat] Gave "+CivColor.LightGreen+damage+CivColor.Gray+" damage to "+CivColor.LightPurple+defender.getName());
				} else {
					String entityName = null;
					if (event.getEntity() instanceof LivingEntity) {
						entityName = ((LivingEntity)event.getEntity()).getCustomName();
					}
					
					if (entityName == null) {
						entityName = event.getEntity().getType().toString();
					}
					
					if (entityName.equalsIgnoreCase("SNOWBALL") && event.getDamage() >= 1.0) entityName = "Bullet";
					
					CivMessage.send(attacker, CivColor.Gray+"    [Combat] Gave "+CivColor.LightGreen+damage+CivColor.Gray+" damage to a "+entityName);
				}
			}
		}
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDamageEntityWarStat(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Player attacker;
		Player defender;
		Double damage;
		
		if (event.getEntity() instanceof Player) {
			defender = (Player)event.getEntity();
		} else {
			defender = null;
		}
		
		if (event.getDamager() instanceof Player) {
			attacker = (Player)event.getDamager();
		} else if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				attacker = (Player)arrow.getShooter();
			} else {
				attacker = null;
			}
		} else {
			attacker = null;
		}
		
		if (attacker == null || defender == null) {
			return;
		}
		
		damage = Double.valueOf(new DecimalFormat("#.#").format(event.getDamage()));
		WarStats.incrementPlayerDamage(defender.getName(), damage);
		WarStats.incrementPlayerAttack(attacker.getName(), damage);
	}
}
