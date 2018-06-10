package com.avrgaming.civcraft.command.admin;

import org.bukkit.event.Listener;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.object.ResidentExperience.EXPSlots;

public class AdminExperienceCommand extends CommandBase implements Listener {

	@Override
	public void init() {
		command = "/ad rxp";
		displayName = "Admin Resident Experience";
		commands.put("view", "[name] View all slots and values.");
		commands.put("add", "[name] [slot] [amount] Changes the player's selected slot XP based on the amount.");
	}
	
	public void view_cmd() throws CivException {
		ResidentExperience re = CivGlobal.getResidentE(args[1]);
		for (EXPSlots sts : EXPSlots.values()) {
			CivMessage.send(sender, sts+"("+re.getSlotString(sts)+"): "+re.getEXPSlots().get(sts));
		}
	}
	
	public void add_cmd() throws CivException {
		if (args.length < 4) throw new CivException("Please check your command: Must include resident name, slot, and value.");
		ResidentExperience re = CivGlobal.getResidentE(args[1]);
		if (EXPSlots.valueOf(args[2].toString().toUpperCase()) == null) {
			String slots = "";
			for (EXPSlots sts : EXPSlots.values()) {
				slots += re.getSlotString(sts);
			}
			throw new CivException("Invalid slot name. Please choose from one of the following: "+slots);
		}
		EXPSlots exps = EXPSlots.valueOf(args[2].toString().toUpperCase());
		double value = Double.valueOf(args[3]);
		re.addResEXPviaAdmin(exps, value);
		CivMessage.sendSuccess(sender, "Changed "+re.getName()+" "+re.getSlotString(exps)+" XP by "+value+". Their total is now "+re.getEXPCount(exps));
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}
	
	@Override
	public void showHelp() {
		showBasicHelp();
	}
	
	@Override
	public void permissionCheck() throws CivException {
	}
}
