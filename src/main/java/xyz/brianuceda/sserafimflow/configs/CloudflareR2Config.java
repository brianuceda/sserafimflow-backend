package xyz.brianuceda.sserafimflow.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class CloudflareR2Config {

    @Bean
    public S3Client r2Client(
            @Value("${cloudflare.r2.account.id}") String accountId,
            @Value("${cloudflare.r2.access.key}") String accessKey,
            @Value("${cloudflare.r2.secret.key}") String secretKey) {
        
        // Crear credenciales para R2
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        // Crear y retornar el cliente R2
        return S3Client.builder()
                .region(Region.US_EAST_1) // R2 usa esta región como placeholder
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .build();
    }
}
