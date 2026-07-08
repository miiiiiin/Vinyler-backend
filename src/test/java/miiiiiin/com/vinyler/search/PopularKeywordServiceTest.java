package miiiiiin.com.vinyler.search;

import miiiiiin.com.vinyler.application.dto.response.PopularKeywordDto;
import miiiiiin.com.vinyler.application.service.PopularKeywordService;
import miiiiiin.com.vinyler.config.RedisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {RedisConfig.class, PopularKeywordService.class})
@Testcontainers
@DisplayName("인기 검색어 통합 테스트 (실제 Redis)")
public class PopularKeywordServiceTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("data.redis.host", redis::getHost);
        registry.add("data.redis.port", () -> redis.getMappedPort(6379));
        // 테스트 컨테이너는 무비번
        registry.add("data.redis.password", () -> "");
    }

    @Autowired
    PopularKeywordService service;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHH");
    @BeforeEach
    void clean() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("기록 : 같은 검색어를 여러 번 치면 점수가 누적된다")
    void record_누적() {
        service.record("재즈");
        service.record("재즈");
        service.record("록");

        List<PopularKeywordDto> topKeywords = service.getTopKeywords(10, 1);

        // 검색 횟수 높은 대로 결과 나오는지 확인
        assertThat(topKeywords).extracting(PopularKeywordDto::keyword)
                .containsExactly("재즈", "록");

        // 재즈 2회 검색 (ZINCRBY가 같은 멤버 점수를 실제로 더한다))
        assertThat(topKeywords.get(0).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("정규화 : 대소문자/공백이 달라도 같은 검색어로 합산된다")
    void record_정규화() {
        service.record("Jazz");
        service.record("JAZZ");
        service.record("  jazz ");

        List<PopularKeywordDto> topKeywords = service.getTopKeywords(10, 1);
        assertThat(topKeywords).hasSize(1);
        // 소문자 jazz와 같은지
        assertThat(topKeywords.get(0).keyword()).isEqualTo("jazz");
        // 3개가 하나로 합쳐져서 검색되는지
        assertThat(topKeywords.get(0).count()).isEqualTo(3);
    }

    @Test
    @DisplayName("윈도우 : 최근 2시간 버킷은 합산하고, 그 밖은 제외한다")
    void getTop_윈도우_합산과_제외() {
        LocalDateTime now = LocalDateTime.now();
        String prefix = "search:keyword:";
        String cur = prefix + now.format(FMT);
        String prev = prefix + now.minusHours(1).format(FMT);
        // 윈도우 밖
        String old  = prefix + now.minusHours(3).format(FMT);

        redisTemplate.opsForZSet().add(prev, "시티팝", 4);
        redisTemplate.opsForZSet().add(cur, "시티팝", 6);
        redisTemplate.opsForZSet().add(old, "재즈", 99);

        List<PopularKeywordDto> topKeywords = service.getTopKeywords(10, 2);

        // 3시간 전 버킷은 윈도우 밖이므로 포함하지 않음
        assertThat(topKeywords).extracting(PopularKeywordDto::keyword)
                .contains("시티팝")
                .doesNotContain("재즈");

        // 2시간 동안 같은 멤버의 검색 점수 합산 (ZUNIONSTORE가 최근 2시간을 합산(4+6=10))
        assertThat(topKeywords.get(0).count()).isEqualTo(10);
    }

    @Test
    @DisplayName("TTL : 기록하면 버킷에 만료 시간이 걸린다")
    void record_TTL() {
        service.record("록");
        String cur = "search:keyword:" + LocalDateTime.now().format(FMT);

        Long ttl = redisTemplate.getExpire(cur, TimeUnit.SECONDS);

        // 만료 미설정이면 -1
        assertThat(ttl).isPositive();
    }
}
