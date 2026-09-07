# Final Project Report – Smart Library Management System

## 1. Project Overview

### 1.1 Project Title
Smart Library Management System

### 1.2 Team Members
Lewis Kimani - Sccj/00630/2023
Patrick Kitheka - Sccj/0649P/2023


### 1.3 Project Description
The Smart Library Management System is a web-based application designed to digitize and streamline library operations. The system supports three user roles: **Members** who can browse and borrow books, **Librarians** who manage the book inventory and process returns, and **Administrators** who oversee users and system settings. The application integrates an AI-powered recommendation engine to enhance the user experience.

### 1.4 Duration
[Start Date] – [End Date] (e.g., August – September 2026)

---

## 2. Technologies Used

| Category | Technology |
|----------|------------|
| **Backend Framework** | Spring Boot 3.2.0 with Java 17 |
| **Build Tool** | Maven |
| **Database** | MySQL 8.0 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Frontend** | Thymeleaf, Bootstrap 5, Font Awesome |
| **Security** | Spring Security (BCrypt, RBAC) |
| **AI Service** | Python Flask, scikit-learn, pandas |
| **Testing** | JUnit 5, Mockito, JaCoCo (coverage) |
| **CI/CD** | GitHub Actions |
| **Version Control** | Git, GitHub |
| **UML Tool** | PlantUML |

---

## 3. Architecture Overview

The system follows a **Model-View-Controller (MVC)** architectural pattern:

- **Presentation Layer** – Thymeleaf templates, CSS/JS, Bootstrap.
- **Controller Layer** – Spring MVC controllers handling HTTP requests.
- **Service Layer** – Business logic (BookService, LoanService, MemberService, CartService).
- **Repository Layer** – Spring Data JPA repositories for database access.
- **Database Layer** – MySQL storing entities (Book, Member, Loan).

A separate **AI Microservice** (Python Flask) provides book recommendations via REST API.

### 3.1 Component Diagram
*(Insert Component Diagram from UML)*

### 3.2 Deployment Diagram
*(Insert Deployment Diagram from UML)*

---

## 4. Key Features Implemented

### 4.1 User Authentication and Authorization
- Secure registration with password validation (min 8 chars, uppercase, lowercase, digit, special).
- Login using email or username.
- Role-based access control (Admin, Librarian, Member).

### 4.2 Member Features
- Browse, search, and filter books.
- Add/remove books from a session-based cart.
- Batch checkout (borrow multiple books at once).
- View loan history with status and fines.
- AI-powered book recommendations.

### 4.3 Librarian Features
- Add, edit, and delete books.
- Process book returns for any member (with automatic fine calculation).

### 4.4 Administrator Features
- View user list with roles.
- Promote/demote users between roles.
- Dashboard with key metrics.

### 4.5 System Features
- Real-time inventory updates (available copies decrease on borrow, increase on return).
- Borrowing limit enforcement (max 5 active loans).
- Duplicate loan prevention.
- Overdue fine calculation ($0.50/day).

---

## 5. Design Patterns Used

| Pattern | Application |
|---------|-------------|
| **MVC** | Separation of concerns: Controller, Service, Repository layers. |
| **Repository** | Spring Data JPA repositories for data access. |
| **Factory** | Spring Beans and dependency injection. |
| **Observer** | Spring Events (e.g., loan created, book returned). |
| **Strategy** | Different borrowing rules (membership types). |
| **DTO** | CartItem for transferring data between layers. |

---

## 6. Testing Summary

### 6.1 Unit Tests
- **Total Tests:** 34
- **Tests Passing:** 34 (after corrections)
- **Test Coverage (JaCoCo):** [Insert percentage, e.g., 72%]

**Tested Components:**
- `BookService` – borrowBook, returnBook, searchBooks
- `LoanService` – getAllLoansForMember, getActiveLoansForMember
- `MemberService` – saveMember, findByEmail, findByUsername
- `CartService` – add, remove, clear, getCount

### 6.2 Integration Tests
- Spring Boot test context loads correctly.
- Repository methods work with test database.
- REST endpoints return expected responses.

### 6.3 JaCoCo Coverage Report
- Available in `target/site/jacoco/index.html`.
- Also available as a GitHub Actions artifact.

---

## 7. Challenges Faced and Solutions

### Challenge 1: LazyInitializationException when accessing `loan.book` in Thymeleaf
- **Cause:** Hibernate session closed before rendering the view.
- **Solution:** Added `@Transactional` to controller methods and used `JOIN FETCH` in repository queries.

### Challenge 2: CSRF token errors during login/registration
- **Cause:** Spring Security enabled CSRF by default; our frontend forms did not include the token.
- **Solution:** Disabled CSRF in `SecurityConfig` (acceptable for a session‑based REST/HTML app).

### Challenge 3: Role-based access control for borrowing
- **Issue:** Only members should borrow; librarians/admins should not.
- **Solution:** Added `@PreAuthorize("hasRole('MEMBER')")` to `BookService.borrowBook()` and restricted URL matchers in `SecurityConfig`.

### Challenge 4: Test failures due to outdated mocks
- **Cause:** `LoanService` used eager methods (`findByMemberWithBook`), but tests mocked old methods (`findByMember`).
- **Solution:** Updated test mocks to match the actual repository method calls.

### Challenge 5: JaCoCo plugin not found in CI
- **Cause:** Missing `<plugin>` block for JaCoCo in `pom.xml`.
- **Solution:** Added the JaCoCo Maven plugin with `prepare-agent` and `report` executions.

---

## 8. CI/CD Pipeline

- **Tool:** GitHub Actions
- **Workflow:** `.github/workflows/ci.yml`
- **Triggers:** Push to `main`, `develop`, and pull requests.
- **Steps:**
    1. Checkout code.
    2. Set up JDK 17.
    3. Start MySQL service.
    4. Run `mvn clean test`.
    5. Generate JaCoCo report.
    6. Upload report as an artifact.

### 8.1 Successful Build Status
- All tests pass.
- JaCoCo report generated and available.
- Build time: ~2–3 minutes.

---

## 9. Future Improvements

1. **JWT Authentication** – replace session-based auth for stateless APIs.
2. **Redis Session Storage** – enable horizontal scaling.
3. **Email Notifications** – reminders for overdue books.
4. **Mobile App** – React Native or Flutter frontend.
5. **Payment Integration** – for fine payments.
6. **Advanced Search** – full-text search with Elasticsearch.
7. **Book Reservations** – hold books before borrowing.
8. **Analytics Dashboard** – popular books, user activity trends.

---

## 10. Conclusion

The Smart Library Management System successfully delivers a comprehensive, secure, and user-friendly platform for library management. The project followed a structured software engineering process, including requirements analysis, design, implementation, testing, and deployment. All core features were implemented, and the system demonstrates strong adherence to object-oriented principles, design patterns, and modern development practices.

The team gained valuable experience in:
- Full-stack development with Spring Boot.
- Role-based security.
- AI integration.
- Testing and CI/CD.
- Agile project management.

---

## 11. References

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Thymeleaf Documentation: https://www.thymeleaf.org/
- Spring Security Documentation: https://spring.io/projects/spring-security
- MySQL Documentation: https://dev.mysql.com/doc/
- PlantUML: https://plantuml.com/
- JaCoCo: https://www.eclemma.org/jacoco/
- GitHub Actions: https://docs.github.com/en/actions