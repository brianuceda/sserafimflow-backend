package com.brianuceda.sserafimflow.implementations;

import org.springframework.web.multipart.MultipartFile;

public interface AwsS3Impl {
  String uploadFile(MultipartFile file, String username);
  String deleteFile(String username);
}