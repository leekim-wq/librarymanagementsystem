package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    List<Book> findByCategory(String category);

    @Query("SELECT b FROM Book b WHERE b.availableQuantity > 0")
    List<Book> findAvailableBooks();

    @Query("SELECT b FROM Book b ORDER BY b.totalBorrows DESC")
    List<Book> findMostBorrowedBooks();

    // ===== NEW: Sum queries for copy counts =====
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Book b")
    Long sumTotalCopies();

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b")
    Long sumAvailableCopies();
}