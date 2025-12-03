package moe.solo.cloneUnsplash.dto.photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @Size(max = 1000, message = "설명은 최대 1000자입니다")
    private String description;

    private List<String> tags;
}
