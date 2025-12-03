package moe.solo.cloneUnsplash.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(max = 100, message = "이름은 최대 100자입니다")
    private String name;

    @Size(max = 500, message = "소개는 최대 500자입니다")
    private String bio;

    @Size(max = 200, message = "위치는 최대 200자입니다")
    private String location;

    private String portfolioUrl;

    @Size(max = 50, message = "인스타그램 사용자명은 최대 50자입니다")
    private String instagramUsername;

    @Size(max = 50, message = "트위터 사용자명은 최대 50자입니다")
    private String twitterUsername;
}
