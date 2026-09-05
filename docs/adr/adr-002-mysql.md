📁 ADR-002: Use MySQL as the Database
Status: Accepted

Context:
The system requires a reliable relational database to store book, member, and loan data. The database must support ACID transactions and enforce data integrity.

Decision:
We selected MySQL 8.0 as the primary database.

Rationale:

MySQL is open-source, widely used, and well-supported.

Spring Data JPA integrates seamlessly with MySQL.

The database supports complex queries and relationships.

It is easy to set up and maintain for development and production.

The platform provides robust transaction support essential for library operations.

Consequences:

The system is tied to a relational database model.

All entities are mapped using JPA annotations.

Database migrations (e.g., adding columns) require manual DDL statements.

The lib_db database must be created before the application starts.

Alternatives Considered:

PostgreSQL – also viable, but MySQL was chosen for familiarity.

MongoDB (NoSQL) – not suitable for transactional loan records.