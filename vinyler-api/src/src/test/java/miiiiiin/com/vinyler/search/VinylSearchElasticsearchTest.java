package miiiiiin.com.vinyler.search;

import miiiiiin.com.vinyler.application.document.VinylDocument;
import miiiiiin.com.vinyler.application.repository.VinylerElasticSearchRepository;
import miiiiiin.com.vinyler.support.ElasticsearchTestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
@Testcontainers
@Import(ElasticsearchTestContainerConfig.class) // 공용 설정 끌어옴
@DisplayName("Vinyl 검색 통합 테스트 (실제 elasticsearch + nori)")
public class VinylSearchElasticsearchTest {

    @Autowired
    VinylerElasticSearchRepository repo;

    @Autowired
    ElasticsearchOperations ops;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 @Setting/@Field 매핑대로 인덱스를 새로 생성
        IndexOperations idx = ops.indexOps(VinylDocument.class);
        if (idx.exists()) idx.delete();
        idx.createWithMapping();
    }

    private  void index(VinylDocument doc) {
        repo.save(doc);
        // 저장 즉시 검색 가능하게
        ops.indexOps(VinylDocument.class).refresh();
    }

    private VinylDocument vinyl(long id, String title, String artist, long likes) {
        return VinylDocument.builder()
                .discogsId(id).title(title).artistsSort(artist)
                .likesCount(likes).reviewsCount(0L)
                .suggest(title + " " + artist).build();
    }

    @Test
    @DisplayName("한국어 검색 : NORI로 '재즈 명반'이 토큰화되어 '명반'으로도 검색된다")
    void 한국어_형태소_검색() {
        //  nori 설정 실제로 붙었는지 검증
        index(vinyl(1L, "재즈 명반", "Miles Davis", 5L));

        // '명반'만 쳐도 잡히면 = 통짜 문자열이 아니라 형태소로 색인됐다는 증거
        Page<VinylDocument> page = repo.searchByKeyword("명반", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(VinylDocument::getTitle).contains("재즈 명반");
    }

    @Test
    @DisplayName("오타 허용 : fuzziness로 'Milez'가 'Miles Davis'를 찾는다")
    void 오타_검색() {
        index(vinyl(1L, "Kind of Blue", "Miles Davis", 5L));

        Page<VinylDocument> page = repo.searchByKeyword("Milez", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("정렬 : 찜 많은 순(likesCount DESC)으로 반환된다")
    void 찜_많은순_정렬() {
        index(vinyl(1L, "재즈 A", "V/A", 3L));
        index(vinyl(2L, "재즈 B", "V/A", 9L));
        index(vinyl(3L, "재즈 C", "V/A", 6L));

        Page<VinylDocument> page = repo.searchByKeyword("재즈",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "likesCount")));

        assertThat(page.getContent()).extracting(VinylDocument::getLikesCount)
                .containsExactly(9L, 6L, 3L);
    }

    @Test
    @DisplayName("자동완성 : '재' 접두어로 'search_as_you_type' 매핑이 동작한다")
    void 자동완성_접두어() {
        index(vinyl(1L, "재즈 명반", "Miles Davis", 5L));

        Page<VinylDocument> page = repo.autocomplete("재", PageRequest.of(0, 5));
        assertThat(page.getTotalElements()).isEqualTo(1);
    }










}
