package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.enums.VinylSortType;
import miiiiiin.com.vinyler.application.dto.response.VinylSearchResultDto;
import miiiiiin.com.vinyler.application.repository.VinylerElasticSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VinylElasticSearchServiceImpl implements VinylElasticSearchService {

    private final VinylerElasticSearchRepository vinylSearchRepository;
    private final PopularKeywordService popularKeywordService;
    @Override
    public Page<VinylSearchResultDto> search(String keyword, VinylSortType sortType, int page, int size) {
        // 검색어 집계 (인기 검색어용)
        popularKeywordService.record(keyword);

        // page/size 정렬 기준을 Pageable로 조립 (정렬 필드는 enum이 알고 있음)
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());

        // 엘라스틱 서치 검색 실행 -> 색인 문서를 응답 DTO로 반환
        return vinylSearchRepository.searchByKeyword(keyword, pageable)
                .map(VinylSearchResultDto::from);
    }
}
