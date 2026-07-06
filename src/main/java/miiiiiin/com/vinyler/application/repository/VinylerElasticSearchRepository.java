package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.document.VinylDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;

public interface VinylerElasticSearchRepository extends ElasticsearchRepository<VinylDocument, Long> {

    @Query("""
        {
            "multi_match" : {
                "query" : "?0",
                "fields" : ["title", "artistsSort"]
            }
        }
    """)
    Page<VinylDocument> searchByKeyword(String keyword, Pageable pageable);
}
