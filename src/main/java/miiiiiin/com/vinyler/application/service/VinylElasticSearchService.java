package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.enums.VinylSortType;
import miiiiiin.com.vinyler.application.dto.response.VinylSearchResultDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VinylElasticSearchService {
    Page<VinylSearchResultDto> search(String keyword, VinylSortType sortType, int page, int size);

    List<VinylSearchResultDto> autocomplete(String prefix);
}
