package com.travelagent.app.services;

import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Iterator;
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

    /**
     * Downloads an image from GCS, resizes and compresses it, and returns as base64 data URL
     * @param fileName The image file name in GCS
     * @param maxWidth Maximum width in pixels (maintains aspect ratio)
     * @param quality JPEG quality (0.0 to 1.0, where 0.7 is 70% quality)
     * @return Base64 data URL string ready for embedding in HTML/PDF
     */
    public String getCompressedImageDataUrl(String fileName, int maxWidth, float quality) {
        try {
            // Download image from GCS
            Blob blob = storage.get(bucketName, fileName);
            if (blob == null || !blob.exists()) {
                System.err.println("Image not found in GCS: " + fileName);
                return null;
            }
            
            byte[] imageBytes = blob.getContent();
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bais);
            
            if (originalImage == null) {
                System.err.println("Failed to read image: " + fileName);
                return null;
            }
            
            // Calculate new dimensions (maintain aspect ratio)
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int newWidth = Math.min(originalWidth, maxWidth);
            int newHeight = (int) ((double) originalHeight * newWidth / originalWidth);
            
            // Resize image
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();
            
            // Compress to JPEG with specified quality
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                throw new IOException("No JPEG writer available");
            }
            
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
            writer.dispose();
            ios.close();
            
            // Convert to base64 data URL
            byte[] compressedBytes = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(compressedBytes);
            return "data:image/jpeg;base64," + base64;
            
        } catch (Exception e) {
            System.err.println("Error compressing image " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets compressed image data URLs for multiple images
     * @param fileNames Set of image file names
     * @param maxWidth Maximum width in pixels
     * @param quality JPEG quality (0.0 to 1.0)
     * @return Set of base64 data URLs
     */
    public Set<String> getMultipleCompressedImageDataUrls(Set<String> fileNames, int maxWidth, float quality) {
        Set<String> dataUrls = new HashSet<>();
        for (String fileName : fileNames) {
            String dataUrl = getCompressedImageDataUrl(fileName, maxWidth, quality);
            if (dataUrl != null) {
                dataUrls.add(dataUrl);
            }
        }
        return dataUrls;
    }
}