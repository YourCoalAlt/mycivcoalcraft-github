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
package com.avrgaming.civcraft.command;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;

public class EconCommand extends CommandBase {

	@Override
	public void init() {
		command = "/econ";
		displayName = "Econ";
		
		commands.put("add", "[player] [amount] - add money to this player.");
		commands.put("set", "[player] [amount] - set money for this player.");
		commands.put("sub", "[player] [amount] - subtract money for this player.");
		
		commands.put("addt", "[town] [amount] - add money to this town.");
		commands.put("sett", "[town] [amount] - set money for this town.");
		commands.put("subt", "[town] [amount] - subtract money for this town.");
		
		commands.put("addc", "[civ] [amount] - add money to this civ.");
		commands.put("setc", "[civ] [amount] - set money for this civ.");
		commands.put("subc", "[civ] [amount] - subtract money for this civ.");
		
		commands.put("setdebt", "[player] [amount] - sets the debt on this player to this amount.");
		commands.put("setdebttown", "[town] [amount]");
		commands.put("setdebtciv", "[civ] [amount]");
		
		commands.put("clearalldebt", "Clears all debt for everyone in the server. Residents, Towns, Civs");
		
	}
	
	public void add_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount.");
		}
		
		Resident res = getNamedResident(1);
		Double amt = getNamedDouble(2);
		res.getTreasury().deposit(amt);
		CivMessage.sendSuccess(sender, "Added "+amt+" to res "+res.getName());
	}
	
	public void set_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount.");
		}
		
		Resident res = getNamedResident(1);
		Double amt = getNamedDouble(2);
		res.getTreasury().setBalance(amt);
		CivMessage.sendSuccess(sender, "Set "+amt+" to res "+res.getName());
	}
	
	public void sub_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount");
		}
		
		Resident res = getNamedResident(1);
		Double amt = getNamedDouble(2);
		res.getTreasury().withdraw(amt);
		CivMessage.sendSuccess(sender, "Withdrew "+amt+" to res "+res.getName());
	}
	
	public void addt_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount.");
		}
		
		Town t = getNamedTown(1);
		Double amt = getNamedDouble(2);
		t.getTreasury().deposit(amt);
		CivMessage.sendSuccess(sender, "Added "+amt+" to town "+t.getName());
	}
	
	public void sett_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount.");
		}
		
		Town t = getNamedTown(1);
		Double amt = getNamedDouble(2);
		t.getTreasury().setBalance(amt);
		CivMessage.sendSuccess(sender, "Set "+amt+" to town "+t.getName());
	}
	
	public void subt_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount");
		}
		
		Town t = getNamedTown(1);
		Double amt = getNamedDouble(2);
		t.getTreasury().withdraw(amt);
		CivMessage.sendSuccess(sender, "Withdrew "+amt+" to town "+t.getName());
	}
	
	public void addc_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount.");
		}
		
		Civilization c = getNamedCiv(1);
		Double amt = getNamedDouble(2);
		c.getTreasury().deposit(amt);
		CivMessage.sendSuccess(sender, "Added "+amt+" to civ "+c.getName());
	}
	
	public void setc_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount.");
		}
		
		Civilization c = getNamedCiv(1);
		Double amt = getNamedDouble(2);
		c.getTreasury().setBalance(amt);
		CivMessage.sendSuccess(sender, "Set "+amt+" to civ "+c.getName());
	}
	
	public void subc_cmd() throws CivException {
		validEcon();
		if (args.length < 3) {
			throw new CivException("Provide name and amount");
		}
		
		Civilization c = getNamedCiv(1);
		Double amt = getNamedDouble(2);
		c.getTreasury().withdraw(amt);
		CivMessage.sendSuccess(sender, "Withdrew "+amt+" to civ "+c.getName());
	}
	
	
	public void clearalldebt_cmd() throws CivException {
		validEcon();
		
		for (Civilization civ : CivGlobal.getCivs()) {
			civ.getTreasury().setDebt(0);
			try {
				civ.saveNow();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		for (Town town : CivGlobal.getTowns()) {
			town.getTreasury().setDebt(0);
			try {
				town.saveNow();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		for (Resident res : CivGlobal.getResidents()) {
			res.getTreasury().setDebt(0);
			try {
				res.saveNow();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		CivMessage.send(sender, "Cleared all debt.");
	}
	
	public void setdebtciv_cmd() throws CivException {
		validEcon();
		
		Civilization civ = getNamedCiv(1);
		Double amount = getNamedDouble(2);
		civ.getTreasury().setDebt(amount);
		civ.save();
		CivMessage.sendSuccess(sender, "Set.");
	}
	
	public void setdebttown_cmd() throws CivException {
		validEcon();
		
		Town town = getNamedTown(1);
		Double amount = getNamedDouble(2);
		town.getTreasury().setDebt(amount);
		town.save();
		CivMessage.sendSuccess(sender, "Set.");
	}
	
	public void setdebt_cmd() throws CivException {
		validEcon();
		
		Resident resident = getNamedResident(1);
		Double amount = getNamedDouble(2);
		resident.getTreasury().setDebt(amount);
		resident.save();
		CivMessage.sendSuccess(sender, "Set.");
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		Player player = getPlayer();
		Resident resident = CivGlobal.getResident(player);
		
		if (resident == null) return;
		CivMessage.sendSuccess(player, resident.getTreasury().getBalance()+" Coins");
	}
	
	@Override
	public void showHelp() {
		Player player;
		try {
			player = getPlayer();
		} catch (CivException e) {
			e.printStackTrace();
			return;
		}
		
		if (!CivPerms.isEcon(player)) return;
		
		showBasicHelp();
	}
	
	@Override
	public void permissionCheck() throws CivException {
	}
	
	private void validEcon() throws CivException {
		if (sender instanceof Player) {
			CivPerms.validAdmin((Player)sender);
		} else if (!sender.isOp()) {
			throw new CivException("Only OP can use this command.");			
		}
	}
	
}