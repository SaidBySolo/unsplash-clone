package moe.solo.cloneUnsplash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moe.solo.cloneUnsplash.dto.auth.AuthResponse;
import moe.solo.cloneUnsplash.dto.auth.LoginRequest;
import moe.solo.cloneUnsplash.dto.auth.RegisterRequest;
import moe.solo.cloneUnsplash.dto.common.ApiResponse;
import moe.solo.cloneUnsplash.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다", response));
    }
}
