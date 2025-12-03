package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.public-url:#{null}}")
    private String publicUrl;

    @Value("${s3.region:us-east-1}")
    private String region;

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        try {
            // 버킷 존재 확인
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            try {
                s3Client.headBucket(headBucketRequest);
                log.info("S3 bucket '{}' already exists", bucketName);
            } catch (NoSuchBucketException e) {
                // 버킷이 없으면 생성
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);
                log.info("S3 bucket '{}' created successfully", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing S3 bucket", e);
            throw new RuntimeException("Could not initialize S3 bucket", e);
        }
    }

    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            String key = folder + "/" + filename;

            // 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded successfully to S3: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Could not upload file", e);
        }
    }

    /**
     * 파일 다운로드 URL 생성 (presigned URL, 7일 유효)
     */
    public String getFileUrl(String key) {
        try {
            // 공개 URL이 설정되어 있으면 공개 엔드포인트 사용, 아니면 내부 엔드포인트 사용
            String effectiveEndpoint = (publicUrl != null && !publicUrl.isEmpty()) ? publicUrl : endpoint;
            
            log.debug("Using endpoint for presigned URL: {}", effectiveEndpoint);
            
            S3Presigner presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(effectiveEndpoint))
                    .region(software.amazon.awssdk.regions.Region.of(region))
                    .credentialsProvider(software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                            software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();
            
            presigner.close();

            log.debug("Generated presigned URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            throw new RuntimeException("Could not generate file URL", e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file from S3", e);
            throw new RuntimeException("Could not delete file", e);
        }
    }

    /**
     * 파일 다운로드 스트림
     */
    public InputStream getFileStream(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            log.error("Error getting file stream from S3", e);
            throw new RuntimeException("Could not get file stream", e);
        }
    }
}
