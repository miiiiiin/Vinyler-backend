package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.document.VinylDocument;
import miiiiiin.com.vinyler.application.dto.enums.VinylSortType;
import miiiiiin.com.vinyler.application.dto.response.VinylSearchResultDto;
import miiiiiin.com.vinyler.application.repository.VinylerElasticSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VinylElasticSearchService 단위 테스트")
public class VinylElasticSearchServiceImplTest {
    @Mock
    private VinylerElasticSearchRepository vinylSearchRepository;

    @Mock
    private PopularKeywordService popularKeywordService;

    @InjectMocks
    private VinylElasticSearchServiceImpl service;

    @Test
    @DisplayName("검색 - 정렬기준(LIKES)이 likesCount DESC로 리포지토리에 전달되고 DTO로 변환된다")
    void search_정렬적용_및_DTO변환() {
        // given
        VinylDocument doc = VinylDocument.builder()
                .discogsId(1L)
                .title("재즈 명반")
                .artistsSort("Miles Davis")
                .likesCount(10L)
                .reviewsCount(2L)
                .build();

        when(vinylSearchRepository.searchByKeyword(eq("재즈"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(doc)));

        // when
        Page<VinylSearchResultDto> result = service.search("재즈", VinylSortType.LIKES, 0, 20);

        // then (리포지토리에 넘어간 Pageable의 정렬이 likesCount DESC인지)
        // repo에 어떤 정렬로 요청했는지 (VinylSortType.LIKES -> likesCount DESC로 변환되는지)
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(vinylSearchRepository).searchByKeyword(eq("재즈"), captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("likesCount");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);

        // VinylDocument -> VinylSearchResultDto 변환 확인
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).discogsId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).title()).isEqualTo("재즈 명반");
    }





}
