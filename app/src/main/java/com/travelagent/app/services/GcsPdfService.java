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
    private boolean isPdfFormat(MultipartFile file) {
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
        // If fileName doesn't contain a path, prepend appropriate subfolder
        String fullPath = fileName;
        if (!fileName.contains("/")) {
            if (fileName.startsWith("itinerary-")) {
                fullPath = "ItineraryPdfs/" + fileName;
            } else {
                fullPath = "AttachmentPdfs/" + fileName;
            }
        }

        Blob blob = storage.get(bucketName, fullPath);
        return blob != null && blob.exists();
    }

    public String uploadPdf(MultipartFile file, String fileName) throws IOException {
        if (!isPdfFormat(file)) {
            throw new IllegalArgumentException(
                    "Unsupported file format. Only PDF files are allowed. Received: " + file.getContentType());
        }

        // Save to AttachmentPdfs subfolder
        String fullPath = "AttachmentPdfs/" + fileName;

        if (doesPdfExist(fileName)) {
            throw new IllegalArgumentException("A PDF with the name '" + fileName + "' already exists.");
        }

        BlobId blobId = BlobId.of(bucketName, fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        storage.create(blobInfo, file.getBytes());
        System.out.println("Uploaded PDF to GCS: " + fullPath);
        return fullPath;
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

            // Save to AttachmentPdfs subfolder
            String fullPath = "AttachmentPdfs/" + fileName;

            BlobId blobId = BlobId.of(bucketName, fullPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            storage.create(blobInfo, file.getBytes());
            uploadedFiles.add(fullPath);
        }

        return uploadedFiles;
    }

    /**
     * Uploads PDF bytes directly to GCS, overwriting if file already exists
     * 
     * @param pdfBytes The PDF content as byte array
     * @param fileName The name to save the file as
     * @return The fileName that was uploaded
     * @throws IOException if upload fails
     */
    public String uploadPdfBytes(byte[] pdfBytes, String fileName) throws IOException {
        // Save to ItineraryPdfs subfolder
        String fullPath = "ItineraryPdfs/" + fileName;

        BlobId blobId = BlobId.of(bucketName, fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/pdf")
                .build();
        storage.create(blobInfo, pdfBytes);
        return fullPath;
    }

    public String getSignedUrl(String fileName) {
        // If fileName doesn't contain a path, prepend AttachmentPdfs/
        String fullPath = fileName;
        if (!fileName.contains("/")) {
            fullPath = "AttachmentPdfs/" + fileName;
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fullPath).build();
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