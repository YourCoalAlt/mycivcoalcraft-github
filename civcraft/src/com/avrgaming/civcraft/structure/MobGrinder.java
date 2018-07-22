package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.mobs.CivVillager;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;
import com.avrgaming.civcraft.util.SimpleBlock;

public class MobGrinder extends Structure {
	
	private int level = 1;
	public int skippedCounter = 0;
	public ReentrantLock lock = new ReentrantLock();
	
	private ArrayList<EntityType> mob_grinder_spawners = new ArrayList<EntityType>();
	private boolean[] enabled = {true, true, true, true, true, true, true, true};
	
	protected MobGrinder(Location center, String id, Town town) throws CivException {
		super(center, id, town);	
	}
	
	public MobGrinder(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	public String getDynmapDescription() {
		String out = "<u><b>Mob Grinder</u></b><br/>";
		out += "Level: "+this.level;
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "pirateflag";
	}
	
	public double getModifyChance() {
		double increase = 1.0;
		
//		double extraction_buff = 0.0;
//		extraction_buff += this.getTown().getBuffManager().getEffectiveDouble(Buff.EXTRACTION);
//		increase += extraction_buff;
		
		return increase;
	}
	
	private int getUnlockedSpawnerCount() {
		return (3+level);
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock cmdBlock) {
		this.level = getTown().saved_mob_grinder_level;
		this.mob_grinder_spawners = getTown().saved_mob_grinder_spawners;
		
		for (int i = 0; i < getUnlockedSpawnerCount(); i++) this.mob_grinder_spawners.add(EntityType.EVOKER);
		this.mob_grinder_spawners.set(0, EntityType.ZOMBIE);
		this.mob_grinder_spawners.set(1, EntityType.SKELETON);
		this.mob_grinder_spawners.set(2, EntityType.CREEPER);
		this.mob_grinder_spawners.set(3, EntityType.SPIDER);
		
		switch (cmdBlock.command) {
		case "/spawner":
			Integer id = Integer.valueOf(cmdBlock.keyvalues.get("id"));
			if (id <= this.mob_grinder_spawners.size() && this.mob_grinder_spawners.get(id) != null) {
				this.addStructureBlock(absCoord, false);
				CivItem.setTypeId(absCoord.getBlock(), CivData.MOB_SPAWNER);
				this.setSpawner(absCoord.getBlock(), this.mob_grinder_spawners.get(id));
			} else {
				CivItem.setTypeId(absCoord.getBlock(), CivData.BEDROCK);
				CivMessage.sendTown(getTown(), "Mob Spawner "+(id+1)+" does not have a mob set!");
			}
			break;
		case "/villager":
			spawnVillager(absCoord.getLocation(), (byte)cmdBlock.getData());
			break;
		}
	}
	
	// https://www.spigotmc.org/threads/create-mob-spawner-itemstack-of-specific-entity.62004/
	public void setSpawner(Block block, EntityType entity) {
		if (block.getType() != Material.MOB_SPAWNER) {
			CivLog.warning("Tried to set spawner on a non-spawner block! "+block.getLocation().toString());
			return;
		}
		BlockState blockState = block.getState();
		CreatureSpawner spawner = ((CreatureSpawner) blockState);
		spawner.setSpawnedType(entity);
		blockState.update();
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	// XXX Villager stuff
	
	public void spawnVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		CivVillager.onSpawn(v, vLoc, "Grinder Manager", false, Profession.BUTCHER);
		
		String vilKey = this.getTown().getName()+":"+v.getCustomName()+":"+v.getLocation().toString();
		if (CivGlobal.getCivVillager(vilKey) != null) {
			v.setHealth(0); v.remove();
		}
		CivGlobal.addCivVillager(vilKey, v);
	}
	
	public void openGrinderMainMenuGUI(Player p, Town town) {
		if (!Buildable.validatePlayerGUI(p, this, true, false, true, true)) return;
		Inventory inv = Bukkit.createInventory(null, 9*5, "Mob Grinder Settings");
		for (int i = 0; i <= 8; i++) inv.setItem(i, LoreGuiItem.build(CivColor.DarkGray+"Inventory Border", CivData.STAINED_GLASS_PANE, 7));
		for (int i = 36; i <= 44; i++) inv.setItem(i, LoreGuiItem.build(CivColor.DarkGray+"Inventory Border", CivData.STAINED_GLASS_PANE, 7));
		
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", CivData.PAPER, 0,
				CivColor.RESET+"This is the Grinder Menu. You can use it to",
				CivColor.RESET+"change and upgrade the options for spawners,",
				CivColor.RESET+"as well as the loot options for the Sword",
				CivColor.RESET+"Output chest(s).",
				CivColor.RESET+""
				));
		
		inv.setItem(2, LoreGuiItem.build(CivColor.RedBold+"In Dev", CivData.BARRIER, 0,
				CivColor.GrayItalic+" « Coming Soon » "
				));
		
		for (int i = 0; i < getUnlockedSpawnerCount(); i++) {
			inv.setItem(9+i, LoreGuiItem.build(CivColor.Bold+"Spawner "+(i+1), CivData.MOB_SPAWNER, 0,
					CivColor.LightBlueBold+"Type: "+CivColor.Yellow+this.mob_grinder_spawners.get(i).toString(),
//					CivColor.LightBlueBold+"Spawn Rate: "+CivColor.Yellow+"-- Seconds",
					CivColor.LightBlueBold+"Spawn Mode: "+CivColor.LightGreen+enabled[i],
					CivColor.GrayItalic+" « Left Click to toggle Spawn Mode » ",
					CivColor.GrayItalic+" « Right Click to change Mob Type » "
					));
		}
		
		inv.setItem(18, LoreGuiItem.build(CivColor.GoldBold+"Auto-Kill", CivData.LAVA_BUCKET, 0,
				CivColor.LightBlueBold+"Mode: "+CivColor.Rose+"Disabled",
				CivColor.RedBold+" « In Dev » ",
				CivColor.GrayItalic+" « Coming Soon » "
				));
		
		p.openInventory(inv);
	}
}
