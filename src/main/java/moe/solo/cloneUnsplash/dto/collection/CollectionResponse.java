package moe.solo.cloneUnsplash.dto.collection;

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
public class CollectionResponse {
    private Long id;
    private String title;
    private String description;
    private Boolean isPrivate;
    private UserSummary user;
    private Integer photosCount;
    private List<String> coverPhotos; // 최대 3개의 커버 사진 URL
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
