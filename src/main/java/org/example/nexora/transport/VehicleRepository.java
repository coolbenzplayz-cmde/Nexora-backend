package org.example.nexora.transport;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Vehicle entities
 */
@Repository
public class VehicleRepository {
    
    public Optional<Vehicle> findById(Long id) { return Optional.empty(); }
    public Vehicle save(Vehicle vehicle) { return vehicle; }
    public List<Vehicle> findAll() { return List.of(); }
    public void deleteById(Long id) {}
    public List<Vehicle> findByAvailableTrue() { return List.of(); }
    public List<Vehicle> findByType(String type) { return List.of(); }
}
