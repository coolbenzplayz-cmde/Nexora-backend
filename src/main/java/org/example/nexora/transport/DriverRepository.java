package org.example.nexora.transport;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Driver entities
 */
@Repository
public class DriverRepository {
    
    public Optional<Driver> findById(Long id) { return Optional.empty(); }
    public Driver save(Driver driver) { return driver; }
    public List<Driver> findAll() { return List.of(); }
    public void deleteById(Long id) {}
    public List<Driver> findByAvailableTrue() { return List.of(); }
    public List<Driver> findByLocationNear(String location) { return List.of(); }
}
