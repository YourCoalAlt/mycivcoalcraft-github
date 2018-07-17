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
package com.avrgaming.civcraft.object.camp;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.components.ConsumeLevelComponent;
import com.avrgaming.civcraft.components.ConsumeLevelComponent.Result;
import com.avrgaming.civcraft.components.SifterComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCampLonghouseLevel;
import com.avrgaming.civcraft.config.ConfigCampUpgrade;
import com.avrgaming.civcraft.database.SQL;
import com.avrgaming.civcraft.database.SQLUpdate;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.exception.InvalidNameException;
import com.avrgaming.civcraft.exception.InvalidObjectException;
import com.avrgaming.civcraft.items.components.Tagged;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.mobs.CivVillager;
import com.avrgaming.civcraft.object.BuildableDamageBlock;
import com.avrgaming.civcraft.object.ControlPoint;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureBlock;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.permission.PlotPermissions;
import com.avrgaming.civcraft.road.RoadBlock;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.template.Template.TemplateType;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.MailToResidentTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.BukkitObjects;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.FireworkEffectPlayer;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;
import com.avrgaming.civcraft.util.SimpleBlock;
import com.avrgaming.civcraft.util.SimpleBlock.Type;

import gpl.AttributeUtil;

public class Camp extends Buildable {
	
	private String ownerName;
	private int hitpoints;
	private int firepoints;
	private BlockCoord corner;	
	
	private HashMap<String, Resident> members = new HashMap<String, Resident>();
//	public static final double SHIFT_OUT = 2;
	public static final String SUBDIR = "camp";
	private boolean undoable = false;
	
	// Blocks to protect
	public ConcurrentHashMap<BlockCoord, CampBlock> campBlocks = new ConcurrentHashMap<BlockCoord, CampBlock>();
	public HashSet<BlockCoord> doors = new HashSet<BlockCoord>();
	
	// Fire locations for the firepit
	public HashSet<BlockCoord> fireFurnaceBlocks = new HashSet<BlockCoord>();
	private Integer coal_per_firepoint;
	private Integer maxFirePoints;
	
	private HashMap<String, ConfigCampUpgrade> upgrades = new HashMap<String, ConfigCampUpgrade>();
	
	// Garden
	private boolean gardenFirstEnabled = false;
	private boolean gardenSecondEnabled = false;
	
	// Sifter
	public SifterComponent sifter = new SifterComponent();
	public ReentrantLock sifterLock = new ReentrantLock(); 
	private boolean sifterEnabled = false;
	
	// Longhouse
	public HashSet<BlockCoord> foodDepositPoints = new HashSet<BlockCoord>();
	public ConsumeLevelComponent consumeComponent;
	private boolean longhouseEnabled = false;
	
	// Control blocks
	public HashMap<BlockCoord, ControlPoint> controlBlocks = new HashMap<BlockCoord, ControlPoint>();
	private Date nextRaidDate;
	private int raidLength;
	
	public static void newCamp(Resident resident, Player player, String name) {
		class SyncTask implements Runnable {
			Resident resident;
			String name;
			Player player;
			
			public SyncTask(Resident resident, String name, Player player) {
				this.resident = resident;
				this.name = name;
				this.player = player;
			}
			
			@Override
			public void run() {
				try {					
					Camp existCamp = CivGlobal.getCamp(name);
					if (existCamp != null) {
						throw new CivException("A camp named "+name+" already exists!");
					}
					
					ItemStack stack = player.getInventory().getItemInMainHand();
					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
					if (craftMat == null || !craftMat.hasComponent("FoundCamp")) {
						throw new CivException("You must be holding an item that can found a camp.");
					}
					
					Camp camp = new Camp(resident, name, player.getLocation());
					camp.buildCamp(player, player.getLocation());
					camp.setUndoable(true);
					CivGlobal.addCamp(camp);
					camp.save();
					CivCraft.playerTagUpdate();
					CivMessage.sendSuccess(player, "You have set up camp!");
					CivMessage.global(player.getName()+" has set up camp "+camp.getName());
					ItemStack newStack = new ItemStack(Material.AIR);
					player.getInventory().setItemInMainHand(newStack);
					resident.clearInteractiveMode();
				} catch (CivException e) {
					CivMessage.sendError(player, e.getMessage());
				}
			}
		}
		TaskMaster.syncTask(new SyncTask(resident, name, player));
	}
	
	public Camp(Resident owner, String name, Location corner) throws CivException {
		this.ownerName = owner.getUUID().toString();
		this.corner = new BlockCoord(corner);
		try {
			this.setName(name);
		} catch (InvalidNameException e1) {
			//e1.printStackTrace();
			throw new CivException("Invalid name, please choose another.");
		}
		nextRaidDate = new Date();
		nextRaidDate.setTime(nextRaidDate.getTime() + 24*60*60*1000);

		try {
			this.firepoints = CivSettings.getInteger(CivSettings.campConfig, "camp.firepoints");
			this.hitpoints = CivSettings.getInteger(CivSettings.campConfig, "camp.hitpoints");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
		loadSettings();
	}
	
	public Camp(ResultSet rs) throws SQLException, InvalidNameException, InvalidObjectException, CivException {
		this.load(rs);
		loadSettings();
	}
	
	@Override
	public void loadSettings() {
		try {
			coal_per_firepoint = CivSettings.getInteger(CivSettings.campConfig, "camp.coal_per_firepoint");
			maxFirePoints = CivSettings.getInteger(CivSettings.campConfig, "camp.firepoints");
			raidLength = CivSettings.getInteger(CivSettings.campConfig, "camp.raid_length");
			
			// Setup sifter
			double gold_nugget_chance = CivSettings.getDouble(CivSettings.campConfig, "camp.sifter_gold_nugget_chance");
			double iron_ignot_chance = CivSettings.getDouble(CivSettings.campConfig, "camp.sifter_iron_ingot_chance");
			
			sifter.addSiftItem(ItemManager.getId(Material.COBBLESTONE), (short) 0, gold_nugget_chance, ItemManager.getId(Material.GOLD_NUGGET), (short)0, 1);
			sifter.addSiftItem(ItemManager.getId(Material.COBBLESTONE), (short) 0, iron_ignot_chance, ItemManager.getId(Material.IRON_INGOT), (short)0, 1);
			sifter.addSiftItem(ItemManager.getId(Material.COBBLESTONE), (short) 0, 1.0, ItemManager.getId(Material.GRAVEL), (short)0, 1);
			
			consumeComponent = new ConsumeLevelComponent();
			consumeComponent.setBuildable(this);
			for (ConfigCampLonghouseLevel lvl : CivSettings.longhouseLevels.values()) {
				consumeComponent.addLevel(lvl.level, lvl.count);
				consumeComponent.setConsumes(lvl.level, lvl.consumes);
			}
			this.consumeComponent.onLoad();
		} catch (InvalidConfiguration e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static final String TABLE_NAME = "CAMPS";
	public static void init() throws SQLException {
		if (!SQL.hasTable(TABLE_NAME)) {
			String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME+" (" + 
					"`id` int(11) unsigned NOT NULL auto_increment," +
					"`name` VARCHAR(64) NOT NULL," +
					"`owner_name` mediumtext NOT NULL," +
					"`firepoints` int(11) DEFAULT 0," +
					"`next_raid_date` long,"+
					"`corner` mediumtext,"+
					"`upgrades` mediumtext,"+
					"`template_name` mediumtext,"+
				"PRIMARY KEY (`id`)" + ")";
			
			SQL.makeTable(table_create);
			CivLog.info("Created "+TABLE_NAME+" table");
		} else {
			CivLog.info(TABLE_NAME+" table OK!");
			SQL.makeCol("name", "VARCHAR(64) NOT NULL", TABLE_NAME);
			SQL.makeCol("upgrades", "mediumtext", TABLE_NAME);
			SQL.makeCol("template_name", "mediumtext", TABLE_NAME);
			SQL.makeCol("next_raid_date", "long", TABLE_NAME);
		}
	}
	
	
	@Override
	public void load(ResultSet rs) throws SQLException, InvalidNameException, InvalidObjectException, CivException {
		this.setId(rs.getInt("id"));
		this.setName(rs.getString("name"));
		this.ownerName = rs.getString("owner_name");
		this.corner = new BlockCoord(rs.getString("corner"));
		this.nextRaidDate = new Date(rs.getLong("next_raid_date"));
		this.setTemplateName(rs.getString("template_name"));
		
		try {
			this.hitpoints = CivSettings.getInteger(CivSettings.campConfig, "camp.hitpoints");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
		
		this.firepoints = rs.getInt("firepoints");
		
		if (this.ownerName == null) {
			CivLog.error("COULD NOT FIND OWNER FOR CAMP ID:"+this.getId());
			return;
		}
		
		this.loadUpgradeString(rs.getString("upgrades"));
		this.bindCampBlocks();
	}
	
	@Override
	public void save() {
		SQLUpdate.add(this);
	}
	
	@Override
	public void saveNow() throws SQLException {
		HashMap<String, Object> hashmap = new HashMap<String, Object>();
		hashmap.put("name", this.getName());
		hashmap.put("owner_name", this.getOwner().getUUIDString());
		hashmap.put("firepoints", this.firepoints);
		hashmap.put("corner", this.corner.toString());
		hashmap.put("next_raid_date", this.nextRaidDate.getTime());
		hashmap.put("upgrades", this.getUpgradeSaveString());
		hashmap.put("template_name", this.getSavedTemplatePath());
		SQL.updateNamedObject(this, hashmap, TABLE_NAME);			
	}	
	
	@Override
	public void delete() throws SQLException {
		for (Resident resident : this.members.values()) {
			resident.setCamp(null);
			resident.save();
		}
		
		this.unbindCampBlocks();
		SQL.deleteNamedObject(this, TABLE_NAME);
		CivGlobal.removeCamp(this);
	}
	
	public void loadUpgradeString(String upgrades) {
		String[] split = upgrades.split(",");
		for (String id : split) {
			if (id == null || id.equalsIgnoreCase("")) continue;
			id = id.trim();
			ConfigCampUpgrade upgrade = CivSettings.campUpgrades.get(id);
			if (upgrade == null) {
				CivLog.warning("Unknown upgrade id "+id+" during load.");
				continue;
			}
			this.upgrades.put(id, upgrade);
			upgrade.processAction(this);
		}
	}
	
	public String getUpgradeSaveString() {
		String out = "";
		for (ConfigCampUpgrade upgrade : this.upgrades.values()) {
			out += upgrade.id+",";
		}
		return out;
	}
	
	public void destroy() {
		this.fancyCampBlockDestory();
		try {
			this.delete();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void disband() {
		this.undoFromTemplate();
		try {
			this.delete();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void undo() {
		this.undoFromTemplate();
		try {
			this.delete();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void undoFromTemplate() {
		Template undo_tpl = new Template();
		try {
			undo_tpl.initUndoTemplate(this.getCorner().toString(), SUBDIR);
			undo_tpl.buildUndoTemplate(undo_tpl, this.getCorner().getBlock());
			undo_tpl.deleteUndoTemplate(this.getCorner().toString(), SUBDIR);
		} catch (IOException | CivException e1) {
			e1.printStackTrace();
		}
	}
	
	public void buildCamp(Player player, Location center) throws CivException {
		String templateFile;
		try {
			templateFile = CivSettings.getString(CivSettings.campConfig, "camp.template");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return;
		}
		Resident resident = CivGlobal.getResident(player);

		/* Load in the template. */
		Template tpl;
		if (resident.desiredTemplate == null) {
			try {
				//tpl.setDirection(center);
				String templatePath = Template.getTemplateFilePath(templateFile, Template.getDirection(center), TemplateType.STRUCTURE, "default");
				this.setTemplateName(templatePath);
				//tpl.load_template(templatePath);
				tpl = Template.getTemplate(templatePath, center);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (CivException e) {
				e.printStackTrace();
				return;
			}
		} else {
			tpl = resident.desiredTemplate;
			resident.desiredTemplate = null;
			this.setTemplateName(tpl.getFilepath());
		}
		
		corner.setFromLocation(this.repositionCenter(center, tpl.dir(), tpl.size_x, tpl.size_z));
		checkBlockPermissionsAndRestrictions(player, corner.getBlock(), tpl.size_x, tpl.size_y, tpl.size_z);
		try {
			tpl.saveUndoTemplate(getCorner().toString(), SUBDIR, getCorner().getLocation());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		buildCampFromTemplate(tpl, corner);
		processCommandSigns(tpl, corner);
		try {
			this.saveNow();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CivException("Internal SQL Error.");
		}
		this.addMember(resident);
		resident.save();
	}
	
	public void reprocessCommandSigns() {		
		Template tpl;
		try {
			tpl = Template.getTemplate(this.getSavedTemplatePath(), null);
		} catch (IOException | CivException e) {
			e.printStackTrace();
			return;
		}
		processCommandSigns(tpl, corner);
	}
	
	private void processCommandSigns(Template tpl, BlockCoord corner) {
		for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			BlockCoord absCoord = new BlockCoord(corner.getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));
			Block b = absCoord.getBlock();
			switch (sb.command) {
			case "/villager":
				spawnVillager(absCoord.getLocation(), (byte)sb.getData());
				break;
			case "/growth_first":
				if (this.isFirstGardenEnabled()) {
					CivGlobal.vanillaGrowthLocations.add(absCoord);
					if (ItemManager.getId(b) != CivData.FARMLAND) ItemManager.setTypeId(b, CivData.FARMLAND);
					this.addCampBlock(absCoord, true);
					this.addCampBlock(new BlockCoord(absCoord.getBlock().getRelative(0, 1, 0)), true);
				} else {
					if (ItemManager.getId(b) != CivData.CONCRETE && ItemManager.getData(b) != 12) {
						ItemManager.setTypeId(b, CivData.CONCRETE);
						ItemManager.setData(b, 12);
					}
					this.addCampBlock(absCoord, false);
					this.addCampBlock(new BlockCoord(absCoord.getBlock().getRelative(0, 1, 0)));
				}
				break;
			case "/growth_second":
				if (this.isSecondGardenEnabled()) {
					CivGlobal.vanillaGrowthLocations.add(absCoord);
					if (ItemManager.getId(b) != CivData.FARMLAND) ItemManager.setTypeId(b, CivData.FARMLAND);
					this.addCampBlock(absCoord, true);
					this.addCampBlock(new BlockCoord(absCoord.getBlock().getRelative(0, 1, 0)), true);
				} else {
					if (ItemManager.getId(b) != CivData.CONCRETE && ItemManager.getData(b) != 12) {
						ItemManager.setTypeId(b, CivData.CONCRETE);
						ItemManager.setData(b, 12);
					}
					this.addCampBlock(absCoord);
					this.addCampBlock(new BlockCoord(absCoord.getBlock().getRelative(0, 1, 0)));
				}
				break;
			case "/fire":
				ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.FIRE));
				break;
			case "/fuelfurnace":
				this.fireFurnaceBlocks.add(absCoord);
				if (ItemManager.getId(b) != CivData.FURNACE && ItemManager.getId(b) != CivData.FURNACE_LIT) {
					byte data = CivData.convertSignDataToChestData((byte)sb.getData());
					ItemManager.setTypeId(b, ItemManager.getId(Material.FURNACE));
					ItemManager.setData(b, data);
				}
				this.addCampBlock(absCoord);

				break;
			case "/sifter":
				Integer id = Integer.valueOf(sb.keyvalues.get("id"));
				switch (id) {
				case 0:
					sifter.setSourceCoord(absCoord);
					break;
				case 1:
					sifter.setDestCoord(absCoord);
					break;
				default:
					CivLog.warning("Unknown ID for sifter in camp:"+id);
					break;
				}
				
				if (this.sifterEnabled) {
					if (ItemManager.getId(b) != CivData.CHEST) {
						ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.CHEST));
						byte data2 = CivData.convertSignDataToChestData((byte)sb.getData());
						ItemManager.setData(absCoord.getBlock(), data2);
					}
				} else {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.SIGN_POST));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, "Sifter");
					sign.setLine(1, "Disabled");
					sign.setLine(2, "Upgrade using");
					sign.setLine(3, "/camp upgrade");
					sign.update();
				}
				this.addCampBlock(absCoord);
				break;
			case "/longhouse":
				if (this.longhouseEnabled) {
					this.foodDepositPoints.add(absCoord);
					if (ItemManager.getId(b) != CivData.CHEST) {
						ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.CHEST));
						byte data3 = CivData.convertSignDataToChestData((byte)sb.getData());
						ItemManager.setData(absCoord.getBlock(), data3);
					}
				} else {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.SIGN_POST));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, "Longhouse");
					sign.setLine(1, "Disabled");
					sign.setLine(2, "Upgrade using");
					sign.setLine(3, "/camp upgrade");
					sign.update();
				}
				this.addCampBlock(absCoord);
				break;
			case "/door":
				this.doors.add(absCoord);
				Block doorBlock = absCoord.getBlock();
				Block doorBlock2 = absCoord.getBlock().getRelative(0, 1, 0);
				
				byte topData = 0x8;
				byte bottomData = 0x0;
				byte doorDirection = CivData.convertSignDataToDoorDirectionData((byte)sb.getData());
				bottomData |= doorDirection;
				
				ItemManager.setTypeIdAndData(doorBlock, ItemManager.getId(Material.WOODEN_DOOR), bottomData, false);
				ItemManager.setTypeIdAndData(doorBlock2, ItemManager.getId(Material.WOODEN_DOOR), topData, false);
				this.addCampBlock(new BlockCoord(doorBlock));
				this.addCampBlock(new BlockCoord(doorBlock2));
				break;
			case "/control":
				this.createControlPoint(absCoord);
				break;
			case "/literal":
				/* Unrecognized command... treat as a literal sign. */
				ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
				ItemManager.setData(absCoord.getBlock(), sb.getData());
				Sign sign = (Sign)absCoord.getBlock().getState();
				sign.setLine(0, sb.message[0]);
				sign.setLine(1, sb.message[1]);
				sign.setLine(2, sb.message[2]);
				sign.setLine(3, sb.message[3]);
				sign.update();
				break;
			}
		}
	}
	
	public void processFirepoints() {
		MultiInventory mInv = new MultiInventory();
		for (BlockCoord bcoord : this.fireFurnaceBlocks) {
			Furnace furnace = (Furnace)bcoord.getBlock().getState();
			mInv.addInventory(furnace.getInventory());
		}
		
		if (mInv.contains(null, CivData.COAL, (short)0, coal_per_firepoint)) {
			try {
				mInv.removeItem(CivData.COAL, (short)0, coal_per_firepoint);
			} catch (CivException e) {
				e.printStackTrace();
			}
			
			this.firepoints++;
			if (firepoints > maxFirePoints) firepoints = maxFirePoints;
		} else if (mInv.contains(null, CivData.COAL, (short)1, coal_per_firepoint)) {
			try {
				mInv.removeItem(CivData.COAL, (short)1, coal_per_firepoint);
			} catch (CivException e) {
				e.printStackTrace();
			}
			
			this.firepoints++;
			if (firepoints > maxFirePoints) firepoints = maxFirePoints;
		} else {
			this.firepoints--;
			CivMessage.sendCamp(this, CivColor.Yellow+"Our campfire doesn't have enough coal to keep burning, its starting to go out! "+this.firepoints+" hours left.");
			double percentLeft = (double)this.firepoints / (double)this.maxFirePoints;
			if (percentLeft < 0.3) {
				CivMessage.sendCamp(this, CivColor.Yellow+ChatColor.BOLD+"Warning! Our campfire is less than 30% out! We need to stock it with more coal or our camp will be destroyed!");
			}
			
			if (this.firepoints < 0) {
					this.destroy();
			}
		}
		this.save();
	}
	
	public void processLonghouse() {
		MultiInventory mInv = new MultiInventory();
		for (BlockCoord bcoord : this.foodDepositPoints) {
			Block b = bcoord.getBlock();
			if (b.getState() instanceof Chest) {
				try {
					mInv.addInventory(((Chest)b.getState()).getInventory());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if (mInv.getInventoryCount() <= 0) {
			CivMessage.sendCamp(this, CivColor.Rose+"Your camp's longhouse could not find an input chest for food! Nothing happens.");
			return;
		}
		
		this.consumeComponent.setSource(mInv);
		Result result = this.consumeComponent.processConsumption();
		this.consumeComponent.onSave();
		
		switch (result) {
		case STARVE:
			CivMessage.sendCamp(this, CivColor.LightGreen+"Your camp's longhouse "+CivColor.Rose+"starved"+consumeComponent.getCountString()+CivColor.LightGreen+" and generated no coins.");
			return;
		case LEVELDOWN:
			CivMessage.sendCamp(this, CivColor.LightGreen+"Your camp's longhouse "+CivColor.Rose+"starved and leveled-down"+CivColor.LightGreen+" and generated no coins.");
			return;
		case STAGNATE:
			CivMessage.sendCamp(this, CivColor.LightGreen+"Your camp's longhouse "+CivColor.Yellow+"stagnated"+CivColor.LightGreen+" and generated no coins.");
			return;
		case UNKNOWN:
			CivMessage.sendCamp(this, CivColor.LightGreen+"Your camp's longhouse has done "+CivColor.Purple+"something unknown"+CivColor.LightGreen+" and generated no coins.");
			return;
		default:
			break;
		}
		
		ConfigCampLonghouseLevel lvl = null;
		if (result == Result.LEVELUP) {
			lvl = CivSettings.longhouseLevels.get(consumeComponent.getLevel()-1);
		} else {
			lvl = CivSettings.longhouseLevels.get(consumeComponent.getLevel());
		}
		
		double total_coins = lvl.coins;
		this.getOwner().getTreasury().deposit(total_coins);
		
		LoreCraftableMaterial craftMat =  LoreCraftableMaterial.getCraftMaterialFromId("civ_token_of_leadership");
		if (craftMat != null) {
			ItemStack token = LoreCraftableMaterial.spawn(craftMat);
			
			Tagged tag = (Tagged) craftMat.getComponent("Tagged");
			Resident res = CivGlobal.getResident(this.getOwnerName());
			token = tag.addTag(token, res.getUUIDString());
	
			AttributeUtil attrs = new AttributeUtil(token);
			attrs.addLore(CivColor.Gray+this.getOwnerName());
			token = attrs.getStack();
			
			Inventory inv = Bukkit.createInventory(null, 9*5); inv.addItem(token);
			BukkitObjects.scheduleAsyncDelayedTask(new MailToResidentTask(res, "camp_token", System.currentTimeMillis(), inv), 0);
//			mInv.addItem(token);
		}
		
		String stateMessage = "";
		switch (result) {
		case GROW:
			stateMessage = CivColor.Green+"grew"+consumeComponent.getCountString()+CivColor.LightGreen;
			break;
		case LEVELUP:
			stateMessage = CivColor.Green+"leveled up"+CivColor.LightGreen;
			break;
		case MAXED:
			stateMessage = CivColor.Green+"is maxed"+consumeComponent.getCountString()+CivColor.LightGreen;
			break;
		default:
			break;
		}
		
		CivMessage.sendCamp(this, CivColor.LightGreen+"Your camp's longhouse (level "+lvl.level+") "+stateMessage+" and generated "+total_coins+" coins. Coins were given to the camp's owner."
				+" A Token of Leadership was mailed to the camp owner.");
	}
	
	private void buildCampFromTemplate(Template tpl, BlockCoord corner) {
		Block cornerBlock = corner.getBlock();
		for (int x = 0; x < tpl.size_x; x++) {
			for (int y = 0; y < tpl.size_y; y++) {
				for (int z = 0; z < tpl.size_z; z++) {
					Block nextBlock = cornerBlock.getRelative(x, y, z);
					if (tpl.blocks[x][y][z].specialType == Type.COMMAND) continue;
					if (tpl.blocks[x][y][z].specialType == Type.LITERAL) {
						// Adding a command block for literal sign placement
						tpl.blocks[x][y][z].command = "/literal";
						tpl.commandBlockRelativeLocations.add(new BlockCoord(cornerBlock.getWorld().getName(), x, y,z));
						continue;
					}
					
					try {
						if (ItemManager.getId(nextBlock) != tpl.blocks[x][y][z].getType()) {
							ItemManager.setTypeId(nextBlock, tpl.blocks[x][y][z].getType());
							ItemManager.setData(nextBlock, tpl.blocks[x][y][z].getData());
						}
						
						if (ItemManager.getId(nextBlock) != CivData.AIR) {
							this.addCampBlock(new BlockCoord(nextBlock.getLocation()));
						}
					} catch (Exception e) {
						CivLog.error(e.getMessage());
					}
				}
			}
		}		
	}
	
	private void bindCampBlocks() {
		// Called mostly on a reload, determines which blocks should be protected based on the corner
		// location and the template's size. We need to verify that each block is a part of the template.
	
		// Load in the template.
		Template tpl;
		try {
			tpl = Template.getTemplate(this.getSavedTemplatePath(), null);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (CivException e) {
			e.printStackTrace();
			return;
		}
		
		for (int y = 0; y < tpl.size_y; y++) {
			for (int z = 0; z < tpl.size_z; z++) {
				for (int x = 0; x < tpl.size_x; x++) {
					int relx = getCorner().getX() + x;
					int rely = getCorner().getY() + y;
					int relz = getCorner().getZ() + z;
					
					BlockCoord coord = new BlockCoord(this.getCorner().getWorldname(), (relx), (rely), (relz));
					if (tpl.blocks[x][y][z].getType() == CivData.AIR) continue;
					if (tpl.blocks[x][y][z].specialType == SimpleBlock.Type.COMMAND) continue;
					this.addCampBlock(coord);
				}
			}
		}
		this.processCommandSigns(tpl, corner);
	}
	
	protected void checkBlockPermissionsAndRestrictions(Player player, Block centerBlock, int regionX, int regionY, int regionZ) throws CivException {
		ChunkCoord ccoord = new ChunkCoord(centerBlock.getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(ccoord);
		if (cc != null) {
			throw new CivException("You cannot build a camp inside a civilization's culture.");
		}
		
		if (player.getLocation().getY() >= 200) {
			throw new CivException("You're too high to build camps.");
		}
		
		if ((regionY + centerBlock.getLocation().getBlockY()) >= 255) {
			throw new CivException("Cannot build camp here, would go over the minecraft height limit.");
		}
		
		if (!player.isOp()) {
			Buildable.validateDistanceFromSpawn(centerBlock.getLocation());
		}
		
		int yTotal = 0;
		int yCount = 0;
		RoadBlock rb;
		LinkedList<RoadBlock> deletedRoadBlocks = new LinkedList<RoadBlock>();
		for (int x = 0; x < regionX; x++) {
			for (int y = 0; y < regionY; y++) {
				for (int z = 0; z < regionZ; z++) {
					Block b = centerBlock.getRelative(x, y, z);
					if (ItemManager.getId(b) == CivData.CHEST) {
						throw new CivException("Cannot build here, would destroy chest.");
					}
		
					BlockCoord coord = new BlockCoord(b);
					ChunkCoord chunkCoord = new ChunkCoord(coord.getLocation());
					TownChunk tc = CivGlobal.getTownChunk(chunkCoord);
					if (tc != null && !tc.perms.hasPermission(PlotPermissions.PlotNodeType.DESTROY, CivGlobal.getResident(player))) {
						// Make sure we have permission to destroy any block in this area.
						throw new CivException("Cannot build here, you need DESTROY permissions to the block at "+b.getX()+","+b.getY()+","+b.getZ());
					}
					
					if (CivGlobal.getProtectedBlock(coord) != null) {
						throw new CivException("Cannot build here, protected blocks in the way.");
					}
					
					if (CivGlobal.getStructureBlock(coord) != null) {
						throw new CivException("Cannot build here, structure blocks in the way.");
					}
				
					if (CivGlobal.getFarmChunk(chunkCoord) != null) {
						throw new CivException("Cannot build here, in the same chunk as a farm improvement.");
					}
		
					if (CivGlobal.getWallChunk(chunkCoord) != null) {
						throw new CivException("Cannot build here, in the same chunk as a wall improvement.");
					}
					
					if (CivGlobal.getCampBlock(coord) != null) {
						throw new CivException("Cannot build here, a camp is in the way.");
					}
					
					yTotal += b.getWorld().getHighestBlockYAt(centerBlock.getX()+x, centerBlock.getZ()+z);
					yCount++;
					
					rb = CivGlobal.getRoadBlock(coord);
					if (CivGlobal.getRoadBlock(coord) != null) {
						/* XXX Special case. Since road blocks can be built in wilderness
						 * we don't want people griefing with them. Building a structure over
						 * a road block should always succeed. */
						deletedRoadBlocks.add(rb);
					}
				}
			}
		}
		
		// Delete any roads that we're building over
		for (RoadBlock roadBlock : deletedRoadBlocks) {
			roadBlock.getRoad().deleteRoadBlock(roadBlock);
		}
		
		double highestAverageBlock = (double)yTotal / (double)yCount;
		if (((centerBlock.getY() > (highestAverageBlock+10)) || 
				(centerBlock.getY() < (highestAverageBlock-10)))) {
			throw new CivException("Cannot build here, you must be closer to the surface.");
		}
	}
	
	public void unbindCampBlocks() {
		ArrayList<ChunkCoord> chunksRemoved = new ArrayList<ChunkCoord>();
		for (BlockCoord bcord : this.campBlocks.keySet()) {
			this.removeCampBlock(bcord);
			ChunkCoord ccord = new ChunkCoord(bcord);
			if (!chunksRemoved.contains(ccord)) {
				CivGlobal.removeCampChunk(ccord);
				chunksRemoved.add(ccord);
			}
		}
	}
	
	private void addCampBlock(BlockCoord coord) {
		addCampBlock(coord, false);
	}
	
	private void addCampBlock(BlockCoord coord, boolean friendlyBreakable) {
		CampBlock cb = new CampBlock(coord, this, friendlyBreakable);
		this.campBlocks.put(coord, cb);
		CivGlobal.addCampBlock(cb);
	}
	
	private void removeCampBlock(BlockCoord absCoord) {
		this.campBlocks.remove(absCoord);
		CivGlobal.removeCampBlock(absCoord);
	}
	
	public void addMember(Resident resident) {
		this.members.put(resident.getName(), resident);
		resident.setCamp(this);
		resident.save();
	}
	
	public void removeMember(Resident resident) {
		this.members.remove(resident.getName());
		resident.setCamp(null);
		resident.save();
	}
	
	public Resident getMember(String name) {
		return this.members.get(name);
	}
	
	public boolean hasMember(String name) {
		return this.members.containsKey(name);
	}
	
	public boolean isOwner(Resident res) {
		if (this.getOwner().getUUID().equals(res.getUUID())) return true;
		return false;		
	}
	
	public Resident getOwner() {
		return CivGlobal.getResidentViaUUID(UUID.fromString(ownerName));
	}
	
	public void setOwner(Resident owner) {
		this.ownerName = owner.getUUID().toString();
	}
	
	public int getHitpoints() {
		return hitpoints;
	}
	
	public void setHitpoints(int hitpoints) {
		this.hitpoints = hitpoints;
	}
	
	public int getFirepoints() {
		return firepoints;
	}
	
	public void setFirepoints(int firepoints) {
		this.firepoints = firepoints;
	}
	
	public BlockCoord getCorner() {
		return corner;
	}
	
	public void setCorner(BlockCoord corner) {
		this.corner = corner;
	}
	
	public void fancyCampBlockDestory() {
		for (BlockCoord coord : this.campBlocks.keySet()) {
			if (CivGlobal.getStructureChest(coord) != null) continue;
			if (CivGlobal.getStructureSign(coord) != null) continue;
			if (ItemManager.getId(coord.getBlock()) == CivData.CHEST) continue;
			if (ItemManager.getId(coord.getBlock()) == CivData.SIGN) continue;
			if (ItemManager.getId(coord.getBlock()) == CivData.WALL_SIGN) continue;
			
			if (CivSettings.alwaysCrumble.contains(ItemManager.getId(coord.getBlock()))) {
				ItemManager.setTypeId(coord.getBlock(), CivData.GRAVEL);
				continue;
			}
						
			Random rand = new Random();
			// Each block has a 10% chance to turn into gravel
			if (rand.nextInt(100) <= 10) {
				ItemManager.setTypeId(coord.getBlock(), CivData.GRAVEL);
				continue;
			}
			
			// Each block has a 50% chance of starting a fire
			if (rand.nextInt(100) <= 50) {
				ItemManager.setTypeId(coord.getBlock(), CivData.FIRE);
				continue;
			}
			
			// Each block has a 1% chance of launching an explosion effect
			if (rand.nextInt(100) <= 1) {
				FireworkEffect effect = FireworkEffect.builder().with(org.bukkit.FireworkEffect.Type.BURST).withColor(Color.ORANGE).withColor(Color.RED).withTrail().withFlicker().build();
				FireworkEffectPlayer fePlayer = new FireworkEffectPlayer();
				for (int i = 0; i < 3; i++) {
					try {
						fePlayer.playFirework(coord.getBlock().getWorld(), coord.getLocation(), effect);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}			
			}
		}
	}
	
	public void createControlPoint(BlockCoord absCoord) {
		Location centerLoc = absCoord.getLocation();
		
		// Build the bedrock tower.
		Block b = centerLoc.getBlock();
		ItemManager.setTypeId(b, CivData.FENCE); ItemManager.setData(b, 0);
		StructureBlock sb = new StructureBlock(new BlockCoord(b), this);
		this.addCampBlock(sb.getCoord());
		
		// Build the control block. 
		b = centerLoc.getBlock().getRelative(0, 1, 0);
		ItemManager.setTypeId(b, CivData.OBSIDIAN);
		sb = new StructureBlock(new BlockCoord(b), this);
		this.addCampBlock(sb.getCoord());
	
		int campControlHitpoints;
		try {
			campControlHitpoints = CivSettings.getInteger(CivSettings.warConfig, "war.control_block_hitpoints_camp");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			campControlHitpoints = 100;
		}
		
		BlockCoord coord = new BlockCoord(b);		
		this.controlBlocks.put(coord, new ControlPoint(coord, this, campControlHitpoints));
	}
	
	public boolean isUndoable() {
		return undoable;
	}
	
	public void setUndoable(boolean undoable) {
		this.undoable = undoable;
	}
	
	@Override
	public String getDisplayName() {
		return "Camp";
	}
	
	@Override
	public void sessionAdd(String key, String value) {
		CivGlobal.getSessionDB().add(key, value, 0, 0, 0);
	}
	
	//XXX TODO make sure these all work...
	@Override
	public void processUndo() throws CivException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void updateBuildProgess() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void build(Player player, Location centerLoc, Template tpl) throws Exception {		
	}
	
	@Override
	protected void runOnBuild(Location centerLoc, Template tpl) throws CivException {
		return;
	}
	
	@Override
	public String getDynmapDescription() {
		return null;
	}
	
	@Override
	public String getMarkerIconName() {
		return null;
	}
	
	@Override
	public void onComplete() { }
	
	@Override
	public void onLoad() { }
	
	@Override
	public void onUnload() { }
	
	public Collection<Resident> getMembers() {
		return this.members.values();
	}
	
	public String getOwnerName() {
		Resident res = CivGlobal.getResidentViaUUID(UUID.fromString(ownerName));
		return res.getName();
	}
	
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public int getLonghouseLevel() {
		return this.consumeComponent.getLevel();
	}
	
	public String getLonghouseCountString() {
		return this.consumeComponent.getCountString();
	}
	
	public String getMembersString() {
		String out = "";
		for (Resident resident : members.values()) {
			out += resident.getName()+" ";
		}
		return out;
	}
	
	public void onControlBlockHit(ControlPoint cp, World world, Player player) {
		world.playSound(cp.getCoord().getLocation(), Sound.BLOCK_ANVIL_USE, 0.2f, 1);
		world.playEffect(cp.getCoord().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
		CivMessage.send(player, CivColor.Gray+"Damaged Control Block ("+cp.getHitpoints()+" / "+cp.getMaxHitpoints()+")");
		CivMessage.sendCamp(this, CivColor.Yellow+"One of our camp's Control Points is under attack! ("+cp.getHitpoints()+" / "+cp.getMaxHitpoints()+")");
	}
	
	public void onControlBlockDestroy(ControlPoint cp, World world, Player player) {		
		ItemManager.setTypeId(cp.getCoord().getLocation().getBlock(), CivData.AIR);
		world.playSound(cp.getCoord().getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, -1.0f);
		world.playSound(cp.getCoord().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
		FireworkEffect effect = FireworkEffect.builder().with(org.bukkit.FireworkEffect.Type.BURST).withColor(Color.YELLOW).withColor(Color.RED).withTrail().withFlicker().build();
		FireworkEffectPlayer fePlayer = new FireworkEffectPlayer();
		for (int i = 0; i < 3; i++) {
			try {
				fePlayer.playFirework(world, cp.getCoord().getLocation(), effect);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		boolean allDestroyed = true;
		for (ControlPoint c : this.controlBlocks.values()) {
			if (c.isDestroyed() == false) {
				allDestroyed = false;
				break;
			}
		}
		
		if (allDestroyed) {
			CivMessage.sendCamp(this, CivColor.Rose+"Our camp has been destroyed!");
			this.destroy();
		} else {
			CivMessage.sendCamp(this, CivColor.Rose+"One of camps's Control Points has been destroyed!");
		}
	}
	
	@Override
	public void onDamage(int amount, World world, Player player, BlockCoord hit, BuildableDamageBlock hit2) {
		ControlPoint cp = this.controlBlocks.get(hit);
		if (cp != null) {
			Date now = new Date();
			Resident resident = CivGlobal.getResident(player);
			if (resident.isProtected()) {
				CivMessage.sendError(player, "You are unable to damage camps while protected.");
				return;
			}
			
			if (now.after(getNextRaidDate())) {
				if (!cp.isDestroyed()) {
					cp.damage(amount);
					if (cp.isDestroyed()) {
						onControlBlockDestroy(cp, world, player);
					} else {
						onControlBlockHit(cp, world, player);
					}
				} else {
					CivMessage.send(player, CivColor.Rose+"Control Block already destroyed.");
				}
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
				CivMessage.send(player, CivColor.Rose+"Cannot damage control blocks for this camp until "+sdf.format(getNextRaidDate()));
			}
		}
	}
	
	public void setNextRaidDate(Date next) {
		this.nextRaidDate = next;
		this.save();
	}
	
	public Date getNextRaidDate() {
		Date raidEnd = new Date(this.nextRaidDate.getTime());
		raidEnd.setTime(this.nextRaidDate.getTime() + 60*60*1000*this.raidLength);
		
		Date now = new Date();
		if (now.getTime() > raidEnd.getTime()) {
			this.nextRaidDate.setTime(nextRaidDate.getTime() + 60*60*1000*24);
		}
		
		return this.nextRaidDate;
	}
	
	public boolean isSifterEnabled() {
		return sifterEnabled;
	}
	
	public void setSifterEnabled(boolean sifterEnabled) {
		this.sifterEnabled = sifterEnabled;
	}
	
	public Collection<ConfigCampUpgrade> getUpgrades() {
		return this.upgrades.values();
	}
	
	public boolean hasUpgrade(String require_upgrade) {
		return this.upgrades.containsKey(require_upgrade);
	}
	
	public void purchaseUpgrade(ConfigCampUpgrade upgrade) throws CivException {
		Resident owner = this.getOwner();
		if (!owner.getTreasury().hasEnough(upgrade.cost)) {
			throw new CivException("The owner does not have the required "+upgrade.cost+" coins to purchase this upgrade.");
		}
		
		this.upgrades.put(upgrade.id, upgrade);
		upgrade.processAction(this);
		
		this.reprocessCommandSigns();
		owner.getTreasury().withdraw(upgrade.cost);
		this.save();
		return;
	}
	
	public boolean isLonghouseEnabled() {
		return longhouseEnabled;
	}
	
	public void setLonghouseEnabled(boolean enabled) {
		this.longhouseEnabled = enabled;
	}
	
	public boolean isFirstGardenEnabled() {
		return gardenFirstEnabled;
	}
	
	public void setFirstGardenEnabled(boolean enabled) {
		this.gardenFirstEnabled = enabled;
	}
	
	public boolean isSecondGardenEnabled() {
		return gardenSecondEnabled;
	}
	
	public void setSecondGardenEnabled(boolean enabled) {
		this.gardenSecondEnabled = enabled;
	}
	
	// XXX Villager Stuff
	
	public void spawnVillager(Location loc, int dir) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(dir), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		CivVillager.onSpawn(v, vLoc, "Camp Manager", false, Profession.NITWIT);
		
		String vilKey = this.getName()+":"+v.getCustomName()+":"+v.getLocation().toString();
		if (CivGlobal.getCivVillager(vilKey) != null) {
			v.setHealth(0); v.remove();
		}
		CivGlobal.addCivVillager(vilKey, v);
	}
	
	public static boolean validatePlayerCampGUI(Player p, Camp camp, boolean hastoBeMemberOfCamp, boolean hastoBeOwnerOfCamp) {
		Resident res = CivGlobal.getResident(p);
		if (hastoBeMemberOfCamp) {
			if (!res.hasCamp()) {
				CivMessage.sendError(res, "Cannot access this villager: You are not in a camp.");
				return false;
			}
			
			if (camp != res.getCamp()) {
				CivMessage.sendError(res, "Cannot access this villager: You are not a member of the owning camp.");
				return false;
			}
		}
		
		if (hastoBeOwnerOfCamp) {
			if (!res.getCamp().isOwner(res)) {
				CivMessage.sendError(res, "Cannot access this villager: You are not an owner of this camp.");
				return false;
			}
		}
		return true;
	}
	
	public void openMainMenuGUI(Player p, Camp camp) {
		if (!validatePlayerCampGUI(p, this, true, false)) return;
		Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST.getDefaultSize()*2, "Camp Manager");
		for (int i = 0; i <= 8; i++) inv.setItem(i, LoreGuiItem.build(CivColor.DarkGray+"Inventory Border", CivData.STAINED_GLASS_PANE, 7));
		for (int i = 45; i <= 53; i++) inv.setItem(i, LoreGuiItem.build(CivColor.DarkGray+"Inventory Border", CivData.STAINED_GLASS_PANE, 7));
		
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", CivData.PAPER, 0,
				CivColor.RESET+"This is the Camp Menu. You can use it to purchase upgrades, see camp stats, control camp settings, view camp wiki, and it is also used as the input for "+
						"the items to obtain the Founding Civ Flag.",
				CivColor.RESET+""
				));
		
		inv.setItem(9, LoreGuiItem.build("Camp Stats", CivData.BARRIER, 0,
				CivColor.RedBold+"In Dev",
				CivColor.GrayItalic+" « Coming Soon » "
				));
		
		inv.setItem(10, LoreGuiItem.build("Camp Members", CivData.SKULL, 3,
				CivColor.RedBold+"In Dev",
				CivColor.GrayItalic+" « Coming Soon » "
				));
		
		ItemStack camp_mainmenu = LoreGuiItem.build(CivColor.GrayBold+"Camp Wiki", CivData.MINECART_WITH_FURNACE, 0,
				CivColor.RESET+"This menu contains the basics of camps, including how to make a civ, camp upgrades, and camp management.");
		camp_mainmenu = LoreGuiItem.setAction(camp_mainmenu, "OpenInventory");
		camp_mainmenu = LoreGuiItem.setActionData(camp_mainmenu, "tutorialInv", "camp_mainmenu");
		inv.setItem(17, camp_mainmenu);
		
		LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
		if (p != null && p.isOnline() && p.isValid()) p.openInventory(inv);
	}
	
}
