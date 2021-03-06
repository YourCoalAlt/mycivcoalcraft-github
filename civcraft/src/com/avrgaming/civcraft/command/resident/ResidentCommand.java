/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.command.resident;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;

public class ResidentCommand extends CommandBase {

	@Override
	public void init() {
		command = "/resident";
		displayName = "Resident";
		
		commands.put("info", "show your resident info");
		commands.put("paydebt", "Pays off your current debt.");
		commands.put("friend", "Manage friends.");
		commands.put("toggle", "Toggles various resident specific settings.");
		commands.put("show", "[name] shows resident info for the given resident.");
		commands.put("resetspawn", "Resets your spawn point back to spawn town.");
		commands.put("exchange", "[type] [amount] - Exchanges this type(iron,gold,diamond,emerald) of ingot at 30% of its value.");
		commands.put("perks", "Displays your perks.");
		commands.put("refresh", "Refreshes your perks.");
		commands.put("timezone", "(timezone) Display your current timezone or change it to (timezone)");
		commands.put("pvptimer", "Remove your PvP Timer. This is a permenant change and can not be undone.");
		commands.put("outlawed", "Check what towns you are outlawed in.");
		commands.put("prevnames", "[name] Check to see the previous usernames of a player.");
		//commands.put("switchtown", "[town] - Allows you to instantly change your town to this town, if this town belongs to your civ.");
	}
	
	public void prevnames_cmd() throws CivException {
		Resident res = getNamedResident(1);
		OfflinePlayer p = CivGlobal.getOfflinePlayer(res);
		Map<String, String> prevNames = CivItem.getPlayerPreviousNames(p);
		CivMessage.sendHeading(sender, "Previous Usernames");
		for (Entry<String, String> s : prevNames.entrySet()) {
			CivMessage.send(sender, s.getValue()+CivColor.DarkGrayItalic+" (Changed: "+s.getKey()+")");
		}
	}
	
	public void outlawed_cmd() throws CivException {
		Resident res = getResident();
		String list = CivColor.Yellow+res.getName()+" is outlawed in: ";
		for (Town t : CivGlobal.getTowns()) {
			if (t.isOutlaw(res.getName())) {
				list += CivColor.LightGreen+t.getName()+", ";
			}
		}
		CivMessage.send(res, list);
	}
	
	public void pvptimer_cmd() throws CivException {
		Resident resident = getResident();
		if (!resident.isProtected()) {
			throw new CivException("You are not protected at this time.");
		}
		
		resident.setisProtected(false);
		CivMessage.sendSuccess(sender, "You are no longer protected.");
	}
	
	public void timezone_cmd() throws CivException {
		Resident resident = getResident();
		
		if (args.length < 2) {
;
			CivMessage.sendSuccess(sender, "Your current timezone is set to "+resident.getTimezone());
			return;
		}
		
		if (args[1].equalsIgnoreCase("list")) {
			CivMessage.sendHeading(sender, "Available TimeZones");
			String out = "";
			for (String zone : TimeZone.getAvailableIDs()) {
				out += zone + ", ";
			}
			CivMessage.send(sender, out);
			return;
		}
		
		TimeZone timezone = TimeZone.getTimeZone(args[1]);
		
		if (timezone.getID().equals("GMT") && !args[1].equalsIgnoreCase("GMT")) {
			CivMessage.send(sender, CivColor.Gray+"We may not have recognized your timezone \""+args[1]+"\" if so, we'll set it to GMT.");
			CivMessage.send(sender, CivColor.Gray+"Type \"/resident timezone list\" to get a list of all available timezones.");
		}
		
		resident.setTimezone(timezone.getID());
		resident.save();
		CivMessage.sendSuccess(sender, "TimeZone has been set to "+timezone.getID());
	}
	
	public void refresh_cmd() throws CivException {
		Resident resident = getResident();
		resident.perks.clear();
		resident.loadPerks();
		CivMessage.sendSuccess(sender, "Reloaded your perks from the website.");
	}
	
	public void perks_cmd() throws CivException {
		Resident resident = getResident();
		
		//CivMessage.sendHeading(sender, "Your Perks");
		//for (Perk p : resident.perks.values()) {
		//	CivMessage.send(sender, "Perk:"+p.getIdent());
		//}
		resident.showPerkPage(0);
	}
	
	/*
	 * We need to figure out how to handle debt for the resident when he switches towns.
	 * Should we even allow this? idk. Maybe war respawn points is enough?
	 */
//	public void switchtown_cmd() throws CivException {
//		Town town = getNamedTown(1);
//		Resident resident = getResident();
//		
//		if (resident.getTown() == town) {
//			throw new CivException("You cannot switch to your own town.");
//		}
//		
//		if (resident.getTown().getMotherCiv() != town.getMotherCiv()) {
//			throw new CivException("You cannot place yourself into a conquered civ's town.");
//		}
//		
//		if (town.getCiv() != resident.getCiv()) {
//			throw new CivException("You cannot switch to a town not in your civ.");
//		}
//		
//		if (town.getMayorGroup().hasMember(resident) && town.getMayorGroup().getMemberCount() <= 1) {
//			throw new CivException("You are the last mayor of the town and cannot leave it.");
//		}
//		
//		resident.getTown().removeResident(resident);
//		try {
//			town.addResident(resident);
//		} catch (AlreadyRegisteredException e) {
//			e.printStackTrace();
//			throw new CivException("You already belong to this town.");
//		}
//		
//	}
	
	public void exchange_cmd() throws CivException {
		Player player = getPlayer();
		Resident resident = getResident();
		String type = getNamedString(1, "Enter a type. E.g. iron, gold, diamond, emerald.");
		Integer amount = getNamedInteger(2);
		
		if (amount <= 0) {
			throw new CivException("You must exchange a positive, non-zero amount.");
		}
		
		type = type.toLowerCase();
		
		Material exchangeType;
		double rate;
		switch (type) {
		case "iron":
			exchangeType = Material.IRON_INGOT;
			rate = CivSettings.iron_rate;
			break;
		case "gold":
			exchangeType = Material.GOLD_INGOT;
			rate = CivSettings.gold_rate;
			break;
		case "diamond":
			exchangeType = Material.DIAMOND;
			rate = CivSettings.diamond_rate;
			break;
		case "emerald":
			exchangeType = Material.EMERALD;
			rate = CivSettings.emerald_rate;
			break;
		default:
			throw new CivException("Unknown exchange type "+type+" must be iron, gold, diamond, or emerald.");
		}

		double exchangeRate;
		try {
			exchangeRate = CivSettings.getDouble(CivSettings.civConfig, "global.exchange_rate");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			throw new CivException("Internal configuration error!");
		}
		
		ItemStack stack = CivItem.newStack(exchangeType);
		int total = 0;
		for (int i = 0; i < player.getInventory().getContents().length; i++) {
			ItemStack is = player.getInventory().getItem(i);
			if (is == null) continue;
			if (LoreCraftableMaterial.isCustom(is)) continue;
			
			if (is.getType() == exchangeType) {
				total += is.getAmount();
			}
		}
		
		if (total == 0) {
			throw new CivException("You do not have any "+type);
		}
		
		if (amount > total) {
			amount = total;
		}
		
		stack.setAmount(amount);
		player.getInventory().removeItem(stack);
		int coins = (int) (amount*rate*exchangeRate);
		
		resident.getTreasury().deposit(coins);
		CivMessage.sendSuccess(player, "Exchanged "+amount+" "+type+" for "+coins+" coins.");
	}
	
	public void resetspawn_cmd() throws CivException {
		Player player = getPlayer();
		Location spawn = player.getWorld().getSpawnLocation();
		player.setBedSpawnLocation(spawn, true);
		CivMessage.sendSuccess(player, "You will now respawn at spawn.");
	}
	
	public void show_cmd() throws CivException {
		if (args.length < 2) {
			throw new CivException("Please enter the resident's name you wish to know about.");
		}
		
		Resident resident = getNamedResident(1);
		show(sender, resident);
	}

	public void toggle_cmd() throws CivException {
		ResidentToggleCommand cmd = new ResidentToggleCommand();	
		cmd.onCommand(sender, null, "friend", this.stripArgs(args, 1));
	}
	
	public void friend_cmd() {
		ResidentFriendCommand cmd = new ResidentFriendCommand();	
		cmd.onCommand(sender, null, "friend", this.stripArgs(args, 1));
	}

	public void paydebt_cmd() throws CivException {
		Resident resident = getResident();
	
		if (!resident.getTreasury().hasEnough(resident.getTreasury().getDebt())) {
			throw new CivException("You do not have the required "+resident.getTreasury().getDebt()+" coins to pay off your debt.");
		}
		

		CivMessage.sendSuccess(sender, "Paid "+resident.getTreasury().getDebt()+" coins of debt.");
		resident.payOffDebt();
	}
	
	public void info_cmd() throws CivException {
		Resident resident = getResident();
    	show(sender, resident);
	}
	
	public static void show(CommandSender sender, Resident resident) throws CivException {
		CivMessage.sendHeading(sender, "Resident "+resident.getName());
		
		if (sender.isOp()) {
			CivMessage.send(sender, CivColor.GrayBold+" -- OP Debug -- ");
			CivMessage.send(sender, "id: "+resident.getId());
			CivMessage.send(sender, "registered: "+resident.getRegistered());
			CivMessage.send(sender, "spy exposure: "+resident.getSpyExposure());
			CivMessage.send(sender, "protected: "+resident.isProtected());
			CivMessage.send(sender, CivColor.GrayBold+" -------------- ");
		}
		
		if (CivGlobal.getOfflinePlayer(resident).isOnline()) {
			CivMessage.send(sender, CivColor.LightBlue+"Is Currently Online");
		} else {
			Date lastOnline = new Date(resident.getLastOnline());
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
			CivMessage.send(sender, CivColor.Green+"Last Online at "+CivColor.LightGreen+sdf.format(lastOnline));
		}
		
		if (resident.hasTown()) {
			CivMessage.send(sender, CivColor.Green+"Resides In: "+CivColor.LightGreen+resident.getTownString()+", "+resident.getCivString());
		} 
		if (resident.hasCamp()) {
			CivMessage.send(sender, CivColor.Green+"Resides In: "+CivColor.LightGreen+"Camp "+resident.getCampString());
		}
		
		if (!resident.hasTown() && !resident.hasCamp()) {
			CivMessage.send(sender, CivColor.Green+"Resides In: "+CivColor.Gray+" Currently Homeless");
		}
		
		if (sender.getName().equalsIgnoreCase(resident.getName()) || sender.isOp()) {
			CivMessage.send(sender, CivColor.Green+"Personal Treasury: "+CivColor.LightGreen+resident.getTreasury().getBalance()+" Coins");
			CivMessage.send(sender, CivColor.Green+"Taxes Owed: "+CivColor.LightGreen+(resident.getPropertyTaxOwed()+resident.getFlatTaxOwed()));
			if (resident.hasTown()) {
				if (resident.getSelectedTown() != null) {
					CivMessage.send(sender, CivColor.Green+"Selected Town: "+CivColor.LightGreen+resident.getSelectedTown().getName());
				} else {
					CivMessage.send(sender, CivColor.Green+"Selected Town: "+CivColor.LightGreen+resident.getTown().getName());
				}
			}
		}
		
		if (resident.getTreasury().inDebt()) {
			CivMessage.send(resident, CivColor.Yellow+"In Debt "+resident.getTreasury().getDebt()+" Coins!");
		}
		
		if (resident.getDaysTilEvict() > 0) {
			CivMessage.send(resident, CivColor.Yellow+"Eviction in "+resident.getDaysTilEvict()+" days.");
		}
		
		CivMessage.send(sender, CivColor.Green+"Groups: "+resident.getGroupsString());
		
		String alts = "";
		if (resident.getAlts() != null) {
			for (String s : resident.getAlts()) {
				AccountLogger al = CivGlobal.getAccount(s);
				if (al == null) {
					alts += "null?"; continue;
				}
				if (!alts.contains(al.getOfflinePlayer().getName()))
					alts += al.getOfflinePlayer().getName()+", ";
			}
		}
		
		if (alts != "") {
			CivMessage.send(sender, CivColor.LightBlueBold+" -- Possible Alt Accounts -- ");
			CivMessage.send(sender, CivColor.Yellow+alts);
		} else {
			CivMessage.send(sender, CivColor.LightBlueBold+" -- Possible Alt Accounts -- ");
			CivMessage.send(sender, CivColor.Yellow+" None ");
		}
		
		CivMessage.send(sender, CivColor.LightBlueBold+" -- Previous Usernames -- ");
		CivMessage.send(sender, CivColor.LightBlueBold+"   Check with "+CivColor.Gray+"/res prevnames (name)");
		
/*		try {
			OfflinePlayer p = CivGlobal.getOfflinePlayer(resident);
			if (p.isOnline()) {
				CivMessage.send(sender, CivColor.LightGreen+"Currently Online and validated.");
			} else {
				CivMessage.send(sender, CivColor.Gray+"Resident is not currently online.");
			}
		} catch (CivException e) {
			CivMessage.send(sender, CivColor.Rose+"Invalid player...? Contact an admin.");
			e.printStackTrace();
		}*/
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
		//info_cmd();
		//CivMessage.send(sender, CivColor.Gray+"Subcommands available: See /resident help");
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}
	
	@Override
	public void permissionCheck() throws CivException {
	}
}
