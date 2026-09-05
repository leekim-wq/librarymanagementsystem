package com.library.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
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

    @Column(name = "quantity")
    private Integer quantity = 1;

    @Column(name = "available_quantity")
    private Integer availableQuantity = 1;

    private Double rating = 0.0;

    @Column(name = "total_borrows")
    private Integer totalBorrows = 0;

    // ---------- Getters and Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalBorrows() { return totalBorrows; }
    public void setTotalBorrows(Integer totalBorrows) { this.totalBorrows = totalBorrows; }

    public boolean isAvailable() {
        return availableQuantity != null && availableQuantity > 0;
    }
}