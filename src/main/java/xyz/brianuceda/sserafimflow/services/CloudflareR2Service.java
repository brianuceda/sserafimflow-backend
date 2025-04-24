package xyz.brianuceda.sserafimflow.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.ResponseTransformer;

import xyz.brianuceda.sserafimflow.implementations.CloudStorageImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.java.Log;

@Service
@Log
public class CloudflareR2Service implements CloudStorageImpl {
    
    private final S3Client r2Client;
    
    @Value("${cloudflare.r2.bucket.name}")
    private String bucketName;
    
    @Value("${cloudflare.r2.cdn.url}")
    private String cdnUrl;

    public CloudflareR2Service(
            @Value("${cloudflare.r2.account.id}") String accountId,
            @Value("${cloudflare.r2.access.key}") String accessKey,
            @Value("${cloudflare.r2.secret.key}") String secretKey) {
        
        // Crear credenciales para R2
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        // Crear el cliente de R2 (compatible con S3) para operaciones de API
        this.r2Client = S3Client.builder()
                .region(Region.US_EAST_1) // Región placeholder
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .build();
    }

    @Override
    public String uploadFile(MultipartFile file, String username) {
        try {
            // Definir un nombre de archivo único basado en el username
            String fileName = username;
            
            // Convertir MultipartFile a File
            File fileObj = convertMultiPartFileToFile(file);
            
            // Determinar el tipo de contenido
            String contentType = file.getContentType();
            
            // Configurar metadatos 
            Map<String, String> metadata = new HashMap<>();
            metadata.put("username", username);
            
            // Configurar el objeto para R2
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .metadata(metadata)
                    .cacheControl("public, max-age=31536000") // Un año de caché
                    .build();
            
            r2Client.putObject(putObjectRequest, RequestBody.fromFile(fileObj));
            
            // Eliminar el archivo temporal
            fileObj.delete();
            
            // Generar URL pública usando la variable de entorno cdnUrl
            String fileUrl = cdnUrl + "/" + fileName;
            
            log.info("Archivo subido a R2: " + fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.severe("Error al subir archivo a R2: " + e.getMessage());
            throw new IllegalArgumentException("Error al subir la imagen: " + e.getMessage());
        }
    }

    @Override
    public String deleteFile(String username) {
        try {
            // Verificar si el archivo existe
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(username)
                    .build();
            
            try {
                r2Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                return "No se encontró ninguna imagen";
            }
            
            // Eliminar el archivo
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(username)
                    .build();
            
            r2Client.deleteObject(deleteObjectRequest);
            
            return "Imagen eliminada correctamente";
        } catch (Exception e) {
            log.severe("Error al eliminar archivo de R2: " + e.getMessage());
            throw new IllegalArgumentException("Error al eliminar la imagen: " + e.getMessage());
        }
    }
    
    // Método para obtener un archivo
    public byte[] getFile(String username) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(username)
                    .build();
            
            // Descargar el archivo como un array de bytes
            return r2Client.getObject(getObjectRequest, ResponseTransformer.toBytes()).asByteArray();
        } catch (NoSuchKeyException e) {
            log.warning("Archivo no encontrado en R2: " + username);
            return null;
        } catch (Exception e) {
            log.severe("Error al obtener archivo de R2: " + e.getMessage());
            throw new IllegalArgumentException("Error al obtener la imagen: " + e.getMessage());
        }
    }
    
    // Método auxiliar para convertir MultipartFile a File
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
