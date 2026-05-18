package com.qmul.digitalid.repository;

import com.qmul.digitalid.model.DigitalID;
import java.util.List;
import java.util.Optional;

public interface DigitalIdRepository {
    void save(DigitalID digitalID);
    Optional<DigitalID> findById(String id);
    Optional<DigitalID> findByNationalIdNumber(String nationalIdNumber);
    List<DigitalID> findAll();
    boolean existsByNationalIdNumber(String nationalIdNumber);
}
