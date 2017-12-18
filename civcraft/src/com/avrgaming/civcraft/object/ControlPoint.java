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

import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.util.BlockCoord;

public class ControlPoint {

	// Location of the control block.
	private BlockCoord coord;
	// Hitpoints for this control block.
	private int hitpoints;
	// Max hitpoints for this control block.
	private int maxHitpoints;
	// Structure this control point belongs to.
	private Buildable buildable;
	
	public ControlPoint (BlockCoord coord, Buildable buildable, int hitpoints) {
		this.coord = coord;
		this.setBuildable(buildable);
		this.maxHitpoints = hitpoints;
		this.hitpoints = this.maxHitpoints;
	}
	
	public BlockCoord getCoord() {
		return coord;
	}
	
	public void setCoord(BlockCoord coord) {
		this.coord = coord;
	}
	
	public int getHitpoints() {
		return hitpoints;
	}
	
	public void setHitpoints(int hitpoints) {
		this.hitpoints = hitpoints;
	}
	
	public int getMaxHitpoints() {
		return maxHitpoints;
	}
	
	public void setMaxHitpoints(int maxHitpoints) {
		this.maxHitpoints = maxHitpoints;
	}

	public void damage(int amount) {
		if (this.hitpoints <= 0) return;
		this.hitpoints -= amount;
		
		if (this.hitpoints <= 0) {
			this.hitpoints = 0;
		}
	}
	
	public boolean isDestroyed() {
		if (this.hitpoints <= 0) return true;
		return false;
	}
	
	public Buildable getBuildable() {
		return buildable;
	}
	
	public void setBuildable(Buildable buildable) {
		this.buildable = buildable;
	}
}
