package org.BABO.client;

import javafx.application.Application;
import org.BABO.client.ui.AppleBooksClient;

/**
 * Punto di ingresso principale per il client JavaFX
 * Avvia l'applicazione Apple Books client con PopupManager integrato
 */

public class ClientApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Avviando Apple Books  Client con PopupManager...");
        System.out.println("📡 Verifica connessione server su http://localhost:8080");

        try {
            // Avvia l'applicazione JavaFX - METODO CORRETTO
            Application.launch(AppleBooksClient.class, args);
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'avvio del client: " + e.getMessage());
            e.printStackTrace();

            // Debug di emergenza (solo se l'app è stata avviata)
            try {
                System.out.println("🔍 Tentativo debug di emergenza...");
                // Rimuovi questa chiamata perché l'app potrebbe non essere stata avviata
                // AppleBooksClient.debugPopupState();
            } catch (Exception debugError) {
                System.err.println("❌ Anche il debug è fallito: " + debugError.getMessage());
            }
        }
    }
}