package org.BABO.shared.dto.Library;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.Library;
import org.BABO.shared.model.Book;
import java.util.List;

/**
 * Data Transfer Object (DTO) che incapsula la risposta del server per le operazioni relative alle librerie.
 * <p>
 * Questa classe è un modello di dati versatile utilizzato per la comunicazione client-server
 * e può contenere diverse tipologie di informazioni a seconda della richiesta, come una lista
 * di nomi di librerie, una lista di libri all'interno di una libreria o un singolo
 * oggetto libreria dopo un'operazione di creazione o modifica.
 * </p>
 */
public class LibraryResponse {

    /**
     * Indica se l'operazione ha avuto successo.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Un messaggio di testo che fornisce dettagli sul risultato dell'operazione.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Una lista di nomi di librerie. Utilizzata per richieste che restituiscono
     * l'elenco delle librerie di un utente.
     */
    @JsonProperty("libraries")
    private List<String> libraries;

    /**
     * Una lista di oggetti {@link Book}. Utilizzata per restituire l'elenco dei libri
     * contenuti in una libreria specifica.
     */
    @JsonProperty("books")
    private List<Book> books;

    /**
     * Un singolo oggetto {@link Library}. Utilizzato per restituire i dettagli di
     * una libreria dopo un'operazione come la creazione o l'aggiornamento.
     */
    @JsonProperty("library")
    private Library library;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public LibraryResponse() {}

    /**
     * Costruttore per una risposta base con solo successo e messaggio.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     */
    public LibraryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Costruttore per una risposta che include una lista di nomi di librerie.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param libraries La lista dei nomi delle librerie.
     */
    public LibraryResponse(boolean success, String message, List<String> libraries) {
        this.success = success;
        this.message = message;
        this.libraries = libraries;
    }

    /**
     * Costruttore per una risposta che include una lista di libri.
     * <p>
     * Il parametro `isBooks` è presente per distinguere questo costruttore da quello
     * che accetta una lista di stringhe (`libraries`).
     * </p>
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param books La lista dei libri.
     * @param isBooks Un flag booleano per la distinzione del costruttore.
     */
    public LibraryResponse(boolean success, String message, List<Book> books, boolean isBooks) {
        this.success = success;
        this.message = message;
        this.books = books;
    }

    /**
     * Costruttore per una risposta che include un singolo oggetto libreria.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param library L'oggetto libreria.
     */
    public LibraryResponse(boolean success, String message, Library library) {
        this.success = success;
        this.message = message;
        this.library = library;
    }

    // Getters

    /**
     * Restituisce lo stato di successo.
     * @return {@code true} se l'operazione è andata a buon fine.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Restituisce il messaggio di feedback.
     * @return Il messaggio.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Restituisce la lista dei nomi delle librerie.
     * @return La lista di stringhe, o {@code null}.
     */
    public List<String> getLibraries() {
        return libraries;
    }

    /**
     * Restituisce la lista dei libri.
     * @return La lista di oggetti {@link Book}, o {@code null}.
     */
    public List<Book> getBooks() {
        return books;
    }

    /**
     * Restituisce il singolo oggetto libreria.
     * @return L'oggetto {@link Library}, o {@code null}.
     */
    public Library getLibrary() {
        return library;
    }

    // Setters

    /**
     * Imposta lo stato di successo.
     * @param success Il nuovo stato.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Imposta il messaggio di feedback.
     * @param message Il nuovo messaggio.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Imposta la lista dei nomi delle librerie.
     * @param libraries La nuova lista.
     */
    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    /**
     * Imposta la lista dei libri.
     * @param books La nuova lista.
     */
    public void setBooks(List<Book> books) {
        this.books = books;
    }

    /**
     * Imposta il singolo oggetto libreria.
     * @param library Il nuovo oggetto.
     */
    public void setLibrary(Library library) {
        this.library = library;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `LibraryResponse`.
     * <p>
     * Utile per scopi di debugging, mostra i principali campi e lo stato dell'oggetto.
     * </p>
     * @return La stringa descrittiva.
     */
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