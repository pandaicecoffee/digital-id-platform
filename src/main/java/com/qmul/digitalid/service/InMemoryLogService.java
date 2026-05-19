package com.qmul.digitalid.service;

import com.qmul.digitalid.model.LogEvent;
import com.qmul.digitalid.model.LogEventType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryLogService implements LogService {

    private final List<LogEvent> logEvents = new ArrayList<>();

    @Override
    public void record(LogEventType logEventType, String digitalIdRef, String performedBy, String description) {
        logEvents.add(new LogEvent(logEventType, digitalIdRef, performedBy, description));
    }

    public void recordWithTimestamp(LogEventType logEventType, String digitalIdRef,
                                    String performedBy, String description,
                                    LocalDateTime timestamp) {
        logEvents.add(new LogEvent(logEventType, digitalIdRef, performedBy, description, timestamp));
    }

    @Override
    public List<LogEvent> getAll() {
        return List.copyOf(logEvents);
    }

    @Override
    public List<LogEvent> getByDigitalId(String digitalIdRef) {
        return logEvents.stream()
                .filter(e -> e.getDigitalIdRef().equals(digitalIdRef))
                .toList();
    }

    @Override
    public void printAll() {
        logEvents.forEach(System.out::println);
    }

}
