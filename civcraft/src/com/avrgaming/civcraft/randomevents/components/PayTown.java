package com.avrgaming.civcraft.randomevents.components;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.randomevents.RandomEventComponent;

public class PayTown extends RandomEventComponent {
	
	@Override
	public void process() {
		String townName = this.getParent().componentVars.get(getString("townname_var"));
		if (townName == null) {
			CivLog.warning("No townname var for pay town.");
			return;
		}

		Town t = CivGlobal.getTown(townName);
		double coins = this.getDouble("amount");
		t.getTreasury().deposit(coins);
		CivMessage.sendTown(t, "We've recieved "+coins+" coins!");	
	}
}
