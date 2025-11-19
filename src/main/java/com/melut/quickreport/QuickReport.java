package com.manus.quickreport;

import com.manus.quickreport.database.DatabaseManager;
import com.manus.quickreport.manager.ReportManager;
import com.manus.quickreport.util.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class QuickReport extends JavaPlugin {

    private static QuickReport instance;
    private DatabaseManager databaseManager;
    private ReportManager reportManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize Managers
        this.messageManager = new MessageManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.reportManager = new ReportManager(this);

        // Setup Database
        if (!databaseManager.connect()) {
            getLogger().severe("Failed to connect to the database! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        databaseManager.createTables();

        // Register Commands
        getCommand("report").setExecutor(reportManager.getReportCommand());
        getCommand("myreports").setExecutor(reportManager.getMyReportsCommand());
        getCommand("queryreport").setExecutor(reportManager.getQueryReportCommand());
        getCommand("reports").setExecutor(reportManager.getReportsCommand());
        getCommand("reportaction").setExecutor(reportManager.getReportActionCommand());

        // Register Listeners
        // No listeners yet, but will be added later for admin notifications

        // PlaceholderAPI Integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.manus.quickreport.papi.QuickReportExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholder support disabled.");
        }

        getLogger().info("QuickReport has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("QuickReport has been disabled!");
    }

    public static QuickReport getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
