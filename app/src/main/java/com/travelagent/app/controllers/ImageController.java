package com.travelagent.app.controllers;

import com.travelagent.app.services.GcsImageService;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final GcsImageService gcsImageService;

    public ImageController(GcsImageService gcsImageService) {
        this.gcsImageService = gcsImageService;
    }

    @GetMapping("/signed-url/{fileName}")
    public ResponseEntity<String> getSignedUrl(@PathVariable String fileName) {
        String signedUrl = gcsImageService.getSignedUrl(fileName);
        return ResponseEntity.ok(signedUrl);
    }

    @PostMapping("/multiple-signed-urls")
    public ResponseEntity<Set<String>> getSignedUrls(@RequestBody Set<String> imageNames) {
        Set<String> signedUrls = gcsImageService.getMultipleSignedUrls(imageNames);
        return ResponseEntity.ok(signedUrls);
    }

}