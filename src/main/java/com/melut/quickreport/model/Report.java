package com.manus.quickreport.model;

import java.util.UUID;

public class Report {
    private final int id;
    private final UUID reporterUUID;
    private final String reporterName;
    private final UUID reportedUUID;
    private final String reportedName;
    private final String reason;
    private final String details;
    private final long timestamp;
    private ReportStatus status;
    private UUID adminUUID;
    private String adminName;
    private String rejectionReason;

    public Report(int id, UUID reporterUUID, String reporterName, UUID reportedUUID, String reportedName, String reason, String details, long timestamp, ReportStatus status, UUID adminUUID, String adminName, String rejectionReason) {
        this.id = id;
        this.reporterUUID = reporterUUID;
        this.reporterName = reporterName;
        this.reportedUUID = reportedUUID;
        this.reportedName = reportedName;
        this.reason = reason;
        this.details = details;
        this.timestamp = timestamp;
        this.status = status;
        this.adminUUID = adminUUID;
        this.adminName = adminName;
        this.rejectionReason = rejectionReason;
    }

    // Getters
    public int getId() { return id; }
    public UUID getReporterUUID() { return reporterUUID; }
    public String getReporterName() { return reporterName; }
    public UUID getReportedUUID() { return reportedUUID; }
    public String getReportedName() { return reportedName; }
    public String getReason() { return reason; }
    public String getDetails() { return details; }
    public long getTimestamp() { return timestamp; }
    public ReportStatus getStatus() { return status; }
    public UUID getAdminUUID() { return adminUUID; }
    public String getAdminName() { return adminName; }
    public String getRejectionReason() { return rejectionReason; }

    // Setters for mutable fields
    public void setStatus(ReportStatus status) { this.status = status; }
    public void setAdminUUID(UUID adminUUID) { this.adminUUID = adminUUID; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
