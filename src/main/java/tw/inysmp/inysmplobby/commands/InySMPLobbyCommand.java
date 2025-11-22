package tw.inysmp.inysmplobby.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.World;
import tw.inysmp.inysmplobby.InySMPLobby;
import tw.inysmp.inysmplobby.utility.WaypointUtility;

public class InySMPLobbyCommand implements CommandExecutor {
    
    private final InySMPLobby plugin = InySMPLobby.getInstance();
    private final String adminPerm = plugin.getAdminConfig().getString("admin-menu.permission", "inysmplobby.admin");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!sender.hasPermission(adminPerm)) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "--- InySMPLobby v" + plugin.getDescription().getVersion() + " ---");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " setlobby - 設定主大廳位置");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " set <名稱> - 設定傳送點");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " tp <名稱> - 傳送到傳送點 (測試)");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " remove <名稱> - 移除傳送點");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload - 重新載入配置");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "setlobby":
                return handleSetLobby(sender, args);
            case "reload":
                return handleReload(sender);
            case "set": 
                return handleSetWaypoint(sender, args);
            case "tp": 
                return handleTeleportWaypoint(sender, args);
            case "remove": 
                return handleRemoveWaypoint(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "未知的子指令。請使用 /" + label + " <setlobby|set|tp|remove|reload>");
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        plugin.handleReload(); // 使用 InySMPLobby 中的統一 reload 方法
        sender.sendMessage(plugin.getPluginPrefix() + " §a配置檔案已重新載入！");
        return true;
    }
    
    private boolean handleSetLobby(CommandSender sender, String[] args) {
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location loc = player.getLocation();
            
            plugin.getConfig().set("lobby-location.world", loc.getWorld().getName());
            plugin.getConfig().set("lobby-location.x", loc.getX());
            plugin.getConfig().set("lobby-location.y", loc.getY());
            plugin.getConfig().set("lobby-location.z", loc.getZ());
            plugin.getConfig().set("lobby-location.yaw", loc.getYaw());
            plugin.getConfig().set("lobby-location.pitch", loc.getPitch());
            plugin.saveConfig();
            
            player.sendMessage(plugin.getPluginPrefix() + " §a大廳位置已設定！");
            return true;
        } 
        else if (args.length == 7 && args[1].matches("-?\\d+(\\.\\d+)?")) {
             try {
                // ... (GUI 傳遞座標邏輯不變)
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                double yaw = Double.parseDouble(args[4]);
                double pitch = Double.parseDouble(args[5]);
                String world = args[6];
                
                plugin.getConfig().set("lobby-location.world", world);
                plugin.getConfig().set("lobby-location.x", x);
                plugin.getConfig().set("lobby-location.y", y);
                plugin.getConfig().set("lobby-location.z", z);
                plugin.getConfig().set("lobby-location.yaw", yaw);
                plugin.getConfig().set("lobby-location.pitch", pitch);
                plugin.saveConfig();
                
                sender.sendMessage(plugin.getPluginPrefix() + " §a大廳位置已透過 GUI 指令設定！");
            } catch (Exception e) {
                sender.sendMessage(plugin.getPluginPrefix() + " §c setlobby 參數錯誤。");
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "此指令只能由玩家執行，或透過 GUI 傳遞完整座標。");
            return true;
        }
    }


    private boolean handleSetWaypoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此指令只能由玩家執行。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "用法: /" + args[0] + " set <名稱>");
            return true;
        }
        
        Player player = (Player) sender;
        String name = args[1].toLowerCase();
        Location loc = player.getLocation();
        
        FileConfiguration waypoints = plugin.getWaypointsConfig();
        String path = "points." + name;

        waypoints.set(path + ".world", loc.getWorld().getName());
        waypoints.set(path + ".x", loc.getX());
        waypoints.set(path + ".y", loc.getY());
        waypoints.set(path + ".z", loc.getZ());
        waypoints.set(path + ".yaw", loc.getYaw());
        waypoints.set(path + ".pitch", loc.getPitch());
        
        plugin.saveWaypointsFile(); 
        
        player.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + "傳送點 " + name + " 已設定！");
        return true;
    }
    
    private boolean handleRemoveWaypoint(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "用法: /" + args[0] + " remove <名稱>");
            return true;
        }

        String name = args[1].toLowerCase();
        FileConfiguration waypoints = plugin.getWaypointsConfig();
        String path = "points." + name;

        if (waypoints.contains(path)) {
            waypoints.set(path, null); 
            plugin.saveWaypointsFile();
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + "傳送點 " + name + " 已移除！");
        } else {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "找不到傳送點 " + name + "。");
        }
        return true;
    }

    private boolean handleTeleportWaypoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此指令只能由玩家執行。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "用法: /" + args[0] + " tp <名稱>");
            return true;
        }

        Player player = (Player) sender;
        String name = args[1].toLowerCase();
        FileConfiguration waypoints = plugin.getWaypointsConfig();
        String path = "points." + name;

        if (waypoints.contains(path)) {
            Location loc = WaypointUtility.loadWaypointLocation(waypoints, path);
            if (loc != null) {
                player.teleport(loc);
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.AQUA + "已傳送到傳送點: " + name);
            } else {
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "傳送失敗，世界載入錯誤或座標無效！");
            }
        } else {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "找不到傳送點 " + name + "。");
        }
        return true;
    }
}