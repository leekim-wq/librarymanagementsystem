package com.library.librarymanagement;

import com.library.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)          // <-- ADD THIS
class LibraryManagementApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the application context loads successfully.
        // Data seeding is disabled, so no Member will be inserted without a username.
    }
}