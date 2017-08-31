package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.config.ConfigQuarry;
import com.avrgaming.civcraft.config.ConfigQuarryItem;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Quarry;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.Trommel;
import com.avrgaming.civcraft.structure.Warehouse;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class QuarryAsyncTask extends CivAsyncTask {

	Quarry quarry;
	
	public static HashSet<String> debugTowns = new HashSet<String>();

	public static void debug(Quarry quarry, String msg) {
		if (debugTowns.contains(quarry.getTown().getName())) {
			CivLog.warning("QuarryDebug:"+quarry.getTown().getName()+":"+msg);
		}
	}	
	
	public QuarryAsyncTask(Structure quarry) {
		this.quarry = (Quarry)quarry;
	}
	
	public void processQuarryUpdate() {
		if (!quarry.isActive()) {
			debug(quarry, "quarry inactive...");
			return;
		}
		
		debug(quarry, "Processing quarry...");
		// Grab each CivChest object we'll require.
		ArrayList<StructureChest> sources = quarry.getAllChestsById(1);
		ArrayList<StructureChest> destinations_wh = new ArrayList<StructureChest>();
		ArrayList<StructureChest> destinations_reg = quarry.getAllChestsById(2);
		
		boolean trommel = false;
		boolean warehouse = false;
		for (Structure s : quarry.getTown().getStructures()) {
			if (s instanceof Warehouse) {
				Warehouse wh = (Warehouse) s;
				if (wh.isComplete() && wh.isEnabled()) {
					if (wh.getQuarryCollector() == 1) {
						for (StructureChest sc : wh.structureChests.values()) {
							if (sc.getChestId() <= wh.getLevel()) {
								destinations_wh.add(sc);
							}
						}
						warehouse = true;
						debug(quarry, "Sending output to Warehouse");
					} else if (wh.getQuarryCollector() == 2) {
						for (Structure s2 : quarry.getTown().getStructures()) {
							if (s2 instanceof Trommel) {
								Trommel tS = (Trommel) s2;
								destinations_wh.addAll(tS.getAllChestsById(1));
								trommel = true;
								debug(quarry, "Sending output to Trommel");
							}
						}
					}
				}
			}
		}
		
		if (!warehouse && !trommel) {
//			destinations_wh = quarry.getAllChestsById(2);
			debug(quarry, "Sending output to Quarry");
		}
		
		if (sources.size() != 2 || destinations_reg.size() != 2) {
			CivLog.error("Bad chests for quarry in town:"+quarry.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations_reg.size());
			return;
		}
		
		if (destinations_wh != null && destinations_reg.size() != 2) {
			CivLog.error("Bad chests for quarry (warehouse) in town:"+quarry.getTown().getName()+" dests:"+destinations_wh.size());
		}
		
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv = new MultiInventory();
		MultiInventory dest_inv_wh = new MultiInventory();
		MultiInventory dest_inv_reg = new MultiInventory();

		try {
			for (StructureChest src : sources) {
				//this.syncLoadChunk(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getZ());				
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Quarry:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					quarry.skippedCounter++;
					return;
				}
				source_inv.addInventory(tmp);
			}
			
			boolean full = true;
			
			if (destinations_wh != null) {
				for (StructureChest dst : destinations_wh) {
					CivMessage.global(""+dst.getChestId());
					//this.syncLoadChunk(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getZ());
					Inventory tmp;
					try {
						tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
					} catch (CivTaskAbortException e) {
						//e.printStackTrace();
						CivLog.warning("Quarry (to Warehouse/Trommel):"+e.getMessage());
						return;
					}
					if (tmp == null) {
						quarry.skippedCounter++;
						return;
					}
					dest_inv_wh.addInventory(tmp);
					for (ItemStack stack : tmp.getContents()) {
						if (stack == null) {
							full = false;
							break;
						}
					}
				}
			}
			
			for (StructureChest dst2 : destinations_reg) {
				//this.syncLoadChunk(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getZ());
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst2.getCoord().getWorldname(), dst2.getCoord().getX(), dst2.getCoord().getY(), dst2.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Quarry:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					quarry.skippedCounter++;
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
			
			if (full) {
				/* Quarry destination chest is full, stop processing. */
				return;
			}
		} catch (InterruptedException e) {
			return;
		}
		
		debug(quarry, "Processing quarry:"+quarry.skippedCounter+1);
		ItemStack[] contents = source_inv.getContents();
		for (int i = 0; i < quarry.skippedCounter+1; i++) {
		
			for(ItemStack stack : contents) {
				if (stack == null) {
					continue;
				}
				
				ConfigQuarryItem cti = CivSettings.quarryItems.get(ItemManager.getId(stack));
				if (cti == null) {
					continue;
				}
				
				if (ItemManager.getId(stack) == cti.item && quarry.getLevel() >= cti.level) {
					try {
						short damage = ItemManager.getData(stack);
						this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(cti.item, 1));
						damage ++;
						stack.setDurability(damage);
						if (damage < cti.max_dura) {
							this.updateInventory(Action.ADD, source_inv, stack);
						}
					} catch (InterruptedException e) {
						return;
					}
					
					ArrayList<ItemStack> newItem = new ArrayList<ItemStack>();
					ArrayList<ConfigQuarry> dropped = getRandomDrops(quarry.getTown(), cti.item);
					if (dropped.size() == 0) {
						newItem.add(getUselessDrop());
					} else {
						for (ConfigQuarry d : dropped) {
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
							debug(quarry, "Updating inventory: "+ni);
							if (warehouse) {
								this.updateInventory(Action.ADD, dest_inv_wh, ni);
							} else if (trommel && ni.getType() == Material.STONE) {
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
		quarry.skippedCounter = 0;
	}
	
	private ItemStack getUselessDrop() {
		Random rand = new Random();
		int uselessDrop = rand.nextInt(4);
		if (uselessDrop == 1) {
			return ItemManager.createItemStack(CivData.DIRT, 1, (byte)CivData.COARSE_DIRT);
		} else if (uselessDrop == 2) {
			return ItemManager.createItemStack(CivData.DIRT, 1);
		} else {
			return getStoneDrop();
		}
	}
	
	private ItemStack getStoneDrop() {
		Random rand = new Random();
		int uselessDrop = rand.nextInt(6);
		if (uselessDrop == 1) {
			return ItemManager.createItemStack(CivData.STONE, 1);
		} else if (uselessDrop == 2) {
			return ItemManager.createItemStack(CivData.STONE, 1, (byte)CivData.GRANITE);
		} else if (uselessDrop == 3) {
			return ItemManager.createItemStack(CivData.STONE, 1, (byte)CivData.DIORITE);
		} else if (uselessDrop == 4) {
			return ItemManager.createItemStack(CivData.STONE, 1, (byte)CivData.ANDESITE);
		} else {
			return ItemManager.createItemStack(CivData.COBBLESTONE, 1);
		}
	}
	
	public void run() {
		if (this.quarry.lock.tryLock()) {
			try {
				try {
					Random rand = new Random();
					ConfigGovernment gov = quarry.getCiv().getGovernment();
					int processRate = (int) (gov.quarry_process_rate*100);
					
					if (processRate < 100) {
						int hundo = rand.nextInt(101);
						if (hundo > processRate) {
							debug(quarry, "Skipped; Govt. "+gov.displayName+"; Penalty at "+hundo+" > "+processRate);
						} else {
							processQuarryUpdate();
							debug(quarry, "Processed; Govt. "+gov.displayName+"; Success at "+hundo+" < "+processRate);
						}
					}
					
					if (processRate == 100) {
						processQuarryUpdate();
						debug(quarry, "Processed; Govt. "+gov.displayName+"; Success at 100% Rate");
					}
					
					if (processRate > 100 && processRate < 200) {
						processQuarryUpdate();
						debug(quarry, "Processed; Govt. "+gov.displayName+"; Success at "+processRate+"% Rate");
						int onetwohundo = rand.nextInt(processRate);
						if (onetwohundo > 100) {
							processQuarryUpdate();
							debug(quarry, "Lucky Double Processed; Govt. "+gov.displayName+"; Bonus at "+onetwohundo+" > 100");
						}
					}
					
					if (processRate == 200) {
						processQuarryUpdate();
						processQuarryUpdate();
						debug(quarry, "Standard Double Processed; Govt. "+gov.displayName+"; Success at 200% Rate");
					}
					
					if (processRate > 200 && processRate < 300) {
						processQuarryUpdate();
						processQuarryUpdate();
						debug(quarry, "Processed; Govt. "+gov.displayName+"; Success at "+processRate+"% Rate");
						int twothreehundo = rand.nextInt(processRate);
						if (twothreehundo > 200) {
							processQuarryUpdate();
							debug(quarry, "Lucky Double Processed; Govt. "+gov.displayName+"; Bonus at "+twothreehundo+" > 200");
						}
					}
					
					if (processRate == 200) {
						processQuarryUpdate();
						processQuarryUpdate();
						processQuarryUpdate();
						debug(quarry, "Standard Double Processed; Govt. "+gov.displayName+"; Success at 200% Rate");
					}
					
					if (processRate > 300) {
						debug(quarry, "Max Process Rate Exceeded, reducing...");
						processQuarryUpdate();
						processQuarryUpdate();
						processQuarryUpdate();
						debug(quarry, "Processed; Govt. "+gov.displayName+"; Success at "+processRate+"% Rate");
						int threefourhundo = rand.nextInt(processRate);
						if (threefourhundo > 300) {
							processQuarryUpdate();
							debug(quarry, "Lucky Double Processed; Govt. "+gov.displayName+"; Bonus at "+threefourhundo+" > 300");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				this.quarry.lock.unlock();
			}
		} else {
			debug(this.quarry, "Failed to get lock while trying to start task, aborting.");
		}
	}
	
	public ArrayList<ConfigQuarry> getRandomDrops(Town t, int input) {
		Random rand = new Random();		
		ArrayList<ConfigQuarry> dropped = new ArrayList<ConfigQuarry>();
		
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
						CivLog.warning("Quarry Process had unknown loot type, "+d.loot_type);
					}
				}
				
				if (dc <= 0) {
					dc = d.drop_chance;
				}
				
				int chance = rand.nextInt(10000);
				if (chance < (dc*10000)) {
					dropped.add(d);
				}
			}
		}
		return dropped;
	}
}
