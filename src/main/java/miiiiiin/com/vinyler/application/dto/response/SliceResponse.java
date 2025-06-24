package miiiiiin.com.vinyler.application.dto.response;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
public class SliceResponse<T> {
    // 현재 페이지의 데이터
    private final List<T> content;
    // 다음 페이지 여부
    private final boolean hasNext;
    // 다음 페이지 요청용 커서 (마지막 요소의 ID)
    private final Long nextCursorId;
    private final int size;

    public SliceResponse(Slice<T> sliceContent, Long lastCursorId) {
        this.content = sliceContent.getContent();
        this.hasNext = sliceContent.hasNext();
        this.nextCursorId = lastCursorId;
        this.size = sliceContent.getSize();
    }
}
