package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    List<Book> findByCategory(String category);

    @Query("SELECT b FROM Book b WHERE b.availableQuantity > 0 ORDER BY b.title ASC")
    List<Book> findAvailableBooks();

    @Query("SELECT b FROM Book b ORDER BY b.totalBorrows DESC")
    List<Book> findMostBorrowedBooks();

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Book b")
    Long sumTotalCopies();

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b")
    Long sumAvailableCopies();

    // ============================================================
    // Multi-field search — title, author, category, description
    // ============================================================
    @Query("""
        SELECT b FROM Book b
        WHERE LOWER(b.title)       LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(b.author)      LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(b.category)    LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(b.description) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY b.rating DESC
    """)
    List<Book> searchAcrossAllFields(@Param("q") String query);

    // Same search but only available books (for AI assistant)
    @Query("""
        SELECT b FROM Book b
        WHERE b.availableQuantity > 0
          AND (LOWER(b.title)       LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.author)      LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.category)    LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY b.rating DESC
    """)
    List<Book> searchAvailableAcrossAllFields(@Param("q") String query);
}