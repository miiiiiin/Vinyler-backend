package miiiiiin.com.vinyler.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {
    @NotNull
    @JsonProperty("discogs_id")
    private Long discogsId;
    @Min(0)
    @Max(5)
    private int rating;
    private String content;
}
