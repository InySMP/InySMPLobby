package tw.inysmp.inysmplobby.utility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import tw.inysmp.inysmplobby.InySMPLobby;

public class LobbyUtility {

    public static void teleportToLobby(Player player) {
        InySMPLobby plugin = InySMPLobby.getInstance();
        ConfigurationSection lobbyCfg = plugin.getConfig().getConfigurationSection("lobby-location");
        
        if (lobbyCfg == null || !lobbyCfg.contains("world")) {
            player.sendMessage(plugin.getMessage("lobby-location-not-set"));
            return;
        }

        String worldName = lobbyCfg.getString("world");
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            plugin.getLogger().severe("Lobby world '" + worldName + "' not found!");
            player.sendMessage(plugin.getMessage("lobby-location-not-set"));
            return;
        }

        double x = lobbyCfg.getDouble("x");
        double y = lobbyCfg.getDouble("y");
        double z = lobbyCfg.getDouble("z");
        float yaw = (float) lobbyCfg.getDouble("yaw");
        float pitch = (float) lobbyCfg.getDouble("pitch");

        Location lobbyLoc = new Location(world, x, y, z, yaw, pitch);
        
        player.teleport(lobbyLoc);
        player.sendMessage(plugin.getMessage("teleport-to-lobby"));
    }
}