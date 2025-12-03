package moe.solo.cloneUnsplash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moe.solo.cloneUnsplash.dto.common.ApiResponse;
import moe.solo.cloneUnsplash.dto.user.UserResponse;
import moe.solo.cloneUnsplash.dto.user.UserUpdateRequest;
import moe.solo.cloneUnsplash.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("userId") Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable("username") String username) {
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        UserResponse response = userService.updateUser(userId, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("프로필이 업데이트되었습니다", response));
    }

    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfileImage(
            @PathVariable("userId") Long userId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        UserResponse response = userService.updateProfileImage(userId, file, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지가 업데이트되었습니다", response));
    }
}
