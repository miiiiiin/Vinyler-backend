package miiiiiin.com.vinyler.application.event;

/** 좋아요 발생/취소 이벤트. delta: +1(좋아요) / -1(취소) */
public record VinylLikedEvent(
        String eventId,
        Long discogsId,
        int delta) {
}
