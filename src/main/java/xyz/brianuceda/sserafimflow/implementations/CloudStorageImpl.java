package xyz.brianuceda.sserafimflow.implementations;

import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageImpl {
  String uploadFile(MultipartFile file, String username);
  String deleteFile(String username);
}
