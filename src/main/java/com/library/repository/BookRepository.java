package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    List<Book> findByCategory(String category);

    @Query("SELECT b FROM Book b ORDER BY b.totalBorrows DESC")
    List<Book> findMostBorrowedBooks();

    @Query("SELECT b FROM Book b WHERE b.availableQuantity > 0")
    List<Book> findAvailableBooks();
}