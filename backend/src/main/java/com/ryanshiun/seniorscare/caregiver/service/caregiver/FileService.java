package com.ryanshiun.seniorscare.caregiver.service.caregiver;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileService {
    String uploadFile(MultipartFile file, String directory) throws IOException;

    boolean deleteFile(String filePath);
}