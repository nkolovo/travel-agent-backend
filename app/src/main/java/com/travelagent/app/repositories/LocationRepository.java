package com.travelagent.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travelagent.app.models.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findById(Long id);

    Optional<Location> findByName(String name);
}
