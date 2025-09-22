package com.travelagent.app.controllers;

import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.dto.ItineraryDto;
import com.travelagent.app.models.Item;
import com.travelagent.app.models.Itinerary;

import com.travelagent.app.services.ItemService;
import com.travelagent.app.services.GcsImageService;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public List<Item> getAllItems() {
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

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type! Only images allowed.");
            }

            // Generate a unique object name, e.g., "item-123.jpg"
            String objectName = "item-" + id + "-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();

            // Upload to GCS
            gcsImageService.uploadImage(file, objectName);

            // Save the object name in the item
            ItemDto itemDto = itemService.getItemById(id);
            itemDto.setImageObjectName(objectName);
            itemService.saveItem(itemDto);

            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
        }
    }
}
