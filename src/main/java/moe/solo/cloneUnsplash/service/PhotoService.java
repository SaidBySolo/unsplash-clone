package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.solo.cloneUnsplash.dto.common.PageResponse;
import moe.solo.cloneUnsplash.dto.photo.PhotoResponse;
import moe.solo.cloneUnsplash.dto.photo.PhotoUpdateRequest;
import moe.solo.cloneUnsplash.dto.photo.PhotoUploadRequest;
import moe.solo.cloneUnsplash.entity.Photo;
import moe.solo.cloneUnsplash.entity.Tag;
import moe.solo.cloneUnsplash.entity.User;
import moe.solo.cloneUnsplash.exception.ResourceNotFoundException;
import moe.solo.cloneUnsplash.exception.UnauthorizedException;
import moe.solo.cloneUnsplash.repository.LikeRepository;
import moe.solo.cloneUnsplash.repository.PhotoRepository;
import moe.solo.cloneUnsplash.repository.TagRepository;
import moe.solo.cloneUnsplash.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;

    @Transactional
    public PhotoResponse uploadPhoto(MultipartFile file, PhotoUploadRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        // 이미지 메타데이터 추출
        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다");
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 읽기 실패", e);
        }

        // S3에 파일 업로드
        String imageUrl = s3Service.uploadFile(file, "photos");

        // Photo 엔티티 생성
        Photo photo = Photo.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(imageUrl)
                .width(image.getWidth())
                .height(image.getHeight())
                .fileSize(file.getSize())
                .user(user)
                .build();

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<Tag> tags = request.getTags().stream()
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build())))
                    .collect(Collectors.toList());
            photo.getTags().addAll(tags);
        }

        photo = photoRepository.save(photo);
        log.info("Photo uploaded: {} by user {}", photo.getId(), username);

        return mapToPhotoResponse(photo, username);
    }

    public PhotoResponse getPhoto(Long photoId, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다: " + photoId));

        // 조회수 증가
        photo.setViewsCount(photo.getViewsCount() + 1);
        photoRepository.save(photo);

        return mapToPhotoResponse(photo, username);
    }

    public PageResponse<PhotoResponse> getPhotos(Pageable pageable, String username) {
        Page<Photo> photoPage = photoRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapToPageResponse(photoPage, username);
    }

    public PageResponse<PhotoResponse> searchPhotos(String keyword, Pageable pageable, String username) {
        Page<Photo> photoPage = photoRepository.searchByKeyword(keyword, pageable);
        return mapToPageResponse(photoPage, username);
    }

    public PageResponse<PhotoResponse> getPhotosByTag(String tagName, Pageable pageable, String username) {
        Page<Photo> photoPage = photoRepository.findByTagName(tagName, pageable);
        return mapToPageResponse(photoPage, username);
    }

    public PageResponse<PhotoResponse> getUserPhotos(Long userId, Pageable pageable, String username) {
        Page<Photo> photoPage = photoRepository.findByUserId(userId, pageable);
        return mapToPageResponse(photoPage, username);
    }

    @Transactional
    public PhotoResponse updatePhoto(Long photoId, PhotoUpdateRequest request, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다"));

        if (!photo.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("본인의 사진만 수정할 수 있습니다");
        }

        if (request.getTitle() != null) {
            photo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            photo.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            photo.getTags().clear();
            List<Tag> tags = request.getTags().stream()
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build())))
                    .collect(Collectors.toList());
            photo.getTags().addAll(tags);
        }

        photo = photoRepository.save(photo);
        log.info("Photo updated: {}", photo.getId());

        return mapToPhotoResponse(photo, username);
    }

    @Transactional
    public void deletePhoto(Long photoId, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다"));

        if (!photo.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("본인의 사진만 삭제할 수 있습니다");
        }

        // S3에서 파일 삭제
        try {
            s3Service.deleteFile(photo.getImageUrl());
        } catch (Exception e) {
            log.warn("Failed to delete photo file from S3: {}", e.getMessage());
        }

        photoRepository.delete(photo);
        log.info("Photo deleted: {}", photoId);
    }

    private PhotoResponse mapToPhotoResponse(Photo photo, String currentUsername) {
        User currentUser = currentUsername != null
                ? userRepository.findByUsername(currentUsername).orElse(null)
                : null;

        boolean liked = false;
        if (currentUser != null) {
            liked = likeRepository.existsByUserAndPhoto(currentUser, photo);
        }

        return PhotoResponse.builder()
                .id(photo.getId())
                .title(photo.getTitle())
                .description(photo.getDescription())
                .imageUrl(s3Service.getFileUrl(photo.getImageUrl()))
                .thumbnailUrl(photo.getThumbnailUrl() != null
                        ? s3Service.getFileUrl(photo.getThumbnailUrl())
                        : s3Service.getFileUrl(photo.getImageUrl()))
                .width(photo.getWidth())
                .height(photo.getHeight())
                .color(photo.getColor())
                .viewsCount(photo.getViewsCount())
                .downloadsCount(photo.getDownloadsCount())
                .likesCount(photo.getLikesCount())
                .user(PhotoResponse.UserSummary.builder()
                        .id(photo.getUser().getId())
                        .username(photo.getUser().getUsername())
                        .name(photo.getUser().getName())
                        .profileImageUrl(photo.getUser().getProfileImageUrl() != null
                                ? s3Service.getFileUrl(photo.getUser().getProfileImageUrl())
                                : null)
                        .build())
                .tags(photo.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .createdAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .likedByCurrentUser(liked)
                .build();
    }

    private PageResponse<PhotoResponse> mapToPageResponse(Page<Photo> photoPage, String username) {
        List<PhotoResponse> content = photoPage.getContent().stream()
                .map(photo -> mapToPhotoResponse(photo, username))
                .collect(Collectors.toList());

        return PageResponse.<PhotoResponse>builder()
                .content(content)
                .page(photoPage.getNumber())
                .size(photoPage.getSize())
                .totalElements(photoPage.getTotalElements())
                .totalPages(photoPage.getTotalPages())
                .last(photoPage.isLast())
                .build();
    }
}
