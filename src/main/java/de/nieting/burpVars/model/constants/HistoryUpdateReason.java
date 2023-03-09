package de.nieting.burpVars.model.constants;

public enum HistoryUpdateReason {
    MANUALLY("Manually updated"),
    AUTOMATICALLY("Automatically updated"),
    REPLACED("Replaced"),
    AUTOMATICALLY_AND_REPLACED("Replaced and automatically updated");

    private final String reason;

    private HistoryUpdateReason(String r) {
        this.reason = r;
    }

    public String getReason() {
        return reason;
    }
}
