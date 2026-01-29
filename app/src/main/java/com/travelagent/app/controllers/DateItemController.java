package com.travelagent.app.controllers;

import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.services.DateItemService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/date-items")
public class DateItemController {
    private final DateItemService dateItemService;

    public DateItemController(DateItemService dateItemService) {
        this.dateItemService = dateItemService;
    }

    @GetMapping("/date/{dateId}")
    public List<DateItemDto> getDateItemsByDate(@PathVariable Long dateId) {
        return dateItemService.getDateItemsByDate(dateId);
    }

    @PostMapping("/upload-pdf")
    public ResponseEntity<String> uploadPdf(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "pdf", required = false) MultipartFile pdfFile,
            @RequestParam(name = "document", required = false) MultipartFile document) {
        
        System.out.println("=== PDF UPLOAD REQUEST DEBUG ===");
        System.out.println("file parameter: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("pdf parameter: " + (pdfFile != null ? pdfFile.getOriginalFilename() : "null"));
        System.out.println("document parameter: " + (document != null ? document.getOriginalFilename() : "null"));
        
        try {
            MultipartFile fileToUpload = null;
            
            if (file != null && !file.isEmpty()) {
                fileToUpload = file;
            } else if (pdfFile != null && !pdfFile.isEmpty()) {
                fileToUpload = pdfFile;
            } else if (document != null && !document.isEmpty()) {
                fileToUpload = document;
            }
            
            if (fileToUpload == null) {
                System.out.println("No file received in any parameter");
                return ResponseEntity.badRequest()
                        .body("No file provided. Make sure to send a file with parameter name 'file', 'pdf', or 'document'.");
            }

            System.out.println("Processing file: " + fileToUpload.getOriginalFilename());
            String fileName = dateItemService.uploadPdfForDateItem(fileToUpload);
            return ResponseEntity.ok(fileName);
        } catch (Exception e) {
            System.out.println("Error in upload: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to upload PDF: " + e.getMessage());
        }
    }

    @GetMapping("/signed-url/{fileName}")
    public ResponseEntity<String> getSignedUrl(@PathVariable String fileName) {
        try {
            String signedUrl = dateItemService.getPdfUrl(fileName);
            return ResponseEntity.ok(signedUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get PDF: " + e.getMessage());
        }
    }

}