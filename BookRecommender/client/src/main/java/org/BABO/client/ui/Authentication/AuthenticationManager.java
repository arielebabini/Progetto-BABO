package org.BABO.client.ui.Authentication;

import org.BABO.client.service.AuthService;
import org.BABO.shared.model.User;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;

/**
 * Gestisce l'autenticazione dell'utente con integrazione sidebar
 */
public class AuthenticationManager {

    private boolean isAuthenticated = false;
    private User currentUser = null;
    private AuthPanel authPanel;
    private AuthService authService;
    private Runnable onAuthStateChanged;

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


    public void showAuthPanel(StackPane mainRoot) {
        authPanel = new AuthPanel();

        authPanel.setOnSuccessfulAuth(this::handleSuccessfulAuthentication);
        authPanel.setOnClosePanel(() -> closeAuthPanel(mainRoot));

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        overlay.getChildren().add(authPanel);
        StackPane.setAlignment(authPanel, Pos.CENTER);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                closeAuthPanel(mainRoot);
            }
        });

        authPanel.setOnMouseClicked(e -> e.consume());

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
                        "📚 Puoi accedere alle tue librerie\n" +
                        "⭐ Scopri le nuove funzionalità disponibili!",
                user.getDisplayName()
        );

        welcomeAlert.setContentText(welcomeMessage);

        DialogPane dialogPane = welcomeAlert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-text-fill: gray;"
        );

        welcomeAlert.showAndWait();
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
     * Aggiorna l'utente corrente
     */
    public void updateCurrentUser(User updatedUser) {
        if (this.isAuthenticated && updatedUser != null) {
            this.currentUser = updatedUser;
            System.out.println("✅ Profilo utente aggiornato: " + updatedUser.getDisplayName());

            notifyAuthStateChanged();
        }
    }

    /**
     * Inizializzazione del manager di autenticazione
     */
    public void initialize() {
        System.out.println("🔧 Inizializzazione AuthenticationManager...");

        checkAuthServiceHealth();

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

        alert.showAndWait();
    }
}