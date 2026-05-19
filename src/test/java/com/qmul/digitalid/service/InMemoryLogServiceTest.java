package com.qmul.digitalid.service;

import com.qmul.digitalid.model.LogEvent;
import com.qmul.digitalid.model.LogEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLogServiceTest {

    private InMemoryLogService logService;

    @BeforeEach
    void setUp() {
        logService = new InMemoryLogService();
    }

    @Test
    void recordedEventAppearsInGetAll() {
        logService.record(LogEventType.IDENTITY_CREATED, "id-001", "Central Authority", "Created");
        List<LogEvent> all = logService.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void multipleEventsAreAllStored() {
        logService.record(LogEventType.IDENTITY_CREATED, "id-001", "Authority", "Created");
        logService.record(LogEventType.STATUS_SUSPENDED, "id-001", "Authority", "Suspended");
        logService.record(LogEventType.VERIFICATION_SUCCESS, "id-001", "Bank Portal", "Verified");
        assertEquals(3, logService.getAll().size());
    }

    @Test
    void getByDigitalIdReturnsOnlyMatchingEvents() {
        logService.record(LogEventType.IDENTITY_CREATED, "id-001", "Authority", "Created id-001");
        logService.record(LogEventType.IDENTITY_CREATED, "id-002", "Authority", "Created id-002");
        logService.record(LogEventType.STATUS_SUSPENDED, "id-001", "Authority", "Suspended id-001");

        List<LogEvent> events = logService.getByDigitalId("id-001");
        assertEquals(2, events.size());
        assertTrue(events.stream().allMatch(e -> e.getDigitalIdRef().equals("id-001")));
    }

    @Test
    void getByDigitalIdReturnsEmptyListForUnknownId() {
        logService.record(LogEventType.IDENTITY_CREATED, "id-001", "Authority", "Created");
        List<LogEvent> events = logService.getByDigitalId("id-999");
        assertTrue(events.isEmpty());
    }

    @Test
    void getAllReturnsEmptyListWhenNoEventsRecorded() {
        assertTrue(logService.getAll().isEmpty());
    }

    @Test
    void recordedEventHasCorrectType() {
        logService.record(LogEventType.STATUS_REVOKED, "id-001", "Authority", "Revoked");
        LogEvent event = logService.getAll().get(0);
        assertEquals(LogEventType.STATUS_REVOKED, event.getType());
    }

    @Test
    void recordedEventHasCorrectDigitalIdRef() {
        logService.record(LogEventType.IDENTITY_CREATED, "id-abc", "Authority", "Created");
        LogEvent event = logService.getAll().get(0);
        assertEquals("id-abc", event.getDigitalIdRef());
    }

    @Test
    void recordedEventHasCorrectPerformedBy() {
        logService.record(LogEventType.VERIFICATION_SUCCESS, "id-001", "Tax Authority", "Verified");
        LogEvent event = logService.getAll().get(0);
        assertEquals("Tax Authority", event.getPerformedBy());
    }

    @Test
    void getAllReturnsCopyAndNotLiveList() {
        logService.record(LogEventType.IDENTITY_CREATED, "id-001", "Authority", "Created");
        List<LogEvent> snapshot = logService.getAll();
        logService.record(LogEventType.STATUS_SUSPENDED, "id-001", "Authority", "Suspended");
        // The snapshot taken before the second record should still have size 1
        assertEquals(1, snapshot.size());
    }
}