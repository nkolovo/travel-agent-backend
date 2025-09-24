package com.travelagent.app.controllers;

import com.travelagent.app.services.GcsImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}