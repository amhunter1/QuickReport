package com.manus.quickreport.util;

import com.manus.quickreport.QuickReport;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final QuickReport plugin;
    private FileConfiguration messagesConfig = null;
    private File messagesFile = null;
    private final String defaultLang = "en"; // Default language

    public MessageManager(QuickReport plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
        }
    }

    public String getMessage(String path, Map<String, String> placeholders, String lang) {
        String message = getMessage(path, lang);
        if (message != null && placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return message;
    }

    public String getMessage(String path, String lang) {
        String message = null;
        if (messagesConfig.isConfigurationSection("messages." + path)) {
            // Try specific language first
            if (messagesConfig.contains("messages." + path + "." + lang)) {
                message = messagesConfig.getString("messages." + path + "." + lang);
            }
            // Fallback to default language
            if (message == null) {
                message = messagesConfig.getString("messages." + path + "." + defaultLang);
            }
        } else {
            // If it's a single string, just get it
            message = messagesConfig.getString("messages." + path);
        }

        if (message == null) {
            return ChatColor.RED + "Error: Message not found for path '" + path + "'";
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path) {
        // For simplicity, we'll use the default language for console/general messages
        return getMessage(path, defaultLang);
    }

    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        // In a real scenario, we would determine the player's language.
        // For now, we'll stick to the default language 'en' as primary,
        // but the getMessage logic will try 'tr' if available and requested.
        String lang = defaultLang; // Placeholder for actual language detection logic

        String message = getMessage(path, lang);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        player.sendMessage(message);
    }

    public void sendMessage(Player player, String path) {
        sendMessage(player, path, new HashMap<>());
    }

    public String getRawMessage(String path, String lang) {
        String message = null;
        if (messagesConfig.isConfigurationSection("messages." + path)) {
            if (messagesConfig.contains("messages." + path + "." + lang)) {
                message = messagesConfig.getString("messages." + path + "." + lang);
            }
            if (message == null) {
                message = messagesConfig.getString("messages." + path + "." + defaultLang);
            }
        } else {
            message = messagesConfig.getString("messages." + path);
        }
        return message;
    }
}
