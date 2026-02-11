package com.travelagent.app.controllers;

import com.travelagent.app.models.User;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.Traveler;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.ItineraryDto;
import com.travelagent.app.dto.TravelerDto;
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

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final DateItemService dateItemService;
    private final UserService userService;

    @Autowired
    private GcsImageService gcsImageService;
    @Autowired
    private GcsPdfService gcsPdfService;
    @Autowired
    private SpringTemplateEngine templateEngine;

    public ItineraryController(ItineraryService itineraryService, UserService userService,
            DateItemService dateItemService) {
        this.itineraryService = itineraryService;
        this.userService = userService;
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

    /**
     * Converts legacy HTML font size attribute to proper CSS font-size
     * Handles: 1=Small, 3=Normal, 5=Large, 7=Huge
     * 
     * @param html The HTML string containing font tags
     * @return HTML with font tags converted to span tags with proper CSS
     */
    private String convertFontSizeToCss(String html) {
        // Map for the specific font sizes used in the application
        // 1=Small, 3=Normal, 5=Large, 7=Huge
        java.util.Map<String, String> sizeMap = java.util.Map.of(
            "1", "0.875em",  // Small
            "3", "1em",      // Normal
            "5", "1.5em",    // Large
            "7", "2em"       // Huge
        );
        
        // Replace each size value with proper em values
        for (java.util.Map.Entry<String, String> entry : sizeMap.entrySet()) {
            String size = entry.getKey();
            String cssSize = entry.getValue();
            
            // <font size="X" color="...">
            html = html.replaceAll("<font size=\"" + size + "\" color=\"([^\"]+)\">",
                    "<span style=\"font-size: " + cssSize + "; color: $1;\">");
            
            // <font color="..." size="X">
            html = html.replaceAll("<font color=\"([^\"]+)\" size=\"" + size + "\">",
                    "<span style=\"color: $1; font-size: " + cssSize + ";\">");
            
            // <font size="X">
            html = html.replaceAll("<font size=\"" + size + "\">",
                    "<span style=\"font-size: " + cssSize + ";\">");
        }
        
        return html;
    }

    /**
     * Helper method to generate cleaned HTML for PDF rendering
     * 
     * @param id The itinerary ID
     * @return Cleaned HTML string ready for PDF conversion
     * @throws Exception if HTML generation fails
     */
    private String generatePdfHtml(Long id) throws Exception {
        ItineraryDto itinerary = itineraryService.getItineraryById(id);
        User user = userService.getUserByUsername(itinerary.getAgent());
        List<DateDto> dates = new ArrayList<>(itinerary.getDates());
        dates.sort(Comparator.comparing(DateDto::getDate));
        itinerary.setDates(dates);

        // Collect all DateItemDtos for all dates in the itinerary
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

        // Render Thymeleaf template to HTML
        Context context = new Context();
        String companyLogoUrl = gcsImageService.getSignedUrl("logo-tag.jpg");
        context.setVariable("companyLogoUrl", companyLogoUrl);
        context.setVariable("itinerary", itinerary);
        context.setVariable("dateItems", allDateItemDtos);
        context.setVariable("user", user);
        context.setVariable("edgeFadeUrl", edgeFadeUrl);
        String html = templateEngine.process("itinerary-pdf", context);

        // Clean up HTML for XML parsing
        html = html.replace("&nbsp;", "&#160;");
        
        // Convert legacy <font size> to proper CSS (size 1-7 scale)
        html = convertFontSizeToCss(html);
        
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

        return html;
    }

    @GetMapping("generate-pdf/{id}")
    public ResponseEntity<StreamingResponseBody> getPdf(@PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean preview) {
        System.out.println("=== getPdf called: id=" + id + ", preview=" + preview + " ===");
        try {
            String html = generatePdfHtml(id);
            String disposition = preview ? "inline" : "attachment";

            // Generate PDF into byte array first (synchronously) to avoid async timeout
            java.io.ByteArrayOutputStream pdfOutputStream = new java.io.ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(pdfOutputStream);
            builder.run();
            byte[] pdfBytes = pdfOutputStream.toByteArray();

            // Now stream the pre-generated bytes
            StreamingResponseBody stream = outputStream -> {
                try {
                    outputStream.write(pdfBytes);
                    outputStream.flush();
                } catch (Exception e) {
                    System.err.println("Failed to stream PDF bytes: " + e.getMessage());
                    e.printStackTrace();
                    throw new IOException("PDF streaming failed", e);
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
            String html = generatePdfHtml(id);

            // Convert HTML to PDF bytes
            java.io.ByteArrayOutputStream pdfOutputStream = new java.io.ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(pdfOutputStream);
            builder.run();
            byte[] pdfBytes = pdfOutputStream.toByteArray();

            // Upload to GCS (overwrites if exists) - returns full path including subfolder
            String fileName = "itinerary-" + id + ".pdf";
            String fullPath = gcsPdfService.uploadPdfBytes(pdfBytes, fileName);

            // Generate permanent share URL (backend endpoint that never expires)
            String backendUrl = System.getenv("BACKEND_URL");
            if (backendUrl == null || backendUrl.isEmpty()) {
                backendUrl = "http://localhost:8080";
            }
            String shareableUrl = backendUrl + "/api/itineraries/share/" + id;

            return ResponseEntity.ok(Map.of(
                    "shareableUrl", shareableUrl,
                    "fileName", fullPath));
        } catch (Exception e) {
            System.err.println("Failed to generate shareable link: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate shareable link: " + e.getMessage()));
        }
    }

    @GetMapping("/share/{id}")
    public ResponseEntity<Void> shareItinerary(@PathVariable Long id) {
        try {
            // Construct full path to PDF in GCS (ItineraryPdfs subfolder)
            String fullPath = "ItineraryPdfs/itinerary-" + id + ".pdf";

            // Generate a fresh signed URL (15 minutes is fine for immediate redirect)
            String signedUrl = gcsPdfService.getSignedUrl(fullPath);

            // Redirect to the signed URL
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(java.net.URI.create(signedUrl))
                    .build();
        } catch (Exception e) {
            System.err.println("Failed to redirect to shared PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
