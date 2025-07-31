package org.BABO.server.controller;

import org.BABO.shared.model.Book;
import org.BABO.server.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per gestire le operazioni sui libri
 */
@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * Recupera tutti i libri
     * GET /api/books
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            System.out.println("📚 Ritornati " + books.size() + " libri");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero di tutti i libri: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint di test per verificare che il server funzioni
     * GET /api/books/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ Book Service is running!");
    }

    /**
     * Recupera un libro specifico per ID
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            if (book != null) {
                System.out.println("📖 Ritornato libro: " + book.getTitle());
                return ResponseEntity.ok(book);
            } else {
                System.out.println("⚠️ Libro non trovato con ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero del libro " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Ricerca libri per titolo o autore
     * GET /api/books/search?q={query}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam(value = "q", required = true) String query) {
        try {
            System.out.println("🔍 Ricerca richiesta con parametro: '" + query + "'");

            if (query == null || query.trim().isEmpty()) {
                System.out.println("⚠️ Query vuota o null");
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooks(query.trim());
            System.out.println("🔍 Ricerca '" + query + "': trovati " + books.size() + " risultati");

            // Debug: stampa i primi risultati
            if (!books.isEmpty()) {
                System.out.println("📖 Primi risultati:");
                for (int i = 0; i < Math.min(3, books.size()); i++) {
                    Book book = books.get(i);
                    System.out.println("  - " + book.getTitle() + " di " + book.getAuthor());
                }
            }

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca '" + query + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera libri in evidenza
     * GET /api/books/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Book>> getFeaturedBooks() {
        try {
            List<Book> books = bookService.getFeaturedBooks();
            System.out.println("⭐ Ritornati " + books.size() + " libri in evidenza");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero dei libri in evidenza: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera libri gratuiti
     * GET /api/books/free
     */
    @GetMapping("/free")
    public ResponseEntity<List<Book>> getFreeBooks() {
        try {
            List<Book> books = bookService.getFreeBooks();
            System.out.println("💰 Ritornati " + books.size() + " libri gratuiti");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero dei libri gratuiti: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera nuove uscite
     * GET /api/books/new-releases
     */
    @GetMapping("/new-releases")
    public ResponseEntity<List<Book>> getNewReleases() {
        try {
            List<Book> books = bookService.getNewReleases();
            System.out.println("✨ Ritornati " + books.size() + " nuove uscite");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero delle nuove uscite: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Ricerca libri per autore e anno
     * GET /api/books/search/author-year?author={author}&year={year}
     */
    @GetMapping("/search/author-year")
    public ResponseEntity<List<Book>> searchBooksByAuthorAndYear(
            @RequestParam(value = "author", required = true) String author,
            @RequestParam(value = "year", required = false) String year) {
        try {
            System.out.println("👤📅 Ricerca per AUTORE-ANNO richiesta: '" + author + "' (" + year + ")");

            if (author == null || author.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooksByAuthorAndYear(author.trim(), year);
            System.out.println("👤📅 Ricerca autore-anno: trovati " + books.size() + " risultati");

            // Debug: stampa i primi risultati
            if (!books.isEmpty()) {
                System.out.println("📖 Primi risultati autore-anno:");
                for (int i = 0; i < Math.min(3, books.size()); i++) {
                    Book book = books.get(i);
                    System.out.println("  - " + book.getTitle() + " di " + book.getAuthor());
                }
            }

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca autore-anno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Ricerca libri SOLO per titolo
     * GET /api/books/search/title?q={query}
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchBooksByTitle(@RequestParam(value = "q", required = true) String query) {
        try {
            System.out.println("📖 Ricerca per TITOLO richiesta: '" + query + "'");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooksByTitle(query.trim());
            System.out.println("📖 Ricerca titolo '" + query + "': trovati " + books.size() + " risultati");

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca per titolo '" + query + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Ricerca libri SOLO per autore
     * GET /api/books/search/author?q={query}
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchBooksByAuthor(@RequestParam(value = "q", required = true) String query) {
        try {
            System.out.println("👤 Ricerca per AUTORE richiesta: '" + query + "'");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooksByAuthor(query.trim());
            System.out.println("👤 Ricerca autore '" + query + "': trovati " + books.size() + " risultati");

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca per autore '" + query + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint di debug per testare la ricerca
     * GET /api/books/debug-search?q={query}
     */
    @GetMapping("/debug-search")
    public ResponseEntity<String> debugSearch(@RequestParam(value = "q", required = false, defaultValue = "test") String query) {
        try {
            System.out.println("🧪 Debug search chiamato con query: '" + query + "'");

            List<Book> allBooks = bookService.getAllBooks();
            System.out.println("📚 Libri totali disponibili: " + allBooks.size());

            if (!allBooks.isEmpty()) {
                System.out.println("📖 Primi 3 libri nel database:");
                for (int i = 0; i < Math.min(3, allBooks.size()); i++) {
                    Book book = allBooks.get(i);
                    System.out.println("  " + (i+1) + ". " + book.getTitle() + " di " + book.getAuthor());
                }
            }

            List<Book> searchResults = bookService.searchBooks(query);
            System.out.println("🔍 Risultati ricerca per '" + query + "': " + searchResults.size());

            String response = String.format(
                    "Debug Search Results:\n" +
                            "Query: '%s'\n" +
                            "Total Books: %d\n" +
                            "Search Results: %d\n" +
                            "Server Time: %s",
                    query, allBooks.size(), searchResults.size(), java.time.LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Errore nel debug search: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Recupera i libri più recensiti
     * GET /api/books/most-reviewed
     */
    @GetMapping("/most-reviewed")
    public ResponseEntity<List<Book>> getMostReviewedBooks() {
        try {
            List<Book> books = bookService.getMostReviewedBooksWithDetails();
            System.out.println("🏆 Ritornati " + books.size() + " libri più recensiti");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero dei libri più recensiti: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera i libri meglio valutati
     * GET /api/books/top-rated
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<Book>> getTopRatedBooks() {
        try {
            List<Book> books = bookService.getTopRatedBooksWithDetails();
            System.out.println("⭐ Ritornati " + books.size() + " libri meglio valutati");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero dei libri meglio valutati: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}