package com.avrgaming.civcraft.anticheat;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AC_ModData {
	
	private final Map<String, String> mods;
	
	public AC_ModData(Map<String, String> mods) {
		this.mods = mods;
	}
	
	public Set<String> getMods() {
		return Collections.unmodifiableSet(mods.keySet());
	}
	
	public Map<String, String> getModsMap() {
		return Collections.unmodifiableMap(mods);
	}
}
