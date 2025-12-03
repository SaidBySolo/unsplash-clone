package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;or
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
     * 파일 업로드 (퍼블릭 읽기 권한)
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

            // 파일 업로드 (ACL: public-read)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
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
     * 파일 URL 생성 (퍼블릭 URL)
     */
    public String getFileUrl(String key) {
        String baseUrl = (publicUrl != null && !publicUrl.isEmpty()) ? publicUrl : endpoint;
        String url = baseUrl + "/" + bucketName + "/" + key;
        log.debug("Generated public URL: {}", url);
        return url;
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
