package miiiiiin.com.vinyler.application.event;

/** 리뷰 작성/삭제 이벤트. delta: +2(작성) / -2(삭제) */
public record ReviewChangedEvent(String eventId, Long discogsId, int delta) {
}
