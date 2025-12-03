package moe.solo.cloneUnsplash.dto.collection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 최대 100자입니다")
    private String title;

    @Size(max = 1000, message = "설명은 최대 1000자입니다")
    private String description;

    @Builder.Default
    private Boolean isPrivate = false;
}
