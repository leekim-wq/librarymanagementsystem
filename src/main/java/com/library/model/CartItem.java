package com.library.model;

public class CartItem {
    private Book book;

    public CartItem(Book book) {
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}