package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String category;
    private String description;
    private Integer quantity = 1;

    @Column(name = "available_quantity")
    private Integer availableQuantity = 1;

    private Double rating = 0.0;

    @Column(name = "total_borrows")
    private Integer totalBorrows = 0;

    /**
     * Check if book is available for borrowing
     * FIXES: isAvailable() method reference error
     */
    public boolean isAvailable() {
        return availableQuantity != null && availableQuantity > 0;
    }
}