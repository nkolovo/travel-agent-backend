package com.travelagent.app.controllers;

import com.travelagent.app.models.Client;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.models.Traveler;
import com.travelagent.app.models.User;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.ItineraryDto;
import com.travelagent.app.dto.TravelerDto;
import com.travelagent.app.services.ClientService;
import com.travelagent.app.services.GcsImageService;
import com.travelagent.app.services.GcsPdfService;
import com.travelagent.app.services.ItineraryService;
import com.travelagent.app.services.UserService;
import com.travelagent.app.services.DateItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final UserService userService;
    private final ClientService clientService;
    private final DateItemService dateItemService;

    @Autowired
    private GcsImageService gcsImageService;
    @Autowired
    private GcsPdfService gcsPdfService;
    @Autowired
    private SpringTemplateEngine templateEngine;

    public ItineraryController(ItineraryService itineraryService, UserService userService,
            ClientService clientService, DateItemService dateItemService) {
        this.itineraryService = itineraryService;
        this.userService = userService;
        this.clientService = clientService;
        this.dateItemService = dateItemService;
    }

    @GetMapping
    public List<ItineraryDto> getAllItineraries(@RequestParam(required = false) String reservationNumber,
            @RequestParam(required = false) String leadName) {
        if (reservationNumber != null || leadName != null) {
            return itineraryService.getItinerariesByFilters(reservationNumber, leadName);
        } else {
            return itineraryService.getAllItineraries();
        }
    }

    @GetMapping("/{id}")
    public ItineraryDto getItineraryById(@PathVariable Long id) {
        ItineraryDto itineraryDto = itineraryService.getItineraryById(id);
        if (itineraryDto.getImageName() != null) {
            String signedUrl = gcsImageService.getSignedUrl(itineraryDto.getImageName());
            itineraryDto.setCoverImageUrl(signedUrl);
        }
        return itineraryDto;
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
            Long itineraryId = Long.valueOf((int) updates.get("itineraryId"));
            Itinerary itinerary = itineraryService.getEntityById(itineraryId);

            String title = (String) updates.get("title");
            int tripCost = (int) updates.get("tripCost");
            int netCost = (int) updates.get("netCost");

            itinerary.setName(title);
            itinerary.setTripPrice(tripCost);
            itinerary.setNetPrice(netCost);
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

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type! Only images allowed.");
            }

            String fileName = file.getOriginalFilename();

            if (!gcsImageService.doesImageExist(fileName)) {
                // Upload to GCS
                gcsImageService.uploadImage(file, fileName);
            }

            // Save the file name in the itinerary
            Itinerary itinerary = itineraryService.getEntityById(id);
            itinerary.setImageName(fileName);
            itineraryService.saveItinerary(itinerary);

            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
        }
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

    @PostMapping("/{itineraryId}/notes/save")
    public boolean saveNotesToItinerary(@PathVariable Long itineraryId, @RequestBody String notes) {
        return itineraryService.saveNotesToItinerary(itineraryId, notes);
    }

    @GetMapping("/{itineraryId}/travelers")
    public List<Traveler> getTravelers(@PathVariable Long itineraryId) {
        return itineraryService.getTravelersForItinerary(itineraryId);
    }

    @PostMapping("/{itineraryId}/traveler")
    public Long saveTraveler(@PathVariable Long itineraryId, @RequestBody TravelerDto traveler) {
        return itineraryService.saveTravelerToItinerary(itineraryId, traveler);
    }

    @DeleteMapping("/{itineraryId}/traveler/{travelerId}")
    public void removeTraveler(@PathVariable Long itineraryId, @PathVariable Long travelerId) {
        itineraryService.removeTravelerFromItinerary(itineraryId, travelerId);
    }

    @GetMapping("generate-pdf/{id}")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) throws IOException {
        ItineraryDto itinerary = itineraryService.getItineraryById(id);
        List<DateDto> dates = new ArrayList<>(itinerary.getDates());
        dates.sort(Comparator.comparing(DateDto::getDate));
        itinerary.setDates(dates);

        // Collect all DateItemDtos for all dates in the itinerary
        List<DateItemDto> allDateItemDtos = new ArrayList<>();
        for (DateDto date : dates) {
            List<DateItemDto> dateItems = dateItemService.getDateItemsByDate(date.getId());
            for (DateItemDto dto : dateItems) {
                if (dto.getImageNames() != null && !dto.getImageNames().isEmpty()) {
                    // Generate signed URLs for all images
                    for (String imageName : dto.getImageNames()) {
                        try {
                            String signedUrl = gcsImageService.getSignedUrl(imageName);
                            dto.getImageUrls().add(signedUrl);
                        } catch (Exception e) {
                            System.err.println("Warning: Failed to generate signed URL for image: " + imageName + " - "
                                    + e.getMessage());
                            // Continue processing other images instead of failing completely
                        }
                    }
                }

                // Generate signed URL for PDF if it exists
                if (dto.getPdfName() != null && !dto.getPdfName().isEmpty()) {
                    try {
                        String pdfSignedUrl = gcsPdfService.getSignedUrl(dto.getPdfName());
                        dto.setPdfUrl(pdfSignedUrl);
                        System.out.println("Generated signed URL for PDF: " + dto.getPdfName());
                    } catch (Exception e) {
                        System.err.println("Warning: Failed to generate signed URL for PDF: " + dto.getPdfName() + " - "
                                + e.getMessage());
                        // Continue processing instead of failing completely
                    }
                }

                allDateItemDtos.add(dto);
            }
        }
        allDateItemDtos.sort(Comparator.comparing(DateItemDto::getPriority));

        // Set signed URL for itinerary image
        if (itinerary.getImageName() != null) {
            String signedUrl = gcsImageService.getSignedUrl(itinerary.getImageName());
            itinerary.setCoverImageUrl(signedUrl);
        }

        InputStream imgStream = getClass().getClassLoader().getResourceAsStream("static/img/edge-fade.png");
        byte[] imgBytes = imgStream.readAllBytes();
        String edgeFadeUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(imgBytes);

        // Render Thymeleaf template to HTML
        Context context = new Context();
        context.setVariable("itinerary", itinerary);
        context.setVariable("dateItems", allDateItemDtos);
        context.setVariable("edgeFadeUrl", edgeFadeUrl);
        String html = templateEngine.process("itinerary-pdf", context);

        // Clean up HTML for XML parsing - more comprehensive escaping
        html = html.replace("&nbsp;", "&#160;");

        // Targeted unescaping only for span tags with background-color styles
        html = html.replaceAll("&lt;span style=&quot;background-color: ([^&]+?)&quot;&gt;",
                "<span style=\"background-color: $1\">");
        html = html.replaceAll("&lt;/span&gt;", "</span>");

        // Fix unclosed BR tags for XML compliance
        html = html.replaceAll("<br>", "<br/>");
        html = html.replaceAll("<BR>", "<br/>");

        // Normalize line breaks
        html = html.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

        // Fix DOCTYPE case - OpenHTMLToPDF requires uppercase DOCTYPE
        html = html.replace("<!doctype html>", "<!DOCTYPE html>");

        // Ensure proper XML structure - remove any BOM or invisible characters
        html = html.trim();
        if (html.startsWith("\uFEFF")) {
            html = html.substring(1); // Remove BOM
        }

        // Convert HTML to PDF
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(pdfStream);
        try {
            builder.run();
        } catch (Exception e) {
            System.err.println("Failed to generate PDF. HTML content length: " + html.length());
            System.err.println("HTML starts with: " + html.substring(0, Math.min(200, html.length())));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        byte[] pdfBytes = pdfStream.toByteArray();

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=itinerary.pdf")
                .body(pdfBytes);
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
        itinerary.setImageName(itineraryDto.getImageName());
        itinerary.setNotes(itineraryDto.getNotes());
        return itinerary;
    }
}
