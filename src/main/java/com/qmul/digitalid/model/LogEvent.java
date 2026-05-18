package com.qmul.digitalid.model;

import java.time.LocalDateTime;

public class LogEvent {
    private final LogEventType type;
    private final String digitalIdRef;
    private final String performedBy;
    private final String description;
    private final LocalDateTime timestamp;

    public LogEvent(LogEventType type, String digitalIdRef, String performedBy, String description) {
        this.type = type;
        this.digitalIdRef = digitalIdRef;
        this.performedBy = performedBy;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public LogEventType getType() {
        return type;
    }
    public String getDigitalIdRef() {
        return digitalIdRef;
    }
    public String getPerformedBy() {
        return performedBy;
    }
    public String getDescription() {
        return description;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + type + " | ID: " + digitalIdRef +
                " | By: " + performedBy + " | " + description;
    }
}
