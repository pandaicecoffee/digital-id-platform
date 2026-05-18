package com.qmul.digitalid.service;

import com.qmul.digitalid.model.LogEvent;
import com.qmul.digitalid.model.LogEventType;

import java.util.List;

public interface LogService {
    void record(LogEventType logEventType, String digitalIdRef, String performedBy, String description);
    List<LogEvent> getAll();
    List<LogEvent> getByDigitalId(String digitalIdRef);
    void printAll();
}



