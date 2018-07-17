package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.config.ConfigTrommel;
import com.avrgaming.civcraft.config.ConfigTrommelItem;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.Trommel;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class TrommelAsyncTask extends CivAsyncTask {
	
	Trommel trommel;
	ArrayList<StructureChest> sources = new ArrayList<StructureChest>();
	ArrayList<StructureChest> destinations = new ArrayList<StructureChest>();
	
	public static HashSet<String> debugTowns = new HashSet<String>();
	
	public static void debug(Trommel trommel, String msg) {
		if (debugTowns.contains(trommel.getTown().getName())) {
			CivLog.warning("TrommelDebug:"+trommel.getTown().getName()+":"+msg);
		}
	}	
	
	public TrommelAsyncTask(Structure trommel) {
		this.trommel = (Trommel)trommel;
	}
	
	public void processTrommelUpdate(int ticks) {
		debug(trommel, "Processing Trommel...");
		ticks += trommel.skippedCounter; // Add in any ticks we previously missed
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv = new MultiInventory();
		MultiInventory dest_inv = new MultiInventory();
		ArrayList<ItemStack> process = new ArrayList<ItemStack>();
		
		try {
			// If it is full, don't even both trying to continue
			boolean full = true;
			for (StructureChest dst : destinations) {
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					trommel.skippedCounter += ticks;
					CivLog.warning("Trommel: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter += ticks;
					return;
				}
				
				if (tmp.firstEmpty() != -1) {
					dest_inv.addInventory(tmp);
					full = false;
					break;
				}
			}
			
			// destination chest is full, stop processing.
			if (full) {
				debug(trommel, "Outputs full, cancelling.");
				return;
			}
			
			boolean maxed = false;
			for (StructureChest src : sources) {
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					trommel.skippedCounter += ticks;
					CivLog.warning("Trommel: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter += ticks;
					return;
				}
				
				// Get collection from all available chess
				for (Iterator<ItemStack> iter = tmp.iterator(); iter.hasNext();) {
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					ConfigTrommelItem cti = CivSettings.trommelItems.get(CivData.getDisplayName(ItemManager.getId(stack), ItemManager.getData(stack)).toUpperCase());
					if (cti != null) {
						if (process.size() >= ticks) {
							maxed = true;
							break;
						}
						
						for (int i = stack.getAmount(); i > 0; i--) {
							ItemStack tis = new ItemStack(stack.getType(), 1, stack.getDurability());
							process.add(tis);
							stack.setAmount(stack.getAmount()-1);
							if (process.size() >= ticks) {
								maxed = true;
								break;
							}
						}
					}
//					else source_inv.addInventory(tmp);
				}
				if (maxed) break;
			}
		} catch (InterruptedException e) {
			return;
		}
		
		debug(trommel, "Processing trommel (Prev. Skipped, & Govt. Process): "+process.size());
		ArrayList<ItemStack> dropped = new ArrayList<ItemStack>();
		for (int i = 0; i < process.size(); i++) {
			ItemStack stack = process.get(i);
			ConfigTrommelItem cti = CivSettings.trommelItems.get(CivData.getDisplayName(ItemManager.getId(stack), ItemManager.getData(stack)).toUpperCase());
			if (ItemManager.getId(stack) == cti.item && ItemManager.getData(stack) == cti.item_data && trommel.getLevel() >= cti.level) {
				try {
					this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(cti.item, 1, (byte)cti.item_data));
				} catch (InterruptedException e) {
					return;
				}
				
				ArrayList<ItemStack> toDrop = getRandomDrops(trommel.getTown(), cti.item, cti.item_data);
				if (toDrop.size() < 1) dropped.add(getReturnDrop());
				else dropped.addAll(toDrop);
			}
		}
		
		try { //Try to add the new item to the dest chest, if we cant, oh well.
			for (ItemStack ni : dropped) {
				debug(trommel, "Updating inventory: "+ni);
				this.updateInventory(Action.ADD, dest_inv, ni);
			}
		} catch (InterruptedException e) {
			trommel.skippedCounter += ticks;
			return;
		}
//		trommel.skippedCounter = 0;
	}
	
	private ItemStack getReturnDrop() {
		Random rand = new Random();
		int uselessDrop = rand.nextInt(10);
		if (uselessDrop >= 0 && uselessDrop <= 3) {
			return ItemManager.createItemStack(CivData.DIRT, 1);
		} else if (uselessDrop >= 4 && uselessDrop <= 7) {
			return ItemManager.createItemStack(CivData.GRAVEL, 1);
		} else {
			return ItemManager.createItemStack(CivData.AIR, 1);
		}
	}
	
	public void run() {
		if (!trommel.isActive()) {
			debug(trommel, "Trommel Disabed...");
			return;
		}
		
		int ticks = 0;
		if (this.trommel.lock.tryLock()) {
			try {
				sources.clear();
				destinations.clear();
				
				if (trommel != null) {
					sources.addAll(trommel.getAllChestsById(1));
					destinations.addAll(trommel.getAllChestsById(2));
				}
				
				if (sources.size() < 1 || destinations.size() < 1) {
					CivLog.error("Bad dest chests for trommel in town: "+trommel.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
					return;
				}
				
/*				boolean quarry = false;
				Warehouse whs = (Warehouse) quarry.getTown().getStructureByType("s_warehouse");
				if (whs != null) {
					if (whs.isComplete() && whs.isEnabled()) {
						if (whs.getQuarryCollector() == 2) {
							debug(quarry, "Input directed from Quarry");
							Quarry qrs = (Quarry) quarry.getTown().getStructureByType("s_quarry");
							for (StructureChest sc : qrs.getAllChestsById(1)) {
								quarry = true;
								sources_other.add(sc);
							}
						}
					}
				}*/
				
				Random rand = new Random();
				ConfigGovernment gov = trommel.getCiv().getGovernment();
				double processRate = (int) (gov.trommel_process_rate*100);
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
							debug(trommel, "Skipped; Govt. "+gov.displayName+"; Maximum Penalty at 0, was sent "+processing);
						}
					} else {
						int toTick = (int) processing;
						processes += toTick;
						ticks += toTick;
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
				processTrommelUpdate(ticks);
				debug(trommel, "Govt. "+gov.displayName+" at "+processRate+"%; Processes:"+processes+", Bonuses:"+bonuses+", Skips:"+skips+", Bonuses Skips:"+bonusesskips);
			} finally {
				this.trommel.lock.unlock();
			}
		} else {
			debug(this.trommel, "Failed to get lock on trommel while trying to start task, aborting.");
		}
	}
	
	public ArrayList<ItemStack> getRandomDrops(Town t, int input, int input_data) {
		Random rand = new Random();		
		ArrayList<ItemStack> dropped = new ArrayList<ItemStack>();
		for (ConfigTrommel d : CivSettings.trommelDrops) {
			if (d.input == input && d.input_data == input_data) {
				double dc = ((trommel.getLevel()-1)*0.002) + d.drop_chance;
				
				if (trommel.getModifyChance() > 1.0) {
					if (d.loot_type.contains("rare")) {
						dc *= 1.1;
					} else if (d.loot_type.contains("uncommon")) {
						dc *= 1.05;
					} else if (d.loot_type.contains("common")) {
						dc *= 0.9;
					} else if (d.loot_type.contains("junk")) {
						dc *= 0.8;
					} else {
						CivLog.warning("Trommel Process had unknown loot type, "+d.loot_type+" from "+d.type);
					}
				} else if (trommel.getModifyChance() < 1.0) {
					if (d.loot_type.contains("rare")) {
						dc *= 0.8;
					} else if (d.loot_type.contains("uncommon")) {
						dc *= 0.9;
					} else if (d.loot_type.contains("common")) {
						dc *= 1.05;
					} else if (d.loot_type.contains("junk")) {
						dc *= 1.1;
					} else {
						CivLog.warning("Trommel Process had unknown loot type, "+d.loot_type+" from "+d.type);
					}
				}
				
				if (dc <= 0) dc = d.drop_chance;
				int chance = rand.nextInt(10000);
				if (chance < (dc*10000)) {
					if (d.custom_id != null) {
						LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.custom_id);
						dropped.add(LoreMaterial.spawn(LoreMaterial.materialMap.get(craftMat.getConfigId()), d.amount));
					} else {
						dropped.add(ItemManager.createItemStack(d.type_id, d.amount, (short)d.type_data));
					}
				}
			}
		}
		return dropped;
	}
}