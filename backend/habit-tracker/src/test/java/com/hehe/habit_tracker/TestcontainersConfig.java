package com.hehe.habit_tracker;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Cấp một Postgres THẬT trong Docker cho integration test. @ServiceConnection tự
 * nối datasource của Spring vào container này -> test KHÔNG đụng Supabase thật,
 * và đúng dialect Postgres (partial unique index, kiểu dữ liệu... y như production).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }
}
