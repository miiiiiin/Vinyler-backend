package miiiiiin.com.vinyler.application.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum VinylSortType {
    REVIEWS("reviewsCount"), //리뷰 많은 순
    LIKES("likesCount"); //찜 많은 순

    // 엘라스틱 서치 문서(VinylDocument)의 실제 정렬 대상 필드명
    private final String field;

    // 위 정렬 타입을 Sort 객체로 변환 (항상 내림차순 = 많은 순)
    public Sort toSort() {
        return  Sort.by(Sort.Direction.DESC, field);
    }
}
