package tw.inysmp.inysmplobby.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import tw.inysmp.inysmplobby.InySMPLobby;
import tw.inysmp.inysmplobby.utility.WaypointUtility;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InySMPLobby plugin = InySMPLobby.getInstance();
        
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.CHEST) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemMeta clickedMeta = clickedItem.getItemMeta(); 
        String clickedName = clickedMeta.getDisplayName();
        
        // 1. 處理普通玩家菜單
        String playerTitle = plugin.getConfig().getString("gui-title", "§8伺服器選擇菜單");
        if (event.getView().getTitle().equals(playerTitle)) {
            event.setCancelled(true);
            
            // A. 處理 config.yml 中的固定指令傳送點
            ConfigurationSection options = plugin.getConfig().getConfigurationSection("teleport-options");
            
            if (options != null) {
                for (String key : options.getKeys(false)) {
                    ConfigurationSection itemSection = options.getConfigurationSection(key);
                    
                    if (clickedName.equals(itemSection.getString("name"))) {
                        String command = itemSection.getString("command");
                        if (command != null && !command.isEmpty()) {
                            player.closeInventory(); 
                            Bukkit.dispatchCommand(player, command);
                            return;
                        }
                    }
                }
            }
            
            // B. 處理 waypoints.yml 中的動態座標傳送點
            NamespacedKey wayKey = new NamespacedKey(plugin, "waypoint");
            if (clickedMeta.getPersistentDataContainer().has(wayKey, PersistentDataType.STRING)) {
                
                String waypointName = clickedMeta.getPersistentDataContainer().get(wayKey, PersistentDataType.STRING);
                FileConfiguration waypointsCfg = plugin.getWaypointsConfig();
                String path = "points." + waypointName;
                
                Location loc = WaypointUtility.loadWaypointLocation(waypointsCfg, path);
                
                if (loc != null) {
                    player.closeInventory();
                    player.teleport(loc);
                    player.sendMessage(plugin.getPluginPrefix() + ChatColor.AQUA + "已傳送到傳送點: " + waypointName);
                } else {
                    player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "傳送失敗，請聯繫管理員檢查座標。");
                }
                return;
            }
        }

        // 2. 處理管理員菜單 (不變)
        FileConfiguration adminCfg = plugin.getAdminConfig(); 
        String adminTitle = adminCfg.getString("admin-menu.gui-title", "§4§l管理員選單");
        if (event.getView().getTitle().equals(adminTitle)) {
            event.setCancelled(true);
            
            String adminPerm = adminCfg.getString("admin-menu.permission", "inysmplobby.admin");
            if (!player.hasPermission(adminPerm)) {
                player.sendMessage(plugin.getMessage("no-permission"));
                player.closeInventory();
                return;
            }

            ConfigurationSection options = adminCfg.getConfigurationSection("admin-menu.options");
            
            if (options != null) {
                for (String key : options.getKeys(false)) {
                    ConfigurationSection itemSection = options.getConfigurationSection(key);
                    
                    if (clickedName.equals(itemSection.getString("name"))) {
                        String rawCommand = itemSection.getString("command");
                        if (rawCommand != null && !rawCommand.isEmpty()) {
                            player.closeInventory(); 
                            
                            // 替換座標變數
                            String command = rawCommand
                                .replace("{x}", String.valueOf(player.getLocation().getX()))
                                .replace("{y}", String.valueOf(player.getLocation().getY()))
                                .replace("{z}", String.valueOf(player.getLocation().getZ()))
                                .replace("{yaw}", String.valueOf(player.getLocation().getYaw()))
                                .replace("{pitch}", String.valueOf(player.getLocation().getPitch()))
                                .replace("{world}", player.getWorld().getName());
                                
                            Bukkit.dispatchCommand(player, command);
                            return;
                        }
                    }
                }
            }
        }
    }
}