package com.library.librarymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")          // <-- ADD THIS
class LibraryManagementApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the application context loads successfully.
    }
}