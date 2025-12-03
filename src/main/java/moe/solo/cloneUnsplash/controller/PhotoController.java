package moe.solo.cloneUnsplash.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moe.solo.cloneUnsplash.dto.common.ApiResponse;
import moe.solo.cloneUnsplash.dto.common.PageResponse;
import moe.solo.cloneUnsplash.dto.photo.PhotoResponse;
import moe.solo.cloneUnsplash.dto.photo.PhotoUpdateRequest;
import moe.solo.cloneUnsplash.dto.photo.PhotoUploadRequest;
import moe.solo.cloneUnsplash.service.DownloadService;
import moe.solo.cloneUnsplash.service.LikeService;
import moe.solo.cloneUnsplash.service.PhotoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;
    private final LikeService likeService;
    private final DownloadService downloadService;

    @PostMapping
    public ResponseEntity<ApiResponse<PhotoResponse>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String[] tags,
            Authentication authentication) {

        PhotoUploadRequest request = PhotoUploadRequest.builder()
                .title(title)
                .description(description)
                .tags(tags != null ? java.util.Arrays.asList(tags) : null)
                .build();

        String username = authentication.getName();
        PhotoResponse response = photoService.uploadPhoto(file, request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("사진이 업로드되었습니다", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PhotoResponse>>> getPhotos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        String username = authentication != null ? authentication.getName() : null;
        PageResponse<PhotoResponse> response = photoService.getPhotos(pageable, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoResponse>> getPhoto(
            @PathVariable("photoId") Long photoId,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        PhotoResponse response = photoService.getPhoto(photoId, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PhotoResponse>>> searchPhotos(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        String username = authentication != null ? authentication.getName() : null;
        PageResponse<PhotoResponse> response = photoService.searchPhotos(keyword, pageable, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<ApiResponse<PageResponse<PhotoResponse>>> getPhotosByTag(
            @PathVariable("tagName") String tagName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        String username = authentication != null ? authentication.getName() : null;
        PageResponse<PhotoResponse> response = photoService.getPhotosByTag(tagName, pageable, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<PhotoResponse>>> getUserPhotos(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        String username = authentication != null ? authentication.getName() : null;
        PageResponse<PhotoResponse> response = photoService.getUserPhotos(userId, pageable, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoResponse>> updatePhoto(
            @PathVariable("photoId") Long photoId,
            @Valid @RequestBody PhotoUpdateRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        PhotoResponse response = photoService.updatePhoto(photoId, request, username);
        return ResponseEntity.ok(ApiResponse.success("사진 정보가 업데이트되었습니다", response));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable("photoId") Long photoId,
            Authentication authentication) {

        String username = authentication.getName();
        photoService.deletePhoto(photoId, username);
        return ResponseEntity.ok(ApiResponse.success("사진이 삭제되었습니다", null));
    }

    @PostMapping("/{photoId}/like")
    public ResponseEntity<ApiResponse<Void>> likePhoto(
            @PathVariable("photoId") Long photoId,
            Authentication authentication) {

        String username = authentication.getName();
        likeService.likePhoto(photoId, username);
        return ResponseEntity.ok(ApiResponse.success("좋아요를 눌렀습니다", null));
    }

    @DeleteMapping("/{photoId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePhoto(
            @PathVariable("photoId") Long photoId,
            Authentication authentication) {

        String username = authentication.getName();
        likeService.unlikePhoto(photoId, username);
        return ResponseEntity.ok(ApiResponse.success("좋아요를 취소했습니다", null));
    }

    @PostMapping("/{photoId}/download")
    public ResponseEntity<ApiResponse<Void>> downloadPhoto(
            @PathVariable("photoId") Long photoId,
            HttpServletRequest request,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        String ipAddress = request.getRemoteAddr();
        downloadService.recordDownload(photoId, username, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("다운로드가 기록되었습니다", null));
    }
}
