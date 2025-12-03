package moe.solo.cloneUnsplash.dto.photo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private String color;
    private Long viewsCount;
    private Long downloadsCount;
    private Long likesCount;
    private UserSummary user;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean likedByCurrentUser;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String username;
        private String name;
        private String profileImageUrl;
    }
}
