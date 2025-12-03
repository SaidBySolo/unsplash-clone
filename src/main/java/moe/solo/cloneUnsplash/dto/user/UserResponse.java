package moe.solo.cloneUnsplash.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String bio;
    private String profileImageUrl;
    private String location;
    private String portfolioUrl;
    private String instagramUsername;
    private String twitterUsername;
    private Long photosCount;
    private Long collectionsCount;
    private Long followersCount;
    private Long followingCount;
    private LocalDateTime createdAt;
}
