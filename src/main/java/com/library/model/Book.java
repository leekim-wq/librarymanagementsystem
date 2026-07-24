package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String isbn;
    private String category;
    private String description;
    private String coverImage;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Integer availableQuantity = 1;

    private LocalDate publishedDate;
    private String publisher;

    @Column(name = "total_borrows")
    private Integer totalBorrows = 0;

    private Double rating = 0.0;

    public boolean isAvailable() {
        return availableQuantity > 0;
    }
}