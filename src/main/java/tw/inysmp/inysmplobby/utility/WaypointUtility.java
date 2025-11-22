package tw.inysmp.inysmplobby.utility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class WaypointUtility {

    /** 輔助方法：從配置載入 Location 物件 */
    public static Location loadWaypointLocation(FileConfiguration config, String path) {
        if (!config.contains(path + ".world")) return null;
        
        String worldName = config.getString(path + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null; // 世界未載入
        
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw", 0.0);
        float pitch = (float) config.getDouble(path + ".pitch", 0.0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
}