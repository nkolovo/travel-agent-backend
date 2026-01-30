package com.travelagent.app.controllers;

import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.services.ItemService;
import com.travelagent.app.services.GcsImageService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ResponseEntity<String> uploadImages(
            @PathVariable Long id,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "file", required = false) MultipartFile singleFile) {

        // Handle both single file and multiple files
        MultipartFile[] filesToProcess;

        if (files != null && files.length > 0) {
            filesToProcess = files;
        } else if (singleFile != null) {
            filesToProcess = new MultipartFile[] { singleFile };
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No files provided. Use 'files' parameter for multiple files or 'file' for single file.");
        }

        // Debug logging
        System.out.println("=== UPLOAD DEBUG INFO ===");
        System.out.println("Item ID: " + id);
        System.out.println("Raw 'files' parameter: " + (files != null ? files.length + " files" : "null"));
        System.out.println("Raw 'file' parameter: " + (singleFile != null ? "1 file" : "null"));
        System.out.println("Files to process: " + filesToProcess.length);
        for (int i = 0; i < filesToProcess.length; i++) {
            System.out.println("File " + (i + 1) + ": " + filesToProcess[i].getOriginalFilename() +
                    " (size: " + filesToProcess[i].getSize() + " bytes)");
        }

        try {
            // Validate all files are images
            for (MultipartFile file : filesToProcess) {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid file type! Only images allowed: " + file.getOriginalFilename());
                }
            }

            // Prepare filenames array
            String[] fileNames = new String[filesToProcess.length];
            for (int i = 0; i < filesToProcess.length; i++) {
                fileNames[i] = filesToProcess[i].getOriginalFilename();
                System.out.println("Processing file: " + fileNames[i]);
            }

            // Filter out files that already exist and prepare lists for upload
            MultipartFile[] filesToUpload = new MultipartFile[filesToProcess.length];
            String[] fileNamesToUpload = new String[filesToProcess.length];
            int uploadCount = 0;

            for (int i = 0; i < filesToProcess.length; i++) {
                boolean exists = gcsImageService.doesImageExist(fileNames[i]);
                System.out.println("File " + fileNames[i] + " exists in GCS: " + exists);
                if (!exists) {
                    filesToUpload[uploadCount] = filesToProcess[i];
                    fileNamesToUpload[uploadCount] = fileNames[i];
                    uploadCount++;
                    System.out.println("Will upload: " + fileNames[i]);
                } else {
                    System.out.println("Skipping existing file: " + fileNames[i]);
                }
            }
            
            // Upload new files to GCS if any
            Set<String> uploadedFiles = new HashSet<>();
            if (uploadCount > 0) {
                System.out.println("Uploading " + uploadCount + " new files...");
                // Create arrays with only the files to upload
                MultipartFile[] actualFilesToUpload = new MultipartFile[uploadCount];
                String[] actualFileNamesToUpload = new String[uploadCount];
                System.arraycopy(filesToUpload, 0, actualFilesToUpload, 0, uploadCount);
                System.arraycopy(fileNamesToUpload, 0, actualFileNamesToUpload, 0, uploadCount);

                uploadedFiles = gcsImageService.uploadMultipleImages(actualFilesToUpload, actualFileNamesToUpload);
                System.out.println("Successfully uploaded: " + uploadedFiles);
            } else {
                System.out.println("No new files to upload - all files already exist");
            }

            // Save all file names to the item (both newly uploaded and existing)
            ItemDto item = itemService.getItemById(id);

            // Just add the new filenames - don't remove existing ones
            for (String fileName : fileNames) {
                item.getImageNames().add(fileName);
                System.out.println("Added file reference: " + fileName);
            }

            itemService.saveItem(item);

            System.out.println("Final imageNames for item: " + item.getImageNames());

            // Check for files in database that are missing from GCS
            Set<String> missingFiles = new HashSet<>();
            for (String imageName : item.getImageNames()) {
                if (!gcsImageService.doesImageExist(imageName)) {
                    missingFiles.add(imageName);
                }
            }

            if (!missingFiles.isEmpty()) {
                System.out.println("WARNING: Files in database but missing from GCS: " + missingFiles);
                System.out.println("You may need to re-upload these files or clean up the database references");
            }

            System.out.println("Frontend only sent " + filesToProcess.length
                    + " file(s). If you selected more, check your frontend upload logic.");

            String message = String.format("Successfully processed %d images (%d uploaded, %d already existed)",
                    filesToProcess.length, uploadedFiles.size(), filesToProcess.length - uploadedFiles.size());
            return ResponseEntity.ok(message);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading images: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request: " + e.getMessage());
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
