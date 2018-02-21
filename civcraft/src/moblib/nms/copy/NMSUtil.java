package moblib.nms.copy;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList;

import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;

public class NMSUtil {

	public static void clearPathfinderGoals(PathfinderGoalSelector goalSelector) {
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);

            bField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
