package com.avrgaming.civcraft.war;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureBlock;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.FireWorkTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;

public class WarListener implements Listener {
	
	ChunkCoord coord = new ChunkCoord();
	private static ArrayList<Material> allowed_placeables = new ArrayList<Material>();
	private static ArrayList<Material> falling_blocks = new ArrayList<Material>();
	
	public static int tnt_yield;
	public static double tnt_playerDamage;
	public static int tnt_structureDamage;
	static {
		try {
			tnt_yield = CivSettings.getInteger(CivSettings.warConfig, "tnt.yield");
			tnt_playerDamage = CivSettings.getDouble(CivSettings.warConfig, "tnt.player_damage");
			tnt_structureDamage = CivSettings.getInteger(CivSettings.warConfig, "tnt.structure_damage");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
	}
	
	public static void addPlaceables() {
		allowed_placeables.add(Material.STONE);
		allowed_placeables.add(Material.GRASS);
		allowed_placeables.add(Material.DIRT);
		allowed_placeables.add(Material.MYCEL);
		allowed_placeables.add(Material.COBBLESTONE);
		allowed_placeables.add(Material.WOOD);
		allowed_placeables.add(Material.SAND);
		allowed_placeables.add(Material.GRAVEL);
		allowed_placeables.add(Material.WOOL);
		allowed_placeables.add(Material.TNT);
		allowed_placeables.add(Material.TORCH);
		allowed_placeables.add(Material.CHEST);
		allowed_placeables.add(Material.TRAPPED_CHEST);
		allowed_placeables.add(Material.ENDER_CHEST);
		allowed_placeables.add(Material.FURNACE);
		allowed_placeables.add(Material.WHITE_SHULKER_BOX);
		allowed_placeables.add(Material.ORANGE_SHULKER_BOX);
		allowed_placeables.add(Material.MAGENTA_SHULKER_BOX);
		allowed_placeables.add(Material.LIGHT_BLUE_SHULKER_BOX);
		allowed_placeables.add(Material.YELLOW_SHULKER_BOX);
		allowed_placeables.add(Material.LIME_SHULKER_BOX);
		allowed_placeables.add(Material.PINK_SHULKER_BOX);
		allowed_placeables.add(Material.GRAY_SHULKER_BOX);
		allowed_placeables.add(Material.SILVER_SHULKER_BOX);
		allowed_placeables.add(Material.CYAN_SHULKER_BOX);
		allowed_placeables.add(Material.PURPLE_SHULKER_BOX);
		allowed_placeables.add(Material.BLUE_SHULKER_BOX);
		allowed_placeables.add(Material.BROWN_SHULKER_BOX);
		allowed_placeables.add(Material.GREEN_SHULKER_BOX);
		allowed_placeables.add(Material.RED_SHULKER_BOX);
		allowed_placeables.add(Material.BLACK_SHULKER_BOX);
		allowed_placeables.add(Material.SIGN_POST);
		allowed_placeables.add(Material.WALL_SIGN);
		allowed_placeables.add(Material.LADDER);
		allowed_placeables.add(Material.VINE);
		allowed_placeables.add(Material.WOOD_PLATE);
		allowed_placeables.add(Material.CAKE_BLOCK);
		allowed_placeables.add(Material.WATER_LILY);
	}
	
	public static void AddFllowingBlocks() {
		falling_blocks.add(Material.COBBLESTONE);
		falling_blocks.add(Material.WOOD);
		falling_blocks.add(Material.WOOL);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		if (!War.isWarTime()) return;
		
		coord.setFromLocation(event.getBlock().getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		if (cc == null) return;
		if (!cc.getCiv().getDiplomacyManager().isAtWar()) return;
				
		if (allowed_placeables.contains(event.getBlock().getType())) return;
		
		CivMessage.sendError(event.getPlayer(), "Must use approved blocks or TNT to break blocks in at-war civilization cultures during WarTime.");
		event.setCancelled(true);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		if (!War.isWarTime()) return;
		
		coord.setFromLocation(event.getBlock().getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		if (cc == null) return;
		if (!cc.getCiv().getDiplomacyManager().isAtWar()) return;
		
		if (allowed_placeables.contains(event.getBlock().getType()) && falling_blocks.contains(event.getBlock().getType())) {
			if (event.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) return;
			event.getBlock().getWorld().spawnFallingBlock(event.getBlock().getLocation(), event.getBlock().getType(), CivItem.getData(event.getBlock()));
			event.getBlock().setType(Material.AIR);
			return;
		}
		
		CivMessage.sendError(event.getPlayer(), "Can only place approved blocks or TNT to break blocks in at-war civilization cultures during WarTime.");
		event.setCancelled(true);
	}
	
	public static final String RESTORE_NAME = "special:TNT";
	private void explodeBlock(Block b) {
		WarRegen.explodeThisBlock(b, WarListener.RESTORE_NAME);
		launchExplodeFirework(b.getLocation());
	}
	
	private void launchExplodeFirework(Location loc) {
		Random rand = new Random();
		int rand1 = rand.nextInt(100);
		
		if (rand1 > 90) {
		FireworkEffect fe = FireworkEffect.builder().withColor(Color.ORANGE).withColor(Color.YELLOW).flicker(true).with(Type.BURST).build();		
		TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 3), 0);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;
		
		if (War.isWarTime()) {
			event.setCancelled(false);
		} else {
			if (event.getEntity() instanceof TNTPrimed) {
				TNTPrimed tnt = (TNTPrimed) event.getEntity();
				if (tnt.getSource() instanceof Player) {
					Player p = (Player) tnt.getSource();
					Resident res = CivGlobal.getResident(p);
					coord.setFromLocation(event.getLocation());
					CultureChunk cc = CivGlobal.getCultureChunk(coord);
					if (cc != null && cc.getCiv() == res.getCiv()) {
						CivMessage.sendError(res, "You can only explode blocks inside your own culture borders when it is not War Time!");
						event.setCancelled(false);
						return;
					}
				}
			}
			event.setCancelled(true);
			return;
		}
		
		if (event.getEntity() == null) return;
		if (event.getEntityType().equals(EntityType.UNKNOWN)) return;
		
		if (event.getEntityType().equals(EntityType.PRIMED_TNT) || event.getEntityType().equals(EntityType.MINECART_TNT)) {
			HashSet<Buildable> structuresHit = new HashSet<Buildable>();
			for (int y = -tnt_yield; y <= tnt_yield; y++) {
				for (int x = -tnt_yield; x <= tnt_yield; x++) {
					for (int z = -tnt_yield; z <= tnt_yield; z++) {
						Location loc = event.getLocation().clone().add(new Vector(x,y,z));
						Block b = loc.getBlock();
						if (loc.distance(event.getLocation()) < tnt_yield) {
							BlockCoord bcoord = new BlockCoord();
							bcoord.setFromLocation(loc);
							StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
							if (sb == null) {
								explodeBlock(b);
								continue;
							} else {
								if (!sb.isDamageable()) continue;
								
								if (sb.getOwner() instanceof TownHall) {
									TownHall th = (TownHall)sb.getOwner();
									if (th.getControlPoints().containsKey(bcoord)) {
										continue;
									}
								}
								
								if (!sb.getOwner().isDestroyed()) {
									if (!structuresHit.contains(sb.getOwner())) {
										structuresHit.add(sb.getOwner());
										if (sb.getOwner() instanceof TownHall) {
											TownHall th = (TownHall)sb.getOwner();
											if (th.getHitpoints() == 0) { 
												explodeBlock(b);
											} else {
												th.onTNTDamage(tnt_structureDamage);
											}
										} else {
											sb.getOwner().onDamage(tnt_structureDamage, b.getWorld(), null, sb.getCoord(), sb);
											CivMessage.sendCiv(sb.getCiv(), CivColor.Yellow+"A "+sb.getOwner().getDisplayName()+" at "+
													sb.getOwner().getCenterLocation().getX()+","+
													sb.getOwner().getCenterLocation().getY()+","+
													sb.getOwner().getCenterLocation().getZ()+")"+" has been hit by TNT! ("+
													sb.getOwner().getHitpoints()+"/"+sb.getOwner().getMaxHitPoints()+")");
										}
									}
								} else {
									explodeBlock(b);
								}
								continue;
							}
						}
					}	
				}
			}
			event.setCancelled(true);
		}
	}
}

