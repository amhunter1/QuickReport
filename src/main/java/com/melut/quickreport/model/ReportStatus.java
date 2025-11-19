package com.manus.quickreport.model;

public enum ReportStatus {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
