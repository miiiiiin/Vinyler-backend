package miiiiiin.com.vinyler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("로컬 PostgreSQL 없이는 컨텍스트 로드 불가 - 인프라 환경에서 실행")
@SpringBootTest
class VinylerBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
