package org.BABO.client.ui;

import org.BABO.client.service.AuthService;
import org.BABO.shared.model.User;
import org.BABO.shared.dto.AuthResponse;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
// Runnable è in java.lang - non serve import!

/**
 * Gestisce l'autenticazione dell'utente con integrazione sidebar
 */
public class AuthenticationManager {

    private boolean isAuthenticated = false;
    private User currentUser = null;
    private AuthPanel authPanel;
    private AuthService authService;
    private Runnable onAuthStateChanged; // Callback per notificare cambiamenti di stato

    public AuthenticationManager() {
        this.authService = new AuthService();
    }

    /**
     * Imposta callback per notificare cambiamenti di stato auth
     */
    public void setOnAuthStateChanged(Runnable callback) {
        this.onAuthStateChanged = callback;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public String getCurrentUserDisplayName() {
        return currentUser != null ? currentUser.getDisplayName() : "Utente";
    }

    public void handleAuthButtonClick() {
        if (isAuthenticated) {
            showUserProfileOptions();
        } else {
            // Il pannello verrà mostrato dal MainWindow
        }
    }

    public void showAuthPanel(StackPane mainRoot) {
        authPanel = new AuthPanel();

        // Configura i callback
        authPanel.setOnSuccessfulAuth(this::handleSuccessfulAuthentication);
        authPanel.setOnClosePanel(() -> closeAuthPanel(mainRoot));

        // Crea un overlay semi-trasparente
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // Centra il pannello di autenticazione
        overlay.getChildren().add(authPanel);
        StackPane.setAlignment(authPanel, Pos.CENTER);

        // Chiudi il pannello cliccando sullo sfondo
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                closeAuthPanel(mainRoot);
            }
        });

        // Previeni la chiusura cliccando sul pannello stesso
        authPanel.setOnMouseClicked(e -> e.consume());

        // Aggiungi l'overlay al root principale
        mainRoot.getChildren().add(overlay);

        System.out.println("🔑 Pannello autenticazione aperto");
    }

    private void closeAuthPanel(StackPane mainRoot) {
        if (mainRoot.getChildren().size() > 1) {
            mainRoot.getChildren().remove(mainRoot.getChildren().size() - 1);
        }
        System.out.println("🚪 Pannello autenticazione chiuso");
    }

    /**
     * Gestisce l'autenticazione riuscita
     */
    private void handleSuccessfulAuthentication(User user) {
        setAuthenticationState(true, user);

        // Mostra messaggio di benvenuto
        Platform.runLater(() -> {
            showWelcomeMessage(user);
        });
    }

    /**
     * Mostra messaggio di benvenuto personalizzato
     */
    private void showWelcomeMessage(User user) {
        Alert welcomeAlert = new Alert(Alert.AlertType.INFORMATION);
        welcomeAlert.setTitle("👋 Benvenuto!");
        welcomeAlert.setHeaderText("Accesso effettuato con successo");

        String welcomeMessage = String.format(
                "Ciao %s!\n\n" +
                        "✅ Sei ora connesso al tuo account\n" +
                        "📚 Puoi accedere a tutti i tuoi libri\n" +
                        "🔖 Le tue preferenze sono sincronizzate\n" +
                        "⭐ Scopri le nuove funzionalità disponibili!",
                user.getDisplayName()
        );

        welcomeAlert.setContentText(welcomeMessage);

        // Styling dell'alert
        DialogPane dialogPane = welcomeAlert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1e1e1e;" +
                        "-fx-text-fill: white;"
        );

        welcomeAlert.showAndWait();
    }

    public void showUserProfileOptions() {
        if (!isAuthenticated || currentUser == null) {
            showAlert("⚠️ Errore", "Nessun utente autenticato");
            return;
        }

        // Questo metodo non è più usato direttamente, viene sostituito dal popup della sidebar
        System.out.println("👤 Reindirizzamento a popup profilo dalla sidebar");
    }

    /**
     * Esegue il logout
     */
    public void logout() {
        performLogout();
    }

    /**
     * Esegue il logout con chiamata al server
     */
    private void performLogout() {
        String userDisplayName = getCurrentUserDisplayName();
        System.out.println("🚪 Esecuzione logout per: " + userDisplayName);

        // Esegui logout sul server (opzionale)
        authService.logoutAsync()
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            System.out.println("✅ Logout completato sul server");
                        } else {
                            System.out.println("⚠️ Logout locale (server non risponde)");
                        }

                        // Esegui logout locale
                        setAuthenticationState(false, null);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("⚠️ Logout locale (errore server): " + throwable.getMessage());
                        setAuthenticationState(false, null);
                    });
                    return null;
                });
    }

    /**
     * Aggiorna lo stato di autenticazione e notifica i listener
     */
    public void setAuthenticationState(boolean authenticated, User user) {
        boolean wasAuthenticated = this.isAuthenticated;

        this.isAuthenticated = authenticated;
        this.currentUser = user;

        if (authenticated && user != null) {
            System.out.println("✅ Utente autenticato: " + user.getDisplayName());
        } else {
            System.out.println("🚪 Utente disconnesso");
        }

        // Notifica il cambiamento di stato se è cambiato qualcosa
        if (wasAuthenticated != authenticated) {
            notifyAuthStateChanged();
        }
    }

    /**
     * Notifica i listener del cambiamento di stato auth
     */
    private void notifyAuthStateChanged() {
        if (onAuthStateChanged != null) {
            Platform.runLater(() -> {
                try {
                    onAuthStateChanged.run();
                } catch (Exception e) {
                    System.err.println("❌ Errore nel callback auth state changed: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Verifica se l'utente ha i permessi per una determinata azione
     */
    public boolean hasPermission(String action) {
        if (!isAuthenticated || currentUser == null) {
            return false;
        }

        // Qui potresti implementare una logica di permessi più complessa
        // basata sui ruoli dell'utente (se aggiunti al modello User)
        return true;
    }

    /**
     * Controlla se il servizio di autenticazione è disponibile
     */
    public void checkAuthServiceHealth() {
        authService.healthCheckAsync()
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            System.out.println("✅ Servizio autenticazione: " + response.getMessage());
                        } else {
                            System.out.println("❌ Servizio autenticazione non disponibile: " + response.getMessage());
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ Errore connessione servizio auth: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Recupera e aggiorna il profilo utente dal server
     */
    public void refreshUserProfile() {
        if (!isAuthenticated || currentUser == null) {
            return;
        }

        authService.getUserProfileAsync(currentUser.getId())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            currentUser = response.getUser();
                            System.out.println("✅ Profilo utente aggiornato: " + currentUser.getDisplayName());
                            notifyAuthStateChanged(); // Aggiorna la UI
                        } else {
                            System.out.println("⚠️ Errore aggiornamento profilo: " + response.getMessage());
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ Errore refresh profilo: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Verifica se l'utente può accedere ai contenuti premium
     */
    public boolean canAccessPremiumContent() {
        if (!isAuthenticated) {
            return false;
        }

        // Qui si potrebbe implementare una logica per verificare
        // l'abbonamento o i privilegi dell'utente
        return true;
    }

    /**
     * Ottiene le statistiche dell'utente (placeholder)
     */
    public String getUserStats() {
        if (!isAuthenticated || currentUser == null) {
            return "Nessun dato disponibile";
        }

        // Placeholder per future implementazioni
        return String.format(
                "📚 Libri letti: 0\n" +
                        "⏱️ Tempo di lettura: 0h\n" +
                        "📖 Libri nella libreria: 0\n" +
                        "⭐ Recensioni scritte: 0"
        );
    }

    /**
     * Inizializzazione del manager di autenticazione
     */
    public void initialize() {
        System.out.println("🔧 Inizializzazione AuthenticationManager...");

        // Controlla lo stato del servizio di autenticazione
        checkAuthServiceHealth();

        // Qui si potrebbe implementare il recupero dello stato di login
        // da un file di configurazione locale o da preferenze salvate

        System.out.println("✅ AuthenticationManager inizializzato");
    }

    /**
     * Cleanup quando l'applicazione viene chiusa
     */
    public void shutdown() {
        System.out.println("🔄 Shutdown AuthenticationManager...");

        if (isAuthenticated) {
            // Esegui logout silenzioso
            authService.logoutAsync()
                    .thenAccept(response -> {
                        System.out.println("✅ Logout silenzioso completato");
                    })
                    .exceptionally(throwable -> {
                        System.out.println("⚠️ Logout silenzioso fallito: " + throwable.getMessage());
                        return null;
                    });
        }

        System.out.println("✅ AuthenticationManager chiuso");
    }

    /**
     * Ottieni l'istanza del servizio di autenticazione
     */
    public AuthService getAuthService() {
        return authService;
    }

    /**
     * Mostra un alert informativo
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styling dell'alert per mantenere coerenza con il tema
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1e1e1e;" +
                        "-fx-text-fill: white;"
        );

        alert.showAndWait();
    }
}