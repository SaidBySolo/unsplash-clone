package moe.solo.cloneUnsplash.controller;

import lombok.RequiredArgsConstructor;
import moe.solo.cloneUnsplash.dto.common.ApiResponse;
import moe.solo.cloneUnsplash.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable("userId") Long userId,
            Authentication authentication) {

        String username = authentication.getName();
        followService.followUser(userId, username);
        return ResponseEntity.ok(ApiResponse.success("팔로우했습니다", null));
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable("userId") Long userId,
            Authentication authentication) {

        String username = authentication.getName();
        followService.unfollowUser(userId, username);
        return ResponseEntity.ok(ApiResponse.success("언팔로우했습니다", null));
    }

    @GetMapping("/{userId}/is-following")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
            @PathVariable("userId") Long userId,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        boolean isFollowing = followService.isFollowing(userId, username);
        return ResponseEntity.ok(ApiResponse.success(isFollowing));
    }
}
