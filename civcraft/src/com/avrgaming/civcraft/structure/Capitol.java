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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.components.ProjectileArrowComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.ControlPoint;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureBlock;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;
import com.avrgaming.civcraft.war.War;

public class Capitol extends TownHall {
	
	private HashMap<Integer, ProjectileArrowComponent> arrowTowers = new HashMap<Integer, ProjectileArrowComponent>();
	private StructureSign respawnSign;
	private int index = 0;
	
	public Capitol(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	protected Capitol(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	private RespawnLocationHolder getSelectedHolder() {
		ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();	
		return respawnables.get(index);
	}
	
	private void changeIndex(int newIndex) {
		ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();
		if (this.respawnSign != null) {
			if (newIndex > respawnables.size()) {
				this.respawnSign.setText("Respawn At\n"+CivColor.Green+CivColor.BOLD+respawnables.get(0).getRespawnName());
				index = 0;
			} else if (newIndex < 0) {
				this.respawnSign.setText("Respawn At\n"+CivColor.Green+CivColor.BOLD+respawnables.get(respawnables.size()).getRespawnName());
				index = respawnables.size();
			} else {
				this.respawnSign.setText("Respawn At\n"+CivColor.Green+CivColor.BOLD+respawnables.get(newIndex).getRespawnName());
				index = newIndex;
			}
			this.respawnSign.update();
		} else {
			CivLog.warning("Could not find civ spawn sign:"+this.getId()+" at "+this.getCorner());
		}
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		if (!War.isWarTime()) return;
		Resident resident = CivGlobal.getResident(player);
		if (resident == null) return;
		
		Boolean hasPermission = false;
		if (resident.getTown().isMayor(resident) || resident.getTown().isAssistant(resident) ||
				resident.getCiv().getLeaderGroup().hasMember(resident) || resident.getCiv().getAdviserGroup().hasMember(resident)) {
			if (resident.getTown().getMotherCiv().equals(this.getCiv())) {
				hasPermission = true;
			}
		}
		
		switch (sign.getAction()) {
		case "prev":
			if (hasPermission) {
				changeIndex((index-1));
			} else {
				CivMessage.sendError(resident, "Must be a leader, adviser, or a mayor or assistant of a town in the mother civ to change respawn location.");
			}
			break;
		case "next":
			if (hasPermission) {
				changeIndex((index+1));
			} else {
				CivMessage.sendError(resident, "Must be a leader, adviser, or a mayor or assistant of a town in the mother civ to change respawn location.");
			}
			break;
		case "respawn":
			if (resident.getTown() != this.getTown()) {
				CivMessage.sendError(resident, "Cannot use repawn signs of a civ that is not yours!");
			return;	
			}
			
			ArrayList<RespawnLocationHolder> respawnables =  this.getTown().getCiv().getAvailableRespawnables();
			if (index >= respawnables.size()) {
				index = 0;
				changeIndex(index);
				CivMessage.sendError(resident, "Unable to find selected respawn point. We've reset the sign, please select respawn point again.");
				return;
			}
			
			RespawnLocationHolder holder = getSelectedHolder();
			int respawnTimeSeconds = this.getRespawnTime();
			Date now = new Date();
			if (resident.getLastKilledTime() != null) {
				long secondsLeft = (resident.getLastKilledTime().getTime() + (respawnTimeSeconds*1000)) - now.getTime();
				if (secondsLeft > 0) {
					secondsLeft /= 1000; 
					CivMessage.sendError(resident, CivColor.Rose+"Cannot respawn yet. You have "+secondsLeft+" seconds left.");
					return;
				}
			}
			
			BlockCoord revive = holder.getRandomRevivePoint();
			Location loc;
			if (revive == null) {
				loc = player.getBedSpawnLocation();
			} else {
				loc = revive.getLocation();
			}
			CivMessage.send(player, CivColor.LightGreen+"Respawning...");
			player.teleport(loc);		
			break;
		}
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		StructureSign structSign;
		if (commandBlock.command.equals("/guide")) {
			spawnInfoVillager(absCoord.getLocation(), (byte)commandBlock.getData());
		} else if (commandBlock.command.equals("/questinfo")) {
			spawnQuestVillager(absCoord.getLocation(), (byte)commandBlock.getData());
		} else if (commandBlock.command.equals("/towerfire")) {
			String id = commandBlock.keyvalues.get("id");
			Integer towerID = Integer.valueOf(id);
			if (!arrowTowers.containsKey(towerID)) {
				ProjectileArrowComponent arrowTower = new ProjectileArrowComponent(this, absCoord.getLocation());
				arrowTower.createComponent(this);
				arrowTower.setTurretLocation(absCoord);
				arrowTowers.put(towerID, arrowTower);
			}
		} else if (commandBlock.command.equals("/next")) {
			ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
			ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
			structSign = new StructureSign(absCoord, this);
			structSign.setText("\n"+ChatColor.BOLD+ChatColor.UNDERLINE+"Next\nLocation");
			structSign.setDirection(commandBlock.getData());
			structSign.setAction("next");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
		} else if (commandBlock.command.equals("/prev")) {
			ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
			ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
			structSign = new StructureSign(absCoord, this);
			structSign.setText("\n"+ChatColor.BOLD+ChatColor.UNDERLINE+"Prevous\nLocation");
			structSign.setDirection(commandBlock.getData());
			structSign.setAction("prev");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
		} else if (commandBlock.command.equals("/respawndata")) {
			ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
			ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
			structSign = new StructureSign(absCoord, this);
			structSign.setText("Capitol");
			structSign.setDirection(commandBlock.getData());
			structSign.setAction("respawn");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
			this.respawnSign = structSign;
			changeIndex(index);
		}
	}
	
	@Override
	public void createControlPoint(BlockCoord absCoord) {
		Location centerLoc = absCoord.getLocation();
		/* Build the bedrock tower. */
		Block b = centerLoc.getBlock();
		
		ItemManager.setTypeId(b, ItemManager.getId(Material.SANDSTONE)); ItemManager.setData(b, 0);
		StructureBlock sb = new StructureBlock(new BlockCoord(b), this);
		this.addStructureBlock(sb.getCoord(), true);
		
		/* Build the control block. */
		b = centerLoc.getBlock().getRelative(0, 1, 0);
		ItemManager.setTypeId(b, CivData.OBSIDIAN);
		sb = new StructureBlock(new BlockCoord(b), this);
		this.addStructureBlock(sb.getCoord(), true);
		
		int capitolControlHitpoints;
		try {
			capitolControlHitpoints = CivSettings.getInteger(CivSettings.warConfig, "war.control_block_hitpoints_capitol");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			capitolControlHitpoints = 100;
		}
		BlockCoord coord = new BlockCoord(b);
		this.controlPoints.put(coord, new ControlPoint(coord, this, capitolControlHitpoints));
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
		CivMessage.sendTown(this.getTown(), CivColor.Rose+CivColor.BOLD+"Our civ's capitol cannot be supported by the blocks underneath!"+
				" It will take us an extra "+invalid_respawn_penalty+" mins to respawn during war if its not fixed in time!");
	}
	
	@Override
	public boolean isValid() {
		if (this.getCiv().isAdminCiv()) return true;
		/* Validate that all of the towns in our civ have town halls. If not, then 
		 * we need to punish by increasing respawn times. */
		for (Town town : this.getCiv().getTowns()) {
			TownHall townhall = town.getTownHall();
			if (townhall == null) return false;
		}
		return super.isValid();
	}
	
	@Override
	public String getRespawnName() {
		return "Capitol\n"+this.getTown().getName();
	}
}
