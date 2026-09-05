📁 ADR-003: Use Cart-Based Borrowing Instead of Direct Borrowing
Status: Accepted

Context:
Users needed a way to borrow multiple books at once. Direct borrowing (one book at a time) would require multiple requests and cause user frustration.

Decision:
We implemented a session-based borrowing cart that allows users to add books, review them, and check out once.

Rationale:

Improves user experience by allowing batch borrowing.

Session-based storage avoids unnecessary database calls.

Checkout validates all books at once, providing clear feedback.

Partial success is supported (some books succeed, others fail).

Reduces the number of database transactions.

Consequences:

The cart relies on HttpSession, which is not distributed across servers (may need Redis for scaling).

Users must log in to use the cart.

The cart is cleared after successful checkout.

Alternatives Considered:

Direct borrowing with a confirmation step – less user-friendly.

Storing cart in database – more complex and slower.