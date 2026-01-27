package com.travelagent.app.controllers;

import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.services.ItemService;
import com.travelagent.app.services.GcsImageService;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    private GcsImageService gcsImageService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDto> getAllItems() {
        return itemService.getAllItems();
    }

    @PostMapping("/save")
    public Long saveItem(@RequestBody ItemDto item) {
        Long id = itemService.saveItem(item);
        return id;
    }

    @PostMapping("/remove/{id}")
    public void removeItem(@PathVariable Long id) {
        itemService.removeItem(id);
    }

    @PostMapping("/restore/{id}")
    public void restoreItem(@PathVariable Long id) {
        itemService.restoreItem(id);
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
            ItemDto item = itemService.getItemById(id);
            item.setImageName(fileName);
            itemService.saveItem(item);

            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
        }
    }

    @GetMapping("/countries")
    public List<String> getCountries() {
        return itemService.getCountries();
    }

    @GetMapping("/locations")
    public List<String> getLocations() {
        return itemService.getLocations();
    }

    @PostMapping("/add/country/{name}")
    public void addCountry(@PathVariable String name) {
        itemService.addCountry(name);
    }

    @PostMapping("/add/location/{country}/{location}")
    public void addLocation(@PathVariable String country, @PathVariable String location) {
        itemService.addLocation(country, location);
    }

}
