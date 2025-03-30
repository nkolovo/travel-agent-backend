package com.travelagent.app.repositories;

import com.travelagent.app.models.Client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findById(Long id);

    Optional<Client> findByName(String name);
}
