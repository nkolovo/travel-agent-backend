package com.travelagent.app.services;

import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
public class GcsImageService {
    private final String bucketName = "personally-travel-app-images";
    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    public boolean doesImageExist(String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        return blob != null && blob.exists();
    }

    public String uploadImage(MultipartFile file, String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        storage.create(blobInfo, file.getBytes());
        return fileName;
    }

    public String getSignedUrl(String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
        URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        return url.toString();
    }
}