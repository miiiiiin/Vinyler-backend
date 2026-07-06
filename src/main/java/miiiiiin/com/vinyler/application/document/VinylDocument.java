package miiiiiin.com.vinyler.application.document;

import lombok.*;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.lang.reflect.Type;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "vinyls") // 매핑될 ES 인덱스 이름 지정
@Setting(settingPath = "elasticsearch/vinyl-settings.json")
public class VinylDocument {

    @Id
    private Long discogsId;

    /** 검색 대상 텍스트 필드 (NORI 분석기 적용)
     *  FieldType.Text : 형태소 분석기 태워서 토큰화하는 타입. 부분 검색/유사 검색이
     *  analyzer = "korean" : 커스텀 analyzer 이름 참조 (vinyl-settings.json)
     */
    @Field(type = FieldType.Text, analyzer = "korean")
    private String title;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String artistsSort;


    /** 정렬/필터용 필드
     * Keyword : 형태소 분석 덦이 " 값 전체"를 그대로 하나의 토큰으로 저장 (발매일 문자열은 굳이 토큰 쪼개서 검색할 필요 X)
     */
    @Field(type = FieldType.Keyword)
    private String releasedFormatted;

    @Field(type = FieldType.Long)
    private Long likesCount;

    @Field(type = FieldType.Long)
    private Long reviewsCount;

    public static VinylDocument from(Vinyl vinyl) {
        return VinylDocument.builder()
                .discogsId(vinyl.getDiscogsId())
                .title(vinyl.getTitle())
                .artistsSort(vinyl.getArtistsSort())
                .releasedFormatted(vinyl.getReleasedFormatted())
                .likesCount(vinyl.getLikesCount())
                .reviewsCount(vinyl.getReviewsCount())
                .build();
    }
}
