package com.library.controller;

import com.library.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @ModelAttribute("cartCount")
    public int getCartCount(HttpSession session) {
        return cartService.getCartCount(session);
    }
}