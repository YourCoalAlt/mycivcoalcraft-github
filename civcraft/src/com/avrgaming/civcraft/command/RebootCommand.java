package com.avrgaming.civcraft.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

public class RebootCommand extends CommandBase {
	
	//TODO Fix flag from thread.sleep, make bukkit task or whatever it's called
	
	@Override
	public void init() {
		command = "/reboot";
		displayName = "Reboot Command";
		sendUnknownToDefault = true;
		
		commands.put("alert", "Restarts the server in 10 seconds.");
		commands.put("quick", "Restarts the server in 30 seconds.");
		commands.put("fast", "Restarts the server in 1 minute.");
		commands.put("medium", "Restarts the server in 2.5 minutes.");
		commands.put("slow", "Restarts the server in 5 minutes.");
		commands.put("snail", "Restarts the server in 10 minutes.");
		commands.put("warn", "Sends a warning message a reboot is in the future, but does not begin the reboot task.");
	}
	
	public void warn_cmd() throws CivException, InterruptedException {
		prepare();
		CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" The server will be rebooting shortly, no known time.");
	}
	
	public void snail_cmd() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					prepare();
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 10 minutes!");
					Thread.sleep(150000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 7 minutes 30 seconds!");
					Thread.sleep(150000);
					RebootCommand.slow_cmd();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static void slow_cmd() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					prepare();
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 5 minutes!");
					Thread.sleep(60000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 4 minutes!");
					Thread.sleep(60000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 3 minutes!");
					Thread.sleep(60000);
					RebootCommand.medium_cmd();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static void medium_cmd() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					prepare();
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 2 minutes 30 seconds!");
					Thread.sleep(30000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 2 minutes!");
					Thread.sleep(30000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 1 minutes 30 seconds!");
					Thread.sleep(30000);
					RebootCommand.fast_cmd();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static void fast_cmd() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					prepare(); set_restart();
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 1 minute!");
					Thread.sleep(30000);
					RebootCommand.quick_cmd();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static void quick_cmd() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					prepare(); set_restart();
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 30 seconds!");
					Thread.sleep(20000);
					RebootCommand.alert_cmd();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static void alert_cmd() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					prepare(); set_restart();
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 10 seconds!");
					Thread.sleep(5000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 5 seconds!");
					Thread.sleep(2000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 3 seconds!");
					Thread.sleep(1000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 2 seconds!");
					Thread.sleep(1000);
					CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server restarting in 1 second!");
					Thread.sleep(2000);
					final_restart();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private static boolean prepare = false;
	private static void prepare() {
		if (!prepare) {
			prepare = true;
			CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server is preparing for a reboot!");
		}
	}
	private static boolean restart = false;
	private static void set_restart() {
		if (!restart) {
			restart = true;
			CivCraft.isRestarting = true;
			CivMessage.sendAll(CivColor.RedBold+"[RESTART] "+CivCraft.server_name+" Server is locked from being joinable!");
		}
	}
	private static void final_restart() throws InterruptedException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					new BukkitRunnable() {
						@Override
						public void run() {
							for (Player o : Bukkit.getOnlinePlayers()) {
								o.kickPlayer(" §b§l« CivilizationCraft »"+"\n"+
										" "+"\n"+
										"§c§lKicked By §r§8»§ §d§o"+"CONSOLE"+"\n"+
										"§c§lReason §r§8»§ §f"+"Server Locked -- Rebooting"+"\n"+
										" "+"\n"+
										" "+"\n"+
										"§7§o[Please wait to re-join the server.]"+"\n");
							}
						}
					}.runTask(CivCraft.getPlugin());
					Thread.sleep(2000);
					Bukkit.spigot().restart();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}
	
	@Override
	public void showHelp() {
		showBasicHelp();
	}
	
	public void showBasicHelp() {
		CivMessage.sendHeading(sender, displayName+" Command Help");
		for (String c : commands.keySet()) {
			String info = commands.get(c);
			info = info.replace("[", CivColor.Yellow+"[");
			info = info.replace("]", "]"+CivColor.Gray);
			info = info.replace("(", CivColor.Yellow+"(");
			info = info.replace(")", ")"+CivColor.Gray);	
			CivMessage.send(sender, CivColor.LightPurple+command+" "+c+CivColor.Gray+" "+info);
		}
	}
	
	@Override
	public void permissionCheck() throws CivException {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (!p.isOp() && !CivPerms.isReload(p)) {
				CivPerms.validReload(p);
			}
		}
	}
}
