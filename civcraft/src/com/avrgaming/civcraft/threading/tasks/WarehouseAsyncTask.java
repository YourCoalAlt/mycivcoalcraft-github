package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.structure.Quarry;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.Trommel;
import com.avrgaming.civcraft.structure.Warehouse;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.MultiInventory;

public class WarehouseAsyncTask extends CivAsyncTask {

	Warehouse warehouse;
	ArrayList<StructureChest> sources = new ArrayList<StructureChest>();
	ArrayList<StructureChest> destinations = new ArrayList<StructureChest>();
	
	public static HashSet<String> debugTowns = new HashSet<String>();

	public static void debug(Warehouse warehouse, String msg) {
		if (debugTowns.contains(warehouse.getTown().getName())) {
			CivLog.warning("WarehouseDebug:"+warehouse.getTown().getName()+": "+msg);
		}
	}	
	
	public WarehouseAsyncTask(Structure warehouse) {
		this.warehouse = (Warehouse)warehouse;
	}
	
	public void processWarehouseUpdate() {
//		boolean trommel = false;
		
		if (sources.size() <1) {
			debug(warehouse, "No sources to transport, cancelling.");
			return;
		}
		
		if (destinations.size() < 1) {
			CivLog.error("Bad dest chests for warehouse in town: "+warehouse.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size()+"; ");
			return;
		}
		
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv = new MultiInventory();
		MultiInventory dest_inv = new MultiInventory();

		try {
			for (StructureChest src : sources) {
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					CivLog.warning("Warehouse: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					warehouse.skippedCounter++;
					return;
				}
				
				boolean brk = false;
				for (ListIterator<ItemStack> iter = tmp.iterator(); iter.hasNext();) {
					ItemStack stack = iter.next();
					if (stack == null) continue;
					else {
						source_inv.addInventory(tmp);
						brk = true;
						break;
					}
				}
				if (brk) break;
			}
			
			boolean full = true;
			
/*			if (trommel) {
				if (destinations_other.size() > 1) {
					for (StructureChest dst : destinations_other) {
						Inventory tmp;
						try {
							tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
						} catch (CivTaskAbortException e) {
							CivLog.warning("Warehouse: "+e.getMessage());
							return;
						}
						if (tmp == null) {
							warehouse.skippedCounter++;
							return;
						}
						
						if (tmp.firstEmpty() != -1) {
							dest_inv_other.addInventory(tmp);
							full = false;
							break;
						} else continue;
					}
				} else {
					CivLog.error("Bad OTHER dest chests for warehouse in town: "+warehouse.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations_other.size());
					return;
				}
			} else {*/
				for (StructureChest dst : destinations) {
					Inventory tmp;
					try {
						tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
					} catch (CivTaskAbortException e) {
						CivLog.warning("Warehouse: "+e.getMessage());
						return;
					}
					if (tmp == null) {
						warehouse.skippedCounter++;
						return;
					}
					
					if (tmp.firstEmpty() != -1) {
						dest_inv.addInventory(tmp);
						full = false;
						break;
					} else continue;
				}
//			}
			
			// destination chest is full, stop processing.
			if (full) {
				debug(warehouse, "Outputs full, cancelling.");
				return;
			}
		} catch (InterruptedException e) {
			return;
		}
		
		debug(warehouse, "Processing warehouse:"+warehouse.skippedCounter+1);
		for (int i = 0; i < warehouse.skippedCounter+1; i++) {
			for (Inventory inv : source_inv.getInventories()) {
				for (ListIterator<ItemStack> iter = inv.iterator(); iter.hasNext();) {
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					try {
						this.updateInventory(Action.REMOVE, source_inv, stack);
/*						if (trommel) {
							ConfigTrommelItem cti = CivSettings.trommelItems.get(CivData.getDisplayName(ItemManager.getId(ni), ItemManager.getData(ni)).toUpperCase());
							// Checks to make sure item is trommel level, AND town has upgraded level to consume so it does not waste space.
							if (cti != null && warehouse.getTown().saved_trommel_level >= cti.level) {
								this.updateInventory(Action.ADD, dest_inv_other, ni);
							} else {
								this.updateInventory(Action.ADD, dest_inv, ni);
						} else {*/
							this.updateInventory(Action.ADD, dest_inv, stack);
//						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		warehouse.skippedCounter = 0;
	}
	
	public void run() {
		if (!warehouse.isActive()) {
			debug(warehouse, "Warehouse Disabed...");
			return;
		}
		
		sources.clear();
		destinations.clear();
		
		Warehouse whs = (Warehouse) warehouse.getTown().getStructureByType("s_warehouse");
		if (whs == null) return;
		
		if (whs.isComplete() && whs.isEnabled()) {
			for (StructureChest sc : whs.structureChests.values()) {
				if (sc.getChestId() <= whs.getLevel()) {
					destinations.add(sc);
				}
			}
		}
		
		for (Structure s : warehouse.getTown().getStructures()) {
			if (s instanceof Trommel) {
				Trommel trommel = (Trommel) s;
				if (whs.canCollectTrommel()) {
					for (StructureChest sc : trommel.getAllChestsById(2)) {
						sources.add(sc);
					}
				}
			}
			
			if (s instanceof Quarry) {
				Quarry quarry = (Quarry) s;
				if (whs.getQuarryCollector() == 1) {
					for (StructureChest sc : quarry.getAllChestsById(2)) {
						sources.add(sc);
					}
				}
			}
		}
		
		if (this.warehouse.lock.tryLock()) {
			try {
				Random rand = new Random();
				ConfigGovernment gov = warehouse.getCiv().getGovernment();
//				int processRate = (int) (gov.warehouse_process_rate*100);
				int processRate = 200;
				int processing = processRate / 100;
				int chance = processRate - (processing*100);
				
				for (int t = CivCraft.structure_process; t > 0; t--) {
					if (processing <= 0) {
						if (chance > 0) {
							int types = rand.nextInt(100);
							if (types >= chance) {
								debug(warehouse, "Skipped; Govt. "+gov.displayName+"; Greater Penalty at "+types+" > "+processRate);
							} else {
								processWarehouseUpdate();
								debug(warehouse, "Processed; Govt. "+gov.displayName+"; Lesser Success at "+processRate);
							}
						} else {
							debug(warehouse, "Skipped; Govt. "+gov.displayName+"; Maximum Penalty at 0");
						}
					} else {
						for (int i = processing; i > 0; i--) {
							processWarehouseUpdate();
							debug(warehouse, "Processed; Govt. "+gov.displayName+"; Stable Success at "+processRate);
							if (chance > 0) {
								int types = rand.nextInt(100);
								if (types >= chance) {
									debug(warehouse, "Skipped; Govt. "+gov.displayName+"; Lesser Penalty at "+types+" > "+processRate);
								} else {
									processWarehouseUpdate();
									debug(warehouse, "Bonus Process; Govt. "+gov.displayName+"; Bonus at "+types+" <= "+processRate);
								}
							}
						}
					}
				}
			} finally {
				this.warehouse.lock.unlock();
			}
		} else {
			debug(this.warehouse, "Failed to get lock while trying to start task, aborting.");
		}
	}
	
}
