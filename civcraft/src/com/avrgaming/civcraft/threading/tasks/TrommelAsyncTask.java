package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
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
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.Trommel;
import com.avrgaming.civcraft.structure.Warehouse;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class TrommelAsyncTask extends CivAsyncTask {
	
	Trommel trommel;
	
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
		if (!trommel.isActive()) {
			debug(trommel, "trommel inactive...");
			return;
		}
		
		debug(trommel, "Processing trommel...");
		// Grab each CivChest object we'll require.
		ArrayList<StructureChest> sources_wh = new ArrayList<StructureChest>();
		ArrayList<StructureChest> sources = trommel.getAllChestsById(1);
		ArrayList<StructureChest> destinations_wh = new ArrayList<StructureChest>();
		ArrayList<StructureChest> destinations_reg = trommel.getAllChestsById(2);
		
//		int swh = 0;
//		int dwh = 0;
		
		boolean getfromWarehouse = false;
		boolean gotoWarehouse = false;
		for (Structure s : trommel.getTown().getStructures()) {
			if (s instanceof Warehouse) {
				Warehouse wh = (Warehouse) s;
				if (wh.isComplete() && wh.isEnabled()) {
					if (wh.canGotoTrommel()) {
						for (StructureChest sc : wh.structureChests.values()) {
							if (sc.getChestId() <= wh.getLevel()) {
								sources_wh.add(sc);
//								swh++;
							}
						}
						getfromWarehouse = true;
						debug(trommel, "Getting input from Warehouse");
					}
					
					if (wh.canCollectTrommel()) {
						for (StructureChest sc : wh.structureChests.values()) {
							if (sc.getChestId() <= wh.getLevel()) {
								destinations_wh.add(sc);
//								dwh++;
							}
						}
						gotoWarehouse = true;
						debug(trommel, "Sending output to Warehouse");
					}
				}
			}
		}
		
		if (!getfromWarehouse) {
			debug(trommel, "Getting input from Trommel");
		}
		
		if (!gotoWarehouse) {
//			destinations = trommel.getAllChestsById(2);
			debug(trommel, "Sending output to Trommel");
		}
		
		
		if (sources.size() != 2 || destinations_reg.size() != 2) {
			CivLog.error("Bad chests for trommel in town:"+trommel.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations_reg.size());
			return;
		}
		
//		if (destinations_wh != null && destinations_wh.size() != swh) {
//			CivLog.error("Bad chests for trommel (warehouse) in town:"+trommel.getTown().getName()+" dests:"+destinations_wh.size());
//			return;
//		}
		
//		if (sources_wh != null && sources_wh.size() != dwh) {
//			CivLog.error("Bad chests for trommel (warehouse) in town:"+trommel.getTown().getName()+" sources:"+sources_wh.size());
//			return;
//		}
		
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv_wh = new MultiInventory();
		MultiInventory source_inv = new MultiInventory();
		MultiInventory dest_inv_wh = new MultiInventory();
		MultiInventory dest_inv_reg = new MultiInventory();

		try {
			if (sources_wh != null) {
				for (StructureChest src : sources_wh) {	
					Inventory tmp;
					try {
						tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
					} catch (CivTaskAbortException e) {
						CivLog.warning("Trommel (from Warehouse):"+e.getMessage());
						return;
					}
					if (tmp == null) {
						trommel.skippedCounter++;
						return;
					}
					source_inv_wh.addInventory(tmp);
				}
			}
			
			for (StructureChest src : sources) {
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					CivLog.warning("Trommel:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter++;
					return;
				}
				source_inv.addInventory(tmp);
			}
			
			boolean full = true;
			boolean fullWH = true;
			
			if (destinations_wh != null) {
				for (StructureChest dst : destinations_wh) {
					Inventory tmp;
					try {
						tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
					} catch (CivTaskAbortException e) {
						CivLog.warning("Trommel (to Warehouse):"+e.getMessage());
						return;
					}
					if (tmp == null) {
						trommel.skippedCounter++;
						return;
					}
					dest_inv_wh.addInventory(tmp);
					for (ItemStack stack : tmp.getContents()) {
						if (stack == null) {
							fullWH = false;
							break;
						}
					}
				}
			}
			
			for (StructureChest dst2 : destinations_reg) {
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst2.getCoord().getWorldname(), dst2.getCoord().getX(), dst2.getCoord().getY(), dst2.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					CivLog.warning("Quarry:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter++;
					return;
				}
				dest_inv_reg.addInventory(tmp);
				for (ItemStack stack : tmp.getContents()) {
					if (stack == null) {
						full = false;
						break;
					}
				}
			}
			
			if (fullWH) {
				debug(trommel, "Warehouse Full, Sending Output to Normal");
				gotoWarehouse = false;
			}
			
			if (full && fullWH) { //Trommel destination chest is full, stop processing.
				return;
			}
		} catch (InterruptedException e) {
			return;
		}
		
		debug(trommel, "Processing trommel: "+trommel.skippedCounter+1);
		ItemStack[] contents = null;
		
		if (getfromWarehouse) {
			contents = source_inv_wh.getContents();
		} else {
			contents = source_inv.getContents();
		}
		
		for (int i = 0; i < trommel.skippedCounter+1; i++) {
			for (ItemStack stack : contents) {
				if (stack == null) continue;
				
				ConfigTrommelItem cti = CivSettings.trommelItems.get(CivData.getDisplayName(ItemManager.getId(stack), ItemManager.getData(stack)).toUpperCase());
				if (cti == null) continue;
				
				if (ItemManager.getId(stack) == cti.item && ItemManager.getData(stack) == cti.item_data && trommel.getLevel() >= cti.level) {
					try {
						if (getfromWarehouse) {
							this.updateInventory(Action.REMOVE, source_inv_wh, ItemManager.createItemStack(cti.item, 1, (byte)cti.item_data));
						} else {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(cti.item, 1, (byte)cti.item_data));
						}
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
							if (gotoWarehouse) {
								this.updateInventory(Action.ADD, dest_inv_wh, ni);
							} else {
								this.updateInventory(Action.ADD, dest_inv_reg, ni);
							}
						}
					} catch (InterruptedException e) {
						return;
					}
					break;
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
		if (this.trommel.lock.tryLock()) {
			try {
				try {
					Random rand = new Random();
					ConfigGovernment gov = trommel.getCiv().getGovernment();
					int processRate = (int) (gov.trommel_process_rate*100);
					
					if (processRate < 100) {
						int hundo = rand.nextInt(101);
						if (hundo > processRate) {
							debug(trommel, "Skipped; Govt. "+gov.displayName+"; Penalty at "+hundo+" > "+processRate);
						} else {
							processTrommelUpdate();
							debug(trommel, "Processed; Govt. "+gov.displayName+"; Success at "+hundo+" < "+processRate);
						}
					}
					
					if (processRate == 100) {
						processTrommelUpdate();
						debug(trommel, "Processed; Govt. "+gov.displayName+"; Success at 100% Rate");
					}
					
					if (processRate > 100 && processRate < 200) {
						processTrommelUpdate();
						debug(trommel, "Processed; Govt. "+gov.displayName+"; Success at "+processRate+"% Rate");
						int onetwohundo = rand.nextInt(processRate);
						if (onetwohundo >= 100) {
							processTrommelUpdate();
							debug(trommel, "Lucky Double Processed; Govt. "+gov.displayName+"; Bonus at "+onetwohundo+" > 100");
						}
					}
					
					if (processRate == 200) {
						processTrommelUpdate();
						processTrommelUpdate();
						debug(trommel, "Standard Double Processed; Govt. "+gov.displayName+"; Success at 200% Rate");
					}
					
					if (processRate > 200 && processRate < 300) {
						processTrommelUpdate();
						processTrommelUpdate();
						debug(trommel, "Processed; Govt. "+gov.displayName+"; Success at "+processRate+"% Rate");
						int twothreehundo = rand.nextInt(processRate);
						if (twothreehundo >= 200) {
							processTrommelUpdate();
							debug(trommel, "Lucky Double Processed; Govt. "+gov.displayName+"; Bonus at "+twothreehundo+" > 200");
						}
					}
					
					if (processRate == 300) {
						processTrommelUpdate();
						processTrommelUpdate();
						processTrommelUpdate();
						debug(trommel, "Standard Double Processed; Govt. "+gov.displayName+"; Success at 300% Rate");
					}
					
					if (processRate > 300) {
						debug(trommel, "Max Process Rate Exceeded, reducing...");
						processTrommelUpdate();
						processTrommelUpdate();
						processTrommelUpdate();
						debug(trommel, "Processed; Govt. "+gov.displayName+"; Success at "+processRate+"% Rate");
						int threefourhundo = rand.nextInt(processRate);
						if (threefourhundo >= 300) {
							processTrommelUpdate();
							debug(trommel, "Lucky Double Processed; Govt. "+gov.displayName+"; Bonus at "+threefourhundo+" > 300");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
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
