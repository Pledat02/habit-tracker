package com.hehe.habit_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** Context load chạy trên Postgres thật (Testcontainers) — cần Docker (có sẵn trên CI runner). */
@SpringBootTest
@Import(TestcontainersConfig.class)
class HabitTrackerApplicationTests {

	@Test
	void contextLoads() {
	}

}
