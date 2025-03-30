package com.travelagent.app.repositories;

import com.travelagent.app.models.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    Optional<Itinerary> findById(Long id);

    @Query(value = "SELECT reservation_number FROM itinerary WHERE CAST(SUBSTRING(reservation_number, 3) AS INT) = (SELECT MAX(CAST(SUBSTRING(reservation_number, 3) AS INT)) FROM itinerary)", nativeQuery = true)
    String findNewestReservationNumber();

    List<Itinerary> findByReservationNumberIgnoreCaseAndLeadNameContainingIgnoreCase(String reservationNumber,
            String leadName);

    List<Itinerary> findByReservationNumberIgnoreCase(String reservationNumber);

    List<Itinerary> findByLeadNameContainingIgnoreCase(String leadName);

}
