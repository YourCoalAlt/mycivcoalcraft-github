package com.avrgaming.civcraft.threading.tasks;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;

public class MailToResidentTask implements Runnable {
	
	String uid;
	String item;
	
	public MailToResidentTask(String uid, String item) {
		this.uid = uid;
		this.item = item;
	}
	
	@Override
	public void run() {
		Resident res = CivGlobal.getResidentViaUUID(UUID.fromString(uid));
		if (res == null) {
			CivLog.error("cannot find resident for uuid ["+uid+"] to send item: ["+item+"]");
			return;
		} else {
			res.addMailData(item);
			if (Bukkit.getPlayer(res.getUUID()).isOnline()) {
				CivMessage.send(res, "You have recieved a package in the mail! Collect it using your Backpack.");
			}
		}
	}
}