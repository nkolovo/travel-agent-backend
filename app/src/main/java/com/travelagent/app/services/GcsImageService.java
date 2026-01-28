package com.travelagent.app.services;

import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class GcsImageService {
    private final String bucketName = "personally-travel-app-images";
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    
    // PDF-compatible image formats
    private final List<String> PDF_COMPATIBLE_FORMATS = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp"
    );

    /**
     * Validates if the image format is compatible with PDF generation
     * @param file The uploaded file
     * @return true if compatible, false otherwise
     */
    public boolean isPdfCompatibleFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            // Try to determine from filename extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String extension = originalFilename.toLowerCase();
                return extension.endsWith(".jpg") || extension.endsWith(".jpeg") || 
                       extension.endsWith(".png") || extension.endsWith(".gif") || extension.endsWith(".bmp");
            }
            return false;
        }
        return PDF_COMPATIBLE_FORMATS.contains(contentType.toLowerCase());
    }

    public boolean doesImageExist(String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        return blob != null && blob.exists();
    }

    public String uploadImage(MultipartFile file, String fileName) throws IOException {
        if (!isPdfCompatibleFormat(file)) {
            throw new IllegalArgumentException("Unsupported image format for PDF generation: " + file.getContentType() + 
                ". Supported formats: JPEG, PNG, GIF, BMP");
        }
        
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        storage.create(blobInfo, file.getBytes());
        return fileName;
    }

    public Set<String> uploadMultipleImages(MultipartFile[] files, String[] fileNames) throws IOException {
        if (files.length != fileNames.length) {
            throw new IllegalArgumentException("Number of files must match number of filenames");
        }
        
        // Validate all files first before uploading any
        for (MultipartFile file : files) {
            if (!isPdfCompatibleFormat(file)) {
                throw new IllegalArgumentException("Unsupported image format for PDF generation: " + file.getContentType() + 
                    ". Supported formats: JPEG, PNG, GIF, BMP");
            }
        }
        
        Set<String> uploadedFiles = new HashSet<>();
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String fileName = fileNames[i];
            
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            storage.create(blobInfo, file.getBytes());
            uploadedFiles.add(fileName);
        }
        
        return uploadedFiles;
    }
    public String getSignedUrl(String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
        URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        return url.toString();
    }

    public Set<String> getMultipleSignedUrls(Set<String> fileNames) {
        Set<String> signedUrls = new HashSet<>();
        for (String fileName : fileNames) {
            String url = getSignedUrl(fileName);
            signedUrls.add(url);
        }
        return signedUrls;
    }
}