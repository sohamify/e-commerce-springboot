package com.example.ecommerce.storage;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorageService {

    /** @return the publicly reachable URL of the uploaded photo */
    String upload(MultipartFile file);
}
