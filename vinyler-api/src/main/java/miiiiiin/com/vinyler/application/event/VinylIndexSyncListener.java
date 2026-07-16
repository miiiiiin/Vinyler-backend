package miiiiiin.com.vinyler.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miiiiiin.com.vinyler.application.document.VinylDocument;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.application.repository.VinylerElasticSearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 발행된 이벤트를 실제로 받아서
 * "DB에서 최신 Vinyl 조회 -> VinylDocument로 변환 -> 엘라스틱 서치에 저장"을 수행
 * db와 엘라스틱 서치 두 저장소 연결되는 부분
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VinylIndexSyncListener {

    private final VinylRepository vinylRepository;
    private final VinylerElasticSearchRepository vinylSearchRepository;

    @Async("esSyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VinylIndexSyncEvent event) {
        vinylRepository.findByDiscogsId(event.discogsId())
                .ifPresentOrElse(
                        vinyl -> {
                            vinylSearchRepository.save(VinylDocument.from(vinyl));
                            log.info("[엘라스틱 서치 동기화] discogsId={} 색인 완료", event.discogsId());
                        },
                        () -> log.warn("[엘라스틱 서치 동기화] discogsId={} Vinyl 객체 미존재, 색인 건너뛰기", event.discogsId())
                );

    }
}
