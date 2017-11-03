package com.avrgaming.civcraft.command.admin;

import org.bukkit.event.Listener;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.ResidentExperience;

public class AdminExperienceCommand extends CommandBase implements Listener {

	@Override
	public void init() {
		command = "/ad rxp";
		displayName = "Admin Resident Experience";
		commands.put("quest", "[name] [amount] Changes the player's quest XP based on the amount.");
		commands.put("mining", "[name] [amount] Changes the player's mining XP based on the amount.");
		commands.put("fishing", "[name] [amount] Changes the player's fishing XP based on the amount.");
	}
	
	public void quest_cmd() throws CivException {
		if (args.length < 3) throw new CivException("Please check your command: Must include resident name and a value.");
		ResidentExperience re = CivGlobal.getResidentE(args[1]);
		double value = Double.valueOf(args[2]);
		re.addQuestEXP(value);
		re.save();
		CivMessage.sendSuccess(sender, "Changed "+re.getName()+" Quest XP by "+value+". Their total is now "+re.getQuestEXP());
	}
	
	public void mining_cmd() throws CivException {
		if (args.length < 3) throw new CivException("Please check your command: Must include resident name and a value.");
		ResidentExperience re = CivGlobal.getResidentE(args[1]);
		double value = Double.valueOf(args[2]);
		re.addMiningEXP(value);
		re.save();
		CivMessage.sendSuccess(sender, "Changed "+re.getName()+" Mining XP by "+value+". Their total is now "+re.getMiningEXP());
	}
	
	public void fishing_cmd() throws CivException {
		if (args.length < 3) throw new CivException("Please check your command: Must include resident name and a value.");
		ResidentExperience re = CivGlobal.getResidentE(args[1]);
		double value = Double.valueOf(args[2]);
		re.addFishingEXP(value);
		re.save();
		CivMessage.sendSuccess(sender, "Changed "+re.getName()+" Fishing XP by "+value+". Their total is now "+re.getFishingEXP());
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
