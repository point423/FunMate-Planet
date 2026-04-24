package com.zjgsu.pjt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // ✅ 使用测试环境配置，避免连接真实数据库
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
