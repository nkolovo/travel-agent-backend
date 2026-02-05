package com.travelagent.app.controllers;

import com.travelagent.app.models.Date;
import com.travelagent.app.models.Traveler;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
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
        Map<HttpStatus, Long> result = itineraryService.createItinerary(itineraryDto);

        HttpStatus status = result.keySet().iterator().next();
        Long id = result.get(status);

        return ResponseEntity.status(status).body(Map.of("id", id));
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateItinerary(@RequestBody Map<String, Object> updates) {
        itineraryService.updateItinerary(updates);
        return ResponseEntity.ok("Itinerary updated successfully.");
    }

    @DeleteMapping("/delete/{id}")
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

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        HttpStatus status = itineraryService.uploadItineraryImage(id, file);
        if (status == HttpStatus.OK) {
            return ResponseEntity.ok("Image uploaded successfully!");
        } else if (status == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type! Only images allowed.");
        } else {
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
    public ResponseEntity<StreamingResponseBody> getPdf(@PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean preview) {
        System.out.println("=== getPdf called: id=" + id + ", preview=" + preview + " ===");
        try {
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
                                System.err.println(
                                        "Warning: Failed to generate signed URL for image: " + imageName + " - "
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
                            System.err.println(
                                    "Warning: Failed to generate signed URL for PDF: " + dto.getPdfName() + " - "
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

            // Clean up HTML for XML parsing
            html = html.replace("&nbsp;", "&#160;");

            // Convert deprecated font tags to CSS spans for better PDF rendering
            html = html.replaceAll("<font size=\"(\\d+)\" color=\"([^\"]+)\">",
                    "<span style=\"font-size: $1em; color: $2;\">");
            html = html.replaceAll("<font color=\"([^\"]+)\" size=\"(\\d+)\">",
                    "<span style=\"color: $1; font-size: $2em;\">");
            html = html.replaceAll("<font size=\"(\\d+)\">",
                    "<span style=\"font-size: $1em;\">");
            html = html.replaceAll("<font color=\"([^\"]+)\">",
                    "<span style=\"color: $1;\">");
            html = html.replaceAll("</font>", "</span>");

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

            // Convert HTML to PDF using streaming to avoid Cloud Run response size limits
            final String finalHtml = html;
            String disposition = preview ? "inline" : "attachment";

            StreamingResponseBody stream = outputStream -> {
                try {
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    builder.withHtmlContent(finalHtml, null);
                    builder.toStream(outputStream);
                    builder.run();
                    outputStream.flush();
                } catch (Exception e) {
                    System.err.println("Failed to generate PDF during streaming: " + e.getMessage());
                    e.printStackTrace();
                    throw new IOException("PDF generation failed", e);
                }
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", disposition + "; filename=itinerary.pdf")
                    .body(stream);
        } catch (Exception e) {
            System.err.println("Unexpected error in getPdf endpoint: " + e.getMessage());
            e.printStackTrace();
            StreamingResponseBody errorStream = outputStream -> {
                String errorMsg = "Error generating PDF: " + e.getMessage();
                outputStream.write(errorMsg.getBytes());
            };
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorStream);
        }
    }

    @PostMapping("generate-shareable-link/{id}")
    public ResponseEntity<Map<String, String>> generateShareableLink(@PathVariable Long id) {
        try {
            String token = itineraryService.generateShareableToken(id);
            // Backend API URL - this generates the PDF directly
            String backendUrl = System.getenv("BACKEND_URL");
            if (backendUrl == null || backendUrl.isEmpty()) {
                backendUrl = "http://localhost:8080"; // Default for development
            }
            String shareableUrl = backendUrl + "/api/itineraries/shared/" + token + "/pdf";
            
            return ResponseEntity.ok(Map.of(
                "url", shareableUrl,
                "shareableUrl", shareableUrl,
                "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate shareable link: " + e.getMessage()));
        }
    }

    @GetMapping("/shared/{token}/pdf")
    public ResponseEntity<StreamingResponseBody> getSharedPdf(@PathVariable String token) {
        try {
            // Get itinerary by token
            ItineraryDto itinerary = itineraryService.getItineraryByToken(token);
            Long id = itinerary.getId();

            // Reuse existing PDF generation logic
            List<DateDto> dates = new ArrayList<>(itinerary.getDates());
            dates.sort(Comparator.comparing(DateDto::getDate));
            itinerary.setDates(dates);

            List<DateItemDto> allDateItemDtos = new ArrayList<>();
            for (DateDto date : dates) {
                List<DateItemDto> dateItems = dateItemService.getDateItemsByDate(date.getId());
                for (DateItemDto dto : dateItems) {
                    if (dto.getImageNames() != null && !dto.getImageNames().isEmpty()) {
                        for (String imageName : dto.getImageNames()) {
                            try {
                                String signedUrl = gcsImageService.getSignedUrl(imageName);
                                dto.getImageUrls().add(signedUrl);
                            } catch (Exception e) {
                                System.err.println("Warning: Failed to generate signed URL for image: " + imageName);
                            }
                        }
                    }

                    if (dto.getPdfName() != null && !dto.getPdfName().isEmpty()) {
                        try {
                            String pdfSignedUrl = gcsPdfService.getSignedUrl(dto.getPdfName());
                            dto.setPdfUrl(pdfSignedUrl);
                        } catch (Exception e) {
                            System.err.println("Warning: Failed to generate signed URL for PDF: " + dto.getPdfName());
                        }
                    }

                    allDateItemDtos.add(dto);
                }
            }
            allDateItemDtos.sort(Comparator.comparing(DateItemDto::getPriority));

            if (itinerary.getImageName() != null) {
                String signedUrl = gcsImageService.getSignedUrl(itinerary.getImageName());
                itinerary.setCoverImageUrl(signedUrl);
            }

            InputStream imgStream = getClass().getClassLoader().getResourceAsStream("static/img/edge-fade.png");
            byte[] imgBytes = imgStream.readAllBytes();
            String edgeFadeUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(imgBytes);

            Context context = new Context();
            context.setVariable("itinerary", itinerary);
            context.setVariable("dateItems", allDateItemDtos);
            context.setVariable("edgeFadeUrl", edgeFadeUrl);
            String html = templateEngine.process("itinerary-pdf", context);

            // Clean up HTML
            html = html.replace("&nbsp;", "&#160;");
            html = html.replaceAll("<font size=\"(\\d+)\" color=\"([^\"]+)\">",
                    "<span style=\"font-size: $1em; color: $2;\">");
            html = html.replaceAll("<font color=\"([^\"]+)\" size=\"(\\d+)\">",
                    "<span style=\"color: $1; font-size: $2em;\">");
            html = html.replaceAll("<font size=\"(\\d+)\">",
                    "<span style=\"font-size: $1em;\">");
            html = html.replaceAll("<font color=\"([^\"]+)\">",
                    "<span style=\"color: $1;\">");
            html = html.replaceAll("</font>", "</span>");
            html = html.replaceAll("<br>", "<br/>");
            html = html.replaceAll("<BR>", "<br/>");
            html = html.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
            html = html.replace("<!doctype html>", "<!DOCTYPE html>");
            html = html.trim();
            if (html.startsWith("\uFEFF")) {
                html = html.substring(1);
            }

            final String finalHtml = html;

            StreamingResponseBody stream = outputStream -> {
                try {
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    builder.withHtmlContent(finalHtml, null);
                    builder.toStream(outputStream);
                    builder.run();
                    outputStream.flush();
                } catch (Exception e) {
                    System.err.println("Failed to generate PDF: " + e.getMessage());
                    e.printStackTrace();
                    throw new IOException("PDF generation failed", e);
                }
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "inline; filename=itinerary.pdf")
                    .body(stream);
        } catch (Exception e) {
            System.err.println("Error generating shared PDF: " + e.getMessage());
            e.printStackTrace();
            StreamingResponseBody errorStream = outputStream -> {
                String errorMsg = "Error: Invalid or expired link";
                outputStream.write(errorMsg.getBytes());
            };
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorStream);
        }
    }
}
