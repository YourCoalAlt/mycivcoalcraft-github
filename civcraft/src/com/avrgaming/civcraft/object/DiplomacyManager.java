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
package com.avrgaming.civcraft.object;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.DiplomaticRelation.Status;

public class DiplomacyManager {
	
	/* Manages diplomatic relationships for the object it is attached to.
	 * Diplomatic relationships are stored in the SessionDB with this civ as the key. 
	 * There will be duplicate data that needs to be cleaned up for mutual relationships.
	 * For example, if Civ A is at war with Civ B, both Civ A and Civ B will have a war relationship
	 * entry. */
	
	private Civilization ourCiv;
	
	// List of our relationships, hashed by civ id.
	private HashMap<Integer, DiplomaticRelation> relations = new HashMap<Integer, DiplomaticRelation>();
	
	// Number of civ's at war with us, will maintain this for fast isWar() lookups
	private int warCount = 0;
	
	public DiplomacyManager(Civilization civ) {
		ourCiv = civ;
	}
	
	public boolean atWarWith(Civilization other) {
		if (ourCiv.getId() == other.getId()) return false;
		
		DiplomaticRelation relation = relations.get(other.getId());
		if (relation != null && relation.getStatus() == DiplomaticRelation.Status.WAR) {
			return true;
		}
		return false;
	}
	
	public boolean isAtWar() {
		return (warCount != 0);
	}
	
	public void deleteRelation(DiplomaticRelation relation) {
		if (relation.getStatus() == DiplomaticRelation.Status.WAR && 
				relations.containsKey(relation.getOtherCiv().getId())) {
			warCount--;
			if (warCount < 0) warCount = 0;
		}
		relations.remove(relation.getOtherCiv().getId());
		
		DiplomaticRelation theirRelation = relation.getOtherCiv().getDiplomacyManager().getRelation(ourCiv);
		if (theirRelation != null) {
			try {
				relation.getOtherCiv().getDiplomacyManager().relations.remove(theirRelation.getOtherCiv().getId());
				theirRelation.delete();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		try {
			relation.delete();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteAllRelations() {
		for (DiplomaticRelation relation : relations.values()) {
			this.deleteRelation(relation);
		}
		this.relations.clear();
	}
	
	public void setAggressor(Civilization aggressor, Civilization otherCiv) {
		DiplomaticRelation relation = relations.get(otherCiv.getId());
		if (relation != null) {
			relation.setAggressor(aggressor);
			relation.save();
		}		
	}
	
	public void setRelation(Civilization otherCiv, DiplomaticRelation.Status status, Date expires) {
		DiplomaticRelation relation = relations.get(otherCiv.getId());
		if (relation == null) {
			relations.put(otherCiv.getId(), new DiplomaticRelation(ourCiv, otherCiv, status, expires));
		} else {
			if (relation.getStatus() == status) return;
			
			if (relation.getStatus() == DiplomaticRelation.Status.WAR) {
				//Status was war, new status is not the same, so reduce our warcount.
				warCount--;
			}
			
			if (expires != null) {
				relation.setExpires(expires);
			}
			relation.setStatus(status);
//			if (status == Status.VASSAL) {
//				//End all wars with this civilization.
//				for (Relation rel : this.getRelations()) {
//					if (rel.getOtherCiv() != otherCiv) {
//						if (rel.getStatus() == Status.WAR) {
//							CivGlobal.setRelation(ourCiv, rel.getOtherCiv(), Status.NEUTRAL);
//							CivMessage.sendCiv(this.ourCiv, 
//									"Our war with "+rel.getOtherCiv().getName()+" has ended because we are now a vassal to "+otherCiv.getName());
//							CivMessage.sendCiv(rel.getOtherCiv(), 
//									"Our war with "+ourCiv.getName()+" has ended because they are now a vassal to "+otherCiv.getName());
//						}
//					}
//				}
//			}
		}
		
		if (status == DiplomaticRelation.Status.WAR) {
			warCount++;
		}
	}
	
	public Status getRelationStatus(Civilization otherCiv) {
		if (otherCiv.getId() == ourCiv.getId()) {
			return DiplomaticRelation.Status.ALLY;
		}
		
		DiplomaticRelation relation = relations.get(otherCiv.getId());
		if (relation == null) {
			return DiplomaticRelation.Status.NEUTRAL;
		}
		return relation.getStatus();
	}
	
	public DiplomaticRelation getRelation(Civilization otherCiv) {
		if (ourCiv == otherCiv && relations.get(ourCiv.getId()) == null) relations.put(ourCiv.getId(), new DiplomaticRelation(ourCiv, otherCiv, Status.ALLY, null));
		return relations.get(otherCiv.getId());
	}
	
	public void addRelation(DiplomaticRelation relation) {
		DiplomaticRelation currentRelation = relations.get(relation.getOtherCiv().getId());
		if (relation.getStatus() == DiplomaticRelation.Status.WAR) {
			if (currentRelation == null || currentRelation.getStatus() != DiplomaticRelation.Status.WAR) {
				warCount++;
			}
		} 
		relations.put(relation.getOtherCiv().getId(), relation);
	}
	
	public Collection<DiplomaticRelation> getRelations() {
		return relations.values();
	}
	
	public int getWarCount() {
		return warCount;
	}
	
	public boolean atWarWith(Player attacker) {
		Resident resident = CivGlobal.getResident(attacker);
		if (resident == null) return false;
		if (!resident.hasTown()) return false;
		return atWarWith(resident.getTown().getCiv());
	}
	
	public Status getRelationStatus(Player player) {
		Resident resident = CivGlobal.getResident(player);
		if (resident == null) return Status.NEUTRAL;
		if (!resident.hasTown()) return Status.NEUTRAL;
		return getRelationStatus(resident.getTown().getCiv());
	}
	
//	public Civilization getMasterCiv() {
//		for (Relation rel : this.relations.values()) {
//			if (rel.getStatus() == Status.VASSAL) {
//				return rel.getOtherCiv();
//			}
//		}
//		return null;
//	}
	
	public boolean isHostileWith(Resident resident) {
		return isHostileWith(resident.getCiv());
	}
	
	public boolean isHostileWith(Civilization civ) {
		DiplomaticRelation relation = this.relations.get(civ.getId());
		if (relation == null) return false;
		switch (relation.getStatus()) {
		case WAR:
		case HOSTILE:
			return true;
		default:
			return false;
		}		
	}
}
