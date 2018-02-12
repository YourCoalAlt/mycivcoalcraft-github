package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.block.Biome;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.config.ConfigLumberMill;
import com.avrgaming.civcraft.config.ConfigLumberMillItem;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.LumberMill;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class LumberMillAsyncTask extends CivAsyncTask {
	
	LumberMill mill;
	public static HashSet<String> debugTowns = new HashSet<String>();
	
	public static void debug(LumberMill mill, String msg) {
		if (debugTowns.contains(mill.getTown().getName())) {
			CivLog.warning("LumberMillDebug: "+mill.getTown().getName()+": "+msg);
		}
	}	
	
	public LumberMillAsyncTask(Structure mill) {
		this.mill = (LumberMill)mill;
	}
	
	public void processLumberMillUpdate() {
		// Grab each CivChest object we'll require.
		ArrayList<StructureChest> sources = mill.getAllChestsById(1);
		ArrayList<StructureChest> destinations = mill.getAllChestsById(2);
		
		if (sources.size() < 1 || destinations.size() < 1) {
			CivLog.error("Bad dest chests for lumbermill in town: "+mill.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
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
					CivLog.warning("LumberMill: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					mill.skippedCounter++;
					return;
				}
				
				// If inventory as what we want, add it and forget any others.
				boolean brk = false;
				for (ListIterator<ItemStack> iter = tmp.iterator(); iter.hasNext();) {
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					ConfigLumberMillItem cli = CivSettings.lumbermillItems.get(ItemManager.getId(stack));
					if (cli == null) continue;
					else {
						source_inv.addInventory(tmp);
						brk = true;
						break;
					}
				}
				
				if (brk) break;
			}
			
			boolean full = true;
			
			for (StructureChest dst : destinations) {
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					CivLog.warning("LumberMill: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					mill.skippedCounter++;
					return;
				}
				
				if (tmp.firstEmpty() != -1) {
					dest_inv.addInventory(tmp);
					full = false;
					break;
				} else continue;
			}
			
			// destination chest is full, stop processing.
			if (full) {
				debug(mill, "Outputs full, cancelling.");
				return;
			}
		} catch (InterruptedException e) {
			return;
		}
		
		debug(mill, "Processing lumbermill:"+mill.skippedCounter+1);
		for (int i = 0; i < mill.skippedCounter+1; i++) {
			for (Inventory inv : source_inv.getInventories()) {
				int index = -1;
				for (ListIterator<ItemStack> iter = inv.iterator(); iter.hasNext();) {
					index++;
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					ConfigLumberMillItem cqi = CivSettings.lumbermillItems.get(ItemManager.getId(stack));
					if (cqi == null) continue;
					
					if (ItemManager.getId(stack) == cqi.item && mill.getLevel() >= cqi.level) {
						try {
							short damage = ItemManager.getData(stack);
							if (damage > cqi.max_dura) {
								this.updateInventory(Action.REMOVE, source_inv, stack);
							} else {
								ItemStack newStack = stack; newStack.setDurability((short) (damage+1));
								this.updateInventory(Action.UPDATE, source_inv, newStack, index, inv);
							}
						} catch (InterruptedException e) {
							return;
						}
						
						ArrayList<ItemStack> newItem = new ArrayList<ItemStack>();
						ArrayList<ConfigLumberMill> dropped = getRandomDrops(mill.getTown(), cqi.item);
						if (dropped.size() == 0) {
							newItem.add(getReturnDrop());
						} else {
							for (ConfigLumberMill d : dropped) {
								if (d.custom_id != null) {
									LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.custom_id);
									newItem.add(LoreMaterial.spawn(LoreMaterial.materialMap.get(craftMat.getConfigId()), d.amount));
								} else {
									newItem.add(ItemManager.createItemStack(d.type_id, d.amount, (short)d.type_data));
								}
							}
						}
						
						try { //Try to add the new item to the dest chest, if we cant, oh well.
							for (ItemStack ni : newItem) {
								debug(mill, "Updating inventory: "+ni);
								this.updateInventory(Action.ADD, dest_inv, ni);
							}
						} catch (InterruptedException e) {
							CivLog.warning("LumberMill:"+e.getMessage());
							return;
						}
						break;
					}
				}
			}
		}
		mill.skippedCounter = 0;
	}
	
	private int getBiomeData(Biome b) {
		if (b == Biome.TAIGA || b == Biome.TAIGA_HILLS || b == Biome.TAIGA_COLD || b == Biome.TAIGA_COLD_HILLS ||
				b == Biome.REDWOOD_TAIGA || b == Biome.REDWOOD_TAIGA_HILLS || b == Biome.MUTATED_REDWOOD_TAIGA || b == Biome.MUTATED_REDWOOD_TAIGA_HILLS ||
				b == Biome.ICE_FLATS || b == Biome.MUTATED_ICE_FLATS || b == Biome.ICE_MOUNTAINS) {
			return 1;
		} else if (b == Biome.BIRCH_FOREST || b == Biome.BIRCH_FOREST_HILLS || b == Biome.MUTATED_BIRCH_FOREST || b == Biome.MUTATED_BIRCH_FOREST_HILLS) {
			return 2;
		} else if (b == Biome.JUNGLE || b == Biome.JUNGLE_HILLS || b == Biome.MUTATED_JUNGLE ||
				b == Biome.JUNGLE_EDGE || b == Biome.MUTATED_JUNGLE_EDGE) {
			return 3;
		} else if (b == Biome.SAVANNA || b == Biome.SAVANNA_ROCK || b == Biome.MUTATED_SAVANNA || b == Biome.MUTATED_SAVANNA_ROCK) {
			return 4;
		} else if (b == Biome.ROOFED_FOREST || b == Biome.MUTATED_ROOFED_FOREST) {
			return 5;
		} else {
			if (b == Biome.FOREST || b == Biome.FOREST_HILLS || b == Biome.MUTATED_FOREST) {
				Random rand = new Random();
				int type = rand.nextInt(2);
				if (type == 0) return 0;
				else return 2;
			} else if (b == Biome.EXTREME_HILLS || b == Biome.MUTATED_EXTREME_HILLS || b == Biome.SMALLER_EXTREME_HILLS ||
					b == Biome.EXTREME_HILLS_WITH_TREES || b == Biome.MUTATED_EXTREME_HILLS_WITH_TREES) {
				Random rand = new Random();
				int type = rand.nextInt(2);
				if (type == 0) return 0;
				else return 1;
			}
			return 0;
		}
	}
	
	private ItemStack getReturnDrop() {
		Random rand = new Random();
		int uselessDrop = rand.nextInt(10);
		if (uselessDrop >= 0 && uselessDrop <= 1) {
			return ItemManager.createItemStack(CivData.DIRT, 1);
		} else if (uselessDrop >= 2 && uselessDrop <= 3) {
			return ItemManager.createItemStack(CivData.GRAVEL, 1);
		} else {
			return getStoneDrop();
		}
	}
	
	private ItemStack getStoneDrop() {
		Random rand = new Random();
		int uselessDrop = rand.nextInt(10);
		if (uselessDrop >= 0 && uselessDrop <= 4) {
			if (getBiomeData(mill.getCenterLocation().getBlock().getBiome()) < 4) {
				return ItemManager.createItemStack(CivData.LOG, 1, (short)getBiomeData(mill.getCenterLocation().getBlock().getBiome()));
			} else {
				return ItemManager.createItemStack(CivData.LOG2, 1, (short) (getBiomeData(mill.getCenterLocation().getBlock().getBiome()) - 4));
			}
		} else if (uselessDrop == 5) {
			if (getBiomeData(mill.getCenterLocation().getBlock().getBiome()) < 4) {
				return ItemManager.createItemStack(CivData.LEAF, 1, (short)getBiomeData(mill.getCenterLocation().getBlock().getBiome()));
			} else {
				return ItemManager.createItemStack(CivData.LEAF2, 1, (short) (getBiomeData(mill.getCenterLocation().getBlock().getBiome()) - 4));
			}
		} else if (uselessDrop == 6) {
			return ItemManager.createItemStack(CivData.SAPLING, 1, (short)getBiomeData(mill.getCenterLocation().getBlock().getBiome()));
		} else {
			return ItemManager.createItemStack(CivData.AIR, 1);
		}
	}
	
	public void run() {
		if (!mill.isActive()) {
			debug(mill, "LumberMill Disabed...");
			return;
		}
		
		if (this.mill.lock.tryLock()) {
			try {
				Random rand = new Random();
				ConfigGovernment gov = mill.getCiv().getGovernment();
//				int processRate = (int) (gov.quarry_process_rate*100);
				int processRate = 100;
				int processing = processRate / 100;
				int chance = processRate - (processing*100);
				
				for (int t = 0; t < CivCraft.structure_process; t++) {
					if (processing <= 0) {
						if (chance > 0) {
							int types = rand.nextInt(100);
							if (types >= chance) {
								debug(mill, "Skipped; Govt. "+gov.displayName+"; Greater Penalty at "+types+" > "+processRate);
							} else {
								processLumberMillUpdate();
								debug(mill, "Processed; Govt. "+gov.displayName+"; Lesser Success at "+processRate);
							}
						} else {
							debug(mill, "Skipped; Govt. "+gov.displayName+"; Maximum Penalty at 0");
						}
					} else {
						for (int i = 0; i < processing; i++) {
							processLumberMillUpdate();
							debug(mill, "Processed; Govt. "+gov.displayName+"; Stable Success at "+processRate);
							if (chance > 0) {
								int types = rand.nextInt(100);
								if (types >= chance) {
									debug(mill, "Skipped; Govt. "+gov.displayName+"; Lesser Penalty at "+types+" > "+processRate);
								} else {
									processLumberMillUpdate();
									debug(mill, "Bonus Process; Govt. "+gov.displayName+"; Bonus at "+types+" <= "+processRate);
								}
							}
						}
					}
				}
			} finally {
				this.mill.lock.unlock();
			}
		} else {
			debug(this.mill, "Failed to get lock while trying to start task, aborting.");
		}
	}
	
	public ArrayList<ConfigLumberMill> getRandomDrops(Town t, int input) {
		Random rand = new Random();		
		ArrayList<ConfigLumberMill> dropped = new ArrayList<ConfigLumberMill>();
		for (ConfigLumberMill d : CivSettings.lumbermillDrops) {
			if (d.input == input) {
				double dc = d.drop_chance;
				
				if (mill.getModifyChance() > 1.0) {
					if (d.loot_type.contains("rare")) {
						dc *= 1.1;
					} else if (d.loot_type.contains("uncommon")) {
						dc *= 1.05;
					} else if (d.loot_type.contains("common")) {
						dc *= 0.9;
					} else if (d.loot_type.contains("junk")) {
						dc *= 0.8;
					} else {
						CivLog.warning("LumberMill Process had unknown loot type, "+d.loot_type+" from "+d.type);
					}
				} else if (mill.getModifyChance() < 1.0) {
					if (d.loot_type.contains("rare")) {
						dc *= 0.8;
					} else if (d.loot_type.contains("uncommon")) {
						dc *= 0.9;
					} else if (d.loot_type.contains("common")) {
						dc *= 1.05;
					} else if (d.loot_type.contains("junk")) {
						dc *= 1.1;
					} else {
						CivLog.warning("LumberMill Process had unknown loot type, "+d.loot_type+" from "+d.type);
					}
				}
				
				if (dc <= 0) dc = d.drop_chance;
				int chance = rand.nextInt(10000);
				if (chance < (dc*10000)) {
					dropped.add(d);
				}
			}
		}
		return dropped;
	}
}
