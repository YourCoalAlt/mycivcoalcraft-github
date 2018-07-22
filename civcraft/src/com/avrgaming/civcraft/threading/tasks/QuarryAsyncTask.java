package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.config.ConfigQuarry;
import com.avrgaming.civcraft.config.ConfigQuarryItem;
import com.avrgaming.civcraft.config.ConfigTrommelItem;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Quarry;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.Trommel;
import com.avrgaming.civcraft.structure.Warehouse;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.CivItem;
import com.avrgaming.civcraft.util.MultiInventory;

public class QuarryAsyncTask extends CivAsyncTask {
	
	Quarry quarry;
	ArrayList<StructureChest> sources = new ArrayList<StructureChest>();
	ArrayList<StructureChest> destinations = new ArrayList<StructureChest>();
	ArrayList<StructureChest> destinations_other = new ArrayList<StructureChest>();
	
	public static HashSet<String> debugTowns = new HashSet<String>();
	
	public static void debug(Quarry quarry, String msg) {
		if (debugTowns.contains(quarry.getTown().getName())) {
			CivLog.warning("QuarryDebug: "+quarry.getTown().getName()+": "+msg);
		}
	}	
	
	public QuarryAsyncTask(Structure quarry) {
		this.quarry = (Quarry)quarry;
	}
	
	public void processQuarryUpdate(int ticks, boolean trommel) {
		debug(quarry, "Processing Quarry...");
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv = new MultiInventory();
		MultiInventory dest_inv = new MultiInventory();
		MultiInventory dest_inv_other = new MultiInventory();

		try {
			for (StructureChest src : sources) {			
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					quarry.skippedCounter += ticks;
					CivLog.warning("Quarry: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					quarry.skippedCounter += ticks;
					return;
				}
				
				// If inventory as what we want, add it and forget any others.
				boolean brk = false;
				for (Iterator<ItemStack> iter = tmp.iterator(); iter.hasNext();) {
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					ConfigQuarryItem cqi = CivSettings.quarryItems.get(CivItem.getId(stack));
					if (cqi == null) continue;
					else {
						source_inv.addInventory(tmp);
						brk = true;
						break;
					}
				}
				if (brk) break;
			}
			
			boolean full = true;
			
			if (trommel) {
				if (destinations_other.size() > 1) {
					for (StructureChest dst : destinations_other) {
						Inventory tmp;
						try {
							tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
						} catch (CivTaskAbortException e) {
							quarry.skippedCounter += ticks;
							CivLog.warning("Quarry: "+e.getMessage());
							return;
						}
						if (tmp == null) {
							quarry.skippedCounter += ticks;
							return;
						}
						
						if (tmp.firstEmpty() != -1) {
							dest_inv_other.addInventory(tmp);
							full = false;
							break;
						} else continue;
					}
				} else {
					CivLog.error("Bad OTHER dest chests for quarry in town: "+quarry.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations_other.size());
					return;
				}
			} else {
				for (StructureChest dst : destinations) {
					Inventory tmp;
					try {
						tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
					} catch (CivTaskAbortException e) {
						quarry.skippedCounter += ticks;
						CivLog.warning("Quarry: "+e.getMessage());
						return;
					}
					if (tmp == null) {
						quarry.skippedCounter += ticks;
						return;
					}
					
					if (tmp.firstEmpty() != -1) {
						dest_inv.addInventory(tmp);
						full = false;
						break;
					} else continue;
				}
			}
			
			// destination chest is full, stop processing.
			if (full) {
				debug(quarry, "Outputs full, cancelling.");
				return;
			}
		} catch (InterruptedException e) {
			return;
		}
		
		ticks += quarry.skippedCounter;
		debug(quarry, "Processing Quarry (Prev. Skipped, & Govt. Process): "+ticks);
		for (int i = 0; i < ticks; i++) {
			for (Inventory inv : source_inv.getInventories()) {
				int index = -1;
				for (Iterator<ItemStack> iter = inv.iterator(); iter.hasNext();) {
					index++;
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					ConfigQuarryItem cqi = CivSettings.quarryItems.get(CivItem.getId(stack));
					if (cqi == null) continue;
					
					if (CivItem.getId(stack) == cqi.item && quarry.getLevel() >= cqi.level) {
						try {
							short damage = CivItem.getData(stack);
							if (damage > cqi.max_dura) {
								this.updateInventory(Action.REMOVE, source_inv, stack);
							} else {
								ItemStack newStack = stack; newStack.setDurability((short) (damage+1));
								this.updateInventory(Action.UPDATE, source_inv, newStack, index, inv);
							}
						} catch (InterruptedException e) {
							return;
						}
						
						ArrayList<ItemStack> dropped = getRandomDrops(quarry.getTown(), cqi.item);
						if (dropped.size() < 1) {
							dropped.add(getReturnDrop());
						}
						
						try { //Try to add the new item to the dest chest, if we cant, oh well.
							for (ItemStack ni : dropped) {
								debug(quarry, "Updating inventory: "+ni);
								if (dest_inv_other != null) {
									if (trommel) {
										ConfigTrommelItem cti = CivSettings.trommelItems.get(CivData.getStackName(ni).toUpperCase());
										// Checks to make sure item is trommel level, AND town has upgraded level to consume so it does not waste space.
										if (cti != null && quarry.getTown().saved_trommel_level >= cti.level) {
											this.updateInventory(Action.ADD, dest_inv_other, ni);
										} else {
											this.updateInventory(Action.ADD, dest_inv, ni);
										}
									} else {
										this.updateInventory(Action.ADD, dest_inv, ni);
									}
								} else {
									this.updateInventory(Action.ADD, dest_inv, ni);
								}
							}
						} catch (InterruptedException e) {
							CivLog.warning("Quarry:"+e.getMessage());
							return;
						}
						break;
					}
				}
			}
		}
		quarry.skippedCounter = 0;
	}
	
	private ItemStack getReturnDrop() {
		Random rand = new Random();
		int uselessDrop = rand.nextInt(10);
		if (uselessDrop >= 0 && uselessDrop <= 2) {
			return CivItem.newStack(Material.DIRT);
		} else if (uselessDrop >= 3 && uselessDrop <= 5) {
			return CivItem.newStack(Material.GRAVEL);
		} else {
			return getStoneDrop();
		}
	}
	
	private ItemStack getStoneDrop() {
		Random rand = new Random();
		int uselessDrop = rand.nextInt(10);
		if (uselessDrop >= 0 && uselessDrop <= 2) {
			return CivItem.newStack(Material.COBBLESTONE, 1);
		} else if (uselessDrop == 3) {
			return CivItem.newStack(Material.STONE, 1);
		} else if (uselessDrop == 4) {
			return CivItem.newStack(Material.STONE, 1, true);
		} else if (uselessDrop == 5) {
			return CivItem.newStack(Material.STONE, 3, true);
		} else if (uselessDrop == 6) {
			return CivItem.newStack(Material.STONE, 5, true);
		} else {
			return CivItem.airStack();
		}
	}
	
	public void run() {
		if (!quarry.isActive()) {
			debug(quarry, "Quarry Disabed...");
			return;
		}
		
		int ticks = 0;
		if (this.quarry.lock.tryLock()) {
			try {
				sources.clear();
				destinations.clear();
				
				if (quarry != null) {
					sources.addAll(quarry.getAllChestsById(1));
					destinations.addAll(quarry.getAllChestsById(2));
				}
				
				if (sources.size() < 1 || destinations.size() < 1) {
					CivLog.error("Bad dest chests for quarry in town: "+quarry.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
					return;
				}
				
				boolean trommel = false;
				Warehouse whs = (Warehouse) quarry.getTown().getStructureByType("s_warehouse");
				if (whs != null) {
					if (whs.isComplete() && whs.isEnabled()) {
						if (whs.getQuarryCollector() == 2) {
							debug(quarry, "Output directed to Trommel");
							Trommel trs = (Trommel) quarry.getTown().getStructureByType("s_trommel");
							for (StructureChest sc : trs.getAllChestsById(1)) {
								trommel = true;
								destinations_other.add(sc);
							}
						}
					}
				}
				
				Random rand = new Random();
				ConfigGovernment gov = quarry.getCiv().getGovernment();
				double processRate = (int) (gov.quarry_process_rate*100);
				double processing = processRate / 100;
				double chance = processRate - (processing*100);
				
				int processes = 0;
				int bonuses = 0; int skips = 0;
				int bonusesskips = 0;
				for (int t = 0; t < CivCraft.structure_process; t++) {
					if (processing <= 0) {
						if (chance > 0) {
							int types = rand.nextInt(100);
							if (types >= chance) {
								skips++;
							} else {
								processes++; ticks++;
							}
						} else {
							skips++;
							debug(quarry, "Skipped; Govt. "+gov.displayName+"; Maximum Penalty at 0, was sent "+processing);
						}
					} else {
						int toTick = (int) processing;
						processes += toTick;
						ticks+= toTick;
						if (chance > 0) {
							for (int i = 0; i < processing; i++) {
								int types = rand.nextInt(100);
								if (types >= chance) {
									bonusesskips++;
								} else {
									toTick++; bonuses++; ticks++;
								}
							}
						}
					}
				}
				
				// Added this dbg msg to cut down on spam in console... keeping other dbg msgs in case we need them.
				processQuarryUpdate(ticks, trommel);
				debug(quarry, "Govt. "+gov.displayName+" at "+processRate+"%; Processes:"+processes+", Bonuses:"+bonuses+", Skips:"+skips+", Bonuses Skips:"+bonusesskips);
			} finally {
				this.quarry.lock.unlock();
			}
		} else {
			debug(this.quarry, "Failed to get lock on quarry while trying to start task, aborting.");
		}
	}
	
	public ArrayList<ItemStack> getRandomDrops(Town t, int input) {
		Random rand = new Random();		
		ArrayList<ItemStack> dropped = new ArrayList<ItemStack>();
		for (ConfigQuarry d : CivSettings.quarryDrops) {
			if (d.input == input) {
				double dc = d.drop_chance;
				
				if (quarry.getModifyChance() > 1.0) {
					if (d.loot_type.contains("rare")) {
						dc *= 1.1;
					} else if (d.loot_type.contains("uncommon")) {
						dc *= 1.05;
					} else if (d.loot_type.contains("common")) {
						dc *= 0.9;
					} else if (d.loot_type.contains("junk")) {
						dc *= 0.8;
					} else {
						CivLog.warning("Quarry Process had unknown loot type, "+d.loot_type+" from "+d.type);
					}
				} else if (quarry.getModifyChance() < 1.0) {
					if (d.loot_type.contains("rare")) {
						dc *= 0.8;
					} else if (d.loot_type.contains("uncommon")) {
						dc *= 0.9;
					} else if (d.loot_type.contains("common")) {
						dc *= 1.05;
					} else if (d.loot_type.contains("junk")) {
						dc *= 1.1;
					} else {
						CivLog.warning("Quarry Process had unknown loot type, "+d.loot_type+" from "+d.type);
					}
				}
				
				if (dc <= 0) dc = d.drop_chance;
				int chance = rand.nextInt(10000);
				if (chance < (dc*10000)) {
					if (d.custom_id != null) {
						LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.custom_id);
						dropped.add(LoreMaterial.spawn(LoreMaterial.materialMap.get(craftMat.getConfigId()), d.amount));
					} else {
						dropped.add(CivItem.newStack(d.type_id, d.amount, d.type_data));
					}
				}
			}
		}
		return dropped;
	}
}
