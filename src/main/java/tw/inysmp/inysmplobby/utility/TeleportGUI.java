package tw.inysmp.inysmplobby.utility;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import tw.inysmp.inysmplobby.InySMPLobby;

import java.util.List;
import java.util.stream.Collectors;

public class TeleportGUI {

    /** 創建一個 ItemStack 項目，會處理顏色代碼 */
    public static ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // 替換顏色代碼
        meta.setDisplayName(name.replace('&', '§'));
        if (lore != null) {
            meta.setLore(lore.stream().map(s -> s.replace('&', '§')).collect(Collectors.toList()));
        }
        
        item.setItemMeta(meta);
        return item;
    }

    /** 打開普通玩家 GUI */
    public static void openGUI(Player player) {
        InySMPLobby plugin = InySMPLobby.getInstance();
        ConfigurationSection configOptions = plugin.getConfig().getConfigurationSection("teleport-options");
        FileConfiguration waypointsCfg = plugin.getWaypointsConfig(); 
        
        String title = plugin.getConfig().getString("gui-title", "§8伺服器選擇菜單");
        
        Inventory gui = Bukkit.createInventory(player, 27, title);
        int slotIndex = 14; 
        
        // 1. 載入 config.yml 中的固定傳送點
        if (configOptions != null) {
            for (String key : configOptions.getKeys(false)) {
                ConfigurationSection itemSection = configOptions.getConfigurationSection(key);
                
                Material material = Material.getMaterial(itemSection.getString("item", "STONE"));
                String name = itemSection.getString("name", "未定義項目");
                List<String> lore = itemSection.getStringList("lore");
                int slot = itemSection.getInt("slot", -1);
                
                if (slot != -1 && material != null) {
                    ItemStack item = createMenuItem(material, name, lore);
                    gui.setItem(slot, item);
                }
            }
        }
        
        // 2. 載入 waypoints.yml 中的動態傳送點
        ConfigurationSection points = waypointsCfg.getConfigurationSection("points");
        if (points != null) {
            for (String key : points.getKeys(false)) {
                
                Material material = Material.ENDER_PEARL; 
                String name = ChatColor.translateAlternateColorCodes('&', "&d&l傳送點: &b" + key);
                
                double x = points.getDouble(key + ".x", 0);
                double y = points.getDouble(key + ".y", 0);
                double z = points.getDouble(key + ".z", 0);
                
                List<String> lore = List.of(
                    ChatColor.translateAlternateColorCodes('&', "&7點擊傳送到 " + key),
                    String.format(ChatColor.translateAlternateColorCodes('&', "&7座標: %.1f, %.1f, %.1f"), x, y, z)
                );
                
                ItemStack item = createMenuItem(material, name, lore);
                
                // 設置自定義標籤來標識它是一個 Waypoint
                ItemMeta meta = item.getItemMeta();
                NamespacedKey wayKey = new NamespacedKey(plugin, "waypoint");
                
                meta.getPersistentDataContainer().set(
                    wayKey, 
                    PersistentDataType.STRING, 
                    key
                );
                item.setItemMeta(meta);
                
                if (slotIndex < 27) { 
                    gui.setItem(slotIndex, item);
                    slotIndex++;
                }
            }
        }

        player.openInventory(gui);
        player.sendMessage(plugin.getMessage("gui-opened"));
    }
}