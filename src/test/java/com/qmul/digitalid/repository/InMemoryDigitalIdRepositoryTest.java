package com.qmul.digitalid.repository;

import com.qmul.digitalid.model.DigitalID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDigitalIdRepositoryTest {

    private InMemoryDigitalIdRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
    }

    private DigitalID buildIdentity(String id, String nin) {
        return new DigitalID(id, nin, "Alice", "Smith",
                LocalDate.of(1990, 1, 1), "London", "British");
    }

    @Test
    void savedIdentityCanBeFoundById() {
        DigitalID identity = buildIdentity("id-001", "NIN-001");
        repository.save(identity);
        Optional<DigitalID> found = repository.findById("id-001");
        assertTrue(found.isPresent());
        assertEquals("id-001", found.get().getId());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        Optional<DigitalID> result = repository.findById("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void savedIdentityCanBeFoundByNationalIdNumber() {
        DigitalID identity = buildIdentity("id-001", "NIN-001");
        repository.save(identity);
        Optional<DigitalID> found = repository.findByNationalIdNumber("NIN-001");
        assertTrue(found.isPresent());
        assertEquals("NIN-001", found.get().getNationalIdNumber());
    }

    @Test
    void findByNationalIdNumberReturnsEmptyForUnknownNin() {
        Optional<DigitalID> result = repository.findByNationalIdNumber("NIN-UNKNOWN");
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllReturnsAllSavedIdentities() {
        repository.save(buildIdentity("id-001", "NIN-001"));
        repository.save(buildIdentity("id-002", "NIN-002"));
        List<DigitalID> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void findAllReturnsEmptyListWhenNoIdentitiesSaved() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void existsByNationalIdNumberReturnsTrueWhenPresent() {
        repository.save(buildIdentity("id-001", "NIN-001"));
        assertTrue(repository.existsByNationalIdNumber("NIN-001"));
    }

    @Test
    void existsByNationalIdNumberReturnsFalseWhenAbsent() {
        assertFalse(repository.existsByNationalIdNumber("NIN-MISSING"));
    }

    @Test
    void savingUpdatedIdentityOverwritesPreviousEntry() {
        DigitalID identity = buildIdentity("id-001", "NIN-001");
        repository.save(identity);
        identity.suspend();
        repository.save(identity);
        DigitalID retrieved = repository.findById("id-001").orElseThrow();
        assertEquals(com.qmul.digitalid.model.DigitalIDStatus.SUSPENDED, retrieved.getStatus());
    }
}