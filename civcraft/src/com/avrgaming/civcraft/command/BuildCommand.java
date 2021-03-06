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
package com.avrgaming.civcraft.command;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuildableInfo;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.Farm;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;

public class BuildCommand extends CommandBase {

	@Override
	public void init() {
		command = "/build";
		displayName = "Build";
		sendUnknownToDefault = true;
		
		commands.put("list", "shows all available structures.");
		commands.put("progress", "Shows progress of currently building structures.");
		commands.put("repairnearest", "Repairs destroyed structures.");
		commands.put("undo", "Undo the last structure built.");
		commands.put("demolish", "[location] - destroys the structure at this location.");
		commands.put("demolishnearest", "- destroys the nearest structure. Requires confirmation.");
		commands.put("refreshnearest", "Refreshes the nearest structure's blocks. Requires confirmation.");
		commands.put("validatenearest", "Validates the nearest structure. Removing any validation penalties if it's ok.");
		commands.put("supportnearest", "Supports the nearest structure. Places dirt if block is not valid support.");
		//commands.put("preview", "shows a preview of this structure at this location.");
	}
	
	public void progress_cmd() throws CivException, IOException {
		Town town = this.getSelectedTown();
		if (town.build_tasks.size() == 0 || town.build_tasks.isEmpty()) {
			throw new CivException("The town is currently not building a structure.");
		}
		
		CivMessage.sendHeading(sender, "Building Structures");
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy H:mm:ss z");
		for (int i = 0; i < town.build_tasks.size(); i++) {
			Buildable b = town.build_tasks.get(i).buildable;
			
			DecimalFormat df = new DecimalFormat("#.##");
			double townHammers = b.getTown().getHammers().total;
			double totalCost = b.getHammerCost();
			double totalBlocks = b.getTotalBlockCount();
			double builtHammers = b.getBuiltHammers();
			double builtPercent = (builtHammers/totalCost)*100;
			
			double totalTime = ((totalCost/(townHammers+Math.pow(totalBlocks/totalCost, 2.987654321))/1024)*60*60*1000);
			double totalBuilt = ((builtHammers/(townHammers+Math.pow(totalBlocks/builtHammers, 2.987654321))/1024)*60*60*1000);
			double timeLeft = -(totalBuilt-totalTime);
			long endTime = (long)(System.currentTimeMillis() + (1000 * timeLeft));
			
			String time = "";
		    int hours = (int) (timeLeft / 3600);
		    int remainder = (int) (timeLeft - (hours * 3600));
		    int mins = remainder / 60;
		    int secs = remainder - (mins * 60);
		    int days = hours / 24;
		    if (days > 0) {
		    	time += days+" dy, ";
		    	hours -= days * 24;
		    }
			if (hours > 0) time += hours+" hr, ";
			if (mins > 0) time += mins+" min, ";
			if (secs > 0) time += secs+" sec";
			
			CivMessage.send(sender, CivColor.GoldBold+(i+1)+". "+CivColor.LightGreenBold+b.getDisplayName()+" "+CivColor.Purple+
						df.format(builtPercent)+"% "+CivColor.LightPurpleItalic+"("+df.format(builtHammers) + "/"+totalCost+") "+
						CivColor.Gold+" Blocks: "+CivColor.YellowItalic+b.builtBlockCount+"/"+b.getTotalBlockCount());
			CivMessage.send(sender, CivColor.Green+"  Completion: "+CivColor.LightGreen+sdf.format(endTime)+
						CivColor.DarkGrayItalic+" ["+CivColor.LightBlueItalic+time+CivColor.DarkGrayItalic+"]");
		}
	}
	
	public void supportnearest_cmd() throws CivException, IOException {
		Player player = getPlayer();
		Resident resident = getResident();
		Buildable buildable = CivGlobal.getNearestBuildable(player.getLocation());
		
		if (buildable.getTown() != resident.getTown()) {
			throw new CivException("You can only support structures inside your own town.");
		}
		
		if (War.isWarTime()) {
			throw new CivException("Cannot support structures during WarTime.");
		}
		
		if (buildable.isIgnoreFloating() || buildable instanceof Farm) {
			throw new CivException(buildable.getDisplayName()+" is exempt from floating structure checks.");
		}
		
		CivMessage.sendSuccess(player, "Running support on "+buildable.getDisplayName()+" at "+buildable.getCenterLocation()+"...");
		buildable.getTown().buildSupport(buildable);
	}
	
	public void validatenearest_cmd() throws CivException {
		Player player = getPlayer();
		Resident resident = getResident();
		Buildable buildable = CivGlobal.getNearestBuildable(player.getLocation());
		
		if (buildable.getTown() != resident.getTown()) {
			throw new CivException("You can only validate structures inside your own town.");
		}
		
		if (War.isWarTime()) {
			throw new CivException("Cannot validate structures during WarTime.");
		}
		
		if (buildable.isIgnoreFloating()) {
			throw new CivException(buildable.getDisplayName()+" is exempt from floating structure checks.");
		}
		
		CivMessage.sendSuccess(player, "Running Validation on "+buildable.getDisplayName()+" at "+buildable.getCenterLocation()+"...");
		buildable.validate(player);
	}
	
	public void refreshnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Resident resident = getResident();
		town.refreshNearestBuildable(resident);
	}
	
	public void repairnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Player player = getPlayer();
		
		if (War.isWarTime()) {
			throw new CivException("You cannot repair structures during WarTime.");
		}
		
		Structure nearest = town.getNearestStrucutre(player.getLocation());
			
		if (nearest == null) {
			throw new CivException ("Couldn't find a structure.");
		}
		
		if (!nearest.isDestroyed()) {
			throw new CivException (nearest.getDisplayName()+" at "+nearest.getCorner()+" is not destroyed.");
		}
		
		if (!town.getCiv().hasTechnology(nearest.getRequiredTechnology())) {
			throw new CivException ("You do not have the technology to repair "+nearest.getDisplayName()+" at "+nearest.getCorner());
		}
	
		if (args.length < 2 || !args[1].equalsIgnoreCase("yes")) {
			CivMessage.send(player, CivColor.LightGreen+"Are you sure you want to repair the structure "+CivColor.Yellow+nearest.getDisplayName()+
					CivColor.LightGreen+" at "+CivColor.Yellow+nearest.getCorner()+CivColor.LightGreen+" for "+CivColor.Yellow+nearest.getRepairCost()+" coins?");
			CivMessage.send(player, CivColor.Gray+"If yes, use /build repairnearest yes");
			return;
		}
		
		town.repairStructure(nearest);		
		CivMessage.sendSuccess(player, nearest.getDisplayName()+" repaired.");
	}
	
	public void demolishnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Player player = getPlayer();
		
		Structure nearest = town.getNearestStrucutre(player.getLocation());
		
		if (nearest == null) {
			throw new CivException ("Couldn't find a structure.");
		}
		
		if (args.length < 2 || !args[1].equalsIgnoreCase("yes")) {
			CivMessage.send(player, CivColor.LightGreen+"Are you sure you want to demolish the structure "+CivColor.Yellow+nearest.getDisplayName()+
					CivColor.LightGreen+" at "+CivColor.Yellow+nearest.getCorner()+CivColor.LightGreen+" ?");
			CivMessage.send(player, CivColor.Gray+"If yes, use /build demolishnearest yes");
						
			nearest.flashStructureBlocks();
			return;
		}
		
		town.demolish(nearest, false);
		CivMessage.sendSuccess(player, nearest.getDisplayName()+" at "+nearest.getCorner()+" demolished.");
	}
	
	
	public void demolish_cmd() throws CivException {
		Town town = getSelectedTown();
		
		
		if (args.length < 2) {
			CivMessage.sendHeading(sender, "Demolish Structure");
			for (Structure struct : town.getStructures()) {
				CivMessage.send(sender, struct.getDisplayName()+" type: "+CivColor.Yellow+struct.getCorner().toString()+
						CivColor.White+" to demolish");
			}
			return;
		}
		
		try {
			BlockCoord coord = new BlockCoord(args[1]);
			Structure struct = town.getStructure(coord);
			if (struct == null) {
				CivMessage.send(sender, CivColor.Rose+"No structure at "+args[1]);
				return;
			}
			struct.getTown().demolish(struct, false);
			CivMessage.sendTown(struct.getTown(), struct.getDisplayName()+" has been demolished.");
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			CivMessage.sendError(sender, "Bad formatting. make sure to enter the text *exactly* as shown in yellow.");
		}
	}
	
	public void undo_cmd() {
		CivMessage.sendError(sender, "This command is currently disabled.");
//		Town town = getSelectedTown();
//		town.processUndo();
	}

	public void list_available_structures() throws CivException {
		CivMessage.sendHeading(sender, "Available Structures");
		Town town = getSelectedTown();
		for (ConfigBuildableInfo sinfo : CivSettings.structures.values()) {
			if (sinfo.isAvailable(town)) {
				String leftString = "";
				if (sinfo.limit == 0) {
					leftString = "Unlimited";
				} else {
					leftString = ""+(sinfo.limit - town.getStructureTypeCount(sinfo.id));
				}
				
				CivMessage.send(sender, CivColor.LightPurple+sinfo.displayName+
						CivColor.Yellow+
						" Cost: "+sinfo.cost+
						" Upkeep: "+sinfo.upkeep+" Hammers: "+sinfo.hammer_cost+ 
						" Left: "+leftString);
			}
		}
	}
	
	public void list_available_wonders() throws CivException {
		CivMessage.sendHeading(sender, "Available Wonders");
		Town town = getSelectedTown();
		for (ConfigBuildableInfo sinfo : CivSettings.wonders.values()) {
			if (sinfo.isAvailable(town)) {
				String leftString = "";
				if (sinfo.limit == 0) {
					leftString = "Unlimited";
				} else {
					leftString = ""+(sinfo.limit - town.getStructureTypeCount(sinfo.id));
				}
				
				if (Wonder.isWonderAvailable(sinfo.id)) {				
					CivMessage.send(sender, CivColor.LightPurple+sinfo.displayName+
							CivColor.Yellow+
							" Cost: "+sinfo.cost+
							" Upkeep: "+sinfo.upkeep+" Hammers: "+sinfo.hammer_cost+ 
							" Left: "+leftString);
				} else {
					Wonder wonder = CivGlobal.getWonderByConfigId(sinfo.id);
					CivMessage.send(sender, CivColor.Gray+sinfo.displayName+" Cost: "+sinfo.cost+" - Already built in "+
							wonder.getTown().getName()+"("+wonder.getTown().getCiv().getName()+")");
				}
			}
		}
	}
	
	public void list_cmd() throws CivException {
		this.list_available_structures();
		this.list_available_wonders();
	}
	
	@Override
	public void doDefaultAction() throws CivException {
//		if (args.length == 0) {		
//			showHelp();
//			return;
//		}
		
		String fullArgs = "";
		for (String arg : args) {
			fullArgs += arg + " ";
		}
		fullArgs = fullArgs.trim();
		
		buildByName(fullArgs);
	}

	public void preview_cmd() throws CivException {
		String fullArgs = this.combineArgs(this.stripArgs(args, 1));
		
		ConfigBuildableInfo sinfo = CivSettings.getBuildableInfoByName(fullArgs);
		if (sinfo == null) {
			throw new CivException("Unknown structure "+fullArgs);
		}
		
		Town town = getSelectedTown();
		if (sinfo.isWonder) {
			Wonder wonder = Wonder.newWonder(getPlayer().getLocation(), sinfo.id, town);
			try {
				wonder.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException("Internal IO Error.");
			}
		} else {
		Structure struct = Structure.newStructure(getPlayer().getLocation(), sinfo.id, town);
			try {
				struct.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException("Internal IO Error.");
			}
		}
		CivMessage.sendSuccess(sender, "Showing preview.");
	}
	
	
	private void buildByName(String fullArgs) throws CivException {
		ConfigBuildableInfo sinfo = CivSettings.getBuildableInfoByName(fullArgs);
		if (sinfo == null) {
			throw new CivException("Unknown structure "+fullArgs);
		}
		
		Town town = getSelectedTown();
		
		if (sinfo.isWonder) {
			Wonder wonder = Wonder.newWonder(getPlayer().getLocation(), sinfo.id, town);
			try {
				wonder.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException("Internal IO Error.");
			}
		} else {
			Structure struct = Structure.newStructure(getPlayer().getLocation(), sinfo.id, town);
			try {
				struct.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException("Internal IO Error.");
			}
		}
	}

	@Override
	public void showHelp() {
		showBasicHelp();		
		CivMessage.send(sender, CivColor.LightPurple+command+" "+CivColor.Yellow+"[structure name] "+
				CivColor.Gray+"builds this structure at your location.");
	}

	@Override
	public void permissionCheck() throws CivException {
		validMayorAssistantLeader();
	}

}
