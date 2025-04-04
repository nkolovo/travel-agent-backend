package com.travelagent.app.controllers;

import com.travelagent.app.models.Client;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.models.User;
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
    public List<Itinerary> getAllItineraries(@RequestParam(required = false) String reservationNumber,
            @RequestParam(required = false) String leadName) {
        System.out.println("Received reservationNumber: " + reservationNumber + ", leadName: " + leadName);
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
    public Itinerary getItineraryById(@PathVariable Long id) {
        return itineraryService.getItineraryById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Long>> createItinerary(@RequestBody Itinerary itinerary) {
        try {
            // Handle user relationship
            if (itinerary.getUser() != null) {
                String username = itinerary.getUser().getUsername();
                User user = userService.getUserByUsername(username);
                itinerary.setUser(user);
            }

            // Handle client if existing or create if new
            if (itinerary.getClient() != null) {
                Optional<Client> returningClient = clientService.getClientByName(itinerary.getClient().getName());
                if (returningClient.isPresent()) {
                    // Returning client, setting itinerary to existing client
                    Client client = returningClient.get();
                    System.out.println(client);
                    itinerary.setClient(client);
                } else {
                    // New client, creating client & setting itinerary to them
                    Client newClient = itinerary.getClient();
                    Client savedClient = clientService.saveClient(newClient);
                    itinerary.setClient(savedClient);
                }
            }

            // Ensure that the dates list is initialized if not provided
            if (itinerary.getDates() == null) {
                itinerary.setDates(new ArrayList<>());
            }

            // If image is not provided, make sure it is set to null
            if (itinerary.getImage() != null && itinerary.getImage().length > 0) {
                byte[] decodedImage = Base64.getDecoder().decode(itinerary.getImage());
                itinerary.setImage(decodedImage);
            } else {
                itinerary.setImage(null); // Set image to null if not provided
            }

            // Save the itinerary to the database
            Itinerary savedItinerary = itineraryService.saveItinerary(itinerary);

            Map<String, Long> response = Map.of("id", savedItinerary.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return an error message with a Map
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

            System.out.println("Fetching itinerary to update");
            Long itineraryId = Long.valueOf((int) updates.get("itineraryId"));
            // Update the itinerary object in the database (assuming you fetch it first)
            Itinerary itinerary = itineraryService.getItineraryById(itineraryId);
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
        Itinerary itinerary = itineraryService.getItineraryById(id);
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

            Itinerary itinerary = itineraryService.getItineraryById(id);
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
        Itinerary itinerary = itineraryService.getItineraryById(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, itinerary.getImageType()) // Dynamically set MIME type
                .body(itinerary.getImage());

    }

    @PostMapping("/add/date/{itineraryId}")
    public Date AddDateToItinerary(@PathVariable Long itineraryId, @RequestBody Date date) {
        return itineraryService.addDateToItinerary(itineraryId, date);
    }

    @PatchMapping("/update/date")
    public Date UpdateDateForItinerary(@RequestBody Date date) {
        return itineraryService.updateDateForItinerary(date);
    }

    @PostMapping("/remove/date/{dateId}/{itineraryId}")
    public boolean RemoveDateFromItinerary(@PathVariable Long dateId, @PathVariable Long itineraryId) {
        return itineraryService.removeDateFromItinerary(dateId, itineraryId);
    }
}
