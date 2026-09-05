📁 ADR-006: Use Session-Based Cart Storage
Status: Accepted

Context:
The borrowing cart needs to persist across multiple page requests without storing data in the database prematurely.

Decision:
We stored the cart items in the user's HTTP session using HttpSession.

Rationale:

Simple to implement with Spring Boot.

No database overhead for temporary data.

Cart is automatically cleared when the session expires.

The cart follows the user across pages.

Consequences:

Cart data is lost if the session is invalidated.

Not suitable for distributed environments without session replication.

The session must be enabled in the application configuration.

Alternatives Considered:

Cookie-based storage – limited size, not secure for sensitive data.

Database-backed cart – introduces unnecessary persistence overhead.