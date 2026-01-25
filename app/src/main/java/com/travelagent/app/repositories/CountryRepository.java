package com.travelagent.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travelagent.app.models.Country;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findById(Long id);

    Optional<Country> findByName(String name);
}
