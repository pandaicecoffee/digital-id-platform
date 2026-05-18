package com.qmul.digitalid.repository;

import com.qmul.digitalid.model.DigitalID;
import java.util.*;

public class InMemoryDigitalIdRepository implements DigitalIdRepository {

    private final Map<String, DigitalID> store = new HashMap<>();

    @Override
    public void save(DigitalID digitalID) {
        store.put(digitalID.getId(), digitalID);
    }

    @Override
    public Optional<DigitalID> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<DigitalID> findByNationalIdNumber(String nationalIdNumber) {
        return store.values().stream()
                .filter(id -> id.getNationalIdNumber().equals(nationalIdNumber))
                .findFirst();
    }

    @Override
    public List<DigitalID> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public boolean existsByNationalIdNumber(String nationalIdNumber) {
        return store.values().stream()
                .anyMatch(id -> id.getNationalIdNumber().equals(nationalIdNumber));
    }
}