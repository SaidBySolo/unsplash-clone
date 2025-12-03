package moe.solo.cloneUnsplash.dto.collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPhotoToCollectionRequest {

    @NotNull(message = "사진 ID는 필수입니다")
    private Long photoId;
}
