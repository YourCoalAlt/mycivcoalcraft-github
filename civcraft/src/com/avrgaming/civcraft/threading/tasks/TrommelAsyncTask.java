package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.config.ConfigTrommel;
import com.avrgaming.civcraft.config.ConfigTrommelItem;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
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
	
	public void processTrommelUpdate() {
		debug(trommel, "Processing Trommel...");
		if (sources.size() < 1 || destinations.size() < 1) {
			CivLog.error("Bad dest chests for quarry in town: "+trommel.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
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
					CivLog.warning("Trommel: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter++;
					return;
				}
				
				boolean brk = false;
				for (ListIterator<ItemStack> iter = tmp.iterator(); iter.hasNext();) {
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					ConfigTrommelItem cti = CivSettings.trommelItems.get(CivData.getDisplayName(ItemManager.getId(stack), ItemManager.getData(stack)).toUpperCase());
					if (cti == null) continue;
					else {
						source_inv.addInventory(tmp);
						brk = true;
						break;
					}
				}
				
				if (brk) break;
//				source_inv.addInventory(tmp);
			}
			
			boolean full = true;
			
			for (StructureChest dst : destinations) {
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					CivLog.warning("Trommel: "+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter++;
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
		} catch (InterruptedException e) {
			return;
		}
		
		debug(trommel, "Processing trommel: "+trommel.skippedCounter+1);
		
		for (int i = 0; i < trommel.skippedCounter+1; i++) {
			for (Inventory inv : source_inv.getInventories()) {
				for (ListIterator<ItemStack> iter = inv.iterator(); iter.hasNext();) {
					ItemStack stack = iter.next();
					if (stack == null) continue;
					
					ConfigTrommelItem cti = CivSettings.trommelItems.get(CivData.getDisplayName(ItemManager.getId(stack), ItemManager.getData(stack)).toUpperCase());
					if (cti == null) continue;
					
					if (ItemManager.getId(stack) == cti.item && ItemManager.getData(stack) == cti.item_data && trommel.getLevel() >= cti.level) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(cti.item, 1, (byte)cti.item_data));
						} catch (InterruptedException e) {
							return;
						}
						
						ArrayList<ItemStack> newItem = new ArrayList<ItemStack>();
						ArrayList<ConfigTrommel> dropped = getRandomDrops(trommel.getTown(), cti.item, cti.item_data);
						if (dropped.size() == 0) {
							newItem.add(getUselessDrop());
						} else {
							for (ConfigTrommel d : dropped) {
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
								debug(trommel, "Updating inventory: "+ni);
								this.updateInventory(Action.ADD, dest_inv, ni);
							}
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
				}
			}
		}
		trommel.skippedCounter = 0;
	}
	
	private ItemStack getUselessDrop() {
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
		
		sources.clear();
		destinations.clear();
		
		if (trommel != null) {
			sources.addAll(trommel.getAllChestsById(1));
			destinations.addAll(trommel.getAllChestsById(2));
		}
		
		if (this.trommel.lock.tryLock()) {
			try {
				Random rand = new Random();
				ConfigGovernment gov = trommel.getCiv().getGovernment();
				int processRate = (int) (gov.trommel_process_rate*100);
				int processing = processRate / 100;
				int chance = processRate - (processing*100);
				
				for (int t = 0; t < CivSettings.getInteger(CivSettings.gameConfig, "timers.struc_process"); t++) {
					if (processing <= 0) {
						if (chance > 0) {
							int types = rand.nextInt(100);
							if (types >= chance) {
								debug(trommel, "Skipped; Govt. "+gov.displayName+"; Greater Penalty at "+types+" > "+processRate);
							} else {
								processTrommelUpdate();
								debug(trommel, "Processed; Govt. "+gov.displayName+"; Lesser Success at "+processRate);
							}
						} else {
							debug(trommel, "Skipped; Govt. "+gov.displayName+"; Maximum Penalty at 0");
						}
					} else {
						for (int i = 0; i < processing; i++) {
							processTrommelUpdate();
							debug(trommel, "Processed; Govt. "+gov.displayName+"; Stable Success at "+processRate);
							if (chance > 0) {
								int types = rand.nextInt(100);
								if (types >= chance) {
									debug(trommel, "Skipped; Govt. "+gov.displayName+"; Lesser Penalty at "+types+" > "+processRate);
								} else {
									processTrommelUpdate();
									debug(trommel, "Bonus Process; Govt. "+gov.displayName+"; Bonus at "+types+" <= "+processRate);
								}
							}
						}
					}
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
			} finally {
				this.trommel.lock.unlock();
			}
		} else {
			debug(this.trommel, "Failed to get lock while trying to start task, aborting.");
		}
	}
	
	public ArrayList<ConfigTrommel> getRandomDrops(Town t, int input, int input_data) {
		Random rand = new Random();		
		ArrayList<ConfigTrommel> dropped = new ArrayList<ConfigTrommel>();
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
					dropped.add(d);
				}
			}
		}
		return dropped;
	}
}