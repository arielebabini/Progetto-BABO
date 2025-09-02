package org.BABO.shared.dto;

import org.BABO.shared.model.User;

import java.util.List;

/**
 * Data Transfer Object (DTO) che incapsula la risposta di un'operazione amministrativa.
 * <p>
 * Questa classe è utilizzata per standardizzare le risposte dal server al client
 * per le operazioni eseguite dagli amministratori. Fornisce informazioni sullo stato
 * dell'operazione, un messaggio di feedback e, opzionalmente, una lista di utenti
 * correlata all'operazione.
 * </p>
 */
public class  AdminResponse {
    /**
     * Indica se l'operazione amministrativa ha avuto successo.
     */
    private final boolean success;

    /**
     * Un messaggio di testo che fornisce dettagli sul risultato dell'operazione.
     */
    private final String message;

    /**
     * Una lista di oggetti {@link User} che possono essere restituiti dall'operazione,
     * ad esempio, in una richiesta di visualizzazione di tutti gli utenti. Può essere {@code null}.
     */
    private final List<User> users;

    /**
     * Costruttore completo per una risposta che include un messaggio e una lista di utenti.
     * <p>
     * Questo costruttore è tipicamente utilizzato per operazioni di successo
     * che restituiscono dati.
     * </p>
     * @param success Lo stato di successo dell'operazione.
     * @param message Il messaggio di feedback.
     * @param users La lista di utenti.
     */
    public AdminResponse(boolean success, String message, List<User> users) {
        this.success = success;
        this.message = message;
        this.users = users;
    }

    /**
     * Costruttore semplificato per una risposta che non include una lista di utenti.
     * <p>
     * È utile per operazioni come l'eliminazione o l'aggiornamento, dove
     * non è necessario restituire dati.
     * </p>
     * @param success Lo stato di successo dell'operazione.
     * @param message Il messaggio di feedback.
     */
    public AdminResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.users = null;
    }

    // Getters

    /**
     * Restituisce lo stato di successo dell'operazione.
     * @return {@code true} se l'operazione è stata completata con successo, {@code false} altrimenti.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Restituisce il messaggio di feedback dell'operazione.
     * @return Il messaggio.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Restituisce la lista di utenti associata alla risposta.
     * @return La lista di utenti, o {@code null} se non presente.
     */
    public List<User> getUsers() {
        return users;
    }
}