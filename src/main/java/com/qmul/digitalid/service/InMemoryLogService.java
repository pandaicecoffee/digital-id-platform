package com.qmul.digitalid.service;

import com.qmul.digitalid.model.LogEvent;
import com.qmul.digitalid.model.LogEventType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryLogService implements LogService {

    private final List<LogEvent> logEvents = new ArrayList<>();

    @Override
    public void record(LogEventType logEventType, String digitalIdRef, String performedBy, String description) {
        logEvents.add(new LogEvent(logEventType, digitalIdRef, performedBy, description));
    }

    @Override
    public List<LogEvent> getAll() {
        return List.copyOf(logEvents);
    }

    @Override
    public List<LogEvent> getByDigitalId(String digitalIdRef) {
        return logEvents.stream()
                .filter(e -> e.getDigitalIdRef().equals(digitalIdRef))
                .collect(Collectors.toList());
    }

    @Override
    public void printAll() {
        logEvents.forEach(System.out::println);
    }

}
