package miiiiiin.com.vinyler.application.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DebugEventListener {

    @EventListener
    public void onLiked(VinylLikedEvent e) {
        log.info("[EVENT] 좋아요 eventId={} discogsId={} delta={}", e.eventId(), e.discogsId(), e.delta());
    }
    @EventListener
    public void onReviewChanged(ReviewChangedEvent e) {
        log.info("[EVENT] 리뷰 eventId={} discogsId={} delta={}", e.eventId(), e.discogsId(), e.delta());
    }
}
