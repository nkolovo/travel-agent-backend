package com.travelagent.app.repositories;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.models.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DateRepository extends JpaRepository<Date, Long> {
    Optional<Date> findById(Long id);

    @Query("SELECT d FROM Date d WHERE d.itinerary.id = :itinerary_id")
    List<Date> findAllByItineraryId(@Param("itinerary_id") Long itinerary_id);

    @Query("SELECT new com.travelagent.app.dto.DateDto(d.id, d.name, d.location, d.date) FROM Date d WHERE d.itinerary.id = :itinerary_id ORDER BY d.date ASC")
    List<DateDto> findAllDtosByItineraryId(@Param("itinerary_id") Long itinerary_id);
}
