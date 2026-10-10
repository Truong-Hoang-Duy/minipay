package com.minipay;

import static org.assertj.core.api.Assertions.assertThat;

import com.minipay.support.TestcontainersConfiguration;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;

/** Proves the application boots against real infrastructure and can talk to each piece. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class InfrastructureSmokeIT {

    static final String SMOKE_TOPIC = "minipay.smoke-test";

    /** Auto-creation is off, so the topic is declared; Spring's KafkaAdmin creates it at startup. */
    @TestConfiguration(proxyBeanMethods = false)
    static class SmokeTopicConfig {
        @Bean
        NewTopic smokeTopic() {
            return TopicBuilder.name(SMOKE_TOPIC).partitions(1).replicas(1).build();
        }
    }

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    KafkaTemplate<String, String> kafka;

    @Test
    void flywayAppliedMigrations() {
        Integer applied =
                jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE", Integer.class);

        assertThat(applied).isGreaterThanOrEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM feature_flag", Integer.class))
                .isZero();
    }

    @Test
    void mysqlRunsWithExpectedServerSettings() {
        assertThat(jdbc.queryForObject("SELECT VERSION()", String.class)).startsWith("8.4");
        assertThat(jdbc.queryForObject("SELECT @@transaction_isolation", String.class))
                .isEqualTo("REPEATABLE-READ");
        assertThat(jdbc.queryForObject("SELECT @@character_set_server", String.class))
                .isEqualTo("utf8mb4");
    }

    @Test
    void redisStoresAndReadsValues() {
        redis.opsForValue().set("smoke:key", "ok");

        assertThat(redis.opsForValue().get("smoke:key")).isEqualTo("ok");
    }

    @Test
    void kafkaAcceptsMessages() throws Exception {
        SendResult<String, String> result =
                kafka.send(SMOKE_TOPIC, "key", "hello").get(10, TimeUnit.SECONDS);

        assertThat(result.getRecordMetadata().offset()).isGreaterThanOrEqualTo(0);
    }
}
