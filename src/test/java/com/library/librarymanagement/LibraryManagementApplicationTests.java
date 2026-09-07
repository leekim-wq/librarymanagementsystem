package com.library.librarymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = "/test-data.sql")   // <-- this tells Spring to run the SQL script before the test
class LibraryManagementApplicationTests {

    @Test
    void contextLoads() {
        // This test simply verifies that the application context loads successfully.
        // The @Sql annotation ensures a valid member exists, so the username column is not null.
    }
}