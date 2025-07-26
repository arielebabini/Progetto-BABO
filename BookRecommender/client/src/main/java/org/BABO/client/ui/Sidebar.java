package org.BABO.client.ui;

import org.BABO.shared.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Gestisce la sidebar dell'applicazione con gestione utente loggato
 * AGGIORNATO: Con sezione "Esplora" al posto di "Book Store"
 */
public class Sidebar {

    private final boolean serverAvailable;
    private final AuthenticationManager authManager;
    private final MainWindow mainWindow;
    private VBox menuItemsBox;
    private VBox authSection; // Container per sezione autenticazione
    private int activeMenuIndex = 0;

    public Sidebar(boolean serverAvailable, AuthenticationManager authManager, MainWindow mainWindow) {
        this.serverAvailable = serverAvailable;
        this.authManager = authManager;
        this.mainWindow = mainWindow;
    }

    public VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(200);
        sidebar.setPrefHeight(700);
        sidebar.setStyle("-fx-background-color: #2c2c2e;");

        // Header
        Label sidebarHeader = new Label("📚 Libreria");
        sidebarHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        sidebarHeader.setTextFill(Color.WHITE);
        sidebarHeader.setPadding(new Insets(20, 0, 5, 20));

        // Menu items
        menuItemsBox = createMenuItems();

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Status server
        Label serverStatus = new Label(serverAvailable ? "🟢 Server Online" : "🔴 Modalità Offline");
        serverStatus.setTextFill(serverAvailable ? Color.LIGHTGREEN : Color.ORANGE);
        serverStatus.setFont(Font.font("System", 12));
        serverStatus.setPadding(new Insets(10, 20, 10, 20));

        // Auth section (dinamica)
        authSection = new VBox(10);
        updateAuthSection();

        sidebar.getChildren().addAll(sidebarHeader, menuItemsBox, spacer, serverStatus, authSection);
        return sidebar;
    }

    private VBox createMenuItems() {
        VBox menuBox = new VBox(5);
        menuBox.setPadding(new Insets(10, 0, 0, 15));

        // ✅ AGGIORNATO: "Book Store" sostituito con "Esplora"
        String[] menuItems = {"🏠 Home", "📚 Le Mie Librerie", "🔍 Esplora", "🎧 Audiobook Store",
                "📖 Tutto", "✅ Letti", "📄 PDF"};

        for (int i = 0; i < menuItems.length; i++) {
            HBox itemBox = createMenuItem(menuItems[i], i == 0);
            final int index = i;

            // ✅ AGGIORNATO: Aggiungi click handler per i menu items
            if (menuItems[i].contains("Home")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null) {
                        System.out.println("🏠 Click Home dalla sidebar");
                        // Chiama il metodo specifico per la home nel MainWindow
                        mainWindow.showHomePage();
                        // E anche nel ContentArea per assicurarsi che torni alla vista normale
                        if (mainWindow.getContentArea() != null) {
                            mainWindow.getContentArea().forceHomeView();
                        }
                    }
                });
            } else if (menuItems[i].contains("Le Mie Librerie")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null) {
                        mainWindow.showLibraryPanel();
                    }
                });
            } else if (menuItems[i].contains("Esplora")) {
                // ✅ NUOVO: Handler per sezione Esplora
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null) {
                        System.out.println("🔍 Sidebar: Click su Esplora");
                        mainWindow.showExploreSection();
                    }
                });
            } else if (menuItems[i].contains("Audiobook Store")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null && mainWindow.getContentArea() != null) {
                        mainWindow.getContentArea().handleMenuClick(index);
                    }
                });
            } else if (menuItems[i].contains("Tutto")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null && mainWindow.getContentArea() != null) {
                        mainWindow.getContentArea().handleMenuClick(index);
                    }
                });
            } else if (menuItems[i].contains("Letti")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null && mainWindow.getContentArea() != null) {
                        mainWindow.getContentArea().handleMenuClick(index);
                    }
                });
            } else if (menuItems[i].contains("PDF")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null && mainWindow.getContentArea() != null) {
                        mainWindow.getContentArea().handleMenuClick(index);
                    }
                });
            }

            menuBox.getChildren().add(itemBox);
        }

        return menuBox;
    }

    private HBox createMenuItem(String text, boolean isActive) {
        HBox item = new HBox();
        item.setPrefHeight(35);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(5, 10, 5, 10));

        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.NORMAL, 14));

        if (isActive) {
            item.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 5; -fx-cursor: hand;");
            label.setTextFill(Color.WHITE);
        } else {
            item.setStyle("-fx-cursor: hand;");
            label.setTextFill(Color.LIGHTGRAY);
        }

        // Effetti hover
        item.setOnMouseEntered(e -> {
            if (!isActive) {
                item.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 5; -fx-cursor: hand;");
                label.setTextFill(Color.WHITE);
            }
        });

        item.setOnMouseExited(e -> {
            if (!isActive) {
                item.setStyle("-fx-cursor: hand;");
                label.setTextFill(Color.LIGHTGRAY);
            }
        });

        item.getChildren().add(label);
        return item;
    }

    private void updateAuthSection() {
        authSection.getChildren().clear();

        if (authManager.isAuthenticated()) {
            // Utente autenticato - usa widget utente con avatar
            VBox userWidget = createUserWidget();
            authSection.getChildren().add(userWidget);

            // Pulsante logout
            Button logoutButton = createAuthButton("🚪 Logout", () -> {
                System.out.println("🔓 Logout richiesto");
                authManager.logout();
            });

            authSection.getChildren().add(logoutButton);

        } else {
            // Utente non autenticato
            Button loginButton = createAuthButton("🔑 Accedi", () -> {
                System.out.println("🔑 Login richiesto da sidebar");
                if (mainWindow != null) {
                    mainWindow.showAuthPanel();
                }
            });

            authSection.getChildren().add(loginButton);
        }
    }

    /**
     * Crea il widget per l'utente loggato
     */
    private VBox createUserWidget() {
        VBox userWidget = new VBox(8);
        userWidget.setStyle(
                "-fx-background-color: rgba(74, 134, 232, 0.15);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-padding: 12px;" +
                        "-fx-cursor: hand;"
        );
        userWidget.setMaxWidth(Double.MAX_VALUE);
        userWidget.setAlignment(Pos.CENTER);

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            // Fallback se l'utente è null
            Label errorLabel = new Label("⚠️ Errore utente");
            errorLabel.setTextFill(Color.ORANGE);
            userWidget.getChildren().add(errorLabel);
            return userWidget;
        }

        // Avatar circolare con iniziali
        StackPane avatar = createUserAvatar(currentUser);

        // Nome utente
        Label userName = new Label(currentUser.getDisplayName());
        userName.setTextFill(Color.WHITE);
        userName.setFont(Font.font("System", FontWeight.BOLD, 13));
        userName.setWrapText(true);
        userName.setMaxWidth(160);

        // Email utente
        Label userEmail = new Label(currentUser.getEmail());
        userEmail.setTextFill(Color.LIGHTGRAY);
        userEmail.setFont(Font.font("System", 10));
        userEmail.setWrapText(true);
        userEmail.setMaxWidth(160);

        // Separatore
        Separator separator = new Separator();
        separator.setMaxWidth(140);

        // Pulsante gestione account
        Button manageButton = new Button("👤 Gestisci Account");
        manageButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4a86e8;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 11px;"
        );
        manageButton.setOnAction(e -> {
            // Apri popup gestione utente
            UserProfilePopup profilePopup = new UserProfilePopup(authManager, () -> {
                System.out.println("👤 Callback logout da profile popup");
                authManager.logout();
            });
            profilePopup.show(mainWindow.getMainRoot());
        });

        userWidget.getChildren().addAll(avatar, userName, userEmail, separator, manageButton);
        VBox.setMargin(userWidget, new Insets(0, 15, 15, 15));

        return userWidget;
    }

    /**
     * Crea avatar circolare con iniziali dell'utente
     */
    private StackPane createUserAvatar(User user) {
        StackPane avatar = new StackPane();
        avatar.setMaxSize(40, 40);
        avatar.setMinSize(40, 40);

        // Cerchio di sfondo
        Circle circle = new Circle(20);
        circle.setFill(Color.web("#4a86e8"));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2);

        // Iniziali
        String initials = getInitials(user);
        Label initialsLabel = new Label(initials);
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        avatar.getChildren().addAll(circle, initialsLabel);
        return avatar;
    }

    /**
     * Estrae le iniziali dal nome dell'utente
     */
    private String getInitials(User user) {
        String name = user.getName();
        String surname = user.getSurname();

        StringBuilder initials = new StringBuilder();

        if (name != null && !name.trim().isEmpty()) {
            initials.append(name.trim().charAt(0));
        }

        if (surname != null && !surname.trim().isEmpty()) {
            initials.append(surname.trim().charAt(0));
        }

        if (initials.length() == 0) {
            // Fallback con prima lettera dell'email o username
            String fallback = user.getEmail() != null ? user.getEmail() : user.getUsername();
            if (fallback != null && !fallback.trim().isEmpty()) {
                initials.append(fallback.trim().charAt(0));
            } else {
                initials.append("?");
            }
        }

        return initials.toString().toUpperCase();
    }

    private Button createAuthButton(String text, Runnable action) {
        Button authButton = new Button(text);
        authButton.setFont(Font.font("System", FontWeight.NORMAL, 12));
        authButton.setPrefWidth(170);

        String baseStyle =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4a86e8;" +
                        "-fx-border-color: #4a86e8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-size: 12px;";

        authButton.setStyle(baseStyle);
        authButton.setOnAction(e -> action.run());

        // Hover effects
        setupButtonHoverEffects(authButton);

        VBox.setMargin(authButton, new Insets(0, 15, 15, 15));
        return authButton;
    }

    private void setupButtonHoverEffects(Button button) {
        String baseStyle =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4a86e8;" +
                        "-fx-border-color: #4a86e8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-size: 12px;";

        String hoverStyle = baseStyle.replace("transparent", "rgba(74, 134, 232, 0.1)");
        String pressStyle = baseStyle.replace("#4a86e8", "derive(#4a86e8, -20%)");

        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnMousePressed(e -> button.setStyle(pressStyle));
        button.setOnMouseReleased(e -> button.setStyle(baseStyle));
    }

    private void updateActiveMenuItem(int newActiveIndex) {
        if (menuItemsBox == null || menuItemsBox.getChildren().isEmpty()) {
            return;
        }

        // Rimuovi lo stato attivo da tutti gli elementi
        for (int i = 0; i < menuItemsBox.getChildren().size(); i++) {
            HBox item = (HBox) menuItemsBox.getChildren().get(i);
            Label label = (Label) item.getChildren().get(0);

            if (i == newActiveIndex) {
                item.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 5; -fx-cursor: hand;");
                label.setTextFill(Color.WHITE);
            } else {
                item.setStyle("-fx-cursor: hand;");
                label.setTextFill(Color.LIGHTGRAY);
            }
        }

        activeMenuIndex = newActiveIndex;
        System.out.println("📋 Menu attivo aggiornato: indice " + newActiveIndex);
    }

    /**
     * Metodo pubblico per aggiornare la sezione auth quando cambia lo stato dell'utente
     * Questo metodo viene chiamato dall'AuthenticationManager tramite callback
     */
    public void refreshAuthSection() {
        System.out.println("🔄 Refreshing sidebar auth section...");

        // Verifica che la sezione auth esista
        if (authSection != null) {
            updateAuthSection();
            System.out.println("✅ Sidebar auth section refreshed");
        } else {
            System.err.println("❌ Auth section is null, cannot refresh");
        }
    }

    /**
     * Metodo pubblico per aggiornare lo stato attivo dal MainWindow
     */
    public void setActiveMenuItem(int index) {
        updateActiveMenuItem(index);
    }

    /**
     * Ottieni l'indice del menu attivo
     */
    public int getActiveMenuIndex() {
        return activeMenuIndex;
    }

    /**
     * ✅ NUOVO: Forza l'attivazione del menu Home
     */
    public void setHomeActive() {
        updateActiveMenuItem(0);
    }

    /**
     * ✅ NUOVO: Forza l'attivazione del menu Esplora
     */
    public void setExploreActive() {
        updateActiveMenuItem(2);
    }

    /**
     * ✅ NUOVO: Verifica se il menu Esplora è attivo
     */
    public boolean isExploreActive() {
        return activeMenuIndex == 2;
    }

    // =====================================================
    // GETTER E METODI DI UTILITÀ
    // =====================================================

    public AuthenticationManager getAuthManager() {
        return authManager;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public boolean isServerAvailable() {
        return serverAvailable;
    }
}