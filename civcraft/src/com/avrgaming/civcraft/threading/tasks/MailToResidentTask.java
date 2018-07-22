package com.avrgaming.civcraft.threading.tasks;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;

public class MailToResidentTask implements Runnable {
	
	Resident res;
	String mail_name;
	Long mail_id;
	Inventory inv;
	
	public MailToResidentTask(Resident res, String mail_name, long mail_id, Inventory inv) {
		this.res = res;
		this.mail_name = mail_name;
		this.mail_id = mail_id;
		this.inv = inv;
	}
	
	@Override
	public void run() {
		res.addMail(res, mail_name, mail_id, inv);
		try {
			OfflinePlayer op = CivGlobal.getOfflinePlayer(res);
			if (op != null && op.isOnline()) {
				CivMessage.send(res, CivCraft.server_name+"You have recieved a package in the mail! Collect it using your Backpack.");
			}
		} catch (CivException e) {
			e.printStackTrace();
		}
	}
}