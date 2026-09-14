package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ============================================================
    // ACTIVE-ONLY queries (catalog, search, listings)
    // These hide soft-deleted books from users.
    // ============================================================

    /** All active books, ordered by title. */
    @Query("SELECT b FROM Book b WHERE b.active = true ORDER BY b.title ASC")
    List<Book> findAllActive();

    /** Find a single active book by ID. */
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.active = true")
    Optional<Book> findActiveById(@Param("id") Long id);

    /** Search across title, author, category, description — active only. */
    @Query("""
        SELECT b FROM Book b
        WHERE b.active = true
          AND (LOWER(b.title)       LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.author)      LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.category)    LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY b.rating DESC
    """)
    List<Book> searchActiveAcrossAllFields(@Param("q") String query);

    /** Active books in a specific category. */
    @Query("SELECT b FROM Book b WHERE b.active = true AND b.category = :category ORDER BY b.title ASC")
    List<Book> findActiveByCategory(@Param("category") String category);

    /** Active books with at least one available copy. */
    @Query("SELECT b FROM Book b WHERE b.active = true AND b.availableQuantity > 0 ORDER BY b.title ASC")
    List<Book> findAvailableBooks();

    /** Active books sorted by popularity (for AI recommendations fallback). */
    @Query("SELECT b FROM Book b WHERE b.active = true ORDER BY b.totalBorrows DESC")
    List<Book> findMostBorrowedBooks();

    /** Total copies across active books. */
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Book b WHERE b.active = true")
    Long sumTotalCopies();

    /** Total available copies across active books. */
    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b WHERE b.active = true")
    Long sumAvailableCopies();

    /** Distinct categories from active books. */
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.active = true AND b.category IS NOT NULL ORDER BY b.category ASC")
    List<String> findAllActiveCategories();

    // ============================================================
    // ADMIN/STAFF queries (include inactive books)
    // ============================================================

    /** All books (active + inactive) — for admin dashboards. */
    List<Book> findAllByOrderByTitleAsc();

    /** Legacy queries — kept for backward compatibility. */
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    List<Book> findByCategory(String category);
}