package miiiiiin.com.vinyler.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.vinyl.Format;

@Data
@Builder
public class FormatDto {
    private String name;
    private List<String> descriptions;

    public static FormatDto of(Format format) {
        return FormatDto.builder()
                .name(format.getName())
                .descriptions(format.getDescriptions())
                .build();
    }
}
