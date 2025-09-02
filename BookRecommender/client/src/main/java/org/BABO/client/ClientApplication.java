package org.BABO.client;

import javafx.application.Application;
import org.BABO.client.ui.BooksClient;

/**
 * Punto di ingresso principale per l'applicazione client JavaFX.
 * <p>
 * Questa classe funge da "launcher" per l'applicazione desktop,
 * avviando il contesto JavaFX necessario per l'interfaccia utente.
 * L'applicazione principale, {@link BooksClient}, viene eseguita
 * all'interno del metodo {@code main}, garantendo che il lifecycle
 * dell'applicazione JavaFX sia gestito correttamente.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 * <li><strong>Avvio Applicazione:</strong> Inizializza e avvia il framework
 * JavaFX, delegando il controllo alla classe {@link BooksClient}.</li>
 * <li><strong>Gestione Eccezioni:</strong> Implementa un blocco {@code try-catch}
 * per catturare eventuali errori critici durante l'avvio, fornendo
 * un feedback immediato e un meccanismo di debug di emergenza.</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see BooksClient
 * @see Application#launch(Class, String...)
 */
public class ClientApplication {

    /**
     * Il metodo principale che avvia l'applicazione client.
     * <p>
     * Questo è il punto di partenza dell'intera applicazione. Invoca
     * {@link Application#launch(Class, String[])} per avviare il
     * toolkit JavaFX e la classe {@link BooksClient}.
     * Vengono forniti messaggi di stato per la console per informare
     * l'utente sull'andamento dell'avvio e sulla connessione al server.
     * </p>
     *
     * @param args gli argomenti della riga di comando.
     */
    public static void main(String[] args) {
        System.out.println("🚀 Avviando Books  Client con PopupManager...");
        System.out.println("📡 Verifica connessione server su http://localhost:8080");

        try {
            // Avvia l'applicazione JavaFX
            Application.launch(BooksClient.class, args);
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'avvio del client: " + e.getMessage());
            e.printStackTrace();

            // Debug di emergenza
            try {
                System.out.println("🔍 Tentativo debug di emergenza...");
            } catch (Exception debugError) {
                System.err.println("❌ Anche il debug è fallito: " + debugError.getMessage());
            }
        }
    }
}