package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
public class LibrarianController {

    @Autowired
    private BookService bookService;

    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/add-book";
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        return "redirect:/books";
    }

    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        return "books/edit-book";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute Book book,
                             RedirectAttributes redirectAttributes) {
        Book existingBook = bookService.getBookById(id).orElse(null);
        if (existingBook != null) {
            book.setId(id);
            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("success", "Book updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Book not found!");
        }
        return "redirect:/books";
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete book!");
        }
        return "redirect:/books";
    }
}