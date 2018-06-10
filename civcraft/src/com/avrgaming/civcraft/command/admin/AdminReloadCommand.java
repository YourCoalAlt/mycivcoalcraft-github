package com.avrgaming.civcraft.command.admin;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigGovernment;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.util.CivColor;

public class AdminReloadCommand extends CommandBase implements Listener {

	@Override
	public void init() {
		command = "/ad reload";
		displayName = "Admin Reload";
		
		commands.put("govs", "Reloads the governments.yml file");
		commands.put("techs", "Reloads the techs.yml file");
		commands.put("mobs", "Reloads the mobs.yml file");
		commands.put("mats", "Reloads the materials.yml file");
		commands.put("structuredata", "Reloads the structures.yml, wonders.yml, and structuredata.yml file");
		commands.put("newspaper", "Reloads the game.yml file (newspapers only)");
	}
	
	public void newspaper_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadNewspaperConfigFiles();
		CivMessage.send(sender, CivColor.Gold+"Reloaded Newspapers");
		CivMessage.global("Extra Extra! The CivCraft Daily News has updated with a new issue!");
	}
	
	public void mobs_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadMobConfigFiles();
		CivMessage.send(sender, CivColor.Gold+"Reloaded Custom Mobs");
	}
	
	public void govs_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadGovConfigFiles();
		for (Civilization civ : CivGlobal.getCivs()) {
			ConfigGovernment gov = civ.getGovernment();
			civ.setGovernment(gov.id);
		}
		CivMessage.send(sender, CivColor.Gold+"Reloaded Governments");
	}
	
	public void techs_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadTechConfigFiles();
		CivMessage.send(sender, CivColor.Gold+"Reloaded Techs");
	}
	
	public void mats_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadMaterialConfigFiles();
		CivMessage.send(sender, CivColor.Gold+"Reloaded Materials");
	}
	
	public void structuredata_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.reloadStructureConfigFiles();
		CivMessage.send(sender, CivColor.Gold+"Reloaded Structures & Data");
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
