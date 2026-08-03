package com.example.ecommerce.storage;

import com.example.ecommerce.common.exception.InvalidPhotoException;
import com.example.ecommerce.config.StorageProperties;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GcsPhotoStorageService implements PhotoStorageService {

    private final Storage storage;
    private final StorageProperties properties;

    public GcsPhotoStorageService(Storage storage, StorageProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    @Override
    public String upload(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidPhotoException("Only image uploads are supported");
        }

        String objectName = "listings/" + UUID.randomUUID() + extensionFor(contentType);
        BlobId blobId = BlobId.of(properties.bucket(), objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        try {
            // Object-level public ACL so served photos work over storage.googleapis.com without
            // needing signed URLs or a bucket-wide public IAM binding.
            storage.create(blobInfo, file.getBytes(), Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return "https://storage.googleapis.com/" + properties.bucket() + "/" + objectName;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
