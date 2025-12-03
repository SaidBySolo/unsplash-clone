package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.solo.cloneUnsplash.dto.collection.AddPhotoToCollectionRequest;
import moe.solo.cloneUnsplash.dto.collection.CollectionRequest;
import moe.solo.cloneUnsplash.dto.collection.CollectionResponse;
import moe.solo.cloneUnsplash.dto.common.PageResponse;
import moe.solo.cloneUnsplash.entity.Collection;
import moe.solo.cloneUnsplash.entity.Photo;
import moe.solo.cloneUnsplash.entity.User;
import moe.solo.cloneUnsplash.exception.BadRequestException;
import moe.solo.cloneUnsplash.exception.ResourceNotFoundException;
import moe.solo.cloneUnsplash.exception.UnauthorizedException;
import moe.solo.cloneUnsplash.repository.CollectionRepository;
import moe.solo.cloneUnsplash.repository.PhotoRepository;
import moe.solo.cloneUnsplash.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final S3Service s3Service;

    @Transactional
    public CollectionResponse createCollection(CollectionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        Collection collection = Collection.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isPrivate(request.getIsPrivate())
                .user(user)
                .build();

        collection = collectionRepository.save(collection);
        log.info("Collection created: {} by user {}", collection.getId(), username);

        return mapToCollectionResponse(collection);
    }

    public CollectionResponse getCollection(Long collectionId, String username) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다"));

        // 비공개 컬렉션은 본인만 조회 가능
        if (collection.getIsPrivate() && !collection.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("이 컬렉션에 접근할 권한이 없습니다");
        }

        return mapToCollectionResponse(collection);
    }

    public PageResponse<CollectionResponse> getPublicCollections(Pageable pageable) {
        Page<Collection> collectionPage = collectionRepository.findByIsPrivateFalseOrderByCreatedAtDesc(pageable);
        return mapToPageResponse(collectionPage);
    }

    public PageResponse<CollectionResponse> getUserCollections(Long userId, Pageable pageable, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        Page<Collection> collectionPage;

        // 본인의 컬렉션이면 전체 조회, 아니면 공개 컬렉션만
        if (user.getUsername().equals(currentUsername)) {
            collectionPage = collectionRepository.findByUser(user, pageable);
        } else {
            collectionPage = collectionRepository.findByUserAndIsPrivateFalse(user, pageable);
        }

        return mapToPageResponse(collectionPage);
    }

    @Transactional
    public CollectionResponse updateCollection(Long collectionId, CollectionRequest request, String username) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다"));

        if (!collection.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("본인의 컬렉션만 수정할 수 있습니다");
        }

        if (request.getTitle() != null) {
            collection.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            collection.setDescription(request.getDescription());
        }
        if (request.getIsPrivate() != null) {
            collection.setIsPrivate(request.getIsPrivate());
        }

        collection = collectionRepository.save(collection);
        log.info("Collection updated: {}", collection.getId());

        return mapToCollectionResponse(collection);
    }

    @Transactional
    public void deleteCollection(Long collectionId, String username) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다"));

        if (!collection.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("본인의 컬렉션만 삭제할 수 있습니다");
        }

        collectionRepository.delete(collection);
        log.info("Collection deleted: {}", collectionId);
    }

    @Transactional
    public void addPhotoToCollection(Long collectionId, AddPhotoToCollectionRequest request, String username) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다"));

        if (!collection.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("본인의 컬렉션만 수정할 수 있습니다");
        }

        Photo photo = photoRepository.findById(request.getPhotoId())
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다"));

        if (collection.getPhotos().contains(photo)) {
            throw new BadRequestException("이미 컬렉션에 포함된 사진입니다");
        }

        collection.getPhotos().add(photo);
        collectionRepository.save(collection);
        log.info("Photo {} added to collection {}", request.getPhotoId(), collectionId);
    }

    @Transactional
    public void removePhotoFromCollection(Long collectionId, Long photoId, String username) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다"));

        if (!collection.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("본인의 컬렉션만 수정할 수 있습니다");
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다"));

        if (!collection.getPhotos().contains(photo)) {
            throw new BadRequestException("컬렉션에 포함되지 않은 사진입니다");
        }

        collection.getPhotos().remove(photo);
        collectionRepository.save(collection);
        log.info("Photo {} removed from collection {}", photoId, collectionId);
    }

    private CollectionResponse mapToCollectionResponse(Collection collection) {
        List<String> coverPhotos = collection.getPhotos().stream()
                .limit(3)
                .map(photo -> s3Service.getFileUrl(photo.getImageUrl()))
                .collect(Collectors.toList());

        return CollectionResponse.builder()
                .id(collection.getId())
                .title(collection.getTitle())
                .description(collection.getDescription())
                .isPrivate(collection.getIsPrivate())
                .user(CollectionResponse.UserSummary.builder()
                        .id(collection.getUser().getId())
                        .username(collection.getUser().getUsername())
                        .name(collection.getUser().getName())
                        .profileImageUrl(collection.getUser().getProfileImageUrl() != null
                                ? s3Service.getFileUrl(collection.getUser().getProfileImageUrl())
                                : null)
                        .build())
                .photosCount(collection.getPhotos().size())
                .coverPhotos(coverPhotos)
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    private PageResponse<CollectionResponse> mapToPageResponse(Page<Collection> collectionPage) {
        List<CollectionResponse> content = collectionPage.getContent().stream()
                .map(this::mapToCollectionResponse)
                .collect(Collectors.toList());

        return PageResponse.<CollectionResponse>builder()
                .content(content)
                .page(collectionPage.getNumber())
                .size(collectionPage.getSize())
                .totalElements(collectionPage.getTotalElements())
                .totalPages(collectionPage.getTotalPages())
                .last(collectionPage.isLast())
                .build();
    }
}
