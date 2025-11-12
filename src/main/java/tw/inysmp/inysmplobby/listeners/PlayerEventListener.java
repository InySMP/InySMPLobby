package tw.inysmp.inysmplobby.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import tw.inysmp.inysmplobby.InySMPLobby;
import tw.inysmp.inysmplobby.utility.AdminGUI;
import tw.inysmp.inysmplobby.utility.LobbyUtility;
import tw.inysmp.inysmplobby.utility.TeleportGUI;

public class PlayerEventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = InySMPLobby.getInstance().getConfig();
        FileConfiguration adminCfg = InySMPLobby.getInstance().getAdminConfig();

        // 1. 處理新玩家傳送 (功能 2)
        if (!player.hasPlayedBefore() && config.getBoolean("teleport-on-first-join", true)) {
            LobbyUtility.teleportToLobby(player);
            player.sendMessage(InySMPLobby.getInstance().getMessage("teleport-on-first-join"));
        }

        // 2. 發放菜單物品 (指南針)
        if (config.getBoolean("menu-item.enabled", false)) {
            Material material = Material.getMaterial(config.getString("menu-item.material", "COMPASS"));
            String name = config.getString("menu-item.name", "§b§l伺服器傳送指南針");
            int slot = config.getInt("menu-item.slot", 0);
            
            if (material != null) {
                ItemStack menuItem = TeleportGUI.createMenuItem(material, name, config.getStringList("menu-item.lore"));
                
                if (player.getInventory().getItem(slot) == null || player.getInventory().getItem(slot).getType() == Material.AIR) {
                    player.getInventory().setItem(slot, menuItem);
                }
            }
        }
        
        // 3. 發放管理員菜單物品 (紅石)，僅限有權限的玩家
        boolean adminEnabled = adminCfg.getBoolean("admin-menu.enabled", false);
        String adminPerm = adminCfg.getString("admin-menu.permission", "inysmplobby.admin");

        if (adminEnabled && player.hasPermission(adminPerm)) {
            Material material = Material.getMaterial(adminCfg.getString("admin-menu.trigger-item.material", "REDSTONE"));
            String name = adminCfg.getString("admin-menu.trigger-item.name", "§c§l管理員工具菜單");
            int slot = adminCfg.getInt("admin-menu.trigger-item.slot", 6);
            
            if (material != null) {
                ItemStack adminItem = TeleportGUI.createMenuItem(material, name, adminCfg.getStringList("admin-menu.trigger-item.lore"));
                
                if (player.getInventory().getItem(slot) == null || player.getInventory().getItem(slot).getType() == Material.AIR) {
                    player.getInventory().setItem(slot, adminItem);
                }
            }
        }
    }

    // 處理指南針/紅石點擊事件
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        FileConfiguration config = InySMPLobby.getInstance().getConfig();
        FileConfiguration adminCfg = InySMPLobby.getInstance().getAdminConfig();
        ItemStack item = event.getItem();

        boolean isClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK ||
                          action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;

        if (!isClick || item == null) return;
        
        // --- 檢查 管理員菜單 (紅石) ---
        boolean adminEnabled = adminCfg.getBoolean("admin-menu.enabled", false);
        String adminPerm = adminCfg.getString("admin-menu.permission", "inysmplobby.admin");
        
        if (adminEnabled) {
            Material triggerMaterial = Material.getMaterial(adminCfg.getString("admin-menu.trigger-item.material", "REDSTONE"));
            String triggerName = adminCfg.getString("admin-menu.trigger-item.name", "§c§l管理員工具菜單");
            int triggerSlot = adminCfg.getInt("admin-menu.trigger-item.slot", 6);
            
            if (item.getType() == triggerMaterial && 
                player.getInventory().getHeldItemSlot() == triggerSlot &&
                item.getItemMeta() != null &&
                item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(triggerName)) {
                
                event.setCancelled(true); 
                
                if (player.hasPermission(adminPerm)) {
                    AdminGUI.openGUI(player); 
                } else {
                    player.sendMessage(InySMPLobby.getInstance().getMessage("no-permission"));
                }
                return;
            }
        }
        
        // --- 檢查 普通菜單 (指南針) ---
        if (config.getBoolean("menu-item.enabled", false)) {
            Material menuMaterial = Material.getMaterial(config.getString("menu-item.material", "COMPASS"));
            String menuName = config.getString("menu-item.name", "§b§l伺服器傳送指南針");
            
            if (item.getType() == menuMaterial && 
                item.getItemMeta() != null && 
                item.getItemMeta().hasDisplayName() && 
                item.getItemMeta().getDisplayName().equals(menuName)) {
                
                event.setCancelled(true); 
                TeleportGUI.openGUI(player); 
            }
        }
    }
}