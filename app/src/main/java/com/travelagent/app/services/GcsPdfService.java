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
public class GcsPdfService {
    private final String bucketName = "personally-travel-app-pdfs";
    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    // PDF file formats
    private final List<String> PDF_FORMATS = Arrays.asList(
            "application/pdf");

    /**
     * Validates if the file is a PDF
     * 
     * @param file The uploaded file
     * @return true if PDF, false otherwise
     */
    public boolean isPdfFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            // Try to determine from filename extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String extension = originalFilename.toLowerCase();
                return extension.endsWith(".pdf");
            }
            return false;
        }
        return PDF_FORMATS.contains(contentType.toLowerCase());
    }

    public boolean doesPdfExist(String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        return blob != null && blob.exists();
    }

    public String uploadPdf(MultipartFile file, String fileName) throws IOException {
        if (!isPdfFormat(file)) {
            throw new IllegalArgumentException(
                    "Unsupported file format. Only PDF files are allowed. Received: " + file.getContentType());
        }

        if (doesPdfExist(fileName)) {
            throw new IllegalArgumentException("A PDF with the name '" + fileName + "' already exists.");
        }

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        storage.create(blobInfo, file.getBytes());
        return fileName;
    }

    public Set<String> uploadMultiplePdfs(MultipartFile[] files, String[] fileNames) throws IOException {
        if (files.length != fileNames.length) {
            throw new IllegalArgumentException("Number of files must match number of filenames");
        }

        // Validate all files first before uploading any
        for (MultipartFile file : files) {
            if (!isPdfFormat(file)) {
                throw new IllegalArgumentException(
                        "Unsupported file format. Only PDF files are allowed. Received: " + file.getContentType());
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