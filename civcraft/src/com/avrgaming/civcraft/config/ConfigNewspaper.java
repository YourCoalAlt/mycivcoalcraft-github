package com.avrgaming.civcraft.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigNewspaper {
	
	public Integer id;
	public Integer itemId;
	public Integer itemData;
	public Integer guiData;
	public String date;
	public String headline;
	public String publisher;
	public Map<String, String> breakingNews;
	public Map<String, String> gamedevNews;
	public Map<String, String> generalNews;
	
	public ConfigNewspaper() {
	}
	
	public ConfigNewspaper(ConfigNewspaper lvl) {
		this.id = lvl.id;
		this.itemId = lvl.itemId;
		this.itemData = lvl.itemData;
		this.guiData = lvl.guiData;
		this.date = lvl.date;
		this.headline = lvl.headline;
		this.publisher = lvl.publisher;
		
		this.breakingNews = new HashMap<String, String>();
		for (Entry<String, String> entry : lvl.breakingNews.entrySet()) {
			this.breakingNews.put(entry.getKey(), entry.getValue());
		}
		
		this.gamedevNews = new HashMap<String, String>();
		for (Entry<String, String> entry : lvl.gamedevNews.entrySet()) {
			this.gamedevNews.put(entry.getKey(), entry.getValue());
		}
		
		this.generalNews = new HashMap<String, String>();
		for (Entry<String, String> entry : lvl.generalNews.entrySet()) {
			this.generalNews.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigNewspaper> newspapers) {
		newspapers.clear();
		List<Map<?, ?>> newspaper_list = cfg.getMapList("newspapers");
		Map<String, String> breakingNews_list = null;
		Map<String, String> gamedevNews_list = null;
		Map<String, String> generalNews_list = null;
		for (Map<?,?> cl : newspaper_list) {
			List<?> breakingNews = (List<?>)cl.get("breakingNews");
			if (breakingNews != null) {
				breakingNews_list = new HashMap<String, String>();
				for (int i = 0; i < breakingNews.size(); i++) {
					String line = (String) breakingNews.get(i);
					String split[];
					split = line.split(",");
					breakingNews_list.put(String.valueOf(split[0]), String.valueOf(split[1]));
				}
			}
			
			List<?> gamedevNews = (List<?>)cl.get("gamedevNews");
			if (gamedevNews != null) {
				gamedevNews_list = new HashMap<String, String>();
				for (int i = 0; i < gamedevNews.size(); i++) {
					String line = (String) gamedevNews.get(i);
					String split[];
					split = line.split(",");
					gamedevNews_list.put(String.valueOf(split[0]), String.valueOf(split[1]));
				}
			}
			
			List<?> generalNews = (List<?>)cl.get("generalNews");
			if (generalNews != null) {
				generalNews_list = new HashMap<String, String>();
				for (int i = 0; i < generalNews.size(); i++) {
					String line = (String) generalNews.get(i);
					String split[];
					split = line.split(",");
					
//					String getWords = split[1].replaceAll("_", " ");
					generalNews_list.put(String.valueOf(split[0]), String.valueOf(split[1]));
				}
			}
			
			ConfigNewspaper newspaper = new ConfigNewspaper();
			newspaper.id = (Integer)cl.get("id");
			newspaper.itemId = (Integer)cl.get("itemId");
			newspaper.itemData = (Integer)cl.get("itemData");
			newspaper.guiData = (Integer)cl.get("guiData");
			newspaper.date = (String)cl.get("date");
			newspaper.headline = (String)cl.get("headline");
			newspaper.publisher = (String)cl.get("publisher");
			newspaper.breakingNews = breakingNews_list;
			newspaper.gamedevNews = gamedevNews_list;
			newspaper.generalNews = generalNews_list;
			newspapers.put(newspaper.id, newspaper);
		}
		CivLog.info("Loaded "+newspapers.size()+" Newspapers.");		
	}
}
