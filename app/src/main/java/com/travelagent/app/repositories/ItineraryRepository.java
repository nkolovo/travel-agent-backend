package com.travelagent.app.repositories;

import com.travelagent.app.models.Itinerary;

import com.travelagent.app.dto.ItineraryDto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
        @Query("SELECT new com.travelagent.app.dto.ItineraryDto(" +
                        "i.id, i.name, i.agent, i.createdDate, i.editedDate, i.dateSold, " +
                        "i.reservationNumber, i.leadName, i.numTravelers, i.arrivalDate, i.departureDate, " +
                        "i.tripPrice, i.netPrice, i.status, i.docsSent, i.imageName " +
                        ") " +
                        "FROM Itinerary i WHERE i.id = :id")
        Optional<ItineraryDto> findByIdDto(@Param("id") Long id);

        @Query("SELECT new com.travelagent.app.dto.ItineraryDto(i.id, i.agent, i.createdDate, i.editedDate, i.dateSold, i.reservationNumber, i.leadName, i.numTravelers, i.arrivalDate, i.departureDate, i.tripPrice, i.netPrice, i.status, i.docsSent) FROM Itinerary i ORDER BY i.editedDate DESC")
        List<ItineraryDto> findAllDtos();

        @Query("SELECT new com.travelagent.app.dto.ItineraryDto(" +
                        "i.id, i.agent, i.createdDate, i.editedDate, i.dateSold, " +
                        "i.reservationNumber, i.leadName, i.numTravelers, i.arrivalDate, i.departureDate, " +
                        "i.tripPrice, i.netPrice, i.status, i.docsSent) " +
                        "FROM Itinerary i ORDER BY i.editedDate DESC")
        List<ItineraryDto> findAllByOrderByEditedDateDesc();

        @Query(value = "SELECT reservation_number FROM itinerary WHERE CAST(SUBSTRING(reservation_number, 3) AS INT) = (SELECT MAX(CAST(SUBSTRING(reservation_number, 3) AS INT)) FROM itinerary)", nativeQuery = true)
        String findNewestReservationNumber();

        @Query("SELECT new com.travelagent.app.dto.ItineraryDto(" +
                        "i.id, i.agent, i.createdDate, i.editedDate, i.dateSold, " +
                        "i.reservationNumber, i.leadName, i.numTravelers, i.arrivalDate, i.departureDate, " +
                        "i.tripPrice, i.netPrice, i.status, i.docsSent) " +
                        "FROM Itinerary i WHERE LOWER(i.reservationNumber) = LOWER(:reservationNumber) AND LOWER(i.leadName) LIKE LOWER(CONCAT('%', :leadName, '%'))")
        List<ItineraryDto> findByReservationNumberIgnoreCaseAndLeadNameContainingIgnoreCase(
                        @Param("reservationNumber") String reservationNumber,
                        @Param("leadName") String leadName);

        @Query("SELECT new com.travelagent.app.dto.ItineraryDto(" +
                        "i.id, i.agent, i.createdDate, i.editedDate, i.dateSold, " +
                        "i.reservationNumber, i.leadName, i.numTravelers, i.arrivalDate, i.departureDate, " +
                        "i.tripPrice, i.netPrice, i.status, i.docsSent) " +
                        "FROM Itinerary i WHERE LOWER(i.reservationNumber) = LOWER(:reservationNumber)")
        List<ItineraryDto> findByReservationNumberIgnoreCase(@Param("reservationNumber") String reservationNumber);

        @Query("SELECT new com.travelagent.app.dto.ItineraryDto(" +
                        "i.id, i.agent, i.createdDate, i.editedDate, i.dateSold, " +
                        "i.reservationNumber, i.leadName, i.numTravelers, i.arrivalDate, i.departureDate, " +
                        "i.tripPrice, i.netPrice, i.status, i.docsSent) " +
                        "FROM Itinerary i WHERE LOWER(i.leadName) LIKE LOWER(CONCAT('%', :leadName, '%'))")
        List<ItineraryDto> findByLeadNameContainingIgnoreCase(@Param("leadName") String leadName);

}
