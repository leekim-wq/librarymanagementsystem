package com.library.service;

import com.library.model.Book;
import com.library.model.CartItem;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private HttpSession session;

    @InjectMocks
    private CartService cartService;

    private Book book;
    private List<CartItem> cart;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        cart = new ArrayList<>();
        // Simulate session returning an existing cart
    }

    @Test
    void getCart_shouldReturnExistingCart() {
        when(session.getAttribute("borrowingCart")).thenReturn(cart);

        List<CartItem> result = cartService.getCart(session);

        assertNotNull(result);
        assertEquals(cart, result);
        verify(session, times(1)).getAttribute("borrowingCart");
    }

    @Test
    void getCart_shouldCreateNewCart_whenNoneExists() {
        when(session.getAttribute("borrowingCart")).thenReturn(null);
        // We need to capture the setAttribute call
        doNothing().when(session).setAttribute(eq("borrowingCart"), any());

        List<CartItem> result = cartService.getCart(session);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session, times(1)).setAttribute(eq("borrowingCart"), any());
    }

    @Test
    void addToCart_shouldAddNewBook_whenNotAlreadyPresent() {
        // Setup: cart is empty
        List<CartItem> emptyCart = new ArrayList<>();
        when(session.getAttribute("borrowingCart")).thenReturn(emptyCart);

        cartService.addToCart(session, book);

        assertEquals(1, emptyCart.size());
        assertEquals(book, emptyCart.get(0).getBook());
    }

    @Test
    void addToCart_shouldNotAddDuplicateBook() {
        // Setup: cart already has the book
        List<CartItem> existingCart = new ArrayList<>();
        existingCart.add(new CartItem(book));
        when(session.getAttribute("borrowingCart")).thenReturn(existingCart);

        cartService.addToCart(session, book);

        assertEquals(1, existingCart.size()); // still only one
    }

    @Test
    void removeFromCart_shouldRemoveBook_whenPresent() {
        List<CartItem> existingCart = new ArrayList<>();
        existingCart.add(new CartItem(book));
        when(session.getAttribute("borrowingCart")).thenReturn(existingCart);

        cartService.removeFromCart(session, 1L);

        assertTrue(existingCart.isEmpty());
    }

    @Test
    void removeFromCart_shouldDoNothing_whenBookNotPresent() {
        List<CartItem> existingCart = new ArrayList<>();
        existingCart.add(new CartItem(book));
        when(session.getAttribute("borrowingCart")).thenReturn(existingCart);

        cartService.removeFromCart(session, 2L); // different ID

        assertEquals(1, existingCart.size()); // still present
    }

    @Test
    void clearCart_shouldRemoveSessionAttribute() {
        doNothing().when(session).removeAttribute("borrowingCart");

        cartService.clearCart(session);

        verify(session, times(1)).removeAttribute("borrowingCart");
    }

    @Test
    void getCartCount_shouldReturnSize() {
        List<CartItem> existingCart = new ArrayList<>();
        existingCart.add(new CartItem(book));
        existingCart.add(new CartItem(new Book()));
        when(session.getAttribute("borrowingCart")).thenReturn(existingCart);

        int count = cartService.getCartCount(session);

        assertEquals(2, count);
    }
}