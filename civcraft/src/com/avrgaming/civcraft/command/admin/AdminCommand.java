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
package com.avrgaming.civcraft.command.admin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.command.ReportChestsTask;
import com.avrgaming.civcraft.command.ReportPlayerInventoryTask;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuff;
import com.avrgaming.civcraft.config.ConfigMaterial;
import com.avrgaming.civcraft.config.ConfigMaterialCategory;
import com.avrgaming.civcraft.config.ConfigTradeGood;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.endgame.EndGameCondition;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.items.BonusGoodie;
import com.avrgaming.civcraft.listener.civcraft.HolographicDisplaysListener;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.object.TradeGood;
import com.avrgaming.civcraft.object.camp.Camp;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.PlayerModerationKick;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class AdminCommand extends CommandBase {
	
	private static boolean lockdown = false;
	
	@Override
	public void init() {
		command = "/ad";
		displayName = "Admin";
		
		commands.put("perm", "toggles your permission overrides, if on, ignores all plot permissions.");
		commands.put("sbperm", "Allows breaking of structure blocks");
		commands.put("cbinstantbreak", "Allows instant breaking of control blocks.");

		commands.put("recover", "Manage recovery commands");
		commands.put("server", "shows the name of this server");
		commands.put("spawnunit", "[unit-id] [town] spawn the unit with this id for this town.");

		commands.put("chestreport", "[radius] check in this radius for chests");
		commands.put("playerreport", "shows all player ender chest reports.");
		
		commands.put("civ", "Admin an individual civilization");
		commands.put("town", "Admin a town.");
		commands.put("war", "Manage war settings, turn wars off and on.... etc.");
		commands.put("lag", "Manage lag on the server by disabling expensive tasks.");	
		commands.put("chat", "Manage admin chat options, tc, cc, listen etc");
		commands.put("res", "Manage resident options, settown, etc");
		commands.put("build", "Manage buildings. Demolish/repair wonders etc.");
		commands.put("items", "Opens inventory which allows you to spawn in custom items.");
		commands.put("item", "Does special things to the item in your hand.");
		commands.put("timer", "Manage timers.");
		commands.put("road", "Road management commands");
		commands.put("clearendgame", "[key] [civ] - clears this end game condition for this civ.");
		commands.put("endworld", "Starts the Apocalypse.");
		commands.put("perk", "Admin perk management.");
		
		commands.put("holo", "Reloads all holograms.");
		commands.put("savesql", "Saves all databases to SQL server.");
		commands.put("lockdown", "Toggles if the server is joinable to players or admins only.");
		commands.put("testc", "Unformal, quick test command.");
		
		commands.put("gui", "Admin GUI commands.");
		commands.put("test", "Formal testing commands.");
		commands.put("reload", "Admin reload commands.");
		commands.put("rxp", "Admin Resident Experience commands.");
		commands.put("debug", "Admin Debugging commands.");
		commands.put("mob", "Admin Mob commands.");
		commands.put("player", "Admin Player commands.");
		commands.put("camp", "Admin Camp commands.");
	}
	
	public void camp_cmd() {
		AdminCampCommand cmd = new AdminCampCommand();	
		cmd.onCommand(sender, null, "camp", this.stripArgs(args, 1));
	}
	
	public void player_cmd() {
		AdminPlayerCommand cmd = new AdminPlayerCommand();	
		cmd.onCommand(sender, null, "player", this.stripArgs(args, 1));
	}
	
	public void mob_cmd() {
		AdminMobCommand cmd = new AdminMobCommand();	
		cmd.onCommand(sender, null, "mob", this.stripArgs(args, 1));
	}
	
	public void rxp_cmd() {
		AdminExperienceCommand cmd = new AdminExperienceCommand();	
		cmd.onCommand(sender, null, "rxp", this.stripArgs(args, 1));
	}
	
	public void debug_cmd() {
		AdminDebugCommand cmd = new AdminDebugCommand();	
		cmd.onCommand(sender, null, "debug", this.stripArgs(args, 1));
	}
	
	public void reload_cmd() {
		AdminReloadCommand cmd = new AdminReloadCommand();	
		cmd.onCommand(sender, null, "reload", this.stripArgs(args, 1));
	}
	
	public void test_cmd() {
		AdminTestCommand cmd = new AdminTestCommand();	
		cmd.onCommand(sender, null, "test", this.stripArgs(args, 1));
	}
	
	public void testc_cmd() throws CivException {
		Player p = getPlayer();
		List<String> list = new ArrayList<String>();
		for (ConfigTradeGood good : CivSettings.goods.values()) {
			list.add(good.id);
		}
		
		Random rand = new Random();
		int selector = rand.nextInt(list.size());
		String goodReward = list.get(selector);
		CivMessage.sendSuccess(sender, "Generated good: "+goodReward);
		
		ConfigTradeGood good = null;
		for (ConfigTradeGood goods : CivSettings.goods.values()) {
			if (goodReward == goods.id) {
				good = goods;
			}
		}
		
		if (good != null) {
			ItemStack stack = ItemManager.createItemStack(good.material, 1, (byte)good.material_data);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(good.name);
			
			List<String> lore = new ArrayList<String>();
			lore.add(CivColor.PurpleBold+"Trade Resource");
			lore.add(CivColor.LightGreenBold+"Coins/Hour: "+CivColor.Yellow+good.value);
			
			String[] split = getBonusDisplayString(good).split(";");
			for (String str : split) {
				lore.add(CivColor.Yellow+str);
			}
			
			meta.setLore(lore);
			stack.setItemMeta(meta);
			p.getInventory().addItem(stack);
		}
	}
	
	public String getBonusDisplayString(ConfigTradeGood good) {
		String out = "";
		for (ConfigBuff cBuff : good.buffs.values()) {
			out += CivColor.LightBlue+CivColor.UNDERLINE+cBuff.name;
			out += ";";
			out += CivColor.WhiteItalic+cBuff.description;
			out += ";";
		}
		return out;		
	}
	
	public void savesql_cmd() {
		try {
			for (AccountLogger al : CivGlobal.getAccounts()) al.saveNow();
			for (Camp c : CivGlobal.getCamps()) c.saveNow();
			for (Civilization c : CivGlobal.getCivs()) c.saveNow();
			for (Town t : CivGlobal.getTowns()) t.saveNow();
			for (Resident r : CivGlobal.getResidents()) r.saveNow();
			for (TownChunk tc : CivGlobal.getTownChunks()) tc.saveNow();
			for (Structure s : CivGlobal.getStructures()) s.saveNow();
			for (Wonder w : CivGlobal.getWonders()) w.saveNow();
			for (TradeGood tg : CivGlobal.getTradeGoods()) tg.saveNow();
			for (BonusGoodie bg : CivGlobal.getBonusGoodies()) bg.saveNow();
//			for (StructureSign ss : CivGlobal.getStructureSigns()) ss.saveNow();
			CivMessage.sendSuccess(sender, "Saved All SQL Information.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void holo_cmd() {
		HolographicDisplaysListener.generateTradeGoodHolograms();
		CivMessage.sendSuccess(sender, "Updated holograms.");
	}
	
	public void gui_cmd() {
		AdminGUICommand cmd = new AdminGUICommand();	
		cmd.onCommand(sender, null, "gui", this.stripArgs(args, 1));
	}
	
	public void lockdown_cmd() {
		new Thread(new Runnable() {
			public void run() {
				try {
					setLockdown(!isLockdown());
					if (isLockdown()) {
						CivMessage.global("A staff member triggered a lockdown! Prepared to be kicked in a moment...");
						CivMessage.global("Kicking all players in 3");
						Thread.sleep(1000);
						CivMessage.global("Kicking all players in 2");
						Thread.sleep(1000);
						CivMessage.global("Kicking all players in 1");
						Thread.sleep(1000);
						CivMessage.global("All non-staff have been kicked.");
						for (Player player : Bukkit.getOnlinePlayers()) {
							if (player.isOp() || CivPerms.isMiniAdmin(player)) {
								CivMessage.send(sender, "Skipping "+player.getName()+" since they are OP, or ranked Mini Admin or higher.");
								continue;
							}
							TaskMaster.syncTask(new PlayerModerationKick(player.getName(), sender.getName(), "The server is currently on lockdown... Try again in a few minutes."));
						}
					} else {
						CivMessage.global("Lockdown cleared. All players are now allowed to join again.");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static boolean isLockdown() {
		return lockdown;
	}

	public void setLockdown(boolean ld) {
		lockdown = ld;
	}
	
	public void perk_cmd() {
		AdminPerkCommand cmd = new AdminPerkCommand();	
		cmd.onCommand(sender, null, "perk", this.stripArgs(args, 1));
	}
	
	public void endworld_cmd() {
		CivGlobal.endWorld = !CivGlobal.endWorld;
		if (CivGlobal.endWorld) {			
			CivMessage.sendSuccess(sender, "It's the end of the world as we know it.");
		} else {
			CivMessage.sendSuccess(sender, "I feel fine.");
		}
	}
	
	public void clearendgame_cmd() throws CivException {
		String key = getNamedString(1, "enter key.");
		Civilization civ = getNamedCiv(2);
		
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
		if (entries.size() == 0) {
			throw new CivException("No end games by that key.");
		}
		
		for (SessionEntry entry : entries) {
			if (EndGameCondition.getCivFromSessionData(entry.value) == civ) {
				CivGlobal.getSessionDB().delete(entry.request_id, entry.key);
				CivMessage.sendSuccess(sender, "Deleted for "+civ.getName());
			}
		}		
	}
	
	public void cbinstantbreak_cmd() throws CivException {
		Resident resident = getResident();
		
		resident.setControlBlockInstantBreak(!resident.isControlBlockInstantBreak());
		CivMessage.sendSuccess(sender, "Set control block instant break:"+resident.isControlBlockInstantBreak());
	}
	
	public static Inventory spawnInventory = null; 
	public void items_cmd() throws CivException {
		Player player = getPlayer();
		
		if (spawnInventory == null) {
			spawnInventory = Bukkit.createInventory(player, LoreGuiItem.MAX_INV_SIZE, "Admin Item Spawn");
			
			/* Build the Category Inventory. */
			for (ConfigMaterialCategory cat : ConfigMaterialCategory.getCategories()) {
				ItemStack infoRec = LoreGuiItem.build(cat.name, 
						ItemManager.getId(Material.WRITTEN_BOOK), 
						0, 
						CivColor.LightBlue+cat.materials.size()+" Items",
						CivColor.Gold+"<Click To Open>");
						infoRec = LoreGuiItem.setAction(infoRec, "OpenInventory");
						infoRec = LoreGuiItem.setActionData(infoRec, "invType", "showGuiInv");
						infoRec = LoreGuiItem.setActionData(infoRec, "invName", cat.name+" Spawn");
						spawnInventory.addItem(infoRec);
						
				/* Build a new GUI Inventory. */
				Inventory inv = Bukkit.createInventory(player, LoreGuiItem.MAX_INV_SIZE, cat.name+" Spawn");
				for (ConfigMaterial mat : cat.materials.values()) {
					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(mat.id);
					ItemStack stack = LoreMaterial.spawn(craftMat);
					stack = LoreGuiItem.asGuiItem(stack);
					stack = LoreGuiItem.setAction(stack, "SpawnItem");
					inv.addItem(stack);
					LoreGuiItemListener.guiInventories.put(inv.getName(), inv);			
				}
			}
			

		}
		
		player.openInventory(spawnInventory);
	}
	
	public void road_cmd() {
		AdminRoadCommand cmd = new AdminRoadCommand();	
		cmd.onCommand(sender, null, "camp", this.stripArgs(args, 1));
	}
	
	public void item_cmd() {
		AdminItemCommand cmd = new AdminItemCommand();	
		cmd.onCommand(sender, null, "camp", this.stripArgs(args, 1));
	}
	
	public void timer_cmd() {
		AdminTimerCommand cmd = new AdminTimerCommand();	
		cmd.onCommand(sender, null, "camp", this.stripArgs(args, 1));	
	}
	
	public void playerreport_cmd() {
	
		LinkedList<OfflinePlayer> offplayers = new LinkedList<OfflinePlayer>();
		for (OfflinePlayer offplayer : Bukkit.getOfflinePlayers()) {
			offplayers.add(offplayer);
		}
		
		CivMessage.sendHeading(sender, "Players with Goodies");
		CivMessage.send(sender, "Processing (this may take a while)");
		TaskMaster.syncTask(new ReportPlayerInventoryTask(sender, offplayers), 0);
	}
	
	public void chestreport_cmd() throws CivException {
		Integer radius = getNamedInteger(1);
		Player player = getPlayer();
		
		LinkedList<ChunkCoord> coords = new LinkedList<ChunkCoord>();
		for (int x = -radius; x < radius; x++) {
			for (int z = -radius; z < radius; z++) {
				ChunkCoord coord = new ChunkCoord(player.getLocation());
				coord.setX(coord.getX() + x); coord.setZ(coord.getZ() + z);
				
				coords.add(coord);
			}
		}
		
		CivMessage.sendHeading(sender, "Chests with Goodies");
		CivMessage.send(sender, "Processing (this may take a while)");
		TaskMaster.syncTask(new ReportChestsTask(sender, coords), 0);	
	}
	
	public void spawnunit_cmd() throws CivException {		
		if (args.length < 2) {
			throw new CivException("Enter a unit id.");
		}
		
		ConfigUnit unit = CivSettings.units.get(args[1]);
		if (unit == null) {
			throw new CivException("No unit called "+args[1]);
		}
		
		Player player = getPlayer();
		Town town = getNamedTown(2);
		
//		if (args.length > 2) {
//			try {
//				player = CivGlobal.getPlayer(args[2]);
//			} catch (CivException e) {
//				throw new CivException("Player "+args[2]+" is not online.");
//			}
//		} else {
//			player = getPlayer();
//		}
		
		Class<?> c;
		try {
			c = Class.forName(unit.class_name);
			Method m = c.getMethod("spawn", Inventory.class, Town.class);
			m.invoke(null, player.getInventory(), town);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException 
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new CivException(e.getMessage());
		}

		
		CivMessage.sendSuccess(sender, "Spawned a "+unit.name);
	}
	
	public void server_cmd() {
		CivMessage.send(sender, Bukkit.getServerName());
	}
	
	
	public void recover_cmd() {
		AdminRecoverCommand cmd = new AdminRecoverCommand();	
		cmd.onCommand(sender, null, "recover", this.stripArgs(args, 1));	
	}
	
	public void town_cmd() {
		AdminTownCommand cmd = new AdminTownCommand();	
		cmd.onCommand(sender, null, "town", this.stripArgs(args, 1));
	}
	
	public void civ_cmd() {
		AdminCivCommand cmd = new AdminCivCommand();	
		cmd.onCommand(sender, null, "civ", this.stripArgs(args, 1));
	}

	public void setfullmessage_cmd() {
		if (args.length < 2) {
			CivMessage.send(sender, "Current:"+CivGlobal.fullMessage);
			return;
		}
		
		synchronized(CivGlobal.maxPlayers) {
			CivGlobal.fullMessage = args[1];
		}
		
		CivMessage.sendSuccess(sender, "Set to:"+args[1]);
		
	}
	
	public void res_cmd() {
		AdminResCommand cmd = new AdminResCommand();	
		cmd.onCommand(sender, null, "war", this.stripArgs(args, 1));	}
	
	public void chat_cmd() {
		AdminChatCommand cmd = new AdminChatCommand();	
		cmd.onCommand(sender, null, "war", this.stripArgs(args, 1));
	}

	public void war_cmd() {
		AdminWarCommand cmd = new AdminWarCommand();	
		cmd.onCommand(sender, null, "war", this.stripArgs(args, 1));
	}
	
	public void lag_cmd() {
		AdminLagCommand cmd = new AdminLagCommand();	
		cmd.onCommand(sender, null, "war", this.stripArgs(args, 1));
	}
	
	public void build_cmd() {
		AdminBuildCommand cmd = new AdminBuildCommand();	
		cmd.onCommand(sender, null, "war", this.stripArgs(args, 1));
	}
	
	public void perm_cmd() throws CivException {
		Resident resident = getResident();
		
		if (resident.isPermOverride()) {
			resident.setPermOverride(false);
			CivMessage.sendSuccess(sender, "Permission override off.");
			return;
		}
		
		resident.setPermOverride(true);
		CivMessage.sendSuccess(sender, "Permission override on.");
		
	}
	
	public void sbperm_cmd() throws CivException {
		Resident resident = getResident();
		if (resident.isSBPermOverride()) {
			resident.setSBPermOverride(false);
			CivMessage.sendSuccess(sender, "Structure Permission override off.");
			return;
		}
		
		resident.setSBPermOverride(true);
		CivMessage.sendSuccess(sender, "Structure Permission override on.");
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
		if (sender instanceof Player) {
			CivPerms.validAdmin((Player)sender);
		} else if (!sender.isOp()) {
			throw new CivException("Only OP can use this command.");			
		}
	}

	@Override
	public void doLogging() {
		CivLog.adminlog(sender.getName(), "/ad "+this.combineArgs(args));
	}
}
