package com.minipay.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Real MySQL, Redis and Kafka for integration tests, using the same images and server flags as
 * docker-compose.yml so that tests and local runs behave the same.
 *
 * <p>{@code @ServiceConnection} reads each container's random host port after it starts and
 * registers matching {@code spring.datasource.*}, {@code spring.data.redis.*} and
 * {@code spring.kafka.*} connection details. Spring caches the application context between test
 * classes with the same configuration, so the containers start once per test run.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.4")
                .withDatabaseName("minipay")
                .withCommand(
                        "--character-set-server=utf8mb4",
                        "--collation-server=utf8mb4_0900_ai_ci",
                        "--default-time-zone=+00:00",
                        "--transaction-isolation=REPEATABLE-READ",
                        "--innodb-print-all-deadlocks=ON");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>("redis:7.4-alpine").withExposedPorts(6379);
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka:3.9.1").withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false");
    }
}
