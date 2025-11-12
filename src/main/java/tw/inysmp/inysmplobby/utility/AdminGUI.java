package tw.inysmp.inysmplobby.utility;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tw.inysmp.inysmplobby.InySMPLobby;

import java.util.List;

public class AdminGUI {
    
    public static void openGUI(Player player) {
        InySMPLobby plugin = InySMPLobby.getInstance();
        
        // 讀取 admin.yml
        FileConfiguration adminCfg = plugin.getAdminConfig(); 
        ConfigurationSection menuCfg = adminCfg.getConfigurationSection("admin-menu");

        if (menuCfg == null) return;

        String title = menuCfg.getString("gui-title", "§4§l管理員選單");
        
        Inventory gui = Bukkit.createInventory(player, 27, title);
        ConfigurationSection options = menuCfg.getConfigurationSection("options");
        
        if (options != null) {
            for (String key : options.getKeys(false)) {
                ConfigurationSection itemSection = options.getConfigurationSection(key);
                
                Material material = Material.getMaterial(itemSection.getString("item", "BARRIER"));
                String name = itemSection.getString("name", "未定義項目");
                List<String> lore = itemSection.getStringList("lore");
                int slot = itemSection.getInt("slot", -1);
                
                if (slot != -1 && material != null) {
                    // 使用 TeleportGUI 中定義的工具方法
                    ItemStack item = TeleportGUI.createMenuItem(material, name, lore); 
                    gui.setItem(slot, item);
                }
            }
        }
        
        player.openInventory(gui);
    }
}