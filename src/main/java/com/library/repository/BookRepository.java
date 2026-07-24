package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find books by title or author (case insensitive, partial match)
     */
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author);

    /**
     * Find books by category
     */
    List<Book> findByCategory(String category);

    /**
     * Find books with available quantity > 0
     */
    @Query("SELECT b FROM Book b WHERE b.availableQuantity > 0")
    List<Book> findAvailableBooks();

    /**
     * Find most borrowed books (sorted by total borrows descending)
     * FIXES: findMostBorrowedBooks() error
     */
    @Query("SELECT b FROM Book b ORDER BY b.totalBorrows DESC")
    List<Book> findMostBorrowedBooks();
}