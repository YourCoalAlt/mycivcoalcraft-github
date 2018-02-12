package com.avrgaming.civcraft.threading.timers;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.LumberMillAsyncTask;
import com.avrgaming.civcraft.threading.tasks.QuarryAsyncTask;
import com.avrgaming.civcraft.threading.tasks.TrommelAsyncTask;
import com.avrgaming.civcraft.threading.tasks.WarehouseAsyncTask;
import com.avrgaming.civcraft.util.BlockCoord;

public class StructureProcessTimer extends CivAsyncTask {
	
	public static ReentrantLock lock = new ReentrantLock();
	
	@Override
	public void run() {		
		if (!lock.tryLock()) return;
		
		try {
			// Loop through each structure, if it has an update function call it in another async process
			Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
			while(iter.hasNext()) {
				Structure struct = iter.next().getValue();
				if (!struct.isActive()) continue;
				
				if (struct.getUpdateEvent() != null && !struct.getUpdateEvent().equals("")) {
					if (struct.getUpdateEvent().equals("trommel_process")) {
						if (!CivGlobal.trommelsEnabled) continue;
						TaskMaster.asyncTask("trommel-"+struct.getCorner().toString(), new TrommelAsyncTask(struct), 0);
					}
					if (struct.getUpdateEvent().equals("lumbermill_process")) {
						if (!CivGlobal.lumbermillsEnabled) continue;
						TaskMaster.asyncTask("lumbermill-"+struct.getCorner().toString(), new LumberMillAsyncTask(struct), 0);
					}
					if (struct.getUpdateEvent().equals("quarry_process")) {
						if (!CivGlobal.quarriesEnabled) continue;
						TaskMaster.asyncTask("quarry-"+struct.getCorner().toString(), new QuarryAsyncTask(struct), 0);
					}
					if (struct.getUpdateEvent().equals("warehouse_transfer")) {
						if (!CivGlobal.warehousesEnabled) continue;
						TaskMaster.asyncTask("warehouse-"+struct.getCorner().toString(), new WarehouseAsyncTask(struct), 0);
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}
}
