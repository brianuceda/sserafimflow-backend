package com.brianuceda.sserafimflow.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.brianuceda.sserafimflow.implementations.AwsS3Impl;

import lombok.extern.java.Log;

@Service
@Log
public class AwsS3Service implements AwsS3Impl {
  @Value("${application.bucket.name}")
  private String bucketName;
  
  private final AmazonS3 s3Client;

  public AwsS3Service(AmazonS3 s3Client) {
    this.s3Client = s3Client;
  }

  @Override
  public String uploadFile(MultipartFile file, String username) {
    try {
      // Archivo a subir a S3 
      String fileName = username;
      File fileObj = convertMultiPartFileToFile(file);  
      
      // Subir archivo a S3
      s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
      fileObj.delete();  
      
      // URL Publica
      String fileUrl = s3Client.getUrl(bucketName, fileName).toString();
      return fileUrl;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error al subir la imagen");
    }
  }

  @Override
  public String deleteFile(String username) {
    String prefix = username;
    ListObjectsV2Result result = s3Client.listObjectsV2(bucketName, prefix);

    if (!result.getObjectSummaries().isEmpty()) {
      result.getObjectSummaries().forEach(s3Object -> 
        s3Client.deleteObject(bucketName, s3Object.getKey())
      );
      return "Imagen eliminada correctamente";
    }
    
    return "No se encontró ninguna imagen";
  }

  private File convertMultiPartFileToFile(MultipartFile file) {
    File convertedFile = new File(file.getOriginalFilename());  
    try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
      fos.write(file.getBytes());
    } catch (IOException e) {
      log.warning("Error convirtiendo MultipartFile: " + e);
    }  
    return convertedFile;
  }
}
