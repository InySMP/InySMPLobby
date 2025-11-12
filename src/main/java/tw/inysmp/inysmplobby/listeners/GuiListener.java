package tw.inysmp.inysmplobby.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import tw.inysmp.inysmplobby.InySMPLobby;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InySMPLobby plugin = InySMPLobby.getInstance();
        
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.CHEST) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        String clickedName = clickedItem.getItemMeta().getDisplayName();
        
        // 1. 處理普通玩家菜單 (從 config.yml 讀取)
        String playerTitle = plugin.getConfig().getString("gui-title", "§8伺服器選擇菜單");
        if (event.getView().getTitle().equals(playerTitle)) {
            event.setCancelled(true);
            
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
        }

        // 2. 處理管理員菜單 (從 admin.yml 讀取)
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