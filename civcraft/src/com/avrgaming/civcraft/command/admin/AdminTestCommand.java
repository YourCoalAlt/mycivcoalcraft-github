package com.avrgaming.civcraft.command.admin;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class AdminTestCommand extends CommandBase implements Listener {

	@Override
	public void init() {
		command = "/ad test";
		displayName = "Admin Test [NOTE these are test commands, they might break the game!]";
		
		commands.put("run", "Runs the command.");
		commands.put("mail", "Runs the command.");
		commands.put("dmg", ".");
	}
	
	public void dmg_cmd() throws CivException {
		double damage = getNamedDouble(1);
		double defense = getNamedDouble(2);
		DecimalFormat df = new DecimalFormat("0.00");
		double returnDamage = Double.valueOf(df.format((damage * (1 - ((defense + (damage/2)) - damage / (2)) / ((25+(damage/2.25))-.8687922)))));
		if (damage < 4.8687893) {
			// This number [4.8687893] was calculated on a TI-84 Plus C Silver Edition, so it is 105% correct.
			double fixedDamage = (-(returnDamage-3.4965076))+4.8687893;
			returnDamage = fixedDamage;
		}
		
		double deathHits = Double.valueOf(df.format(20 / returnDamage));
//		double rtndmg = damage * ( 1 - Math.min(20, Math.max( defense / 5, defense - damage / ( toughness / 4 + 2 ) ) ) / 25 );
		//calc 6.5 * (1 - ((0 + 3.25) - 6.5 / (0 / 4 + 2)) / 25)
		//calc 8 * (1 - ((20 + 4) - 8 / (2)) / 25)
		//calc (8 * (1 - ((20 + 4) - 8 / (2)) / ((25+(8/2.25))-.8687922)))
		CivMessage.global("Final Damage: "+returnDamage+" [] Death Hits: "+deathHits);
	}
	
	public void mail_cmd() throws CivException {
		Resident res = getResident();
		Inventory inv = Bukkit.createInventory(null, 9*4);
		inv.addItem(new ItemStack(LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_vanilla_knowledge_book"))));
		inv.addItem(new ItemStack(LoreMaterial.spawn(LoreMaterial.materialMap.get("civ_hopper"))));
		inv.addItem(new ItemStack(Material.COBBLESTONE));
		res.addMail(res, "This is a test mail!", System.currentTimeMillis(), inv);
	}
	
	public void run_cmd() {
		if (!(sender instanceof Player)) {
			CivMessage.sendError(sender, "Only a player can execute this command.");
			return;
		}
		Player p = (Player)sender;
		openGUI(p);
	}
	
	public static void openGUI(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9*3, "Trade Panel");
		p.openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		Player p = (Player) event.getPlayer();
		if (inv.getName().equalsIgnoreCase("Trade Panel")) {
			int toRemove = 0;
			boolean addedNotRequired = false;
			for (ItemStack stack : inv.getContents().clone()) {
				if (stack == null || stack.getType() == Material.AIR) {
					continue;
				}
				
				LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
				if (craftMat != null && craftMat.getConfigId().toLowerCase().equals("civ_hammers")) {
					toRemove += stack.getAmount();
					inv.removeItem(stack);
				} else {
					if (craftMat != null) {
						ItemStack newMat = LoreCraftableMaterial.spawn(craftMat, stack.getAmount());
						p.getWorld().dropItemNaturally(event.getPlayer().getEyeLocation(), newMat);
						addedNotRequired = true;
					} else {
						p.getWorld().dropItemNaturally(event.getPlayer().getEyeLocation(), stack);
						addedNotRequired = true;
					}
				}
			}
			
			if (addedNotRequired == true) {
				CivMessage.send(p, CivColor.GrayItalic+"We dropped non-required items back on the ground.");
			}
			
			int depositAmt = toRemove*5;
			Resident res = CivGlobal.getResident(p);
			res.getTreasury().deposit(depositAmt*5);
			CivMessage.sendSuccess(p, "Deposited "+depositAmt+" to your inventory!");
		}
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
		//Admin is checked in parent command
	}
}
