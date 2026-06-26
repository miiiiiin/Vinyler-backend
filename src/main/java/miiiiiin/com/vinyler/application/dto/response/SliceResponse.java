package miiiiiin.com.vinyler.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@Schema(description = "커서 기반 페이징 응답")
public class SliceResponse<T> {
    @Schema(description = "현재 페이지의 데이터 목록")
    private final List<T> content;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;

    @Schema(description = "다음 페이지 요청용 커서 ID (마지막 요소의 ID)", example = "42")
    private final Long nextCursorId;

    @Schema(description = "요청한 페이지 크기", example = "10")
    private final int size;

    public SliceResponse(Slice<T> sliceContent, Long lastCursorId) {
        this.content = sliceContent.getContent();
        this.hasNext = sliceContent.hasNext();
        this.nextCursorId = lastCursorId;
        this.size = sliceContent.getSize();
    }
}
