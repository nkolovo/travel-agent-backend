package com.travelagent.app.repositories;

import com.travelagent.app.models.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DateRepository extends JpaRepository<Date, Long> {
    Optional<Date> findById(Long id);
    Optional<List<Date>> findByItineraryId(Long itineraryId);
}
