package com.avrgaming.civcraft.loregui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.backpack.Backpack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigNewspaper;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class NewspaperInventory implements GuiAction {
	
	static Inventory guiInventory;
	
	public String[] messages(String... messages) {
		return messages;
	}
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		guiInventory = Bukkit.getServer().createInventory(p,9*3, "Daily News");
		
		for (int i = 0; i < 27; i++) {
			ItemStack is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		for (ConfigNewspaper news : CivSettings.newspapers.values()) {
			List<String> bnl = new ArrayList<>();
			for (String line : news.breakingNews.keySet()) {
				if (news.breakingNews.get("1").contains("noreport")) {
					bnl.add(CivColor.LightGray+"No Reports ;");
					continue;
				}
				String ln = news.breakingNews.get(line)+" ;";
				bnl.add(ln);
			}
			
			List<String> gdl = new ArrayList<>();
			for (String line : news.gamedevNews.keySet()) {
				if (news.gamedevNews.get("1").contains("noreport")) {
					bnl.add(CivColor.LightGray+"No Reports ;");
					continue;
				}
				String ln = news.gamedevNews.get(line)+" ;";
				gdl.add(ln);
			}
			
			List<String> gl = new ArrayList<>();
			for (String line : news.generalNews.keySet()) {
				if (news.generalNews.get("1").contains("noreport")) {
					bnl.add(CivColor.LightGray+"No Reports ;");
					continue;
				}
				String ln = news.generalNews.get(line)+" ;";
				gl.add(ln);
			}
			
			
			String loreReq = "    "+CivColor.LightPurple+"["+news.date+" by "+news.publisher+"] ;";
			loreReq += " ;";
			loreReq += CivColor.LightBlueBold+"Breaking News: ;";
			for (String s : bnl) {
				loreReq += "  "+CivColor.White+s;
			}
			loreReq += " ;";
			loreReq += CivColor.RoseBold+"Game Dev News: ;";
			for (String s : gdl) {
				loreReq += "  "+CivColor.White+s;
			}
			loreReq += " ;";
			loreReq += CivColor.LightGreenBold+"General News: ;";
			for (String s : gl) {
				loreReq += "  "+CivColor.White+s;
			}
			
//			CivMessage.send(p, loreReq.split(";"));
			
			ItemStack is;
			is = LoreGuiItem.build(CivColor.BOLD+news.headline, news.itemId, news.itemData, loreReq.split(";"));
			guiInventory.setItem(news.guiData, is);
		}
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Topics");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", Backpack.guiInventory.getName());
		guiInventory.setItem((9*3)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}