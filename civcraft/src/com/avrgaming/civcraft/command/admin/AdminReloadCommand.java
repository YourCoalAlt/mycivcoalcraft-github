package com.avrgaming.civcraft.command.admin;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.Listener;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.CivColor;

public class AdminReloadCommand extends CommandBase implements Listener {

	@Override
	public void init() {
		command = "/ad reload";
		displayName = "Admin Reload";
		
		commands.put("decformat", "Fixes decimal formats in some SQL saving features. WARNING: Can cause server lag.");
		commands.put("govs", "Reloads the governments.yml file");
		commands.put("structuredata", "Reloads the structuredata.yml file");
		commands.put("newspaper", "Reloads the game.yml file (newspapers only)");
		commands.put("killvillagers", "Removes all villagers spawned by CivCraft. WARNING: Only use if you are rebooting server, or else they don't respawn!");
	}
	
	public void killvillagers_cmd() {
		int chunksSearched = 0;
		for (TownChunk tc : CivGlobal.getTownChunks()) {
			chunksSearched++;
			tc.getChunkCoord().getChunk().load();
			for (World w : Bukkit.getWorlds()) {
				for (Entity e : w.getEntities()) {
					if (e instanceof Villager) {
						e.remove();
					}
				}
			}
		}
		CivMessage.send(sender, CivColor.Gold+"Removed all villagers from "+chunksSearched+" searched chunks.");
	}
	
	public void decformat_cmd() {
		for (ResidentExperience re : CivGlobal.getResidentsExperience()) {
			re.setQuestEXP(re.getQuestEXP());
			re.setMiningEXP(re.getMiningEXP());
			re.setFishingEXP(re.getFishingEXP());
		}
		CivMessage.send(sender, CivColor.Gold+"Reformatted decimals for ResidentExperience profiles.");
	}
	
	public void newspaper_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadNewspaperConfigFiles();
		CivMessage.send(sender, CivColor.Gold+"Reloaded the Newspaper");
		CivMessage.global("Extra Extra! The CivCraft Daily News has updated with a new issue!");
	}
	
	public void govs_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadGovConfigFiles();
		for (Civilization civ : CivGlobal.getCivs()) {
			ConfigGovernment gov = civ.getGovernment();
			civ.setGovernment(gov.id);
		}
		CivMessage.send(sender, CivColor.Gold+"Reloaded Governments");
	}
	
	public void structuredata_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadStructureConfigFiles();
		CivMessage.send(sender, CivColor.Gold+"Reloaded Structure Data");
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
	}
}
