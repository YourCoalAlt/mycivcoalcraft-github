package com.avrgaming.civcraft.threading.timers;

import java.util.ArrayList;

import com.avrgaming.civcraft.components.Component;
import com.avrgaming.civcraft.components.ProjectileComponent;
import com.avrgaming.civcraft.main.CivGlobal;

public class ProjectileComponentTimer implements Runnable {
	
	@Override
	public void run() {
		try {
			if (!CivGlobal.towersEnabled) { return; }
			ProjectileComponent.componentsLock.lock();
			try {
				ArrayList<Component> projectileComponents = ProjectileComponent.componentsByType.get(ProjectileComponent.class.getName());
				if (projectileComponents == null) {
					return;
				}
				
				for (Component c : projectileComponents) {
					ProjectileComponent projectileComponent = (ProjectileComponent)c;
					projectileComponent.process();
				}
			} finally {
				ProjectileComponent.componentsLock.unlock();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
