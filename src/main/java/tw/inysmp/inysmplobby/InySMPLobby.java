package tw.inysmp.inysmplobby;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit; // <-- 修正 2: 導入 Bukkit

import tw.inysmp.inysmplobby.commands.LobbyCommand;
import tw.inysmp.inysmplobby.commands.MenuCommand;
import tw.inysmp.inysmplobby.listeners.GuiListener;
import tw.inysmp.inysmplobby.listeners.PlayerEventListener;

import java.io.File;
import java.io.IOException; // 雖然在此版本未使用到，但保留

public final class InySMPLobby extends JavaPlugin {

    private static InySMPLobby instance;

    // 修正 1: 宣告 pluginPrefix 欄位
    private String pluginPrefix; 

    private File messageFile;
    private FileConfiguration messageConfig;

    @Override
    public void onEnable() {
        instance = this;
        
        sendConsole("&6[&bIny&aSMP&eLobby&6] &2&lThe Plugin is loading now ... ");

        this.saveDefaultConfig();
        // 注意: 為了保持與您資源檔的命名一致性，請確認您的資源檔案是 'message.yml' 或 'messages.yml'
        loadMessageFile("messages.yml"); // 建議使用 messages.yml

        this.getCommand("lobby").setExecutor(new LobbyCommand());
        this.getCommand("teleportmenu").setExecutor(new MenuCommand());

        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        sendConsole("&6[&bIny&aSMP&eLobby&6] &ev" + getDescription().getVersion() + " &a已啟動！");
    }

    @Override
    public void onDisable() {
        sendConsole("&6[&bIny&aSMP&eLobby&6] &ev" + getDescription().getVersion() + " &aClose！");
    }


    public static InySMPLobby getInstance() {
        return instance;
    }

    /**
     * 載入並處理訊息檔案，並初始化插件前綴。
     * @param fileName 訊息檔案名稱 (例如: "messages.yml")
     */
    public void loadMessageFile(String fileName) {
        if (messageFile == null) {
            // 修正 3: 使用參數化的檔名
            messageFile = new File(getDataFolder(), fileName);
        }
        if (!messageFile.exists()) {
            this.saveResource(fileName, false);
        }
        
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);

        // 確保 prefix 有預設值，即使 messages.yml 中沒有
        String rawPrefix = messageConfig.getString("prefix", "&6[&bIny&aSMP&eLobby&6] &r");
        this.pluginPrefix = ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    /**
     * 獲取並格式化訊息。
     * @param path messages.yml 中的路徑。
     * @return 處理顏色和前綴後的訊息。
     */
    public String getMessage(String path) {
        // 檢查訊息是否存在
        if (!messageConfig.contains(path)) {
            return ChatColor.RED + "[InySMPLobby Error] 消息路徑錯誤: " + path;
        }
        
        // 處理列表訊息
        if (messageConfig.isList(path)) {
            StringBuilder sb = new StringBuilder();
            // 在列表中的每一行結尾不應該有換行符號
            for (int i = 0; i < messageConfig.getStringList(path).size(); i++) {
                String line = messageConfig.getStringList(path).get(i);
                sb.append(processColors(line));
                if (i < messageConfig.getStringList(path).size() - 1) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }

        // 處理單行訊息
        String message = messageConfig.getString(path);
        return processColors(message);
    }

    /**
     * 處理顏色代碼和前綴替換。
     */
    private String processColors(String message) {
        // 處理顏色代碼
        message = ChatColor.translateAlternateColorCodes('&', message);
        // 替換前綴
        message = message.replace("{prefix}", this.pluginPrefix);
        return message;
    }

    /**
     * 發送訊息到控制台。
     */
    private void sendConsole(String message) {
        // 必須使用 Bukkit.getConsoleSender()
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }
}