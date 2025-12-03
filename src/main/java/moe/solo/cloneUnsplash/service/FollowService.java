package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.solo.cloneUnsplash.entity.Follow;
import moe.solo.cloneUnsplash.entity.User;
import moe.solo.cloneUnsplash.exception.BadRequestException;
import moe.solo.cloneUnsplash.exception.ResourceNotFoundException;
import moe.solo.cloneUnsplash.repository.FollowRepository;
import moe.solo.cloneUnsplash.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void followUser(Long targetUserId, String currentUsername) {
        User follower = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("팔로우할 사용자를 찾을 수 없습니다"));

        if (follower.getId().equals(following.getId())) {
            throw new BadRequestException("자기 자신을 팔로우할 수 없습니다");
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new BadRequestException("이미 팔로우 중입니다");
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
        log.info("User {} followed user {}", currentUsername, targetUserId);
    }

    @Transactional
    public void unfollowUser(Long targetUserId, String currentUsername) {
        User follower = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("언팔로우할 사용자를 찾을 수 없습니다"));

        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new BadRequestException("팔로우 중이 아닙니다");
        }

        followRepository.deleteByFollowerAndFollowing(follower, following);
        log.info("User {} unfollowed user {}", currentUsername, targetUserId);
    }

    public boolean isFollowing(Long targetUserId, String currentUsername) {
        if (currentUsername == null) {
            return false;
        }

        User follower = userRepository.findByUsername(currentUsername).orElse(null);
        User following = userRepository.findById(targetUserId).orElse(null);

        if (follower == null || following == null) {
            return false;
        }

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }
}
