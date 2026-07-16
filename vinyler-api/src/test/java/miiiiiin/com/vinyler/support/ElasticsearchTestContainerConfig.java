package miiiiiin.com.vinyler.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class ElasticsearchTestContainerConfig {

    // docker/elasticsearch/Dockerfile(Nori 포함)을 테스트 실행 시점에 직접 빌드
    // 미리 수동으로 이미지를 만들어둘 필요가 없어 로컬/CI 어디서든 동일하게 동작
    static final ImageFromDockerfile NORI_IMAGE = new ImageFromDockerfile()
            .withDockerfile(Paths.get("docker/elasticsearch/Dockerfile"));

    // spring.elasticsearch.uris 를 이 컨테이너로 자동 연결
    @Bean
    @ServiceConnection
    ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer(
                DockerImageName.parse(NORI_IMAGE.get())
                        .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false")
                // 엘라스틱 서치 콜드 스타트가 기본 대기시간(60초)보다 오래 걸릴 수 있어 넉넉히 설정
                .withStartupTimeout(Duration.ofMinutes(3));
    }
}
