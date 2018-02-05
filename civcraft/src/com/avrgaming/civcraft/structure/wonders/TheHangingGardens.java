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
package com.avrgaming.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.DiplomaticRelation;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.object.DiplomaticRelation.Status;
import com.avrgaming.civcraft.war.War;

public class TheHangingGardens extends Wonder {

	public TheHangingGardens(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	public TheHangingGardens(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_hanging_gardens_growth");
		addBuffToCiv(this.getCiv(), "buff_hanging_gardens_additional_growth");
		addBuffToTown(this.getTown(), "buff_hanging_gardens_regen");
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_hanging_gardens_growth");
		removeBuffFromCiv(this.getCiv(), "buff_hanging_gardens_additional_growth");
		removeBuffFromTown(this.getTown(), "buff_hanging_gardens_regen");
	}
	
	@Override
	public void onLoad() {
		if (this.isActive()) {
			addBuffs();
		}
	}
	
	@Override
	public void onComplete() {
		addBuffs();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		removeBuffs();
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		for (Town t : this.getTown().getCiv().getTowns()) {
			for (Resident res : t.getResidents()) {
				try {
					Player player = CivGlobal.getPlayer(res);
					if (player.isDead() || !player.isValid()) continue;
					
					// Health
					Double maxHP = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
					if (player.getHealth() < maxHP) {
						TownChunk tc = CivGlobal.getTownChunk(player.getLocation());
						if (tc != null && tc.getTown() == this.getTown()) {
							if (player.getHealth() >= (maxHP-1)) {
								player.setHealth(maxHP);
							} else {
								player.setHealth(player.getHealth() + 1);
							}
						}
					}
					
					// Food Level (NOT SATURATION)
					int maxFood = 15; // TODO make config? Not full player, just making their lives easier.
					if (player.getFoodLevel() < maxFood) {
						CultureChunk cc = CivGlobal.getCultureChunk(player.getLocation());
						if (cc != null) {
							DiplomaticRelation.Status status = cc.getCiv().getDiplomacyManager().getRelationStatus(player);
							if (status == Status.ALLY || status == Status.PEACE || War.isWarTime()) {
								if (player.getFoodLevel() >= (maxFood-1)) {
									player.setFoodLevel(maxFood);
								} else {
									player.setFoodLevel(player.getFoodLevel() + 1);
								}
							}
						}
					}
				} catch (CivException e) {
					//Player not online;
				}
				
			}
		}
	}
	
}
