package com.avrgaming.civcraft.object;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.util.BlockCoord;

public class StructureTables {

	private BlockCoord coord;
	private Buildable owner;
	private int direction;
	
	/* The chest id defines which chests are 'paired' for double chests. */
	private int taskTable;
	private int upgradeTable;
	
	public StructureTables(BlockCoord coord, Buildable owner) {
		this.setCoord(coord);
		this.setOwner(owner);
	}
	
	public BlockCoord getCoord() {
		return coord;
	}
	
	public void setCoord(BlockCoord coord) {
		this.coord = coord;
	}
	
	public Buildable getOwner() {
		return owner;
	}
	
	public void setOwner(Buildable owner) {
		this.owner = owner;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public int getTaskTable() {
		return taskTable;
	}
	
	public void setTaskTable(int id) {
		this.taskTable = id;
	}
	
	public int getUpgradeTable() {
		return upgradeTable;
	}
	
	public void setUpgradeTable(int id) {
		this.upgradeTable = id;
	}
}
