package com.travelagent.app.services;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItineraryDto;
import com.travelagent.app.dto.TravelerDto;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.DateItem;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.models.Traveler;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItineraryRepository;
import com.travelagent.app.repositories.TravelerRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final DateRepository dateRepository;
    private final TravelerRepository travelerRepository;

    public ItineraryService(ItineraryRepository itineraryRepository, DateRepository dateRepository,
            TravelerRepository travelerRepository) {
        this.itineraryRepository = itineraryRepository;
        this.dateRepository = dateRepository;
        this.travelerRepository = travelerRepository;
    }

    public List<ItineraryDto> getAllItineraries() {
        return itineraryRepository.findAllByOrderByEditedDateDesc();
    }

    public List<ItineraryDto> getItinerariesByFilters(String reservationNumber, String leadName) {
        if (reservationNumber != null && leadName != null) {
            return itineraryRepository.findByReservationNumberIgnoreCaseAndLeadNameContainingIgnoreCase(
                    reservationNumber,
                    leadName);
        } else if (reservationNumber != null) {
            return itineraryRepository.findByReservationNumberIgnoreCase(reservationNumber);
        } else if (leadName != null) {
            return itineraryRepository.findByLeadNameContainingIgnoreCase(leadName);
        } else {
            return itineraryRepository.findAllDtos();
        }
    }

    public ItineraryDto getItineraryById(Long id) {
        Optional<ItineraryDto> itineraryDtoOpt = itineraryRepository.findByIdDto(id);
        if (itineraryDtoOpt.isPresent()) {
            ItineraryDto itineraryDto = itineraryDtoOpt.get();
            List<Date> dates = dateRepository.findAllByItineraryId(id);
            List<DateDto> dateDtos = dates.stream()
                    .map(date -> {
                        DateDto dateDto = new DateDto();
                        dateDto.setId(date.getId());
                        dateDto.setName(date.getName());
                        dateDto.setLocation(date.getLocation());
                        dateDto.setDate(date.getDate());
                        return dateDto;
                    })
                    .toList();
            itineraryDto.setDates(dateDtos);
            return itineraryDto;
        } else {
            throw new RuntimeException("Could not find itinerary with ID " + id);
        }
    }

    public Itinerary getEntityById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));
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

    public Date addDateToItinerary(Long itineraryId, DateDto date) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Could not find itinerary with ID " + itineraryId));
        itinerary.setEditedDate(LocalDateTime.now());

        List<DateDto> existingDates = dateRepository.findAllDtosByItineraryId(itineraryId);
        existingDates.add(date);

        LocalDate minDate = existingDates.stream()
                .map(d -> LocalDate.parse(d.getDate()))
                .min(Comparator.naturalOrder())
                .get();
        LocalDate maxDate = existingDates.stream()
                .map(d -> LocalDate.parse(d.getDate()))
                .max(Comparator.naturalOrder())
                .get();
        itinerary.setArrivalDate(minDate);
        itinerary.setDepartureDate(maxDate);

        Date dateToSave = mapToDate(date);
        dateToSave.setItinerary(itinerary);
        return dateRepository.save(dateToSave);
    }

    public DateDto updateDateForItinerary(DateDto dateDto) {
        Date existingDate = dateRepository.findById(dateDto.getId())
                .orElseThrow(() -> new RuntimeException("Could not find date with ID " + dateDto.getId()));

        Itinerary itinerary = existingDate.getItinerary();
        itinerary.setEditedDate(LocalDateTime.now());
        List<DateDto> existingDates = dateRepository.findAllDtosByItineraryId(itinerary.getId());
        if (!existingDates.isEmpty()) {
            LocalDate newDate = LocalDate.parse(dateDto.getDate());
            LocalDate minDate = newDate;
            LocalDate maxDate = newDate;

            for (DateDto d : existingDates) {
                LocalDate dDate = LocalDate.parse(d.getDate());
                if (dDate.isBefore(minDate))
                    minDate = dDate;
                if (dDate.isAfter(maxDate))
                    maxDate = dDate;
            }

            if (newDate.isBefore(minDate)) {
                itinerary.setArrivalDate(newDate);
            }
            if (newDate.isAfter(maxDate)) {
                itinerary.setDepartureDate(newDate);
            }
        } else {
            LocalDate newDate = LocalDate.parse(dateDto.getDate());
            itinerary.setArrivalDate(newDate);
            itinerary.setDepartureDate(newDate);
        }

        existingDate.setName(dateDto.getName());
        existingDate.setLocation(dateDto.getLocation());
        existingDate.setDate(dateDto.getDate());

        Date savedDate = dateRepository.save(existingDate);
        return mapToDateDto(savedDate);
    }

    public boolean removeDateFromItinerary(Long dateId, Long itineraryId) {
        try {
            itineraryRepository.findById(itineraryId).ifPresent(itinerary -> {
                // Get the date to be removed,
                // and update itinerary trip and net prices accordingly
                dateRepository.findById(dateId).ifPresent(date -> {
                    int dateItemsRetailTotal = 0;
                    int dateItemsNetTotal = 0;
                    for (DateItem di : date.getDateItems()) {
                        dateItemsRetailTotal += di.getRetailPrice();
                        dateItemsNetTotal += di.getNetPrice();
                    }
                    if (dateItemsRetailTotal > 0 || dateItemsNetTotal > 0) {
                        itinerary.setTripPrice(itinerary.getTripPrice() - dateItemsRetailTotal);
                        itinerary.setNetPrice(itinerary.getNetPrice() - dateItemsNetTotal);
                    }
                });
                // Remove the date
                itinerary.getDates().removeIf(date -> date.getId().equals(dateId));

                // Recompute arrival and departure dates if any dates remain
                List<Date> remainingDates = itinerary.getDates();
                if (!remainingDates.isEmpty()) {
                    LocalDate minDate = remainingDates.stream()
                            .map(d -> LocalDate.parse(d.getDate()))
                            .min(Comparator.naturalOrder())
                            .get();
                    LocalDate maxDate = remainingDates.stream()
                            .map(d -> LocalDate.parse(d.getDate()))
                            .max(Comparator.naturalOrder())
                            .get();
                    itinerary.setArrivalDate(minDate);
                    itinerary.setDepartureDate(maxDate);
                } else {
                    // No dates left, clear arrival/departure
                    itinerary.setArrivalDate(null);
                    itinerary.setDepartureDate(null);
                }

                itineraryRepository.save(itinerary);
            });
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Could not remove date from itinerary, exception: " + e.getMessage());
        }
    }

    public boolean saveNotesToItinerary(Long itineraryId, String notes) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Could not find itinerary with ID " + itineraryId));
        itinerary.setNotes(notes);
        itinerary.setEditedDate(LocalDateTime.now());
        itineraryRepository.save(itinerary);
        return true;
    }

    public List<Traveler> getTravelersForItinerary(Long itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Could not find itinerary with ID " + itineraryId));
        return itinerary.getTravelers();
    }

    public Long saveTravelerToItinerary(Long itineraryId, TravelerDto travelerDto) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Could not find itinerary with ID " + itineraryId));

        // Get existing traveler or create new one
        boolean existingTraveler = (travelerDto.getId() != null);
        Traveler traveler = existingTraveler
                ? travelerRepository.findById(travelerDto.getId())
                        .orElseThrow(
                                () -> new RuntimeException("Could not find traveler with ID " + travelerDto.getId()))
                : new Traveler();

        // Update traveler fields
        traveler.setFirstName(travelerDto.getFirstName());
        traveler.setLastName(travelerDto.getLastName());
        traveler.setDateOfBirth(travelerDto.getDateOfBirth());
        traveler.setEmail(travelerDto.getEmail());
        traveler.setPhone(travelerDto.getPhone());
        traveler.setPassportNumber(travelerDto.getPassportNumber());

        if (!existingTraveler)
            traveler.setItinerary(itinerary);

        return travelerRepository.save(traveler).getId();
    }

    public void removeTravelerFromItinerary(Long itineraryId, Long travelerId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Could not find itinerary with ID " + itineraryId));
        Traveler traveler = travelerRepository.findById(travelerId)
                .orElseThrow(() -> new RuntimeException("Could not find traveler with ID " + travelerId));

        itinerary.getTravelers().removeIf(t -> t.getId().equals(travelerId));

        travelerRepository.delete(traveler);
        itineraryRepository.save(itinerary);
    }

    private Date mapToDate(DateDto dateDto) {
        Date date = new Date();
        date.setId(dateDto.getId());
        date.setName(dateDto.getName());
        date.setLocation(dateDto.getLocation());
        date.setDate(dateDto.getDate());
        return date;
    }

    public DateDto mapToDateDto(Date date) {
        DateDto dateDto = new DateDto();
        dateDto.setId(date.getId());
        dateDto.setName(date.getName());
        dateDto.setLocation(date.getLocation());
        dateDto.setDate(date.getDate());
        return dateDto;
    }

}
