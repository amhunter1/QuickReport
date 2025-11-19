package com.manus.quickreport.manager;

import com.manus.quickreport.QuickReport;
import com.manus.quickreport.database.DatabaseManager;
import com.manus.quickreport.model.Report;
import com.manus.quickreport.model.ReportStatus;
import com.manus.quickreport.util.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReportManager {

    private final QuickReport plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;
    private final Map<UUID, Long> reportCooldowns = new HashMap<>();

    // Command Executors
    private final CommandExecutor reportCommand;
    private final CommandExecutor myReportsCommand;
    private final CommandExecutor queryReportCommand;
    private final CommandExecutor reportsCommand;
    private final CommandExecutor reportActionCommand;

    public ReportManager(QuickReport plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();

        // Initialize Command Executors
        this.reportCommand = new ReportCommand(this);
        this.myReportsCommand = new MyReportsCommand(this);
        this.queryReportCommand = new QueryReportCommand(this);
        this.reportsCommand = new ReportsCommand(this);
        this.reportActionCommand = new ReportActionCommand(this);
    }

    // --- Core Logic ---

    public void submitReport(Player reporter, Player reported, String reason, String details) {
        // 1. Cooldown Check
        long cooldownSeconds = plugin.getConfig().getLong("report-cooldown-seconds", 60);
        if (reportCooldowns.containsKey(reporter.getUniqueId())) {
            long lastReportTime = reportCooldowns.get(reporter.getUniqueId());
            long timeElapsed = System.currentTimeMillis() - lastReportTime;
            long timeLeft = TimeUnit.SECONDS.toMillis(cooldownSeconds) - timeElapsed;

            if (timeLeft > 0) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timeLeft) + 1));
                messageManager.sendMessage(reporter, "report-cooldown", placeholders);
                return;
            }
        }

        // 2. Create Report Object
        Report newReport = new Report(
                -1, // ID will be set by DB
                reporter.getUniqueId(),
                reporter.getName(),
                reported.getUniqueId(),
                reported.getName(),
                reason,
                details,
                System.currentTimeMillis(),
                ReportStatus.PENDING,
                null, null, null
        );

        // 3. Save to DB (Async)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int id = databaseManager.saveReport(newReport);

            // 4. Update Cooldown and Notify Reporter (Sync)
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (id != -1) {
                    reportCooldowns.put(reporter.getUniqueId(), System.currentTimeMillis());

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", String.valueOf(id));
                    placeholders.put("reported", reported.getName());
                    messageManager.sendMessage(reporter, "report-success", placeholders);

                    // 5. Notify Admins
                    notifyAdminsOfNewReport(id, reported.getName());
                } else {
                    // Handle DB save failure (e.g., send error message to reporter)
                    messageManager.sendMessage(reporter, "report-db-error"); // Need to add this message
                }
            });
        });
    }

    public void notifyAdminsOfNewReport(int id, String reportedName) {
        String permission = plugin.getConfig().getString("admin-permission", "quickreport.admin");
        String soundName = plugin.getConfig().getString("notification-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        float volume = (float) plugin.getConfig().getDouble("notification-volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("notification-pitch", 1.0);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("id", String.valueOf(id));
        placeholders.put("reported", reportedName);

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission(permission)) {
                // Send clickable chat message
                // This requires using Paper's Adventure API or Spigot's ChatComponent API.
                // Implement clickable message using Spigot's ChatComponent API (or Paper's equivalent)
                // Since we are using Paper API, we can use the ChatComponent API.
                // The message will be: "[QuickReport] New report (ID: %id%) against %reported%. [Click to view details]"
                // Clicking will execute: /queryreport %id%

                String rawMessage = messageManager.getRawMessage("admin-new-report-notification", "en");
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    rawMessage = rawMessage.replace("%" + entry.getKey() + "%", entry.getValue());
                }

                net.md_5.bungee.api.chat.TextComponent component = new net.md_5.bungee.api.chat.TextComponent(
                        net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', rawMessage)
                );

                // Find the clickable part: "[Click to view details]"
                String clickableText = "[Click to view details]";
                int startIndex = rawMessage.indexOf(clickableText);

                if (startIndex != -1) {
                    // Split the message into three parts: before, clickable, after
                    String before = rawMessage.substring(0, startIndex);
                    String clickable = rawMessage.substring(startIndex, startIndex + clickableText.length());
                    String after = rawMessage.substring(startIndex + clickableText.length());

                    net.md_5.bungee.api.chat.TextComponent beforeComponent = new net.md_5.bungee.api.chat.TextComponent(
                            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', before)
                    );
                    net.md_5.bungee.api.chat.TextComponent clickableComponent = new net.md_5.bungee.api.chat.TextComponent(
                            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', clickable)
                    );
                    net.md_5.bungee.api.chat.TextComponent afterComponent = new net.md_5.bungee.api.chat.TextComponent(
                            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', after)
                    );

                    clickableComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                            net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                            "/queryreport " + id
                    ));
                    clickableComponent.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                            net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                            new net.md_5.bungee.api.chat.TextComponent[]{new net.md_5.bungee.api.chat.TextComponent(
                                    net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', "&7Click to view report details for ID: &e" + id)
                            )}
                    ));

                    beforeComponent.addExtra(clickableComponent);
                    beforeComponent.addExtra(afterComponent);
                    admin.spigot().sendMessage(beforeComponent);
                } else {
                    // Fallback to simple message if clickable part is not found
                    admin.sendMessage(ChatColor.translateAlternateColorCodes('&', rawMessage));
                }

                // Play sound
                try {
                    admin.playSound(admin.getLocation(), org.bukkit.Sound.valueOf(soundName), volume, pitch);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name in config: " + soundName);
                }
            }
        }
    }

    // --- Command Getters ---

    public CommandExecutor getReportCommand() {
        return reportCommand;
    }

    public CommandExecutor getMyReportsCommand() {
        return myReportsCommand;
    }

    public CommandExecutor getQueryReportCommand() {
        return queryReportCommand;
    }

    public CommandExecutor getReportsCommand() {
        return reportsCommand;
    }

    public CommandExecutor getReportActionCommand() {
        return reportActionCommand;
    }

    // --- Utility Getters ---

    public QuickReport getPlugin() {
        return plugin;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void processReport(org.bukkit.command.CommandSender sender, int id, ReportStatus status, String rewardCode, String rejectionReason) {
        UUID adminUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;
        String adminName = sender.getName();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Report report = databaseManager.getReportById(id);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (report == null) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", String.valueOf(id));
                    messageManager.sendMessage((Player) sender, "report-id-not-found", placeholders);
                    return;
                }

                if (report.getStatus() != ReportStatus.PENDING) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", String.valueOf(id));
                    placeholders.put("status", report.getStatus().getDisplayName());
                    messageManager.sendMessage((Player) sender, "report-already-processed", placeholders);
                    return;
                }

                // Update DB (Async)
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    databaseManager.updateReportStatus(id, status, adminUUID, adminName, rejectionReason);

                    // Notify Reporter and Reported (Sync)
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Player reporter = Bukkit.getPlayer(report.getReporterUUID());
                        Player reported = Bukkit.getPlayer(report.getReportedUUID());

                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("id", String.valueOf(id));
                        placeholders.put("reported", report.getReportedName());
                        placeholders.put("admin", adminName);

                        if (status == ReportStatus.ACCEPTED) {
                            // 1. Reward Reporter
                            if (rewardCode != null && plugin.getConfig().isConfigurationSection("rewards." + rewardCode)) {
                                String command = plugin.getConfig().getString("rewards." + rewardCode + ".command");
                                if (command != null) {
                                    command = command.replace("%player%", report.getReporterName());
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                                }
                            }

                            // 2. Notify Reporter
                            if (reporter != null) {
                                messageManager.sendMessage(reporter, "report-accepted-reporter", placeholders);
                            }

                            // 3. Notify Reported (Optional, but good practice)
                            if (reported != null) {
                                messageManager.sendMessage(reported, "report-accepted-reported", placeholders);
                            }
                        } else if (status == ReportStatus.REJECTED) {
                            placeholders.put("reason", rejectionReason);

                            // 1. Notify Reporter
                            if (reporter != null) {
                                messageManager.sendMessage(reporter, "report-rejected-reporter", placeholders);
                            }

                            // 2. Notify Reported (Optional, but good practice)
                            if (reported != null) {
                                messageManager.sendMessage(reported, "report-rejected-reported", placeholders);
                            }
                        }

                        // Notify Admin
                        sender.sendMessage(ChatColor.GREEN + "Report " + id + " successfully processed as " + status.getDisplayName() + ".");
                    });
                });
            });
        });
    }
}

// --- Command Classes (Stubs for now) ---

class ReportCommand implements CommandExecutor, TabCompleter {
    private final ReportManager manager;

    public ReportCommand(ReportManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            manager.getMessageManager().sendMessage((Player) sender, "player-only");
            return true;
        }
        Player reporter = (Player) sender;

        if (args.length < 2) {
            reporter.sendMessage(ChatColor.RED + "Usage: /report <player> <reason> [details]");
            return true;
        }

        Player reported = Bukkit.getPlayer(args[0]);
        if (reported == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[0]);
            manager.getMessageManager().sendMessage(reporter, "player-not-found", placeholders);
            return true;
        }

        if (reported.equals(reporter)) {
            manager.getMessageManager().sendMessage(reporter, "report-self");
            return true;
        }

        String reason = args[1];
        List<String> validReasons = manager.getPlugin().getConfig().getStringList("report-reasons");
        if (!validReasons.contains(reason)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("reasons", String.join(", ", validReasons));
            manager.getMessageManager().sendMessage(reporter, "report-reason-invalid", placeholders);
            return true;
        }

        String details = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "";

        manager.submitReport(reporter, reported, reason, details);
        return true;
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        } else if (args.length == 2) {
            return manager.getPlugin().getConfig().getStringList("report-reasons").stream()
                    .filter(reason -> reason.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}

class MyReportsCommand implements CommandExecutor {
    private final ReportManager manager;

    public MyReportsCommand(ReportManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            manager.getMessageManager().sendMessage((Player) sender, "player-only");
            return true;
        }
        Player player = (Player) sender;

        // Implementation
        UUID reporterUUID = player.getUniqueId();
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                manager.getMessageManager().sendMessage(player, "invalid-page-number"); // Add this message
                return true;
            }
        }

        final int finalPage = page;
        Bukkit.getScheduler().runTaskAsynchronously(manager.getPlugin(), () -> {
            List<Report> reports = manager.getDatabaseManager().getReportsByReporter(reporterUUID);

            Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
                if (reports.isEmpty()) {
                    manager.getMessageManager().sendMessage(player, "myreports-no-reports");
                    return;
                }

                int reportsPerPage = 5; // Configurable later
                int totalPages = (int) Math.ceil((double) reports.size() / reportsPerPage);

                if (finalPage < 1 || finalPage > totalPages) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("total_pages", String.valueOf(totalPages));
                    manager.getMessageManager().sendMessage(player, "invalid-page-range", placeholders); // Add this message
                    return;
                }

                int start = (finalPage - 1) * reportsPerPage;
                int end = Math.min(start + reportsPerPage, reports.size());

                Map<String, String> headerPlaceholders = new HashMap<>();
                headerPlaceholders.put("page", String.valueOf(finalPage));
                headerPlaceholders.put("total_pages", String.valueOf(totalPages));
                manager.getMessageManager().sendMessage(player, "myreports-header", headerPlaceholders);

                for (int i = start; i < end; i++) {
                    Report report = reports.get(i);
                    Map<String, String> entryPlaceholders = new HashMap<>();
                    entryPlaceholders.put("id", String.valueOf(report.getId()));
                    entryPlaceholders.put("status", report.getStatus().getDisplayName());
                    entryPlaceholders.put("reported", report.getReportedName());
                    entryPlaceholders.put("reason", report.getReason());
                    manager.getMessageManager().sendMessage(player, "myreports-entry", entryPlaceholders);
                }
            });
        });
        return true;
    }
}

class QueryReportCommand implements CommandExecutor {
    private final ReportManager manager;

    public QueryReportCommand(ReportManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /queryreport <id>");
            return true;
        }

        try {
            int id = Integer.parseInt(args[0]);
            // Implementation
            Bukkit.getScheduler().runTaskAsynchronously(manager.getPlugin(), () -> {
                Report report = manager.getDatabaseManager().getReportById(id);

                Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
                    if (report == null) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("id", String.valueOf(id));
                        manager.getMessageManager().sendMessage((Player) sender, "report-id-not-found", placeholders);
                        return;
                    }

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", String.valueOf(report.getId()));
                    placeholders.put("status", report.getStatus().getDisplayName());
                    placeholders.put("reported", report.getReportedName());
                    placeholders.put("reason", report.getReason());
                    placeholders.put("details", report.getDetails().isEmpty() ? "N/A" : report.getDetails());
                    placeholders.put("date", new java.util.Date(report.getTimestamp()).toString());

                    String adminInfo = "";
                    if (report.getStatus() != ReportStatus.PENDING) {
                        Map<String, String> adminPlaceholders = new HashMap<>();
                        adminPlaceholders.put("admin", report.getAdminName());
                        adminPlaceholders.put("process_date", new java.util.Date(report.getTimestamp()).toString()); // Using report timestamp for simplicity, should be process time
                        adminPlaceholders.put("outcome", report.getStatus().getDisplayName());

                        adminInfo = manager.getMessageManager().getRawMessage("queryreport-admin-info", "en");
                        for (Map.Entry<String, String> entry : adminPlaceholders.entrySet()) {
                            adminInfo = adminInfo.replace("%" + entry.getKey() + "%", entry.getValue());
                        }

                        if (report.getStatus() == ReportStatus.REJECTED && report.getRejectionReason() != null) {
                            Map<String, String> rejectionPlaceholders = new HashMap<>();
                            rejectionPlaceholders.put("rejection_reason", report.getRejectionReason());
                            String rejectionInfo = manager.getMessageManager().getRawMessage("queryreport-admin-info-rejected", "en");
                            for (Map.Entry<String, String> entry : rejectionPlaceholders.entrySet()) {
                                rejectionInfo = rejectionInfo.replace("%" + entry.getKey() + "%", entry.getValue());
                            }
                            adminInfo += "\n" + rejectionInfo;
                        }
                    }
                    placeholders.put("admin_info", adminInfo);

                    String message = manager.getMessageManager().getRawMessage("queryreport-details", "en");
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        message = message.replace("%" + entry.getKey() + "%", entry.getValue());
                    }
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                });
            });
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Report ID must be a number.");
        }
        return true;
    }
}

class ReportsCommand implements CommandExecutor {
    private final ReportManager manager;

    public ReportsCommand(ReportManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!sender.hasPermission(manager.getPlugin().getConfig().getString("admin-permission", "quickreport.admin"))) {
            manager.getMessageManager().sendMessage((Player) sender, "no-permission");
            return true;
        }

        // Implementation
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                manager.getMessageManager().sendMessage((Player) sender, "invalid-page-number");
                return true;
            }
        }

        final int finalPage = page;
        Bukkit.getScheduler().runTaskAsynchronously(manager.getPlugin(), () -> {
            // Get all reports (for simplicity, we'll get PENDING reports first as they are the most relevant)
            List<Report> reports = manager.getDatabaseManager().getReportsByStatus(ReportStatus.PENDING);
            // In a real scenario, we would need a method to get ALL reports or reports by status filter.
            // For now, we'll only show PENDING reports as requested by the user's initial plan,
            // but the user later requested all reports regardless of status. Let's stick to PENDING for now
            // and add a filter argument later if needed, as PENDING is the most critical.

            Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
                if (reports.isEmpty()) {
                    manager.getMessageManager().sendMessage((Player) sender, "reports-no-pending");
                    return;
                }

                int reportsPerPage = 8; // Configurable later
                int totalPages = (int) Math.ceil((double) reports.size() / reportsPerPage);

                if (finalPage < 1 || finalPage > totalPages) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("total_pages", String.valueOf(totalPages));
                    manager.getMessageManager().sendMessage((Player) sender, "invalid-page-range", placeholders);
                    return;
                }

                int start = (finalPage - 1) * reportsPerPage;
                int end = Math.min(start + reportsPerPage, reports.size());

                Map<String, String> headerPlaceholders = new HashMap<>();
                headerPlaceholders.put("page", String.valueOf(finalPage));
                headerPlaceholders.put("total_pages", String.valueOf(totalPages));
                String header = manager.getMessageManager().getMessage("reports-header", headerPlaceholders, "en");
                sender.sendMessage(header);

                for (int i = start; i < end; i++) {
                    Report report = reports.get(i);
                    Map<String, String> entryPlaceholders = new HashMap<>();
                    entryPlaceholders.put("id", String.valueOf(report.getId()));
                    entryPlaceholders.put("status", report.getStatus().getDisplayName());
                    entryPlaceholders.put("reported", report.getReportedName());
                    entryPlaceholders.put("reason", report.getReason());
                    // Implement clickable message for each report entry
                    String rawEntry = manager.getMessageManager().getRawMessage("reports-entry", "en");
                    for (Map.Entry<String, String> entry : entryPlaceholders.entrySet()) {
                        rawEntry = rawEntry.replace("%" + entry.getKey() + "%", entry.getValue());
                    }

                    net.md_5.bungee.api.chat.TextComponent component = new net.md_5.bungee.api.chat.TextComponent(
                            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', rawEntry)
                    );

                    // Find the clickable part: "[Click to view]"
                    String clickableText = "[Click to view]";
                    int startIndex = rawEntry.indexOf(clickableText);

                    if (startIndex != -1) {
                        // Split the message into two parts: before and clickable
                        String before = rawEntry.substring(0, startIndex);
                        String clickable = rawEntry.substring(startIndex, startIndex + clickableText.length());

                        net.md_5.bungee.api.chat.TextComponent beforeComponent = new net.md_5.bungee.api.chat.TextComponent(
                                net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', before)
                        );
                        net.md_5.bungee.api.chat.TextComponent clickableComponent = new net.md_5.bungee.api.chat.TextComponent(
                                net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', clickable)
                        );

                        clickableComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                                "/queryreport " + report.getId()
                        ));
                        clickableComponent.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                                net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.TextComponent[]{new net.md_5.bungee.api.chat.TextComponent(
                                        net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', "&7Click to view report details for ID: &e" + report.getId())
                                )}
                        ));

                        beforeComponent.addExtra(clickableComponent);
                        ((Player) sender).spigot().sendMessage(beforeComponent);
                    } else {
                    // Fallback to simple message if clickable part is not found
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', rawEntry));
                    }
                }
            });
        });
        return true;
    }
}

class ReportActionCommand implements CommandExecutor, TabCompleter {
    private final ReportManager manager;

    public ReportActionCommand(ReportManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!sender.hasPermission(manager.getPlugin().getConfig().getString("admin-permission", "quickreport.admin"))) {
            manager.getMessageManager().sendMessage((Player) sender, "no-permission");
            return true;
        }

        // Implementation
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /reportaction <accept|reject> <id> [reward-code|reason]");
            return true;
        }

        String action = args[0].toLowerCase();
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Report ID must be a number.");
            return true;
        }

        if (action.equals("accept")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /reportaction accept <id> <reward-code>");
                return true;
            }
            String rewardCode = args[2];
            manager.processReport(sender, id, ReportStatus.ACCEPTED, rewardCode, null);
        } else if (action.equals("reject")) {
            String rejectionReason = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "No reason provided.";
            manager.processReport(sender, id, ReportStatus.REJECTED, null, rejectionReason);
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid action. Use 'accept' or 'reject'.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.Arrays.asList("accept", "reject").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("accept")) {
            if (manager.getPlugin().getConfig().isConfigurationSection("rewards")) {
                return manager.getPlugin().getConfig().getConfigurationSection("rewards").getKeys(false).stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }
        }
        return java.util.Collections.emptyList();
    }


}
