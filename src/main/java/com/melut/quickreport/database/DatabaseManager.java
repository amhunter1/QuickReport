package com.manus.quickreport.database;

import com.manus.quickreport.QuickReport;
import com.manus.quickreport.model.Report;
import com.manus.quickreport.model.ReportStatus;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final QuickReport plugin;
    private Connection connection;
    private final String databasePath;

    public DatabaseManager(QuickReport plugin) {
        this.plugin = plugin;
        this.databasePath = plugin.getDataFolder().getAbsolutePath() + File.separator + "reports.db";
    }

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            plugin.getLogger().info("SQLite connection established.");
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Could not connect to SQLite database: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("SQLite connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing SQLite connection: " + e.getMessage());
        }
    }

    public void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS reports ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "reporter_uuid TEXT NOT NULL,"
                + "reporter_name TEXT NOT NULL,"
                + "reported_uuid TEXT NOT NULL,"
                + "reported_name TEXT NOT NULL,"
                + "reason TEXT NOT NULL,"
                + "details TEXT,"
                + "timestamp INTEGER NOT NULL,"
                + "status TEXT NOT NULL DEFAULT 'PENDING',"
                + "admin_uuid TEXT,"
                + "admin_name TEXT,"
                + "rejection_reason TEXT"
                + ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Reports table checked/created.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating reports table: " + e.getMessage());
        }
    }

    public int saveReport(Report report) {
        String sql = "INSERT INTO reports (reporter_uuid, reporter_name, reported_uuid, reported_name, reason, details, timestamp, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, report.getReporterUUID().toString());
            pstmt.setString(2, report.getReporterName());
            pstmt.setString(3, report.getReportedUUID().toString());
            pstmt.setString(4, report.getReportedName());
            pstmt.setString(5, report.getReason());
            pstmt.setString(6, report.getDetails());
            pstmt.setLong(7, report.getTimestamp());
            pstmt.setString(8, report.getStatus().name());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving report: " + e.getMessage());
        }
        return -1;
    }

    public void updateReportStatus(int id, ReportStatus status, UUID adminUUID, String adminName, String rejectionReason) {
        String sql = "UPDATE reports SET status = ?, admin_uuid = ?, admin_name = ?, rejection_reason = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, adminUUID != null ? adminUUID.toString() : null);
            pstmt.setString(3, adminName);
            pstmt.setString(4, rejectionReason);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating report status: " + e.getMessage());
        }
    }

    public Report getReportById(int id) {
        String sql = "SELECT * FROM reports WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createReportFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting report by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Report> getReportsByStatus(ReportStatus status) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE status = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(createReportFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting reports by status: " + e.getMessage());
        }
        return reports;
    }

    public List<Report> getReportsByReporter(UUID reporterUUID) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE reporter_uuid = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reporterUUID.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(createReportFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting reports by reporter: " + e.getMessage());
        }
        return reports;
    }

    public Map<String, Integer> getTopReporters(ReportStatus status, int limit) {
        Map<String, Integer> topReporters = new HashMap<>();
        String sql = "SELECT reporter_name, COUNT(*) as count FROM reports WHERE status = ? GROUP BY reporter_name ORDER BY count DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topReporters.put(rs.getString("reporter_name"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting top reporters: " + e.getMessage());
        }
        return topReporters;
    }

    private Report createReportFromResultSet(ResultSet rs) throws SQLException {
        UUID adminUUID = rs.getString("admin_uuid") != null ? UUID.fromString(rs.getString("admin_uuid")) : null;
        return new Report(
                rs.getInt("id"),
                UUID.fromString(rs.getString("reporter_uuid")),
                rs.getString("reporter_name"),
                UUID.fromString(rs.getString("reported_uuid")),
                rs.getString("reported_name"),
                rs.getString("reason"),
                rs.getString("details"),
                rs.getLong("timestamp"),
                ReportStatus.valueOf(rs.getString("status")),
                adminUUID,
                rs.getString("admin_name"),
                rs.getString("rejection_reason")
        );
    }
}
