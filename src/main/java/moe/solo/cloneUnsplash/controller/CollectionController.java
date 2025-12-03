package moe.solo.cloneUnsplash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moe.solo.cloneUnsplash.dto.collection.AddPhotoToCollectionRequest;
import moe.solo.cloneUnsplash.dto.collection.CollectionRequest;
import moe.solo.cloneUnsplash.dto.collection.CollectionResponse;
import moe.solo.cloneUnsplash.dto.common.ApiResponse;
import moe.solo.cloneUnsplash.dto.common.PageResponse;
import moe.solo.cloneUnsplash.service.CollectionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionResponse>> createCollection(
            @Valid @RequestBody CollectionRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        CollectionResponse response = collectionService.createCollection(request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("컬렉션이 생성되었습니다", response));
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<CollectionResponse>> getCollection(
            @PathVariable("collectionId") Long collectionId,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        CollectionResponse response = collectionService.getCollection(collectionId, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CollectionResponse>>> getPublicCollections(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<CollectionResponse> response = collectionService.getPublicCollections(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<CollectionResponse>>> getUserCollections(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        String username = authentication != null ? authentication.getName() : null;
        PageResponse<CollectionResponse> response = collectionService.getUserCollections(userId, pageable, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<CollectionResponse>> updateCollection(
            @PathVariable("collectionId") Long collectionId,
            @Valid @RequestBody CollectionRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        CollectionResponse response = collectionService.updateCollection(collectionId, request, username);
        return ResponseEntity.ok(ApiResponse.success("컬렉션이 업데이트되었습니다", response));
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCollection(
            @PathVariable("collectionId") Long collectionId,
            Authentication authentication) {

        String username = authentication.getName();
        collectionService.deleteCollection(collectionId, username);
        return ResponseEntity.ok(ApiResponse.success("컬렉션이 삭제되었습니다", null));
    }

    @PostMapping("/{collectionId}/photos")
    public ResponseEntity<ApiResponse<Void>> addPhotoToCollection(
            @PathVariable("collectionId") Long collectionId,
            @Valid @RequestBody AddPhotoToCollectionRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        collectionService.addPhotoToCollection(collectionId, request, username);
        return ResponseEntity.ok(ApiResponse.success("사진이 컬렉션에 추가되었습니다", null));
    }

    @DeleteMapping("/{collectionId}/photos/{photoId}")
    public ResponseEntity<ApiResponse<Void>> removePhotoFromCollection(
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("photoId") Long photoId,
            Authentication authentication) {

        String username = authentication.getName();
        collectionService.removePhotoFromCollection(collectionId, photoId, username);
        return ResponseEntity.ok(ApiResponse.success("사진이 컬렉션에서 제거되었습니다", null));
    }
}
