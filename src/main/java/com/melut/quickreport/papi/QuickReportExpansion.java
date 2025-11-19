package com.manus.quickreport.papi;

import com.manus.quickreport.QuickReport;
import com.manus.quickreport.model.ReportStatus;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class QuickReportExpansion extends PlaceholderExpansion {

    private final QuickReport plugin;

    public QuickReportExpansion(QuickReport plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "quickreport";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is a persistent expansion
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("kabul_edilen")) {
            // %quickreport_kabul_edilen%
            return String.valueOf(plugin.getDatabaseManager().getReportsByReporter(player.getUniqueId()).stream()
                    .filter(r -> r.getStatus() == ReportStatus.ACCEPTED)
                    .count());
        }

        if (params.equalsIgnoreCase("reddedilen")) {
            // %quickreport_reddedilen%
            return String.valueOf(plugin.getDatabaseManager().getReportsByReporter(player.getUniqueId()).stream()
                    .filter(r -> r.getStatus() == ReportStatus.REJECTED)
                    .count());
        }

        // Leaderboard placeholders: %quickreport_top_kabul_isim_<sıra>% ve %quickreport_top_kabul_sayi_<sıra>%
        if (params.startsWith("top_kabul_isim_")) {
            try {
                int rank = Integer.parseInt(params.substring("top_kabul_isim_".length()));
                return getTopReporterName(ReportStatus.ACCEPTED, rank);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (params.startsWith("top_kabul_sayi_")) {
            try {
                int rank = Integer.parseInt(params.substring("top_kabul_sayi_".length()));
                return getTopReporterCount(ReportStatus.ACCEPTED, rank);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (params.startsWith("top_red_isim_")) {
            try {
                int rank = Integer.parseInt(params.substring("top_red_isim_".length()));
                return getTopReporterName(ReportStatus.REJECTED, rank);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (params.startsWith("top_red_sayi_")) {
            try {
                int rank = Integer.parseInt(params.substring("top_red_sayi_".length()));
                return getTopReporterCount(ReportStatus.REJECTED, rank);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private String getTopReporterName(ReportStatus status, int rank) {
        Map<String, Integer> topReporters = plugin.getDatabaseManager().getTopReporters(status, rank);
        if (topReporters.size() >= rank) {
            return (String) topReporters.keySet().toArray()[rank - 1];
        }
        return "N/A";
    }

    private String getTopReporterCount(ReportStatus status, int rank) {
        Map<String, Integer> topReporters = plugin.getDatabaseManager().getTopReporters(status, rank);
        if (topReporters.size() >= rank) {
            return String.valueOf(topReporters.values().toArray()[rank - 1]);
        }
        return "0";
    }
}
