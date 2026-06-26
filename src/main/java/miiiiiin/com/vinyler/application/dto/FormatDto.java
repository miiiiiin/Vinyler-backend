package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.vinyl.Format;

@Data
@Builder
@Schema(description = "음반 포맷 정보")
public class FormatDto {
    @Schema(description = "포맷 이름", example = "Vinyl")
    private String name;

    @Schema(description = "포맷 설명 목록", example = "[\"LP\", \"Album\", \"Stereo\"]")
    private List<String> descriptions;

    public static FormatDto of(Format format) {
        return FormatDto.builder()
                .name(format.getName())
                .descriptions(format.getDescriptions())
                .build();
    }
}
