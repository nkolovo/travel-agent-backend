package com.travelagent.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travelagent.app.models.Traveler;

public interface TravelerRepository extends JpaRepository<Traveler, Long> {
    Optional<Traveler> findById(Long id);
}
