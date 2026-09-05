📁 ADR-004: Use Role-Based Access Control (RBAC) with Spring Security
Status: Accepted

Context:
The system has three distinct user roles (Admin, Librarian, Member) with different permissions. We needed a robust way to enforce access control.

Decision:
We implemented RBAC using Spring Security, with roles stored in the role column of the members table.

Rationale:

Spring Security provides built-in support for role-based authorization.

URL-based security rules are easy to configure in SecurityConfig.

Method-level security (@PreAuthorize) adds an extra layer of protection.

Thymeleaf sec:authorize tags allow conditional rendering in the UI.

The solution is standard, well-documented, and easy to maintain.

Consequences:

Roles must be assigned correctly in the database.

Every protected endpoint must have appropriate security annotations or URL matchers.

Users with insufficient roles receive 403 Forbidden responses.

Alternatives Considered:

Custom permission system – more complex and time-consuming.

Hard-coded role checks in controllers – less maintainable.