package com.qmul.digitalid.service;

import com.qmul.digitalid.model.LogEvent;
import com.qmul.digitalid.model.LogEventType;

import java.util.ArrayList;
import java.util.List;

public class InMemoryLogService {

    private final List<LogEvent> logEvents = new ArrayList<>();

    @Override
    public void record(LogEventType logEventType, String digitalIdRef, String performedBy, String description) {
        logEvents.add(new LogEvent(logEventType, digitalIdRef, performedBy, description));
    }

    @Override
    public List<LogEvent> getAll() {
        return List.copyOf(logEvents);
    }

}
