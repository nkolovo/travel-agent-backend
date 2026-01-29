package com.travelagent.app.controllers;

import com.travelagent.app.services.GcsPdfService;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final GcsPdfService gcsPdfService;

    public PdfController(GcsPdfService gcsPdfService) {
        this.gcsPdfService = gcsPdfService;
    }

    @GetMapping("/signed-url/{fileName}")
    public ResponseEntity<String> getSignedUrl(@PathVariable String fileName) {
        String signedUrl = gcsPdfService.getSignedUrl(fileName);
        return ResponseEntity.ok(signedUrl);
    }

    @PostMapping("/multiple-signed-urls")
    public ResponseEntity<Set<String>> getSignedUrls(@RequestBody Set<String> pdfNames) {
        Set<String> signedUrls = gcsPdfService.getMultipleSignedUrls(pdfNames);
        return ResponseEntity.ok(signedUrls);
    }

}