package com.avrgaming.civcraft.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CivCache {
	
	// Arrows fired that need to be updated.
	public static Map<UUID, ArrowFiredCache> arrowsFired = new HashMap<UUID, ArrowFiredCache>();
	
	// Cannon balls fired that need to be updated.
	public static Map<UUID, CannonFiredCache> cannonBallsFired = new HashMap<UUID, CannonFiredCache>();
	
}
