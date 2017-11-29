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
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCultureLevel;
import com.avrgaming.civcraft.config.ConfigHappinessState;
import com.avrgaming.civcraft.config.ConfigTownLevel;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.items.BonusGoodie;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.AttrSource;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.BuildableDamageBlock;
import com.avrgaming.civcraft.object.ControlPoint;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureBlock;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.FireworkEffectPlayer;
import com.avrgaming.civcraft.util.ItemFrameStorage;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;
import com.avrgaming.civcraft.war.War;
import com.avrgaming.civcraft.war.WarStats;

public class TownHall extends Structure implements RespawnLocationHolder {

	//TODO make this configurable.
	public static int MAX_GOODIE_FRAMES = 8;
	
	private BlockCoord[] techbar = new BlockCoord[10];
	
	private BlockCoord technameSign;
	private byte technameSignData; //Hold the sign's orientation
	
	private BlockCoord techdataSign;
	private byte techdataSignData; //Hold the sign's orientation
	
	private ArrayList<ItemFrameStorage> goodieFrames = new ArrayList<ItemFrameStorage>();
	private ArrayList<BlockCoord> respawnPoints = new ArrayList<BlockCoord>();
	private ArrayList<BlockCoord> revivePoints = new ArrayList<BlockCoord>();
	protected HashMap<BlockCoord, ControlPoint> controlPoints = new HashMap<BlockCoord, ControlPoint>();
	
	public ArrayList<BlockCoord> nextGoodieFramePoint = new ArrayList<BlockCoord>();
	public ArrayList<Integer> nextGoodieFrameDirection = new ArrayList<Integer>();

	protected TownHall(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public TownHall(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	public String getDynmapDescription() {
		String out = "";
		out += "<b>Town Hall of "+this.getTown().getName()+"</b>";
		ConfigCultureLevel culturelevel = CivSettings.cultureLevels.get(this.getTown().getCultureLevel());
		out += "<br/>Culture: Level:"+culturelevel.level+" ("+this.getTown().getAccumulatedCulture()+"/"+culturelevel.amount+")";
		out += "<br/>Flat Tax: "+this.getTown().getFlatTax()*100+"%";
		out += "<br/>Property Tax: "+this.getTown().getTaxRate()*100+"%";
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "star";
	}
	
	@Override
	public void delete() throws SQLException {
		if (this.getTown() != null) {
			/* Remove any protected item frames. */
			for (ItemFrameStorage framestore : goodieFrames) {
				BonusGoodie goodie = CivGlobal.getBonusGoodie(framestore.getItem());
				if (goodie != null) {
					goodie.replenish();
				}
				CivGlobal.removeProtectedItemFrame(framestore.getFrameID());
			}
		}
		super.delete();		
	}
	
	public void addTechBarBlock(BlockCoord coord, int index) {
		techbar[index] = coord;
	}

	public BlockCoord getTechBarBlockCoord(int i) {
		if (techbar[i] == null)	return null;
		return techbar[i];
	}
	
	public BlockCoord getTechBar(int i) {
		return techbar[i];
	}
	
	public int getTechBarSize() {
		return techbar.length;
	}
	
	public BlockCoord getTechnameSign() {
		return technameSign;
	}

	public void setTechnameSign(BlockCoord technameSign) {
		this.technameSign = technameSign;
	}

	public BlockCoord getTechdataSign() {
		return techdataSign;
	}

	public void setTechdataSign(BlockCoord techdataSign) {
		this.techdataSign = techdataSign;
	}

	public byte getTechdataSignData() {
		return techdataSignData;
	}

	public void setTechdataSignData(byte techdataSignData) {
		this.techdataSignData = techdataSignData;
	}

	public byte getTechnameSignData() {
		return technameSignData;
	}

	public void setTechnameSignData(byte technameSignData) {
		this.technameSignData = technameSignData;
	}

	public void createGoodieItemFrame(BlockCoord absCoord, int slotId, int direction) {
		if (slotId >= MAX_GOODIE_FRAMES) return;
		
		/* Make sure there isn't another frame here. We have the position of the sign, but the entity's
		 * position is the block it's attached to. We'll use the direction from the sign data to determine
		 * which direction to look for the entity. */
		Block attachedBlock;
		BlockFace facingDirection;
		switch (direction) {
		case CivData.DATA_SIGN_EAST:
			attachedBlock = absCoord.getBlock();
			facingDirection = BlockFace.EAST;
			break;
		case CivData.DATA_SIGN_WEST:
			attachedBlock = absCoord.getBlock();
			facingDirection = BlockFace.WEST;
			break;
		case CivData.DATA_SIGN_NORTH:
			attachedBlock = absCoord.getBlock();
			facingDirection = BlockFace.NORTH;
			break;
		case CivData.DATA_SIGN_SOUTH:
			attachedBlock = absCoord.getBlock();
			facingDirection = BlockFace.SOUTH;
			break;
		default:
			CivLog.error("Bad sign data for /itemframe sign in town hall.");
			return;
		}
		
		Block itemFrameBlock = absCoord.getBlock();
		if (ItemManager.getId(itemFrameBlock) != CivData.AIR) {
			ItemManager.setTypeId(itemFrameBlock, CivData.AIR);
		}
		
		ItemFrameStorage itemStore;
		ItemFrame frame = null;
		Entity entity = CivGlobal.getEntityAtLocation(absCoord.getBlock().getLocation());
		if (entity == null || (!(entity instanceof ItemFrame))) {
			itemStore = new ItemFrameStorage(attachedBlock.getLocation(), facingDirection);
		} else {
			try {
				frame = (ItemFrame)entity;
				itemStore = new ItemFrameStorage(frame, attachedBlock.getLocation());
			} catch (CivException e) {
				e.printStackTrace();
				return;
			}
			if (facingDirection != BlockFace.EAST) {
				itemStore.setFacingDirection(facingDirection);
			}
		}
		itemStore.setBuildable(this);
		goodieFrames.add(itemStore);
	}
	
	public ArrayList<ItemFrameStorage> getGoodieFrames() {
		return this.goodieFrames;
	}
	
	public void setRespawnPoint(BlockCoord absCoord) {
		this.respawnPoints.add(absCoord);
	}
	
	public BlockCoord getRandomRespawnPoint() {
		if (this.respawnPoints.size() == 0) return null;
		Random rand = new Random();
		return this.respawnPoints.get(rand.nextInt(this.respawnPoints.size()));
	}
	
	public int getRespawnTime() {
		try {
			int baseRespawn = CivSettings.getInteger(CivSettings.warConfig, "war.respawn_time");	
			int controlRespawn = CivSettings.getInteger(CivSettings.warConfig, "war.control_block_respawn_time");
			int invalidRespawnPenalty = CivSettings.getInteger(CivSettings.warConfig, "war.invalid_respawn_penalty");
			
			int totalRespawn = baseRespawn;
			for (ControlPoint cp : this.controlPoints.values()) {
				if (cp.isDestroyed()) {
					totalRespawn += controlRespawn;
				}
			}
			
			if (this.validated && !this.isValid()) {
				totalRespawn += invalidRespawnPenalty;
			}
			
			// Search for any town in our civ with the medicine goodie.
			for (Town t : this.getCiv().getTowns()) {
				if (t.getBuffManager().hasBuff(Buff.MEDICINE)) {
					int respawnTimeBonus = t.getBuffManager().getEffectiveInt(Buff.MEDICINE);
					totalRespawn = Math.max(1, (totalRespawn-respawnTimeBonus));
					break;
				}
			}
			return totalRespawn;
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
		return 30;
	}
	
	public void setRevivePoint(BlockCoord absCoord) {
		this.revivePoints.add(absCoord);
	}
	
	public BlockCoord getRandomRevivePoint() {
		if (this.revivePoints.size() == 0 || !this.isComplete()) {
			return new BlockCoord(this.getCorner());
		}
		Random rand = new Random();
		int index = rand.nextInt(this.revivePoints.size());
		return this.revivePoints.get(index);
		
	}
	
	public void createControlPoint(BlockCoord absCoord) {
		Location centerLoc = absCoord.getLocation();
		
		/* Build the bedrock tower. */
		Block b = centerLoc.getBlock();
		ItemManager.setTypeId(b, CivData.FENCE); ItemManager.setData(b, 0);
		StructureBlock sb = new StructureBlock(new BlockCoord(b), this);
		this.addStructureBlock(sb.getCoord(), true);
		
		/* Build the control block. */
		b = centerLoc.getBlock().getRelative(0, 1, 0);
		ItemManager.setTypeId(b, CivData.OBSIDIAN);
		sb = new StructureBlock(new BlockCoord(b), this);
		this.addStructureBlock(sb.getCoord(), true);
		
		int townhallControlHitpoints;
		try {
			townhallControlHitpoints = CivSettings.getInteger(CivSettings.warConfig, "war.control_block_hitpoints_townhall");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			townhallControlHitpoints = 100;
		}
		BlockCoord coord = new BlockCoord(b);
		this.controlPoints.put(coord, new ControlPoint(coord, this, townhallControlHitpoints));
	}
	
	public void onControlBlockDestroy(ControlPoint cp, World world, Player player, StructureBlock hit) {
		//Should always have a resident and a town at this point.
		Resident attacker = CivGlobal.getResident(player);
		
		ItemManager.setTypeId(hit.getCoord().getLocation().getBlock(), CivData.AIR);
		world.playSound(hit.getCoord().getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5F, 0.5F);
		world.playSound(hit.getCoord().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.75F);
		world.playSound(hit.getCoord().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.75F);
		
		FireworkEffect effect = FireworkEffect.builder().with(Type.BURST).withColor(Color.YELLOW).withColor(Color.RED).withTrail().withFlicker().build();
		FireworkEffectPlayer fePlayer = new FireworkEffectPlayer();
		for (int i = 0; i < 3; i++) {
			try {
				fePlayer.playFirework(world, hit.getCoord().getLocation(), effect);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		boolean allDestroyed = true;
		for (ControlPoint c : this.controlPoints.values()) {
			if (!c.isDestroyed()) {
				allDestroyed = false;
				break;
			}
		}

		if (allDestroyed) {
			if (this.getTown().getCiv().getCapitolName().equals(this.getTown().getName())) {
				CivMessage.global(CivColor.LightBlueBold+"The civilization of "+this.getTown().getCiv().getName()+" has been conquered by "+attacker.getCiv().getName()+"!");
				CivMessage.worldSound(Sound.ENTITY_ENDERDRAGON_DEATH, 0.35F);
				for (Town town : this.getTown().getCiv().getTowns()) {
					town.defeated = true;
				}
				
				War.transferDefeated(this.getTown().getCiv(), attacker.getTown().getCiv());
				WarStats.logCapturedCiv(attacker.getTown().getCiv(), this.getTown().getCiv());
				War.saveDefeatedCiv(this.getCiv(), attacker.getTown().getCiv());
			
				if (CivGlobal.isCasualMode()) {
					HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(this.getCiv().getRandomLeaderSkull("Victory Over "+this.getCiv().getName()+"!"));
					for (ItemStack stack : leftovers.values()) {
						player.getWorld().dropItem(player.getLocation(), stack);
					}
				}
			} else {
				CivMessage.global(CivColor.YellowBold+"The town of "+getTown().getName()+" in "+this.getCiv().getName()+" has been conquered by "+attacker.getCiv().getName()+"!");
				CivMessage.worldSound(Sound.ENTITY_WITHER_DEATH, 0.75F);
				//this.getTown().onDefeat(attacker.getTown().getCiv());
				this.getTown().defeated = true;
				//War.defeatedTowns.put(this.getTown().getName(), attacker.getTown().getCiv());
				WarStats.logCapturedTown(attacker.getTown().getCiv(), this.getTown());
				War.saveDefeatedTown(this.getTown().getName(), attacker.getTown().getCiv());
			}
		} else {
			CivMessage.sendTown(hit.getTown(), CivColor.Rose+"One of our Town Hall's Control Points has been destroyed!");
			CivMessage.sendCiv(attacker.getTown().getCiv(), CivColor.LightGreen+"We've destroyed a control block in "+hit.getTown().getName()+"!");
			CivMessage.sendCiv(hit.getTown().getCiv(), CivColor.Rose+"A control block in "+hit.getTown().getName()+" has been destroyed!");
		}
	}
	
	public void onControlBlockHit(ControlPoint cp, World world, Player player, StructureBlock hit) {
		world.playSound(hit.getCoord().getLocation(), Sound.BLOCK_ANVIL_USE, 0.25F, 0.75F);
		world.playEffect(hit.getCoord().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
		CivMessage.send(player, CivColor.LightGray+"Damaged Control Block ("+cp.getHitpoints()+" / "+cp.getMaxHitpoints()+")");
		CivMessage.sendTown(hit.getTown(), CivColor.Yellow+"One of our Town Hall's Control Points is under attack!");
	}
	
	@Override
	public void onDamage(int amount, World world, Player player, BlockCoord coord, BuildableDamageBlock hit) {
		ControlPoint cp = this.controlPoints.get(coord);
		Resident resident = CivGlobal.getResident(player);
		if (!resident.canDamageControlBlock()) {
			CivMessage.send(player, CivColor.Rose+"Cannot damage control blocks due to missing/invalid Town Hall or Capitol structure.");
			return;
		}
		
		if (cp != null) {
			if (!cp.isDestroyed()) {
				if (resident.isControlBlockInstantBreak()) {
					cp.damage(cp.getHitpoints());
				} else{
					cp.damage(amount);
				}
				
				if (cp.isDestroyed()) {
					onControlBlockDestroy(cp, world, player, (StructureBlock)hit);
				} else {
					onControlBlockHit(cp, world, player, (StructureBlock)hit);
				}
			} else {
				CivMessage.send(player, CivColor.Rose+"Control Block already destroyed.");
			}
		} else {
			CivMessage.send(player, CivColor.Rose+"Cannot Damage " +this.getDisplayName()+ ", go after the control points!");
		}
	}
	
	public void regenControlBlocks() {
		for (BlockCoord coord : this.controlPoints.keySet()) { 
			ItemManager.setTypeId(coord.getBlock(), CivData.OBSIDIAN);
			ControlPoint cp = this.controlPoints.get(coord);
			cp.setHitpoints(cp.getMaxHitpoints());
		}
	}
	
	@Override
	public void onLoad() {
		// We must load goodies into the frame as we find them from the trade outpost's 
		// onLoad() function, otherwise we run into timing issues over which loads first.
	}
	
	@Override
	public void onPreBuild(Location loc) throws CivException {		
		TownHall oldTownHall = this.getTown().getTownHall();
		if (oldTownHall != null) {
			ChunkCoord coord = new ChunkCoord(loc);
			TownChunk tc = CivGlobal.getTownChunk(coord);
			if (tc == null || tc.getTown() != this.getTown()) {
				throw new CivException("Cannot rebuild your town hall outside of your town borders.");
			}
			
			if (War.isWarTime()) {
				throw new CivException("Cannot rebuild your town hall during war time.");
			}
			
			this.getTown().clearBonusGoods();
			
			try {
				this.getTown().demolish(oldTownHall, true);
			} catch (CivException e) {
				e.printStackTrace();
			}
			CivMessage.sendTown(this.getTown(), "Your old town hall or capitol was demolished to make way for your new one.");
			this.autoClaim = false;
		} else {
			this.autoClaim = true;
		}
	}
	
	@Override
	public void onInvalidPunish() {
		int invalid_respawn_penalty;
		try {
			invalid_respawn_penalty = CivSettings.getInteger(CivSettings.warConfig, "war.invalid_respawn_penalty");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return;
		}
		CivMessage.sendTown(this.getTown(), CivColor.Rose+CivColor.BOLD+"Our town's town hall cannot be supported by the blocks underneath!"+
				" It will take us an extra "+invalid_respawn_penalty+" mins to respawn during war if its not fixed in time!");
	}
	
	@Override
	public List<BlockCoord> getRespawnPoints() {
		return this.revivePoints;
	}
	
	@Override
	public String getRespawnName() {
		return "Town Hall\n"+this.getTown().getName();
	}	
	
	public HashMap<BlockCoord, ControlPoint> getControlPoints() {
		return this.controlPoints;
	}
	
/*	public void onCannonDamage(int damage, CannonProjectile projectile) throws CivException {
		if (!this.getCiv().getDiplomacyManager().isAtWar() || !War.isWarTime()) {
			return;
		}
		
		this.hitpoints -= damage;
		if (hitpoints <= 0) {
			for (BlockCoord coord : this.controlPoints.keySet()) {
				ControlPoint cp = this.controlPoints.get(coord);				
				if (cp != null) {
					if (cp.getHitpoints() > CannonProjectile.controlBlockHP) {
						cp.damage(cp.getHitpoints()-1);
						this.hitpoints = this.getMaxHitPoints()/2;
						CivMessage.sendCiv(getCiv(), "Our "+this.getDisplayName()+" has been hit by a cannon and a control block was set to "+CannonProjectile.controlBlockHP+" HP!");
						CivMessage.sendCiv(getCiv(), "Our "+this.getDisplayName()+" has regenerated "+this.getMaxHitPoints()/2+" HP! If it drops to zero, we will lose another Control Point.");
						return;
					}
				}
			}
			CivMessage.sendCiv(getCiv(), "Our "+this.getDisplayName()+" is out of hitpoints, walls can be destroyed by cannon and TNT blasts!");
			hitpoints = 0;
		}
		CivMessage.sendCiv(getCiv(), "Our "+this.getDisplayName()+" has been hit by a cannon! ("+this.hitpoints+"/"+this.getMaxHitPoints()+")");
	}*/
	
	public void onCannonDamage(int damage) {
		if (!this.getCiv().getDiplomacyManager().isAtWar() || !War.isWarTime()) return;
		this.hitpoints -= damage;
		if (hitpoints <= 0) {
			CivMessage.sendCiv(getCiv(), "Our "+this.getDisplayName()+" is out of hitpoints, walls can be destroyed by cannon blasts!");
			hitpoints = 0;
		}
		CivMessage.sendCiv(getCiv(), "Our "+this.getDisplayName()+" has been hit by a cannon! ("+this.hitpoints+"/"+this.getMaxHitPoints()+")");
	}
	
	public void onTNTDamage(int damage) {
		if (!this.getCiv().getDiplomacyManager().isAtWar() || !War.isWarTime()) return;
		if (hitpoints >= damage+1) {
			this.hitpoints -= damage;
			CivMessage.sendCiv(getCiv(), "Our "+this.getDisplayName()+" has been hit by TNT! ("+this.hitpoints+"/"+this.getMaxHitPoints()+")");
		}
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		switch (sb.command) {
		case "/guideinfo":
			spawnInfoVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		case "/questinfo":
			spawnQuestVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		}
	}
	
	// XXX Villager stuff
	
	public void spawnInfoVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName(this.getTown().getName()+"'s Handy Assistant");
		v.setProfession(Profession.PRIEST);
		for (Villager vg : CivGlobal.structureVillagers.keySet()) {
			if (vg.getLocation().equals(v.getLocation())) {
				CivGlobal.removeStructureVillager(v);
				v.remove();
			}
		}
		CivGlobal.addStructureVillager(v);
	}
	
	public void openMainInfoGUI(Player p, Town t) {
		DecimalFormat df = new DecimalFormat();
		ConfigTownLevel level = CivSettings.townLevels.get(t.getLevel());
		Inventory inv = Bukkit.createInventory(null, 9*3, t.getName()+"'s Town Info");
		
		String colorTile = CivColor.LightGreen;
		if (t.getTileImprovementCount() >= level.tile_improvements) {
			colorTile = CivColor.Rose;
		} else if (t.getTileImprovementCount() >= (level.tile_improvements-2)) {
			colorTile = CivColor.Yellow;
		}
		
		String colorPlot = CivColor.LightGreen;
		if (t.getTownChunks().size() >= level.plots) {
			colorPlot = CivColor.Rose;
		} else if (t.getTownChunks().size() >= (level.plots-16)) {
			colorPlot = CivColor.Yellow;
		}
		
/*		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Town Hall Guide Menu. You can use it",
				CivColor.RESET+"to view different stats within the town, or if",
				CivColor.RESET+"you are a high enough level, use it to change.",
				CivColor.RESET+"settings within the town. Civilization leaders",
				CivColor.RESET+"also have access to additonal options."
				));*/
		
		inv.setItem(1, LoreGuiItem.build(CivColor.GreenBold+"Town Location", CivData.FILLED_MAP, 0, 
				CivColor.LightGreen+"X: "+t.getTownHall().getCenterLocation().getX()+"",
				CivColor.LightGreen+"Y: "+t.getTownHall().getCorner().getY()+"",
				CivColor.LightGreen+"Z: "+t.getTownHall().getCenterLocation().getZ()+""
				));
		inv.setItem(2, LoreGuiItem.build(CivColor.GreenBold+"Score Points", CivData.EMPTY_MAP, 0, 
				CivColor.LightGreen+t.getScore()
				));
		
		inv.setItem(6, LoreGuiItem.build(CivColor.GreenBold+"Town Level", CivData.GOLD_NUGGET, 0, 
				CivColor.LightGreen+t.getLevel()+" ("+t.getLevelTitle()+")"
				));
		inv.setItem(7, LoreGuiItem.build(CivColor.GreenBold+"Level Information", CivData.SHULKER_SHELL, 0, 
				CivColor.Green+"Plots: "+CivColor.LightGreen+"("+colorPlot+t.getTownChunks().size()+"/"+level.plots+CivColor.LightGreen+")",
				CivColor.Green+"    Plot Cost: "+CivColor.LightGreen+TownChunk.getNextPlotCost(t)+" Coins/Plot",
				CivColor.Green+"Tiles: "+CivColor.LightGreen+"("+colorTile+t.getTileImprovementCount()+"/"+level.tile_improvements+CivColor.LightGreen+")"
				));
		
		inv.setItem(9, LoreGuiItem.build(CivColor.GreenBold+"Growth", CivData.WHEAT_ITEM, 0, 
				CivColor.LightGreen+df.format(t.getGrowth().total),
				CivColor.LightGray+"« Click for More Options »"
				));
		inv.setItem(10, LoreGuiItem.build(CivColor.GreenBold+"Hammers", CivData.COBBLESTONE_WALL, 0, 
				CivColor.LightGreen+df.format(t.getHammers().total),
				CivColor.LightGray+"« Click for More Options »"
				));
		inv.setItem(11, LoreGuiItem.build(CivColor.GreenBold+"Beakers", CivData.EMPTY_BOTTLE, 0, 
				CivColor.LightGreen+df.format(t.getBeakers().total),
				CivColor.LightGray+"« Click for More Options »"
				));
		
		ConfigCultureLevel clc = CivSettings.cultureLevels.get(t.getCultureLevel());	
		inv.setItem(18, LoreGuiItem.build(CivColor.GreenBold+"Culture", CivData.NETHERWART_ITEM, 0, 
				CivColor.Green+"Level: "+CivColor.LightGreen+clc.level,
				CivColor.Green+"Amount: "+CivColor.LightGreen+t.getAccumulatedCulture()+" / "+clc.amount,
				CivColor.LightGray+"« Click for More Options »"
				));
		ConfigHappinessState state = t.getHappinessState();
		inv.setItem(19, LoreGuiItem.build(CivColor.GreenBold+"Happiness", CivData.OTHER_FLOWERS, 8, 
				CivColor.Green+"Stat: "+CivColor.LightGreen+df.format(Math.floor(t.getHappiness())),
				CivColor.Green+"State: "+CivColor.valueOf(state.color)+state.name
				));
		
		
		Structure bank = t.getStructureByType("s_bank");
		if (bank != null) {
			inv.setItem(13, LoreGuiItem.build(CivColor.GreenBold+"Money Statistics", CivData.GOLD_INGOT, 0, 
					CivColor.Green+"Treasury: "+CivColor.LightGreen+t.getBalance()+" Coins",
					CivColor.Green+"Upkeep: "+CivColor.LightGreen+"-"+t.getTotalUpkeep()+" Coins/Day",
					" ",
					CivColor.Green+"Treasury Interest Rate: "+CivColor.LightGreen+df.format(((Bank)bank).getInterestRate()*100)+"%",
					CivColor.Green+"Principal: "+CivColor.LightGreen+"+"+((int) (t.getTreasury().getPrincipalAmount()*((Bank)bank).getInterestRate()))+" Coins/Day"
					));
		} else {
			inv.setItem(13, LoreGuiItem.build(CivColor.GreenBold+"Money Statistics", CivData.GOLD_INGOT, 0, 
					CivColor.Green+"Treasury: "+CivColor.LightGreen+t.getBalance()+" Coins",
					CivColor.Green+"Upkeep: "+CivColor.LightGreen+t.getTotalUpkeep()+" Coins/Day"
					));
		}
		
		inv.setItem(22, LoreGuiItem.build(CivColor.GreenBold+"Taxes", CivData.PAPER, 0, 
				CivColor.Green+"Tax Rate: "+CivColor.LightGreen+t.getTaxRateString(),
				CivColor.Green+"Flat Tax: "+CivColor.LightGreen+t.getFlatTax()+" Coins"
				));
		
		
		inv.setItem(15, LoreGuiItem.build(CivColor.GreenBold+"Trade Goods", CivData.ITEMFRAME, 0,
				t.getGoodiesNameAndColor().split(";")
				));
		
		inv.setItem(17, LoreGuiItem.build(CivColor.GreenBold+"Members", CivData.BOOK_AND_QUILL, 0,
				t.getResidentsNameAndColor().split(";")
				));
		
		
		inv.setItem(24, LoreGuiItem.build(CivColor.RoseBold+"In Development", CivData.BEDROCK, 0, 
				CivColor.LightGray+"« Check Back Later »"
				));
		
		inv.setItem(26, LoreGuiItem.build(CivColor.GreenBold+"Structure Support", CivData.BEACON, 0, 
				CivColor.Yellow+"Use the command '/build supportnearest' on a",
				CivColor.Yellow+"structure to use & fill in invalid blocks!",
				CivColor.Green+"Currently Storing: "+CivColor.LightGreen+t.getSupportDeposit()+" Blocks",
				CivColor.LightGray+"« Click to Deposit Blocks »"
				));
		
		p.openInventory(inv);
	}
	
	public void openGrowthStatsGUI(Player p, Town t) {
		AttrSource sources = t.getGrowth();
		Inventory inv = Bukkit.createInventory(null, 9*1, t.getName()+"'s Growth Stat Information");
		
		inv.setItem(0, LoreGuiItem.build(CivColor.GreenBold+"Sources", CivData.SNOW_BLOCK, 0, 
				sources.getSourceDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(1, LoreGuiItem.build(CivColor.GreenBold+"Rates", CivData.CLAY_BLOCK, 0, 
				sources.getRateDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(2, LoreGuiItem.build(CivColor.GreenBold+"Totals", CivData.WHEAT_ITEM, 0, 
				CivColor.Green+"Growth Amount: "+CivColor.LightGreen+t.getGrowth().total,
				CivColor.Green+"   Growth Rate: "+CivColor.LightGreen+(t.getGrowthRate().total*100)+"%"));
		
		inv.setItem(8, LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Info Menu"));
		p.openInventory(inv);
	}
	
	public void openHammerStatsGUI(Player p, Town t) {
		AttrSource sources = t.getHammers();
		Inventory inv = Bukkit.createInventory(null, 9*1, t.getName()+"'s Hammer Stat Information");
		
		inv.setItem(0, LoreGuiItem.build(CivColor.GreenBold+"Sources", CivData.SNOW_BLOCK, 0, 
				sources.getSourceDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(1, LoreGuiItem.build(CivColor.GreenBold+"Rates", CivData.CLAY_BLOCK, 0, 
				sources.getRateDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(2, LoreGuiItem.build(CivColor.GreenBold+"Totals", CivData.COBBLESTONE_WALL, 0, 
				CivColor.Green+"Hammer Amount: "+CivColor.LightGreen+t.getHammers().total,
				CivColor.Green+"   Hammer Rate: "+CivColor.LightGreen+(t.getHammerRate().total*100)+"%"));
		
		inv.setItem(8, LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Info Menu"));
		p.openInventory(inv);
	}
	
	public void openBeakerStatsGUI(Player p, Town t) {
		AttrSource sources = t.getBeakers();
		Inventory inv = Bukkit.createInventory(null, 9*1, t.getName()+"'s Beaker Stat Information");
		
		inv.setItem(0, LoreGuiItem.build(CivColor.GreenBold+"Sources", CivData.SNOW_BLOCK, 0, 
				sources.getSourceDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(1, LoreGuiItem.build(CivColor.GreenBold+"Rates", CivData.CLAY_BLOCK, 0, 
				sources.getRateDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(2, LoreGuiItem.build(CivColor.GreenBold+"Totals", CivData.EMPTY_BOTTLE, 0, 
				CivColor.Green+"Beaker Amount: "+CivColor.LightGreen+t.getBeakers().total,
				CivColor.Green+"   Beaker Rate: "+CivColor.LightGreen+(t.getBeakerRate().total*100)+"%"));
		
		inv.setItem(8, LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Info Menu"));
		p.openInventory(inv);
	}
	
	public void openCultureStatsGUI(Player p, Town t) {
		AttrSource sources = t.getCulture();
		Inventory inv = Bukkit.createInventory(null, 9*1, t.getName()+"'s Culture Stat Information");
		
		inv.setItem(0, LoreGuiItem.build(CivColor.GreenBold+"Sources", CivData.SNOW_BLOCK, 0, 
				sources.getSourceDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(1, LoreGuiItem.build(CivColor.GreenBold+"Rates", CivData.CLAY_BLOCK, 0, 
				sources.getRateDisplayString(CivColor.Green, CivColor.LightGreen).split(";")));
		
		inv.setItem(2, LoreGuiItem.build(CivColor.GreenBold+"Totals", CivData.NETHERWART_ITEM, 0, 
				CivColor.Green+"Culture Amount: "+CivColor.LightGreen+t.getCulture().total+"/hr",
				CivColor.Green+"   Culture Rate: "+CivColor.LightGreen+(t.getCultureRate().total*100)+"%"));
		
		inv.setItem(8, LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Info Menu"));
		p.openInventory(inv);
	}
	
	public void openTownBuffsListGUI(Player p, Town t) {
		Inventory inv = Bukkit.createInventory(null, 9*6, t.getName()+"'s Town-Applied Buffs");
		for (Buff b : this.getTown().getBuffManager().getAllBuffs()) {
			inv.addItem(LoreGuiItem.build(CivColor.GreenBold+b.getDisplayName(), CivData.IRON_NUGGET, 0, 
					CivColor.colorize(CivColor.RESET+b.getDescription())));
		}
		inv.setItem((9*6)-1, LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Info Menu"));
		p.openInventory(inv);
	}
	
	public void openSupportDepositGUI(Player p, Town t) {
		Inventory inv = Bukkit.createInventory(null, 9*6, t.getName()+"'s Building Support");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Support Deposit menu. You can add",
				CivColor.RESET+"dirt or gravel into this GUI, and the blocks will",
				CivColor.RESET+"be virtually saved. When you begin to build a",
				CivColor.RESET+"structure, we will automatically try to place the",
				CivColor.RESET+"blocks to support it (if we can)."
				));
		
		inv.setItem((9*6)-1, LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Info Menu"));
		p.openInventory(inv);
	}
	
	
	public void spawnQuestVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName(this.getTown().getName()+"'s Quest Viewer");
		v.setProfession(Profession.LIBRARIAN);
		for (Villager vg : CivGlobal.structureVillagers.keySet()) {
			if (vg.getLocation().equals(v.getLocation())) {
				CivGlobal.removeStructureVillager(v);
				v.remove();
			}
		}
		CivGlobal.addStructureVillager(v);
	}
	
	public void openTownQuestGUI(Player p, Town t) {
		Inventory inv = Bukkit.createInventory(null, 9*3, t.getName()+"'s Quest Viewer");
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" ",
				CivColor.RESET+" "
				));
		
		if (t.getStructureByType("ti_mine") != null) {
			inv.addItem(LoreGuiItem.build(CivColor.GreenBold+"Mine Tasks", CivData.STONE_PICKAXE, 0, 
					CivColor.LightGray+"« Click to View »"));
		} else {
			inv.addItem(LoreGuiItem.build(CivColor.RedBold+"Mine Tasks", CivData.BEDROCK, 0, 
					CivColor.LightGray+" « Cannot View Tasks »", CivColor.Rose+"« Structure Not Found »"));
		}
		
		if (t.getStructureByType("ti_lab") != null) {
			inv.addItem(LoreGuiItem.build(CivColor.GreenBold+"Lab Tasks", CivData.EMPTY_BOTTLE, 0, 
					CivColor.LightGray+"« Click to View »"));
		} else {
			inv.addItem(LoreGuiItem.build(CivColor.RedBold+"Lab Tasks", CivData.BEDROCK, 0, 
					CivColor.LightGray+" « Cannot View Tasks »", CivColor.Rose+"« Structure Not Found »"));
		}
		
		if (t.getStructureByType("ti_monument") != null) {
			inv.addItem(LoreGuiItem.build(CivColor.GreenBold+"Monument Tasks", CivData.SKULL, 4, 
					CivColor.LightGray+"« Click to View »"));
		} else {
			inv.addItem(LoreGuiItem.build(CivColor.RedBold+"Monument Tasks", CivData.BEDROCK, 0, 
					CivColor.LightGray+" « Cannot View Tasks »", CivColor.Rose+"« Structure Not Found »"));
		}
		
		if (t.getStructureByType("ti_cottage") != null) {
			inv.addItem(LoreGuiItem.build(CivColor.GreenBold+"Cottage Tasks", CivData.REDSTONE_LAMP, 0, 
					CivColor.LightGray+"« Click to View »"));
		} else {
			inv.addItem(LoreGuiItem.build(CivColor.RedBold+"Cottage Tasks", CivData.BEDROCK, 0, 
					CivColor.LightGray+" « Cannot View Tasks »", CivColor.Rose+"« Structure Not Found »"));
		}
		p.openInventory(inv);
	}
}