package com.avrgaming.civcraft.items.units;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMission;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.MissionLogger;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.BookUtil;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public class Spy extends UnitMaterial {
	
	public Spy(String id, ConfigUnit configUnit) {
		super(id, configUnit);
	}
	
	public static void spawn(Inventory inv, Town town) throws CivException {
		ItemStack is = LoreMaterial.spawn(Unit.SPY_UNIT);
		UnitMaterial.setOwningTown(town, is);
		AttributeUtil at = new AttributeUtil(is);
		ConfigUnit u = Unit.SPY_UNIT.getUnit();
		for (String str : u.description) {
			at.addLore(CivColor.colorize(str));
		}
		is = at.getStack();
		
		if (!Unit.addItemNoStack(inv, is)) {
			throw new CivException("Cannot make "+Unit.SPY_UNIT.getUnit().name+". Barracks chest is full! Make Room!");
		}
	}
	
	public static void spawn(Inventory inv) throws CivException {
		ItemStack is = LoreMaterial.spawn(Unit.SPY_UNIT);
		AttributeUtil at = new AttributeUtil(is);
		ConfigUnit u = Unit.SPY_UNIT.getUnit();
		for (String str : u.description) {
			at.addLore(CivColor.colorize(str));
		}
		is = at.getStack();
		
		if (!Unit.addItemNoStack(inv, is)) {
			throw new CivException("Cannot make "+Unit.SPY_UNIT.getUnit().name+". Barracks chest is full! Make Room!");
		}
	}
	
	@Override
	public void onInteract(PlayerInteractEvent event) {
		event.setCancelled(true);
		Player p = event.getPlayer();
		Resident res = CivGlobal.getResident(p);
		Inventory inv = Bukkit.createInventory(null, 9*4, "Spy Mission Menu");
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Spy Unit Mission Menu. All",
				CivColor.RESET+"available spy missions you can perform will",
				CivColor.RESET+"be listed in here. Remember, spying can be",
				CivColor.RESET+"dangerous so be careful!",
				CivColor.RESET+""
				));
		
		for (ConfigMission m : CivSettings.missions.values()) {
			if (!res.hasTown()) {
				ItemStack is = LoreGuiItem.build(m.name, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must belong to a town to spy.");
				inv.setItem(m.position, is);
			//} else if (UnitMaterial.getOwningTown(event.getItem()).getCiv() != res.getTown().getCiv() || UnitMaterial.getOwningTown(event.getItem()) == null) {
			//	ItemStack is = LoreGuiItem.build(m.name, ItemManager.getId(Material.BEDROCK), 0, CivColor.Rose+"Must be spy unit from your civ.");
			//	inv.setItem(m.position, is);
			} else {
				String out = "";
				boolean can = true;
				
				for (String str : m.description) {
					out += str+";";
				}
				
				if (res.getTreasury().hasEnough(m.cost)) {
					out += CivColor.Green+"Cost: "+CivColor.LightGreen+m.cost+" Coins;";
				} else {
					can = false;
					out += CivColor.Red+"Cost: "+CivColor.Rose+m.cost+" Coins;";
				}
				
				out += CivColor.Green+"Range: "+CivColor.LightGreen+m.range+" Blocks;";
				out += CivColor.Green+"Spy Location:;";
				ChunkCoord coord = new ChunkCoord(event.getPlayer().getLocation());
				CultureChunk cc = CivGlobal.getCultureChunk(coord);
				Town target = cc.getTown();
				if (cc == null || cc.getCiv() == res.getCiv()) {
					can = false;
					out += CivColor.Rose+"    Cannot spy on yourself.;";
				} else if ((cc != null && cc.getCiv().isAdminCiv())) {
					can = false;
					out += CivColor.Rose+"    Cannot spy on admin civ.;";
				} else {
					out += CivColor.Green+"    Spying on Town "+CivColor.LightGreen+target.getName();
				}
				
				
				DecimalFormat df = new DecimalFormat();
				
				double failChance = MissionBook.getMissionFailChance(m, target);
				String successChance = df.format((1 - failChance)*100)+"%;";
				out += CivColor.Green+"Chance of Success: "+CivColor.LightGreen+successChance;
				
				double compChance = MissionBook.getMissionCompromiseChance(m, target);
				String compromiseChance = df.format(compChance)+"%;";
				out += CivColor.Green+"Chance of Compromise: "+CivColor.LightGreen+compromiseChance;
				
				String length = "";
				int mins = m.length / 60;
				int seconds = m.length % 60;
				if (mins > 0) {
					length += mins+" min";
					if (seconds > 0) {
						length += " ";
					}
				}
				
				if (seconds > 0) {
					length += seconds+" sec";
				}
				out += CivColor.Green+"Time of Completion: "+CivColor.LightGreen+length+";";
				
				if (can) {
					out += CivColor.Gold+"<Click To Perform Mission>";
				} else {
					out += CivColor.LightGrayItalic+"<Cannot Execute Mission>";
				}
				
				ItemStack si = LoreGuiItem.build(m.name, m.itemId, m.itemData, out.split(";"));
				//si = LoreGuiItem.setAction(si, "_BuildChooseStructureTemplate");
				//si = LoreGuiItem.setActionData(si, "info", m.id);
				inv.setItem(m.position, si);
			}
		}
		p.openInventory(inv);
	}
	
	@Override
	public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {
		Player player = (Player)event.getEntity();
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			return;
		}
		
		ArrayList<String> bookout = MissionLogger.getMissionLogs(resident.getTown());
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) book.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Mission Report");
		meta.setAuthor("Mission Reports");
		meta.setTitle("Missions From "+resident.getTown().getName());
		
		String out = "";
		for (String str : bookout) {
			out += str+"\n";
		}
		BookUtil.paginate(meta, out);
		meta.setLore(lore);
		book.setItemMeta(meta);
		player.getWorld().dropItem(player.getLocation(), book);
	}
}
