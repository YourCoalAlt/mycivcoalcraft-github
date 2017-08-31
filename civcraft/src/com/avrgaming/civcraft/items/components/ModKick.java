package com.avrgaming.civcraft.items.components;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.PlayerModerationKick;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class ModKick extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(CivColor.LightPurple+"[Staff Tool]");
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {
		if (!event.getPlayer().hasPermission(CivSettings.MINI_ADMIN)) {
			CivMessage.send(event.getPlayer(), CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"The item you are holding is a staff-only tool. Please dispose of it or you can get in trouble!");
		}
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		if (event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			if (!p.hasPermission(CivSettings.MINI_ADMIN)) {
				CivMessage.send(p, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"The item you are holding is a staff-only tool. Please dispose of it or you can get in trouble!");
				return;
			}
			
			if (!(event.getEntity() instanceof Player)) {
				return;
			}
			
			Player d = (Player) event.getEntity();
			TaskMaster.syncTask(new PlayerModerationKick(d.getName(), p.getName(), "You have been kicked via Admin Weapondry."));
			CivMessage.globalModerator(CivColor.RoseBold+"[Kick] "+CivColor.RESET+"Player "+CivColor.LightGreenItalic+d.getName()+CivColor.RESET+" has been kicked by "+p.getName()+CivColor.RESET+
					". Reason:"+CivColor.LightGreenItalic+"You have been kicked via Admin Weapondry.");
			event.setCancelled(true);
		}
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRangedAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		CivMessage.global("test");
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player p = (Player) arrow.getShooter();
				if (!p.hasPermission(CivSettings.MINI_ADMIN)) {
					CivMessage.send(p, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"The item you are holding is a staff-only tool. Please dispose of it or you can get in trouble!");
					return;
				}
				
				if (!(event.getEntity() instanceof Player)) {
					CivMessage.sendError(p, "Entity shot was not a player - "+event.getEntity().getName());
					return;
				}
				
				Player d = (Player) event.getEntity();
				TaskMaster.syncTask(new PlayerModerationKick(d.getName(), p.getName(), "You have been kicked via Admin Weapondry."));
				CivMessage.globalModerator(CivColor.RoseBold+"[Kick] "+CivColor.RESET+"Player "+CivColor.LightGreenItalic+d.getName()+CivColor.RESET+" has been kicked by "+p.getName()+CivColor.RESET+
						". Reason:"+CivColor.LightGreenItalic+"You have been kicked via Admin Weapondry.");
				event.setCancelled(true);
			}
		}
	}
}
