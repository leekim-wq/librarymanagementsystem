📁 ADR-001: Use Spring Boot as the Backend Framework
Status: Accepted

Context:
We needed a robust, enterprise-grade framework for building the library management system's backend. The framework had to support REST API development, object-relational mapping, security, and rapid development.

Decision:
We chose Spring Boot 3.2.0 with Java 17 as the core backend framework.

Rationale:

Spring Boot provides auto-configuration, reducing boilerplate code.

It offers seamless integration with Spring Data JPA for database operations.

Spring Security provides built-in authentication and authorization mechanisms.

The framework supports embedded Tomcat, simplifying deployment.

Extensive community support and documentation are available.

The dependency injection pattern aligns with object-oriented design principles.

Consequences:

The application runs on Java 17, requiring a compatible JDK.

Maven is used for dependency management.

Development follows the MVC pattern with Controllers, Services, and Repositories.

The application can be packaged as a self-contained JAR file.

Alternatives Considered:

Django (Python) – less suitable for the required object-oriented design approach.

Node.js with Express – lacks built-in security and ORM features.