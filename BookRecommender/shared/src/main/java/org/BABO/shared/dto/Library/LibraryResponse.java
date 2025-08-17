package org.BABO.shared.dto.Library;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.Library;
import org.BABO.shared.model.Book;
import java.util.List;

/**
 * DTO per le risposte delle operazioni sulle librerie
 */
public class LibraryResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("libraries")
    private List<String> libraries; // Lista nomi delle librerie

    @JsonProperty("books")
    private List<Book> books; // Lista libri in una libreria

    @JsonProperty("library")
    private Library library; // Singola libreria creata/modificata

    // Costruttori
    public LibraryResponse() {}

    public LibraryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LibraryResponse(boolean success, String message, List<String> libraries) {
        this.success = success;
        this.message = message;
        this.libraries = libraries;
    }

    public LibraryResponse(boolean success, String message, List<Book> books, boolean isBooks) {
        this.success = success;
        this.message = message;
        this.books = books;
    }

    public LibraryResponse(boolean success, String message, Library library) {
        this.success = success;
        this.message = message;
        this.library = library;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public List<Book> getBooks() {
        return books;
    }

    public Library getLibrary() {
        return library;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    @Override
    public String toString() {
        return "LibraryResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", libraries=" + (libraries != null ? libraries.size() + " items" : "null") +
                ", books=" + (books != null ? books.size() + " items" : "null") +
                ", library=" + library +
                '}';
    }
}