package miiiiiin.com.vinyler.application.event;

/**
 * Vinyl의 색인 대상 데이터(제목/아티스트/찜수/리뷰수)가 변경되었음을 알리는 이벤트
 * discogsId만 담고, 실제 최신 데이터는 리스너가 DB에서 다시 조회한다
 */
public record VinylIndexSyncEvent(Long discogsId) {

}
