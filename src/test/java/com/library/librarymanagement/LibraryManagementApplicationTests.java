package com.library.librarymanagement;

import com.library.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)   // <-- add this
class LibraryManagementApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the context loads successfully.
    }
}