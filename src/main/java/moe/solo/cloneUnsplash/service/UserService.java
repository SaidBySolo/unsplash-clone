package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.solo.cloneUnsplash.dto.user.UserResponse;
import moe.solo.cloneUnsplash.dto.user.UserUpdateRequest;
import moe.solo.cloneUnsplash.entity.User;
import moe.solo.cloneUnsplash.exception.ResourceNotFoundException;
import moe.solo.cloneUnsplash.exception.UnauthorizedException;
import moe.solo.cloneUnsplash.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return mapToUserResponse(user);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        // 본인만 수정 가능
        if (!user.getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("본인의 프로필만 수정할 수 있습니다");
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getPortfolioUrl() != null) {
            user.setPortfolioUrl(request.getPortfolioUrl());
        }
        if (request.getInstagramUsername() != null) {
            user.setInstagramUsername(request.getInstagramUsername());
        }
        if (request.getTwitterUsername() != null) {
            user.setTwitterUsername(request.getTwitterUsername());
        }

        user = userRepository.save(user);
        log.info("User profile updated: {}", user.getUsername());

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfileImage(Long userId, MultipartFile file, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("본인의 프로필만 수정할 수 있습니다");
        }

        // 기존 프로필 이미지 삭제
        if (user.getProfileImageUrl() != null) {
            try {
                s3Service.deleteFile(user.getProfileImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old profile image: {}", e.getMessage());
            }
        }

        // 새 프로필 이미지 업로드
        String imageUrl = s3Service.uploadFile(file, "profiles");
        user.setProfileImageUrl(imageUrl);

        user = userRepository.save(user);
        log.info("Profile image updated for user: {}", user.getUsername());

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl() != null
                        ? s3Service.getFileUrl(user.getProfileImageUrl())
                        : null)
                .location(user.getLocation())
                .portfolioUrl(user.getPortfolioUrl())
                .instagramUsername(user.getInstagramUsername())
                .twitterUsername(user.getTwitterUsername())
                .photosCount((long) user.getPhotos().size())
                .collectionsCount((long) user.getCollections().size())
                .followersCount((long) user.getFollowers().size())
                .followingCount((long) user.getFollowing().size())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
