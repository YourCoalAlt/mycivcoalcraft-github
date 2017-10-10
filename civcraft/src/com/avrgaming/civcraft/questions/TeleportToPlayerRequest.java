package com.avrgaming.civcraft.questions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class TeleportToPlayerRequest implements QuestionResponseInterface {
	
	public Resident tpist;
	public Player tper;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			CivMessage.send(tper, CivColor.LightGray+tpist.getName()+" accepted our teleport request.");
			try {
				Player p = CivGlobal.getPlayer(tpist);
				
				boolean canTP = false;
				ItemStack stack = tper.getInventory().getItemInMainHand();
				LoreCraftableMaterial cmat = LoreCraftableMaterial.getCraftMaterial(stack);
				if (cmat != null && cmat.getConfigId().equals("civ_teleport_to_player")) {
					canTP = true;
					if (stack.getAmount() <= 1) tper.getInventory().removeItem(stack);
					else {
						ItemStack ns = LoreMaterial.spawn(LoreMaterial.materialMap.get(cmat.getConfigId()), stack.getAmount()-1);
						tper.getInventory().removeItem(stack);
						tper.getInventory().addItem(ns);
					}
				}
				
				if (canTP) tper.teleport(p);
				else {
					CivMessage.sendError(tper, "Cannot teleport to "+tpist.getName()+", you do not have a teleport item on you.");
					CivMessage.sendError(tpist, "Cannot teleport "+tper.getName()+" to you, they do not have a teleport item on them.");
				}
			} catch (CivException e) {
				e.printStackTrace();
			}
		} else {
			CivMessage.send(tper, CivColor.LightGray+tpist.getName()+" denied our teleport request.");
		}
	}
	
	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}
}
