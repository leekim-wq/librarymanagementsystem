package com.library.service;

import com.library.model.Book;
import com.library.model.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "borrowingCart";

    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Book book) {
        List<CartItem> cart = getCart(session);
        // Prevent duplicates (optional)
        boolean exists = cart.stream().anyMatch(item -> item.getBook().getId().equals(book.getId()));
        if (!exists) {
            cart.add(new CartItem(book));
        }
    }

    public void removeFromCart(HttpSession session, Long bookId) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getBook().getId().equals(bookId));
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public int getCartCount(HttpSession session) {
        return getCart(session).size();
    }
}