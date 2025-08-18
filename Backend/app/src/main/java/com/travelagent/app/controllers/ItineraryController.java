package com.travelagent.app.controllers;

import com.travelagent.app.models.Client;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.models.User;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItineraryDto;

import com.travelagent.app.services.ClientService;
import com.travelagent.app.services.ItineraryService;
import com.travelagent.app.services.UserService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final UserService userService;
    private final ClientService clientService;

    public ItineraryController(ItineraryService itineraryService, UserService userService,
            ClientService clientService) {
        this.itineraryService = itineraryService;
        this.userService = userService;
        this.clientService = clientService;
    }

    @GetMapping
    public List<ItineraryDto> getAllItineraries(@RequestParam(required = false) String reservationNumber,
            @RequestParam(required = false) String leadName) {
        if (reservationNumber != null || leadName != null) {
            // Call the service to filter itineraries based on the reservationNumber and/or
            // leadName
            return itineraryService.getItinerariesByFilters(reservationNumber, leadName);
        } else {
            // Return all itineraries if no filters are provided
            return itineraryService.getAllItineraries();
        }
    }

    @GetMapping("/{id}")
    public ItineraryDto getItineraryById(@PathVariable Long id) {
        return itineraryService.getItineraryById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Long>> createItinerary(@RequestBody ItineraryDto itineraryDto) {
        try {
            Itinerary itinerary = mapToItinerary(itineraryDto);

            // Handle user relationship
            if (itineraryDto.getAgent() != null) {
                User user = userService.getUserByUsername(itineraryDto.getAgent());
                itinerary.setUser(user);
            }

            // Handle client relationship
            if (itineraryDto.getClientName() != null) {
                Client returningClient = clientService.getClientByName(itineraryDto.getClientName());
                if (returningClient != null) {
                    itinerary.setClient(returningClient);
                } else {
                    Client savedClient = clientService.saveClient(new Client(itineraryDto.getClientName()));
                    itinerary.setClient(savedClient);
                }
            }

            // Ensure that the dates list is initialized if not provided
            if (itinerary.getDates() == null) {
                itinerary.setDates(new ArrayList<>());
            }

            // Save the itinerary to the database
            Itinerary savedItinerary = itineraryService.saveItinerary(itinerary);

            Map<String, Long> response = Map.of("id", savedItinerary.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Long> errorResponse = new HashMap<>();
            errorResponse.put(e.getMessage(), 422L);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateItinerary(@RequestBody Map<String, Object> updates) {
        try {
            String title = (String) updates.get("title");
            int tripCost = (int) updates.get("tripCost");
            String coverImage = (String) updates.get("coverImage");

            Long itineraryId = Long.valueOf((int) updates.get("itineraryId"));
            ItineraryDto itineraryDto = itineraryService.getItineraryById(itineraryId);
            Itinerary itinerary = mapToItinerary(itineraryDto);

            itinerary.setName(title);
            itinerary.setTripPrice(tripCost);
            System.out.println("Image string: " + coverImage);
            itinerary.setImage(Base64.getDecoder().decode(coverImage)); // If storing images as bytes
            System.out.println("Image bytes: " + itinerary.getImage());
            itinerary.setEditedDate(LocalDateTime.now());
            itineraryService.saveItinerary(itinerary);
            return ResponseEntity.ok("Itinerary updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating itinerary: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteItinerary(@PathVariable Long id) {
        itineraryService.deleteItinerary(id);
    }

    @GetMapping("latest-reservation")
    public String getLatestReservation() {
        String lastReservation = itineraryService.getLatestReservationNumber();
        if (lastReservation == null)
            return String.format("PG%06d", 1);

        int lastNumber = Integer.parseInt(lastReservation.replaceAll("^PG", ""));
        int newNumber = lastNumber + 1;
        return String.format("PG%06d", newNumber);
    }

    @PostMapping("/update/name/{id}")
    public String updateItineraryName(@PathVariable Long id, @RequestBody String name) {
        ItineraryDto itineraryDto = itineraryService.getItineraryById(id);
        Itinerary itinerary = mapToItinerary(itineraryDto);
        itinerary.setName(name);
        itineraryService.saveItinerary(itinerary);
        return "Itinerary name updated.";
    }

    // Upload Image (Accepts Any Image Format)
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type! Only images allowed.");
            }

            ItineraryDto itineraryDto = itineraryService.getItineraryById(id);
            Itinerary itinerary = mapToItinerary(itineraryDto);
            itinerary.setImage(file.getBytes());
            itinerary.setImageType(file.getContentType()); // Save MIME type
            itineraryService.saveItinerary(itinerary);
            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
        }
    }

    // Retrieve Image (Returns Correct Format)
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        ItineraryDto itineraryDto = itineraryService.getItineraryById(id);
        Itinerary itinerary = mapToItinerary(itineraryDto);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, itinerary.getImageType()) // Dynamically set MIME type
                .body(itinerary.getImage());

    }

    @PostMapping("/add/date/{itineraryId}")
    public Date AddDateToItinerary(@PathVariable Long itineraryId, @RequestBody DateDto date) {
        return itineraryService.addDateToItinerary(itineraryId, date);
    }

    @PatchMapping("/update/date")
    public DateDto UpdateDateForItinerary(@RequestBody DateDto date) {
        return itineraryService.updateDateForItinerary(date);
    }

    @PostMapping("/remove/date/{dateId}/{itineraryId}")
    public boolean RemoveDateFromItinerary(@PathVariable Long dateId, @PathVariable Long itineraryId) {
        return itineraryService.removeDateFromItinerary(dateId, itineraryId);
    }

    private Itinerary mapToItinerary(ItineraryDto itineraryDto) {
        Itinerary itinerary = new Itinerary();
        itinerary.setId(itineraryDto.getId());
        itinerary.setName(itineraryDto.getName());
        itinerary.setAgent(itineraryDto.getAgent());
        itinerary.setCreatedDate(itineraryDto.getCreatedDate());
        itinerary.setEditedDate(itineraryDto.getEditedDate());
        itinerary.setDateSold(itineraryDto.getDateSold());
        itinerary.setReservationNumber(itineraryDto.getReservationNumber());
        itinerary.setLeadName(itineraryDto.getLeadName());
        itinerary.setNumTravelers(itineraryDto.getNumTravelers());
        itinerary.setArrivalDate(itineraryDto.getArrivalDate());
        itinerary.setDepartureDate(itineraryDto.getDepartureDate());
        itinerary.setTripPrice(itineraryDto.getTripPrice());
        itinerary.setStatus(itineraryDto.getStatus());
        itinerary.setDocsSent(itineraryDto.isDocsSent());
        return itinerary;
    }
}
