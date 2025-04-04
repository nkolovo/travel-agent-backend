package com.travelagent.app.services;

import com.travelagent.app.models.Date;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItineraryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final DateRepository dateRepository;

    public ItineraryService(ItineraryRepository itineraryRepository, DateRepository dateRepository) {
        this.itineraryRepository = itineraryRepository;
        this.dateRepository = dateRepository;
    }

    public List<Itinerary> getAllItineraries() {
        return itineraryRepository.findAllByOrderByEditedDateDesc();
    }

    public List<Itinerary> getItinerariesByFilters(String reservationNumber, String leadName) {
        System.out.println("In getItinerariesByFilters with reservationNumber: " + reservationNumber + " and leadName: "
                + leadName);
        if (reservationNumber != null && leadName != null) {
            return itineraryRepository.findByReservationNumberIgnoreCaseAndLeadNameContainingIgnoreCase(
                    reservationNumber,
                    leadName);
        } else if (reservationNumber != null) {
            return itineraryRepository.findByReservationNumberIgnoreCase(reservationNumber);
        } else if (leadName != null) {
            return itineraryRepository.findByLeadNameContainingIgnoreCase(leadName);
        } else {
            return itineraryRepository.findAll();
        }
    }

    public Itinerary getItineraryById(Long id) {
        System.out.println("Finding itinerary with ID: " + id);
        return itineraryRepository.findById(id).orElseThrow(() -> new RuntimeException("Itinerary not found"));
    }

    public String getLatestReservationNumber() {
        return itineraryRepository.findNewestReservationNumber();
    }

    public Itinerary saveItinerary(Itinerary itinerary) {
        return itineraryRepository.save(itinerary);
    }

    public void deleteItinerary(Long id) {
        itineraryRepository.deleteById(id);
    }

    public Date addDateToItinerary(Long itineraryId, Date date) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Could not find itinerary with ID " + itineraryId));
        date.setItinerary(itinerary);
        return dateRepository.save(date);
    }

    public Date updateDateForItinerary(Date date) {
        Date existingDate = dateRepository.findById(date.getId())
                .orElseThrow(() -> new RuntimeException("Could not find date with ID " + date.getId()));
        existingDate.setName(date.getName());
        existingDate.setLocation(date.getLocation());
        existingDate.setDate(date.getDate());
        return dateRepository.save(existingDate);
    }

    public boolean removeDateFromItinerary(Long dateId, Long itineraryId) {
        System.out.println("Removing date: " + dateId + " from itinerary: " + itineraryId);
        try {
            itineraryRepository.findById(itineraryId).ifPresent(itinerary -> {
                itinerary.getDates().removeIf(date -> date.getId().equals(dateId));
                itineraryRepository.save(itinerary);
            });
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Could not remove date from itinerary, exception: " + e.getMessage());
        }
    }
}
