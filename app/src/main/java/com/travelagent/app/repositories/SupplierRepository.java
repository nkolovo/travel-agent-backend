package com.travelagent.app.repositories;

import com.travelagent.app.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    // Find supplier by name
    Optional<Supplier> findByName(String name);
    
    // Find all active (non-deleted) suppliers
    @Query("SELECT s FROM Supplier s WHERE s.deleted = false")
    List<Supplier> findAllActive();
    
    // Find active supplier by ID
    @Query("SELECT s FROM Supplier s WHERE s.id = :id AND s.deleted = false")
    Optional<Supplier> findActiveById(@Param("id") Long id);
}