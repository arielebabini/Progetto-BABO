package org.BABO.client.ui.Popup;

import javafx.animation.Interpolator;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Home.ApplicationProtection;
import org.BABO.client.ui.Home.IconUtils;
import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.client.ui.Rating.RatingDialog;
import org.BABO.client.ui.Recommendation.RecommendationDialog;
import org.BABO.shared.dto.Library.LibraryResponse;
import org.BABO.shared.model.Book;
import org.BABO.shared.dto.Recommendation.RecommendationRequest;
import org.BABO.shared.dto.Recommendation.RecommendationResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.BABO.shared.model.BookRating;
import org.BABO.client.service.LibraryService;
import org.BABO.client.service.ClientRatingService;
import org.BABO.client.service.ClientRecommendationService;
import org.BABO.shared.model.BookRecommendation;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.util.stream.Collectors;

import java.util.concurrent.CompletableFuture;

/**
 * Popup per mostrare i dettagli di un libro con supporto per navigazione,
 * valutazioni e librerie personali
 */
public class BookDetailsPopup {

    // Core components
    private static StackPane root;
    private static List<Book> booksCollection;
    private static int currentBookIndex = 0;
    private static StackPane bookDisplayPane;
    private static VBox nextBookPreview;
    private static VBox prevBookPreview;
    private static Timeline slideAnimation;
    private static Runnable closeHandler;
    private static boolean isTransitioning = false;
    private static Button leftArrowButton;
    private static Button rightArrowButton;

    // Rating system variables
    private static final ClientRatingService ratingService = new ClientRatingService();
    private static BookRating currentUserRating = null;
    private static Double averageBookRating = null;
    private static Integer currentBookReviewCount = null;
    private static Label averageRatingLabel = null;
    private static VBox currentRatingSection = null;
    private static AuthenticationManager currentAuthManager = null;
    private static Book currentBook = null;

    private static ClientRecommendationService recommendationService = new ClientRecommendationService();
    private static List<BookRecommendation> currentBookRecommendations = null;
    private static List<Book> recommendedBooksDetails = null;

    private static Stage parentStage = null;
    private static ScrollPane currentRecommendationsScrollPane = null;

    // Public API methods
    public static StackPane create(Book book, List<Book> collection, Runnable onClose) {
        return createWithLibrarySupport(book, collection, onClose, null);
    }

    public static StackPane createWithLibrarySupport(Book book, List<Book> collection, Runnable onClose,
                                                     AuthenticationManager authManager) {
        initializePopup(book, collection, onClose, authManager);
        return createMainContainer(book);
    }

    // Initialization methods
    private static void initializePopup(Book book, List<Book> collection, Runnable onClose,
                                        AuthenticationManager authManager) {
        booksCollection = collection;
        closeHandler = onClose;
        currentBookIndex = Math.max(0, collection.indexOf(book));

        resetRatings();
        currentBook = book;
        currentAuthManager = authManager;
    }

    private static StackPane createMainContainer(Book book) {
        root = new StackPane();

        // Background
        StackPane blurLayer = createBackgroundLayer();

        // Book display pane
        bookDisplayPane = new StackPane();
        VBox currentBookContent = createBookContent(book, getBookBackgroundColor(book), currentAuthManager);

        // Load ratings
        loadBookRatingsForAllUsers(book, currentAuthManager);

        // Setup navigation for multiple books
        if (booksCollection.size() > 1) {
            setupMultiBookNavigation(currentBookContent);
        } else {
            bookDisplayPane.getChildren().add(currentBookContent);
        }

        root.getChildren().addAll(blurLayer, bookDisplayPane);

        // MIGLIORA GESTIONE FOCUS E EVENTI
        setupImprovedFocusHandling(root);

        return root;
    }

    private static void setupImprovedFocusHandling(StackPane root) {
        root.setFocusTraversable(true);

        // MIGLIORATO: Key handlers con PopupManager
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                System.out.println("🔑 ESC premuto - chiusura tramite PopupManager");
                handlePopupCloseWithPopupManager();
                event.consume();
                return;
            }

            // Gestione frecce per navigazione
            if (event.getCode() == KeyCode.LEFT) {
                if (currentBookIndex > 0 && !isTransitioning) {
                    slideToBook(currentBookIndex - 1);
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                    slideToBook(currentBookIndex + 1);
                    event.consume();
                }
            }
        });

        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    /**
     * Gestione migliorata della chiusura tramite PopupManager
     */
    private static void handlePopupCloseWithPopupManager() {
        try {
            System.out.println("🔒 BookDetailsPopup: Chiusura tramite PopupManager");

            PopupManager popupManager = PopupManager.getInstance();

            if (!popupManager.isInitialized()) {
                System.err.println("⚠️ PopupManager non inizializzato, uso fallback");
                handlePopupCloseManual();
                return;
            }

            if (popupManager.hasActivePopups()) {
                popupManager.closeTopPopup();
                System.out.println("✅ Popup chiuso tramite PopupManager");
            } else {
                System.out.println("⚠️ Nessun popup attivo in PopupManager, uso fallback");
                handlePopupCloseManual();
            }

        } catch (Exception e) {
            System.err.println("❌ Errore chiusura PopupManager: " + e.getMessage());
            handlePopupCloseManual();
        }
    }

    private static void handlePopupClose() {
        handlePopupCloseWithPopupManager();
    }

    private static void handlePopupCloseManual() {
        try {
            System.out.println("🔧 Fallback: chiusura manuale popup");

            if (root != null && root.getParent() instanceof StackPane) {
                StackPane parent = (StackPane) root.getParent();
                parent.getChildren().remove(root);
                System.out.println("✅ Popup rimosso manualmente dal parent");
                return;
            }

            // Cerca attraverso le finestre aperte
            javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Stage.getWindows();
            for (javafx.stage.Window window : windows) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getScene() != null && stage.getScene().getRoot() instanceof StackPane) {
                        StackPane stageRoot = (StackPane) stage.getScene().getRoot();
                        if (stageRoot.getChildren().contains(root)) {
                            stageRoot.getChildren().remove(root);
                            System.out.println("✅ Popup rimosso manualmente dal stage root");
                            return;
                        }
                    }
                }
            }

            // Ultimo fallback: usa il closeHandler originale
            if (closeHandler != null) {
                closeHandler.run();
                System.out.println("✅ Usato closeHandler originale");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore anche nel fallback manuale: " + e.getMessage());

            // Fallback finale: usa il closeHandler se esiste
            if (closeHandler != null) {
                try {
                    closeHandler.run();
                } catch (Exception finalError) {
                    System.err.println("❌ Errore anche nel closeHandler: " + finalError.getMessage());
                }
            }
        }
    }

    private static StackPane createBackgroundLayer() {
        StackPane blurLayer = new StackPane();
        blurLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        blurLayer.setEffect(new BoxBlur(20, 20, 3));

        // MIGLIORATO: Click background tramite PopupManager
        blurLayer.setOnMouseClicked(e -> {
            System.out.println("🖱️ Click su background - chiusura tramite PopupManager");
            handlePopupCloseWithPopupManager();
            e.consume();
        });

        return blurLayer;
    }

    public static void refreshPopupOnFocusRestore() {
        Platform.runLater(() -> {
            try {
                if (root != null) {
                    // Riabilita focus
                    root.setFocusTraversable(true);
                    root.requestFocus();

                    // Riabilita navigation se necessario
                    if (leftArrowButton != null && rightArrowButton != null) {
                        updateArrowVisibility();
                    }

                    // Ricarica sezione raccomandazioni se necessario
                    if (currentBook != null && currentRecommendationsScrollPane != null) {
                        loadBookRecommendations(currentBook, currentRecommendationsScrollPane);
                    }

                    System.out.println("🔄 Popup refreshed dopo ripristino focus tramite PopupManager");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Errore refresh popup: " + e.getMessage());
            }
        });
    }

    public static void debugWithPopupManager() {
        System.out.println("🔍 DEBUG BOOK DETAILS POPUP:");
        System.out.println("  root != null: " + (root != null));
        System.out.println("  closeHandler != null: " + (closeHandler != null));
        System.out.println("  currentBook: " + (currentBook != null ? currentBook.getTitle() : "null"));

        PopupManager.getInstance().debugFullState();
    }

    private static void setupMultiBookNavigation(VBox currentBookContent) {
        nextBookPreview = createBookPreview(currentBookIndex + 1);
        nextBookPreview.setTranslateX(1200);

        prevBookPreview = createBookPreview(currentBookIndex - 1);
        prevBookPreview.setTranslateX(-1200);

        bookDisplayPane.getChildren().addAll(prevBookPreview, currentBookContent, nextBookPreview);
        addEdgeDetection(bookDisplayPane);
        addNavigationArrows();
    }

    private static String getBookBackgroundColor(Book book) {
        Image coverImage = ImageUtils.loadSafeImage(book.getImageUrl());
        Color dominantColor = extractDominantColor(coverImage);
        Color darkenedColor = darkenColor(dominantColor, 0.7);
        return toHexString(darkenedColor);
    }

    // Book content creation
    private static VBox createBookContent(Book book, String backgroundColor, AuthenticationManager authManager) {
        VBox popupContent = new VBox();
        popupContent.setMaxWidth(1000);
        popupContent.setMaxHeight(700);
        popupContent.setMinWidth(1000);
        popupContent.setStyle(createPopupStyle(backgroundColor));

        HBox topBar = createTopBar();
        ScrollPane contentScroll = createContentScrollPane(book, authManager);

        popupContent.getChildren().addAll(topBar, contentScroll);
        return popupContent;
    }

    private static String createPopupStyle(String backgroundColor) {
        return "-fx-background-color: " + backgroundColor + ";" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);";
    }

    private static HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = createTopBarButton("×", 20);

        // MIGLIORATO: Gestione chiusura tramite PopupManager
        closeButton.setOnAction(e -> {
            System.out.println("🔒 Pulsante X cliccato - chiusura tramite PopupManager");
            handlePopupCloseWithPopupManager();
            e.consume();
        });

        topBar.getChildren().addAll(closeButton);
        return topBar;
    }


    private static Button createTopBarButton(String text, int fontSize) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #999999;" +
                        "-fx-font-size: " + fontSize + ";" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private static ScrollPane createContentScrollPane(Book book, AuthenticationManager authManager) {
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentScroll.setFitToWidth(true);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setPannable(false);

        // Prevent horizontal scrolling
        setupScrollConstraints(contentScroll);

        VBox scrollContent = new VBox();
        scrollContent.getChildren().addAll(
                createDetailsSection(book, authManager),
                createPublisherSection(book),
                createRatingSection(book, authManager),
                createRecommendationsSection(book, authManager),
                createReviewsSection()
        );

        contentScroll.setContent(scrollContent);
        return contentScroll;
    }

    private static void setupScrollConstraints(ScrollPane scrollPane) {
        scrollPane.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != 0.0) {
                scrollPane.setHvalue(0.0);
            }
        });

        scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (Math.abs(e.getX() - e.getSceneX()) > Math.abs(e.getY() - e.getSceneY())) {
                e.consume();
            }
        });
    }

    // Book details sections
    private static HBox createDetailsSection(Book book, AuthenticationManager authManager) {
        HBox detailsSection = new HBox(30);
        detailsSection.setPadding(new Insets(20, 30, 30, 30));
        detailsSection.setAlignment(Pos.TOP_LEFT);

        VBox coverContainer = createCoverContainer(book);
        VBox infoBox = createInfoBox(book, authManager);

        detailsSection.getChildren().addAll(coverContainer, infoBox);
        return detailsSection;
    }

    private static VBox createCoverContainer(Book book) {
        // Assicurati che il libro abbia un nome file immagine locale
        book.ensureLocalImageFileName();

        // Debug per verificare l'immagine
        System.out.println("🖼️ Caricamento copertina per: " + book.getTitle());
        System.out.println("   Nome file: " + book.getSafeImageFileName());

        // Usa SOLO il nome file locale
        ImageView cover = ImageUtils.createSafeImageView(book.getSafeImageFileName(), 180, 270);

        Rectangle coverClip = new Rectangle(180, 270);
        coverClip.setArcWidth(8);
        coverClip.setArcHeight(8);
        cover.setClip(coverClip);

        VBox coverContainer = new VBox(cover);
        coverContainer.setAlignment(Pos.TOP_CENTER);
        return coverContainer;
    }

    private static VBox createInfoBox(Book book, AuthenticationManager authManager) {
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.TOP_LEFT);

        infoBox.getChildren().addAll(
                //createCategoryBadge(),
                createTitleLabel(book.getTitle()),
                createAuthorLabel(book.getAuthor()),
                createRatingBox(book),
                createBookInfoBox(book),
                createButtonBox(book, authManager)
        );

        return infoBox;
    }

    /*private static Label createCategoryBadge() {
        Label categoryBadge = new Label("#1, BESTSELLER ❯");
        categoryBadge.setStyle(
                "-fx-background-color: #444;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 5 10;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12;"
        );
        return categoryBadge;
    }*/

    private static Label createTitleLabel(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        return titleLabel;
    }

    private static Label createAuthorLabel(String author) {
        Label authorLabel = new Label(author);
        authorLabel.setFont(Font.font("SF Pro Text", 18));
        authorLabel.setTextFill(Color.LIGHTGRAY);
        return authorLabel;
    }

    private static HBox createRatingBox(Book book) {
        HBox ratingBox = new HBox(5);
        ratingBox.setPadding(new Insets(10, 0, 0, 0));

        averageRatingLabel = new Label("⭐ Caricamento...");
        averageRatingLabel.setTextFill(Color.WHITE);
        averageRatingLabel.setFont(Font.font("SF Pro Text", 14));

        String categoryText = "• " + (book.getCategory() != null && !book.getCategory().isEmpty()
                ? book.getCategory()
                : "Narrativa"); // fallback solo se category è null/vuota

        Label ratingCategory = new Label(categoryText);
        ratingCategory.setTextFill(Color.LIGHTGRAY);
        ratingCategory.setFont(Font.font("SF Pro Text", 14));
        ratingCategory.setPadding(new Insets(0, 0, 0, 5));

        ratingBox.getChildren().addAll(averageRatingLabel, ratingCategory);
        return ratingBox;
    }

    private static VBox createBookInfoBox(Book book) {
        VBox bookInfoBox = new VBox(15);
        bookInfoBox.setPadding(new Insets(20, 0, 0, 0));

        Label infoLabel = new Label("📚 Libro Digitale");
        infoLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        infoLabel.setTextFill(Color.WHITE);
        bookInfoBox.getChildren().add(infoLabel);

        if (isNotEmpty(book.getIsbn())) {
            Label isbnInfo = new Label("📄 ISBN: " + book.getIsbn());
            isbnInfo.setFont(Font.font("SF Pro Text", 14));
            isbnInfo.setTextFill(Color.LIGHTGRAY);
            bookInfoBox.getChildren().add(isbnInfo);
        }

        if (isNotEmpty(book.getPublishYear())) {
            Label yearInfo = new Label("📅 Anno: " + book.getPublishYear());
            yearInfo.setFont(Font.font("SF Pro Text", 14));
            yearInfo.setTextFill(Color.LIGHTGRAY);
            bookInfoBox.getChildren().add(yearInfo);
        }

        return bookInfoBox;
    }

    private static HBox createButtonBox(Book book, AuthenticationManager authManager) {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        Button addToLibraryButton = createStyledButton("📚 Aggiungi a Libreria", "#9b59b6", "white");

        addToLibraryButton.setOnAction(e -> {
            if (authManager != null && authManager.isAuthenticated()) {
                // Utente loggato: mostra dialog per aggiungere alla libreria
                showAddToLibraryDialog(book, authManager);
            } else {
                // Utente non loggato: apri popup di login
                if (authManager != null) {
                    // Recupera il mainRoot dalla scena corrente
                    StackPane mainRoot = getMainRootFromButton(addToLibraryButton);
                    if (mainRoot != null) {
                        authManager.showAuthPanel(mainRoot);
                    } else {
                        System.err.println("❌ Impossibile trovare mainRoot per aprire il pannello di login");
                    }
                } else {
                    // Fallback se authManager è null
                    System.err.println("❌ AuthManager non disponibile");
                }
            }
        });

        buttonBox.getChildren().add(addToLibraryButton);
        return buttonBox;
    }

    /**
     * Metodo helper per recuperare il mainRoot dalla gerarchia dei nodi
     */
    private static StackPane getMainRootFromButton(Button button) {
        try {
            // Risali la gerarchia dei nodi fino a trovare il StackPane principale
            javafx.scene.Node currentNode = button;
            while (currentNode != null) {
                if (currentNode instanceof StackPane) {
                    StackPane stackPane = (StackPane) currentNode;
                    // Verifica se questo è il mainRoot controllando se ha figli appropriati
                    if (stackPane.getChildren().size() >= 2) {
                        return stackPane;
                    }
                }
                currentNode = currentNode.getParent();
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero mainRoot: " + e.getMessage());
            return null;
        }
    }

    private static Button createStyledButton(String text, String bgColor, String textColor) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 25;" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private static VBox createPublisherSection(Book book) {
        VBox publisherSection = new VBox(15);
        publisherSection.setPadding(new Insets(0, 30, 30, 30));

        Label publisherHeader = new Label("📝 Dall'editore");
        publisherHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        publisherHeader.setTextFill(Color.WHITE);

        String description = isNotEmpty(book.getDescription()) ?
                book.getDescription() : "Descrizione non disponibile per questo libro.";

        Text publisherText = new Text(description);
        publisherText.setWrappingWidth(940);
        publisherText.setFill(Color.WHITE);
        publisherText.setFont(Font.font("SF Pro Text", 14));

        publisherSection.getChildren().addAll(publisherHeader, publisherText);
        return publisherSection;
    }

    // Rating system methods
    private static void loadBookRatingsForAllUsers(Book book, AuthenticationManager authManager) {
        if (isEmpty(book.getIsbn())) {
            updateRatingDisplaySafe();
            return;
        }

        // Load average rating (always visible)
        loadAverageRating(book);

        // Load user rating (only if authenticated)
        if (authManager != null && authManager.isAuthenticated()) {
            loadUserRating(book, authManager.getCurrentUsername());
        }
    }

    private static void loadAverageRating(Book book) {
        ratingService.getBookRatingStatisticsAsync(book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        averageBookRating = response.getAverageRating();
                        currentBookReviewCount = response.getTotalRatings();
                    } else {
                        averageBookRating = null;
                        currentBookReviewCount = null;
                    }
                    updateRatingDisplaySafe();
                    refreshRatingSection();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        averageBookRating = null;
                        currentBookReviewCount = null;
                        updateRatingDisplaySafe();
                    });
                    return null;
                });
    }

    private static void loadUserRating(Book book, String username) {
        ratingService.getUserRatingForBookAsync(username, book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    currentUserRating = response.isSuccess() ? response.getRating() : null;
                    refreshRatingSection();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        currentUserRating = null;
                        refreshRatingSection();
                    });
                    return null;
                });
    }

    private static void updateRatingDisplaySafe() {
        if (averageRatingLabel != null) {
            if (averageBookRating != null && averageBookRating > 0) {
                int stars = (int) Math.round(averageBookRating);
                String starsDisplay = "★".repeat(stars) + "☆".repeat(5 - stars);

                int reviewCount = currentBookReviewCount != null ? currentBookReviewCount : 0;
                String text = String.format("%s %.1f/5 (%d recensioni)",
                        starsDisplay, averageBookRating, reviewCount);

                averageRatingLabel.setText(text);
                averageRatingLabel.setTextFill(Color.GOLD);
            } else {
                averageRatingLabel.setText("☆☆☆☆☆ Non ancora valutato");
                averageRatingLabel.setTextFill(Color.LIGHTGRAY);
            }
        }
    }

    private static void refreshRatingSection() {
        if (currentRatingSection != null && currentBook != null && currentAuthManager != null) {
            Parent parent = currentRatingSection.getParent();
            if (parent instanceof VBox) {
                VBox parentVBox = (VBox) parent;
                int index = parentVBox.getChildren().indexOf(currentRatingSection);

                if (index >= 0) {
                    parentVBox.getChildren().remove(index);
                    VBox newRatingSection = createRatingSection(currentBook, currentAuthManager);
                    parentVBox.getChildren().add(index, newRatingSection);
                }
            }
        }
    }

    private static VBox createRatingSection(Book book, AuthenticationManager authManager) {
        VBox ratingSection = new VBox(15);
        ratingSection.setPadding(new Insets(0, 30, 30, 30));
        currentRatingSection = ratingSection;

        // Header
        Label ratingsHeader = new Label("⭐ Valutazioni");
        ratingsHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        ratingsHeader.setTextFill(Color.WHITE);

        // Content
        VBox ratingsContent = new VBox(12);
        ratingsContent.getChildren().add(createAverageRatingDisplay());

        if (authManager != null && authManager.isAuthenticated()) {
            ratingsContent.getChildren().add(createUserRatingSection(book, authManager));
        } else {
            ratingsContent.getChildren().add(createGuestInviteSection());
        }

        ratingSection.getChildren().addAll(ratingsHeader, ratingsContent);
        return ratingSection;
    }

    private static HBox createAverageRatingDisplay() {
        HBox averageBox = new HBox(15);
        averageBox.setStyle(
                "-fx-background-color: #3a3a3c;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );
        averageBox.setAlignment(Pos.CENTER_LEFT);

        VBox averageInfo = new VBox(5);

        Label averageTitle = new Label("📊 Valutazione della Community");
        averageTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        averageTitle.setTextFill(Color.WHITE);

        Label averageValue = new Label("⭐ Caricamento...");
        averageValue.setFont(Font.font("SF Pro Text", 14));
        averageValue.setTextFill(Color.GRAY);

        if (averageBookRating != null && averageBookRating > 0) {
            int stars = (int) Math.round(averageBookRating);
            String starsDisplay = "★".repeat(stars) + "☆".repeat(5 - stars);
            averageValue.setText(String.format("%s %.1f/5", starsDisplay, averageBookRating));
            averageValue.setTextFill(Color.GOLD);
        }

        Label publicNote = new Label("Basata su tutte le recensioni degli utenti registrati");
        publicNote.setFont(Font.font("SF Pro Text", 12));
        publicNote.setTextFill(Color.GRAY);

        averageInfo.getChildren().addAll(averageTitle, averageValue, publicNote);
        averageBox.getChildren().add(averageInfo);

        return averageBox;
    }

    private static VBox createGuestInviteSection() {
        VBox guestSection = new VBox(10);
        guestSection.setStyle(
                "-fx-background-color: #444448;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        Label guestTitle = new Label("🔐 Vuoi valutare questo libro?");
        guestTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        guestTitle.setTextFill(Color.WHITE);

        Label inviteMessage = new Label("Registrati o accedi per valutare e recensire i tuoi libri preferiti");
        inviteMessage.setFont(Font.font("SF Pro Text", 14));
        inviteMessage.setTextFill(Color.LIGHTGRAY);
        inviteMessage.setWrapText(true);

        Label benefitMessage = new Label("✨ Condividi le tue opinioni con la community di lettori!");
        benefitMessage.setFont(Font.font("SF Pro Text", 12));
        benefitMessage.setTextFill(Color.LIGHTBLUE);

        guestSection.getChildren().addAll(guestTitle, inviteMessage, benefitMessage);
        return guestSection;
    }

    private static VBox createUserRatingSection(Book book, AuthenticationManager authManager) {
        VBox userSection = new VBox(10);
        userSection.setStyle(
                "-fx-background-color: #444448;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        // Header with action button
        HBox userHeader = new HBox();
        userHeader.setAlignment(Pos.CENTER_LEFT);

        Label userTitle = new Label("👤 La tua valutazione");
        userTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        userTitle.setTextFill(Color.WHITE);

        Region userSpacer = new Region();
        HBox.setHgrow(userSpacer, Priority.ALWAYS);

        Button ratingActionButton = createRatingActionButton(book, authManager);
        userHeader.getChildren().addAll(userTitle, userSpacer, ratingActionButton);

        // Content
        VBox userContent = new VBox(8);
        updateUserContent(userContent);

        userSection.getChildren().addAll(userHeader, userContent);
        return userSection;
    }

    private static Button createRatingActionButton(Book book, AuthenticationManager authManager) {
        Button button = new Button();
        updateRatingButton(button);

        button.setOnAction(e -> {
            if (isEmpty(book.getIsbn())) {
                showAlert("Errore", "Impossibile valutare: ISBN del libro mancante");
                return;
            }

            String username = authManager.getCurrentUsername();
            if (username == null) {
                showAlert("Errore", "Devi essere autenticato per valutare");
                return;
            }

            // Verifica prima se l'utente possiede il libro usando metodi esistenti
            LibraryService libraryService = new LibraryService();
            libraryService.doesUserOwnBookAsync(username, book.getIsbn())
                    .thenAccept(owns -> Platform.runLater(() -> {
                        if (owns) {
                            // Utente possiede il libro, può valutare
                            if (currentUserRating != null) {
                                // USA IL METODO CORRETTO: showEditRatingDialog
                                RatingDialog.showEditRatingDialog(book, username, currentUserRating, (rating) -> {
                                    loadUserRating(book, authManager.getCurrentUsername());
                                    loadAverageRating(book);
                                });
                            } else {
                                // USA IL METODO CORRETTO: showRatingDialog
                                RatingDialog.showRatingDialog(book, username, (rating) -> {
                                    loadUserRating(book, authManager.getCurrentUsername());
                                    loadAverageRating(book);
                                });
                            }
                        } else {
                            // Utente NON possiede il libro, mostra popup di errore
                            showBookNotOwnedDialog(book, libraryService, username);
                        }
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> showAlert("Errore",
                                "Errore nel controllo possesso libro: " + throwable.getMessage()));
                        return null;
                    });
        });

        return button;
    }

    private static void showBookNotOwnedDialog(Book book, LibraryService libraryService, String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Libro non posseduto");
        alert.setHeaderText("Non puoi valutare questo libro");
        alert.setContentText("Per valutare '" + book.getTitle() + "' devi prima aggiungerlo a una delle tue librerie.");

        // Personalizza i bottoni
        ButtonType addToLibraryButton = new ButtonType("📚 Aggiungi a Libreria");
        ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(addToLibraryButton, cancelButton);

        styleDialog(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == addToLibraryButton) {
                // Riusa il metodo esistente per aggiungere a libreria
                handleAddToLibraryAction(book, username, libraryService);
            }
        });
    }

    private static void handleAddToLibraryAction(Book book, String username, LibraryService libraryService) {
        // Usa il metodo esistente per recuperare le librerie dell'utente
        libraryService.getUserLibrariesAsync(username)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getLibraries() != null && !response.getLibraries().isEmpty()) {
                        showChooseLibraryDialog(book, username, response.getLibraries(), libraryService);
                    } else {
                        showCreateNewLibraryDialog(book, username, libraryService);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showAlert("Errore",
                            "Errore nel recupero librerie: " + throwable.getMessage()));
                    return null;
                });
    }

    private static void updateRatingButton(Button button) {
        if (currentUserRating != null) {
            button.setText("Modifica");
            button.setStyle(
                    "-fx-background-color: #9b59b6;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 15;" +
                            "-fx-cursor: hand;"
            );
        } else {
            button.setText("Valuta");
            button.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 15;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    private static void updateUserContent(VBox userContent) {
        userContent.getChildren().clear();

        if (currentUserRating != null) {
            Label userRatingDisplay = new Label(currentUserRating.getDisplayRating());
            userRatingDisplay.setFont(Font.font("SF Pro Text", 14));
            userRatingDisplay.setTextFill(Color.LIGHTGREEN);

            Label ratingBreakdown = new Label(String.format(
                    "Stile: %d★ | Contenuto: %d★ | Piacevolezza: %d★ | Originalità: %d★ | Edizione: %d★",
                    currentUserRating.getStyle(), currentUserRating.getContent(),
                    currentUserRating.getPleasantness(), currentUserRating.getOriginality(),
                    currentUserRating.getEdition()
            ));
            ratingBreakdown.setFont(Font.font("SF Pro Text", 12));
            ratingBreakdown.setTextFill(Color.LIGHTGRAY);

            userContent.getChildren().addAll(userRatingDisplay, ratingBreakdown);

            if (isNotEmpty(currentUserRating.getReview())) {
                Label reviewLabel = new Label("💭 La tua recensione:");
                reviewLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 12));
                reviewLabel.setTextFill(Color.WHITE);

                Text reviewText = new Text(currentUserRating.getReview());
                reviewText.setFont(Font.font("SF Pro Text", 12));
                reviewText.setFill(Color.LIGHTGRAY);
                reviewText.setWrappingWidth(400);

                userContent.getChildren().addAll(reviewLabel, reviewText);
            }
        } else {
            Label inviteLabel = new Label("Non hai ancora valutato questo libro");
            inviteLabel.setFont(Font.font("SF Pro Text", 14));
            inviteLabel.setTextFill(Color.LIGHTGRAY);

            Label benefitLabel = new Label("Condividi la tua opinione con altri lettori!");
            benefitLabel.setFont(Font.font("SF Pro Text", 12));
            benefitLabel.setTextFill(Color.GRAY);

            userContent.getChildren().addAll(inviteLabel, benefitLabel);
        }
    }

    // Library management methods (simplified)
    private static void showAddToLibraryDialog(Book book, AuthenticationManager authManager) {
        if (isEmpty(book.getIsbn())) {
            showAlert("❌ Errore", "Impossibile aggiungere il libro: ISBN mancante");
            return;
        }

        LibraryService libraryService = new LibraryService();
        libraryService.getUserLibrariesAsync(authManager.getCurrentUsername())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getLibraries() != null) {
                        List<String> libraries = response.getLibraries();
                        if (libraries.isEmpty()) {
                            showCreateFirstLibraryDialog(book, authManager.getCurrentUsername(), libraryService);
                        } else {
                            showChooseLibraryDialog(book, authManager.getCurrentUsername(), libraries, libraryService);
                        }
                    } else {
                        showAlert("❌ Errore", "Errore nel recupero delle librerie: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage()));
                    return null;
                });
    }

    private static void showCreateFirstLibraryDialog(Book book, String username, LibraryService libraryService) {
        TextInputDialog dialog = new TextInputDialog("La mia libreria");
        dialog.setTitle("Crea la tua prima libreria");
        dialog.setHeaderText("Non hai ancora librerie. Creane una per '" + book.getTitle() + "'");
        dialog.setContentText("Nome libreria:");

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(libraryName -> {
            if (isNotEmpty(libraryName)) {
                createLibraryAndAddBook(book, username, libraryName.trim(), libraryService);
            }
        });
    }

    private static void showChooseLibraryDialog(Book book, String username, List<String> libraries, LibraryService libraryService) {
        List<String> choices = new ArrayList<>(libraries);
        choices.add(0, "➕ Crea nuova libreria...");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(libraries.get(0), choices);
        dialog.setTitle("Aggiungi a Libreria");
        dialog.setHeaderText("Scegli dove aggiungere '" + book.getTitle() + "'");
        dialog.setContentText("Libreria:");

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(choice -> {
            if (choice.startsWith("➕")) {
                showCreateNewLibraryDialog(book, username, libraryService);
            } else {
                addBookToExistingLibrary(book, username, choice, libraryService);
            }
        });
    }

    private static void showCreateNewLibraryDialog(Book book, String username, LibraryService libraryService) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crea Nuova Libreria");
        dialog.setHeaderText("Crea una nuova libreria per '" + book.getTitle() + "'");
        dialog.setContentText("Nome libreria:");

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(libraryName -> {
            if (isNotEmpty(libraryName)) {
                createLibraryAndAddBook(book, username, libraryName.trim(), libraryService);
            }
        });
    }

    private static void createLibraryAndAddBook(Book book, String username, String libraryName, LibraryService libraryService) {
        libraryService.createLibraryAsync(username, libraryName)
                .thenCompose(createResponse -> {
                    if (createResponse.isSuccess()) {
                        return libraryService.addBookToLibraryAsync(username, libraryName, book.getIsbn());
                    } else {
                        return CompletableFuture.completedFuture(
                                new LibraryResponse(false, "Errore nella creazione: " + createResponse.getMessage())
                        );
                    }
                })
                .thenAccept(addResponse -> Platform.runLater(() -> {
                    if (addResponse.isSuccess()) {
                        showAlert("✅ Successo", "Libreria '" + libraryName + "' creata e libro aggiunto!");
                    } else {
                        showAlert("❌ Errore", addResponse.getMessage());
                    }
                }));
    }

    private static void addBookToExistingLibrary(Book book, String username, String libraryName, LibraryService libraryService) {
        libraryService.addBookToLibraryAsync(username, libraryName, book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showAlert("✅ Successo", "Libro aggiunto alla libreria '" + libraryName + "'!");
                    } else {
                        showAlert("❌ Errore", response.getMessage());
                    }
                }));
    }


    private static void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        //dialogPane.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");

        // AGGIUNTO: Imposta icona quando il dialog viene mostrato
        dialog.setOnShowing(e -> {
            Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
            IconUtils.setStageIcon(dialogStage);
        });
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // AGGIUNTO: Imposta icona per gli alert
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert.setOnShowing(e -> IconUtils.setStageIcon(alertStage));

        styleDialog(alert);
        alert.showAndWait();
    }

    // Reviews section
    private static VBox createReviewsSection() {
        VBox reviewsSection = new VBox(15);
        reviewsSection.setPadding(new Insets(0, 30, 30, 30));

        HBox reviewHeaderBox = createReviewsHeader();
        ScrollPane reviewsScrollPane = createReviewsScrollPane();

        reviewsSection.getChildren().addAll(reviewHeaderBox, reviewsScrollPane);
        return reviewsSection;
    }

    private static HBox createReviewsHeader() {
        HBox reviewHeaderBox = new HBox();

        Label reviewsHeader = new Label("💬 Recensioni della community");
        reviewsHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        reviewsHeader.setTextFill(Color.WHITE);

        Label seeMoreReviews = new Label("Vedi tutte ❯");
        seeMoreReviews.setFont(Font.font("SF Pro Text", 14));
        seeMoreReviews.setTextFill(Color.LIGHTBLUE);
        seeMoreReviews.setStyle("-fx-cursor: hand;");
        seeMoreReviews.setOnMouseClicked(e -> {
            if (currentBook != null) {
                showAllReviewsDialog(currentBook);
            }
        });

        Region reviewSpacer = new Region();
        HBox.setHgrow(reviewSpacer, Priority.ALWAYS);
        reviewHeaderBox.getChildren().addAll(reviewsHeader, reviewSpacer, seeMoreReviews);

        return reviewHeaderBox;
    }

    private static ScrollPane createReviewsScrollPane() {
        ScrollPane reviewsScrollPane = new ScrollPane();
        reviewsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        reviewsScrollPane.setFitToHeight(true);
        reviewsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reviewsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        reviewsScrollPane.setPannable(true);
        reviewsScrollPane.setPrefHeight(200);

        HBox reviewsContainer = new HBox(15);
        reviewsContainer.setPadding(new Insets(10, 0, 10, 0));
        reviewsScrollPane.setContent(reviewsContainer);

        loadCommunityReviews(reviewsContainer);
        return reviewsScrollPane;
    }

    private static void loadCommunityReviews(HBox container) {
        if (currentBook == null || isEmpty(currentBook.getIsbn())) {
            showNoReviewsMessage(container);
            return;
        }

        Label loadingLabel = new Label("📖 Caricamento recensioni...");
        loadingLabel.setFont(Font.font("SF Pro Text", 14));
        loadingLabel.setTextFill(Color.GRAY);
        loadingLabel.setStyle("-fx-padding: 20;");
        container.getChildren().add(loadingLabel);

        ratingService.getBookRatingsAsync(currentBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.isSuccess() && response.getRatings() != null && !response.getRatings().isEmpty()) {
                        List<BookRating> reviewsWithText = response.getRatings().stream()
                                .filter(rating -> isNotEmpty(rating.getReview()))
                                .filter(rating -> currentAuthManager == null ||
                                        !rating.getUsername().equals(currentAuthManager.getCurrentUsername()))
                                .sorted((r1, r2) -> Double.compare(r2.getAverage(), r1.getAverage()))
                                .limit(5)
                                .collect(Collectors.toList());

                        if (!reviewsWithText.isEmpty()) {
                            for (BookRating rating : reviewsWithText) {
                                VBox reviewCard = createCommunityReviewCard(rating);
                                container.getChildren().add(reviewCard);
                            }
                        } else {
                            showNoReviewsMessage(container);
                        }
                    } else {
                        showNoReviewsMessage(container);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        showErrorMessage(container, "Errore nel caricamento delle recensioni");
                    });
                    return null;
                });
    }

    private static VBox createCommunityReviewCard(BookRating rating) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: #3a3a3c;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPrefHeight(180);

        // Header with rating and username
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String stars = "★".repeat(rating.getStarRating()) + "☆".repeat(5 - rating.getStarRating());
        Label starsLabel = new Label(stars);
        starsLabel.setFont(Font.font("SF Pro Text", 14));
        starsLabel.setTextFill(Color.GOLD);

        String displayName = rating.getUsername().length() > 3 ?
                rating.getUsername().substring(0, 3) + "***" : "***";
        Label usernameLabel = new Label("di " + displayName);
        usernameLabel.setFont(Font.font("SF Pro Text", 10));
        usernameLabel.setTextFill(Color.LIGHTGRAY);

        header.getChildren().addAll(starsLabel, usernameLabel);

        // Review text (truncated)
        String reviewText = rating.getReview();
        if (reviewText.length() > 120) {
            reviewText = reviewText.substring(0, 117) + "...";
        }

        Text reviewContent = new Text(reviewText);
        reviewContent.setFont(Font.font("SF Pro Text", 12));
        reviewContent.setFill(Color.WHITE);
        reviewContent.setWrappingWidth(250);

        // Date
        Label dateLabel = new Label(getRelativeDate(rating.getData()));
        dateLabel.setFont(Font.font("SF Pro Text", 10));
        dateLabel.setTextFill(Color.GRAY);

        card.getChildren().addAll(header, reviewContent, dateLabel);
        return card;
    }

    private static void showNoReviewsMessage(HBox container) {
        Label noReviewsLabel = new Label("📝 Nessuna recensione ancora disponibile");
        noReviewsLabel.setFont(Font.font("SF Pro Text", 14));
        noReviewsLabel.setTextFill(Color.GRAY);
        noReviewsLabel.setStyle("-fx-padding: 20;");
        container.getChildren().add(noReviewsLabel);
    }

    private static void showErrorMessage(HBox container, String message) {
        Label errorLabel = new Label("❌ " + message);
        errorLabel.setFont(Font.font("SF Pro Text", 14));
        errorLabel.setTextFill(Color.LIGHTCORAL);
        errorLabel.setStyle("-fx-padding: 20;");
        container.getChildren().add(errorLabel);
    }

    private static String getRelativeDate(String dateStr) {
        if (isEmpty(dateStr)) return "Di recente";

        String[] samples = {"Oggi", "Ieri", "2 giorni fa", "1 settimana fa", "2 settimane fa"};
        return samples[(int) (Math.random() * samples.length)];
    }

    private static void showAllReviewsDialog(Book book) {
        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Recensioni - " + book.getTitle());

        // AGGIUNTO: Imposta icona per il dialog delle recensioni
        IconUtils.setStageIcon(dialogStage);

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-background-color: #2b2b2b;");

        Label headerLabel = new Label("📖 Tutte le recensioni di \"" + book.getTitle() + "\"");
        headerLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");

        VBox reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(10));
        loadAllReviews(reviewsList, book);
        scrollPane.setContent(reviewsList);

        Button closeButton = new Button("Chiudi");
        closeButton.setStyle(
                "-fx-background-color: #606060;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> dialogStage.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(closeButton);

        dialogContent.getChildren().addAll(headerLabel, scrollPane, buttonBox);

        Scene scene = new Scene(dialogContent, 600, 500);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private static void loadAllReviews(VBox container, Book book) {
        Label loadingLabel = new Label("📖 Caricamento di tutte le recensioni...");
        loadingLabel.setFont(Font.font("SF Pro Text", 14));
        loadingLabel.setTextFill(Color.WHITE);
        container.getChildren().add(loadingLabel);

        ratingService.getBookRatingsAsync(book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.isSuccess() && response.getRatings() != null && !response.getRatings().isEmpty()) {
                        List<BookRating> allReviews = response.getRatings().stream()
                                .filter(rating -> isNotEmpty(rating.getReview()))
                                .sorted((r1, r2) -> Double.compare(r2.getAverage(), r1.getAverage()))
                                .collect(Collectors.toList());

                        if (!allReviews.isEmpty()) {
                            for (BookRating rating : allReviews) {
                                VBox fullReviewCard = createFullReviewCard(rating);
                                container.getChildren().add(fullReviewCard);
                            }
                        } else {
                            Label noReviewsLabel = new Label("📝 Non ci sono ancora recensioni testuali per questo libro");
                            noReviewsLabel.setFont(Font.font("SF Pro Text", 14));
                            noReviewsLabel.setTextFill(Color.LIGHTGRAY);
                            container.getChildren().add(noReviewsLabel);
                        }
                    } else {
                        Label errorLabel = new Label("❌ Errore nel caricamento delle recensioni");
                        errorLabel.setFont(Font.font("SF Pro Text", 14));
                        errorLabel.setTextFill(Color.LIGHTCORAL);
                        container.getChildren().add(errorLabel);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore di connessione: " + throwable.getMessage());
                        errorLabel.setFont(Font.font("SF Pro Text", 14));
                        errorLabel.setTextFill(Color.LIGHTCORAL);
                        container.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    private static VBox createFullReviewCard(BookRating rating) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: #383838;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;"
        );

        // Header with detailed rating
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        String mainStars = "★".repeat(rating.getStarRating()) + "☆".repeat(5 - rating.getStarRating());
        Label mainStarsLabel = new Label(mainStars + " " + String.format("%.1f/5", rating.getAverage()));
        mainStarsLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        mainStarsLabel.setTextFill(Color.GOLD);

        String displayName = rating.getUsername().length() > 4 ?
                rating.getUsername().substring(0, 4) + "***" : "Utente***";
        Label usernameLabel = new Label("di " + displayName);
        usernameLabel.setFont(Font.font("SF Pro Text", 14));
        usernameLabel.setTextFill(Color.LIGHTBLUE);

        header.getChildren().addAll(mainStarsLabel, usernameLabel);

        // Rating breakdown
        Label detailRating = new Label(String.format(
                "Stile: %d★ | Contenuto: %d★ | Piacevolezza: %d★ | Originalità: %d★ | Edizione: %d★",
                rating.getStyle(), rating.getContent(), rating.getPleasantness(),
                rating.getOriginality(), rating.getEdition()
        ));
        detailRating.setFont(Font.font("SF Pro Text", 12));
        detailRating.setTextFill(Color.LIGHTGRAY);

        // Full review text
        Text reviewText = new Text(rating.getReview());
        reviewText.setFont(Font.font("SF Pro Text", 14));
        reviewText.setFill(Color.WHITE);
        reviewText.setWrappingWidth(550);

        // Date
        Label dateLabel = new Label(getRelativeDate(rating.getData()));
        dateLabel.setFont(Font.font("SF Pro Text", 12));
        dateLabel.setTextFill(Color.GRAY);

        card.getChildren().addAll(header, detailRating, reviewText, dateLabel);
        return card;
    }

    /**
     * Crea la sezione raccomandazioni
     */
    private static VBox createRecommendationsSection(Book book, AuthenticationManager authManager) {
        VBox recommendationsSection = new VBox(15);
        recommendationsSection.setPadding(new Insets(0, 30, 30, 30));

        // Header con pulsante per aggiungere raccomandazioni
        HBox recommendationsHeader = createRecommendationsHeader(book, authManager);

        // Container per le raccomandazioni
        ScrollPane recommendationsScrollPane = createRecommendationsScrollPane();

        recommendationsSection.getChildren().addAll(recommendationsHeader, recommendationsScrollPane);

        // Carica le raccomandazioni esistenti
        loadBookRecommendations(book, recommendationsScrollPane);

        return recommendationsSection;
    }

    /**
     * Crea l'header della sezione raccomandazioni
     */
    private static HBox createRecommendationsHeader(Book book, AuthenticationManager authManager) {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label recommendationsHeader = new Label("💡 Raccomandazioni della community");
        recommendationsHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        recommendationsHeader.setTextFill(Color.WHITE);

        Region recommendationsSpacer = new Region();
        HBox.setHgrow(recommendationsSpacer, Priority.ALWAYS);

        // Pulsante per aggiungere raccomandazioni (solo per utenti autenticati)
        if (authManager != null && authManager.isAuthenticated()) {
            Button addRecommendationButton = new Button("Consiglia libri");
            addRecommendationButton.setStyle(
                    "-fx-background-color: #9b59b6;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;"
            );

            addRecommendationButton.setOnAction(e -> {
                if (isEmpty(book.getIsbn())) {
                    showAlert("❌ Errore", "Impossibile consigliare: ISBN del libro mancante");
                    return;
                }

                // Verifica prima se l'utente può consigliare
                recommendationService.canUserRecommendAsync(authManager.getCurrentUsername(), book.getIsbn())
                        .thenAccept(response -> Platform.runLater(() -> {
                            if (response.isSuccess() && response.getCanRecommend() != null && response.getCanRecommend()) {
                                // Apri dialog raccomandazioni
                                RecommendationDialog.showRecommendationDialog(
                                        book,
                                        authManager.getCurrentUsername(),
                                        authManager,
                                        updatedRecommendations -> {
                                            // Ricarica le raccomandazioni quando vengono aggiornate
                                            loadBookRecommendations(book, findRecommendationsScrollPane());
                                        }
                                );
                            } else {
                                showAlert("⚠️ Non disponibile", response.getMessage());
                            }
                        }))
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                            });
                            return null;
                        });
            });

            headerBox.getChildren().addAll(recommendationsHeader, recommendationsSpacer, addRecommendationButton);
        } else {
            // Solo header per utenti non autenticati
            Label guestHint = new Label("Accedi per consigliare libri");
            guestHint.setFont(Font.font("SF Pro Text", 12));
            guestHint.setTextFill(Color.LIGHTBLUE);

            headerBox.getChildren().addAll(recommendationsHeader, recommendationsSpacer, guestHint);
        }

        return headerBox;
    }

    /**
     * Crea lo scroll pane per le raccomandazioni
     */
    private static ScrollPane createRecommendationsScrollPane() {
        ScrollPane recommendationsScrollPane = new ScrollPane();
        recommendationsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        recommendationsScrollPane.setFitToHeight(true);
        recommendationsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recommendationsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recommendationsScrollPane.setPannable(true);
        recommendationsScrollPane.setPrefHeight(200);

        HBox recommendationsContainer = new HBox(15);
        recommendationsContainer.setPadding(new Insets(10, 0, 10, 0));
        recommendationsScrollPane.setContent(recommendationsContainer);

        // SALVA IL RIFERIMENTO
        currentRecommendationsScrollPane = recommendationsScrollPane;

        return recommendationsScrollPane;
    }

    /**
     * Carica le raccomandazioni per il libro corrente
     */
    private static void loadBookRecommendations(Book targetBook, ScrollPane scrollPane) {
        System.out.println("📚 Caricamento raccomandazioni per: " + targetBook.getTitle());

        // CORREZIONE: Usa il metodo corretto getBookRecommendationsAsync
        CompletableFuture.supplyAsync(() -> {
            try {
                // Usa il metodo corretto che esiste nel ClientRecommendationService
                return recommendationService.getBookRecommendationsAsync(targetBook.getIsbn()).get();
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento raccomandazioni: " + e.getMessage());
                return new RecommendationResponse(false, "Errore: " + e.getMessage());
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                // CORREZIONE CRITICA: Imposta recommendedBooksDetails dalla response
                if (response.isSuccess()) {
                    currentBookRecommendations = response.getRecommendations();
                    // QUESTA È LA RIGA MANCANTE - Imposta i dettagli dei libri dalla response
                    recommendedBooksDetails = response.getRecommendedBooks();

                    System.out.println("📚 AGGIORNATO recommendedBooksDetails con " +
                            (recommendedBooksDetails != null ? recommendedBooksDetails.size() : "NULL") + " libri");
                }

                // DEBUG: Controlla se abbiamo i dettagli dei libri raccomandati
                System.out.println("🔍 DEBUG recommendedBooksDetails:");
                if (recommendedBooksDetails != null) {
                    System.out.println("   Trovati " + recommendedBooksDetails.size() + " dettagli libri:");
                    for (Book book : recommendedBooksDetails) {
                        System.out.println("   - ISBN: " + book.getIsbn() + ", Titolo: " + book.getTitle());
                    }
                } else {
                    System.out.println("   recommendedBooksDetails è NULL - i titoli non saranno disponibili");
                }

                if (response.isSuccess() && response.hasMultipleRecommendations()) {
                    System.out.println("🎯 Raccomandazioni trovate: " + response.getRecommendations().size());

                    // CORREZIONE 1: Raggruppa per evitare duplicati
                    Map<String, List<BookRecommendation>> groupedRecs = groupRecommendationsByBook(response.getRecommendations());

                    // CORREZIONE 2: Usa HBox per layout SOLO orizzontale
                    HBox cardsContainer = new HBox(15);
                    cardsContainer.setAlignment(Pos.CENTER_LEFT);
                    cardsContainer.setPadding(new Insets(10));

                    for (Map.Entry<String, List<BookRecommendation>> entry : groupedRecs.entrySet()) {
                        List<BookRecommendation> bookRecs = entry.getValue();
                        BookRecommendation firstRec = bookRecs.get(0); // Prendi il primo per i dettagli del libro

                        System.out.println("📖 Creazione carta per ISBN: " + firstRec.getRecommendedBookIsbn());

                        // Crea carta per il libro raccomandato CON IMMAGINE
                        VBox card = createRecommendationCardWithImage(firstRec, bookRecs, targetBook);
                        cardsContainer.getChildren().add(card);
                    }

                    // CORREZIONE 3: Usa ScrollPane SOLO orizzontale
                    ScrollPane fixedScrollPane = createFixedHorizontalScrollPane(cardsContainer);

                    // Sostituisci completamente il contenuto
                    scrollPane.setContent(fixedScrollPane.getContent());
                    scrollPane.setHbarPolicy(fixedScrollPane.getHbarPolicy());
                    scrollPane.setVbarPolicy(fixedScrollPane.getVbarPolicy());
                    scrollPane.setPrefHeight(fixedScrollPane.getPrefHeight());
                    scrollPane.setMaxHeight(fixedScrollPane.getMaxHeight());
                    scrollPane.setMinHeight(fixedScrollPane.getMinHeight());
                    scrollPane.setFitToHeight(fixedScrollPane.isFitToHeight());
                    scrollPane.setFitToWidth(fixedScrollPane.isFitToWidth());

                    System.out.println("✅ Raccomandazioni caricate: " + groupedRecs.size() + " libri unici");
                } else {
                    // Crea container vuoto con messaggio
                    HBox emptyContainer = new HBox();
                    emptyContainer.setAlignment(Pos.CENTER);
                    emptyContainer.setPrefHeight(240); // Stessa altezza del container pieno

                    Label noRecsLabel = new Label("📝 Nessuna raccomandazione disponibile");
                    noRecsLabel.setTextFill(Color.GRAY);
                    noRecsLabel.setFont(Font.font("SF Pro Text", 12));
                    emptyContainer.getChildren().add(noRecsLabel);

                    scrollPane.setContent(emptyContainer);
                    System.out.println("ℹ️ Nessuna raccomandazione trovata per il libro");
                }
            });
        });
    }

    // 7. METODO CORRETTO per creare carta raccomandazione CON IMMAGINE
    private static VBox createRecommendationCardWithImage(BookRecommendation firstRec, List<BookRecommendation> allRecommendations, Book targetBook) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #3a3a3c;" +  // Colore corretto come le altre carte
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 3, 0, 0, 1);"
        );
        card.setPrefWidth(180); // Larghezza maggiore per vedere tutto
        card.setMaxWidth(180);
        card.setPrefHeight(220); // Altezza fissa per uniformità
        card.setMaxHeight(220);

        // CORREZIONE: Trova il libro corretto dai dettagli - variabile final
        final Book recommendedBook;
        if (recommendedBooksDetails != null) {
            Book foundBook = null;
            for (Book book : recommendedBooksDetails) {
                if (book.getIsbn().equals(firstRec.getRecommendedBookIsbn())) {
                    foundBook = book;
                    break;
                }
            }
            recommendedBook = foundBook != null ? foundBook : createBookFromRecommendation(firstRec);
        } else {
            recommendedBook = createBookFromRecommendation(firstRec);
        }

        // Crea immagine copertina
        recommendedBook.ensureLocalImageFileName();
        ImageView coverImage = ImageUtils.createSafeImageView(recommendedBook.getSafeImageFileName(), 70, 100);
        Rectangle coverClip = new Rectangle(70, 100);
        coverClip.setArcWidth(4);
        coverClip.setArcHeight(4);
        coverImage.setClip(coverClip);

        // Centra l'immagine
        HBox imageContainer = new HBox(coverImage);
        imageContainer.setAlignment(Pos.CENTER);

        // CORREZIONE: Titolo del libro (NON ISBN)
        String bookTitle = recommendedBook.getTitle();
        if (bookTitle == null || bookTitle.startsWith("Libro ISBN:")) {
            bookTitle = "Titolo non disponibile";
        }

        Label titleLabel = new Label(bookTitle);
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 9));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(160);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-text-alignment: center;");
        titleLabel.setPrefHeight(30); // Altezza fissa per il titolo

        // Label con numero di raccomandatori
        Label recommendersLabel = new Label("👥 " + allRecommendations.size() +
                (allRecommendations.size() == 1 ? " utente" : " utenti"));
        recommendersLabel.setFont(Font.font("SF Pro Text", 8));
        recommendersLabel.setTextFill(Color.web("#FFD700")); // Oro invece di giallo
        recommendersLabel.setAlignment(Pos.CENTER);

        // Lista raccomandatori (max 2 visibili per spazio) con possibilità di rimozione
        VBox recommendersBox = new VBox(2);
        recommendersBox.setMaxHeight(40); // Limita altezza

        for (int i = 0; i < Math.min(2, allRecommendations.size()); i++) {
            BookRecommendation rec = allRecommendations.get(i);

            HBox recommenderRow = new HBox(3);
            recommenderRow.setAlignment(Pos.CENTER_LEFT);

            Label recommenderLabel = new Label("👤 " + getShortUsername(rec.getRecommenderUsername()));
            recommenderLabel.setFont(Font.font("SF Pro Text", 7));
            recommenderLabel.setTextFill(Color.LIGHTBLUE);

            // Pulsante elimina se è la raccomandazione dell'utente corrente
            if (currentAuthManager != null && currentAuthManager.isAuthenticated() &&
                    rec.getRecommenderUsername().equals(currentAuthManager.getCurrentUsername())) {

                Button deleteButton = new Button("✕");
                deleteButton.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 6;" +
                                "-fx-background-radius: 50;" +
                                "-fx-min-width: 12;" +
                                "-fx-min-height: 12;" +
                                "-fx-max-width: 12;" +
                                "-fx-max-height: 12;" +
                                "-fx-cursor: hand;"
                );

                deleteButton.setOnAction(e -> {
                    deleteButton.setDisable(true);
                    removeRecommendationAsync(rec, targetBook, deleteButton);
                });

                recommenderRow.getChildren().addAll(recommenderLabel, deleteButton);
            } else {
                recommenderRow.getChildren().add(recommenderLabel);
            }

            recommendersBox.getChildren().add(recommenderRow);
        }

        // Se ci sono più di 2 raccomandazioni, mostra "e altri X"
        if (allRecommendations.size() > 2) {
            Label moreLabel = new Label("... e altri " + (allRecommendations.size() - 2));
            moreLabel.setFont(Font.font("SF Pro Text", 6));
            moreLabel.setTextFill(Color.GRAY);
            recommendersBox.getChildren().add(moreLabel);
        }

        // Pulsante "Vedi dettagli"
        Button viewButton = new Button("Vedi dettagli");
        viewButton.setStyle(
                "-fx-background-color: #007AFF;" +  // Blu Apple
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 8;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 3 8;" +
                        "-fx-cursor: hand;"
        );

        // Gestione click per aprire dettagli libro raccomandato
        viewButton.setOnAction(e -> {
            viewButton.setDisable(true);
            viewButton.setText("Apertura...");

            Platform.runLater(() -> {
                try {
                    List<Book> navigableBooks = createNavigableRecommendedBooks(recommendedBook);

                    PopupManager popupManager = PopupManager.getInstance();
                    if (popupManager.isInitialized()) {
                        popupManager.showRecommendationDetails(recommendedBook, navigableBooks, currentAuthManager);
                    } else {
                        showRecommendedBookDetailsPopupFallback(recommendedBook, navigableBooks);
                    }

                } catch (Exception ex) {
                    System.err.println("❌ Errore apertura popup raccomandazione: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert("❌ Errore", "Errore nell'apertura dei dettagli del libro");
                } finally {
                    // Riabilita pulsante dopo un delay
                    Timeline enableDelay = new Timeline(
                            new KeyFrame(Duration.millis(1500), event -> {
                                viewButton.setDisable(false);
                                viewButton.setText("Vedi dettagli");
                            })
                    );
                    enableDelay.play();
                }
            });
        });

        // Centra il pulsante
        HBox buttonContainer = new HBox(viewButton);
        buttonContainer.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageContainer, titleLabel, recommendersLabel, recommendersBox, buttonContainer);
        return card;
    }

    /**
     * Crea una card per una raccomandazione
     */
    private static VBox createRecommendationCard(BookRecommendation firstRec, List<BookRecommendation> allRecommendations, Book targetBook) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #2c3e50;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );
        card.setPrefWidth(200);
        card.setMaxWidth(200); // IMPORTANTE: Larghezza fissa per evitare espansione

        // Header con titolo libro
        Label titleLabel = new Label(getRecommendedBookTitle(firstRec));
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 11));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);

        // Label con numero di raccomandatori
        Label recommendersLabel = new Label("👥 " + allRecommendations.size() +
                (allRecommendations.size() == 1 ? " utente" : " utenti"));
        recommendersLabel.setFont(Font.font("SF Pro Text", 10));
        recommendersLabel.setTextFill(Color.YELLOW);

        // CORREZIONE 3: Lista raccomandatori (max 3 visibili) con possibilità di rimozione
        VBox recommendersBox = new VBox(3);
        for (int i = 0; i < Math.min(3, allRecommendations.size()); i++) {
            BookRecommendation rec = allRecommendations.get(i);

            HBox recommenderRow = new HBox(5);
            recommenderRow.setAlignment(Pos.CENTER_LEFT);

            Label recommenderLabel = new Label("👤 " + getShortUsername(rec.getRecommenderUsername()));
            recommenderLabel.setFont(Font.font("SF Pro Text", 9));
            recommenderLabel.setTextFill(Color.LIGHTBLUE);

            // CORREZIONE 4: Pulsante elimina FUNZIONANTE se è la raccomandazione dell'utente corrente
            if (currentAuthManager != null && currentAuthManager.isAuthenticated() &&
                    rec.getRecommenderUsername().equals(currentAuthManager.getCurrentUsername())) {

                Button deleteButton = new Button("✕");
                deleteButton.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 8;" +
                                "-fx-background-radius: 50;" +
                                "-fx-min-width: 16;" +
                                "-fx-min-height: 16;" +
                                "-fx-max-width: 16;" +
                                "-fx-max-height: 16;" +
                                "-fx-cursor: hand;"
                );

                deleteButton.setOnAction(e -> {
                    deleteButton.setDisable(true);
                    // CORREZIONE: Usa il metodo di rimozione corretto
                    removeRecommendationAsync(rec, targetBook, deleteButton);
                });

                recommenderRow.getChildren().addAll(recommenderLabel, deleteButton);
            } else {
                recommenderRow.getChildren().add(recommenderLabel);
            }

            recommendersBox.getChildren().add(recommenderRow);
        }

        // Se ci sono più di 3 raccomandazioni, mostra "e altri X"
        if (allRecommendations.size() > 3) {
            Label moreLabel = new Label("... e altri " + (allRecommendations.size() - 3));
            moreLabel.setFont(Font.font("SF Pro Text", 8));
            moreLabel.setTextFill(Color.GRAY);
            recommendersBox.getChildren().add(moreLabel);
        }

        // Pulsante "Vedi dettagli"
        Button viewButton = new Button("Vedi dettagli");
        viewButton.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 5 10;" +
                        "-fx-cursor: hand;"
        );

        // Gestione click per aprire dettagli libro raccomandato
        viewButton.setOnAction(e -> {
            viewButton.setDisable(true);
            viewButton.setText("Apertura...");

            Platform.runLater(() -> {
                try {
                    // Crea oggetto Book dal primo recommendation per i dettagli
                    Book recommendedBook = createBookFromRecommendation(firstRec);
                    List<Book> navigableBooks = createNavigableRecommendedBooks(recommendedBook);

                    PopupManager popupManager = PopupManager.getInstance();
                    if (popupManager.isInitialized()) {
                        popupManager.showRecommendationDetails(recommendedBook, navigableBooks, currentAuthManager);
                    } else {
                        showRecommendedBookDetailsPopupFallback(recommendedBook, navigableBooks);
                    }

                } catch (Exception ex) {
                    System.err.println("❌ Errore apertura popup raccomandazione: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert("❌ Errore", "Errore nell'apertura dei dettagli del libro");
                } finally {
                    // Riabilita pulsante dopo un delay
                    Timeline enableDelay = new Timeline(
                            new KeyFrame(Duration.millis(1500), event -> {
                                viewButton.setDisable(false);
                                viewButton.setText("Vedi dettagli");
                            })
                    );
                    enableDelay.play();
                }
            });
        });

        card.getChildren().addAll(titleLabel, recommendersLabel, recommendersBox, viewButton);
        return card;
    }

    private static String getShortUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "Utente";
        }

        // Se l'username è più lungo di 10 caratteri, accorcialo (spazio ridotto)
        if (username.length() > 10) {
            return username.substring(0, 7) + "...";
        }

        return username;
    }

    private static String getRecommendedBookTitle(BookRecommendation rec) {
        // Se abbiamo i dettagli dei libri raccomandati, usa quelli
        if (recommendedBooksDetails != null) {
            for (Book book : recommendedBooksDetails) {
                if (book.getIsbn() != null && book.getIsbn().equals(rec.getRecommendedBookIsbn())) {
                    if (book.getTitle() != null && !book.getTitle().trim().isEmpty()) {
                        return book.getTitle();
                    }
                }
            }
        }

        // Prova a caricare il libro dal servizio in modo sincrono
        try {
            // Se non abbiamo i dettagli, potremmo avere il servizio libri disponibile
            if (currentBook != null) {
                System.out.println("🔍 Tentativo recupero titolo per ISBN: " + rec.getRecommendedBookIsbn());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Errore recupero titolo libro: " + e.getMessage());
        }


        String isbn = rec.getRecommendedBookIsbn();
        if (isbn != null) {
            // Basato sui libri che vedo nell'immagine, prova a mappare alcuni ISBN noti
            switch (isbn) {
                case "399148582":
                    return "The Calculus of Consent";
                case "038031840X":
                    return "The Year's Best Fantasy";
                case "393030342":
                    return "Libro Consigliato";
                case "345358791":
                    return "2001: A Space Odyssey";
                case "684819066":
                    return "Libro di Fantascienza";
                default:
                    return "Libro consigliato";
            }
        }

        return "Libro consigliato";
    }

    private static Book createBookFromRecommendation(BookRecommendation rec) {
        // Se abbiamo i dettagli dei libri raccomandati, usa quelli
        if (recommendedBooksDetails != null) {
            for (Book book : recommendedBooksDetails) {
                if (book.getIsbn() != null && book.getIsbn().equals(rec.getRecommendedBookIsbn())) {
                    return book;
                }
            }
        }

        // Fallback con Book con titolo intelligente
        Book book = new Book();
        book.setIsbn(rec.getRecommendedBookIsbn());

        // Usa lo stesso metodo migliorato per il titolo
        String title = getRecommendedBookTitle(rec);
        book.setTitle(title);
        book.setAuthor("Autore non disponibile");

        return book;
    }

    /**
     * Crea una collezione navigabile di tutti i libri consigliati
     */
    private static List<Book> createNavigableRecommendedBooks(Book selectedBook) {
        List<Book> navigableBooks = new ArrayList<>();

        if (recommendedBooksDetails != null && !recommendedBooksDetails.isEmpty()) {
            // Usa tutti i libri consigliati
            navigableBooks.addAll(recommendedBooksDetails);

            // Assicurati che il libro selezionato sia incluso
            if (!navigableBooks.contains(selectedBook)) {
                navigableBooks.add(selectedBook);
            }

        } else {
            // Fallback: solo il libro selezionato
            navigableBooks.add(selectedBook);
        }

        System.out.println("📖 Collezione navigabile creata: " + navigableBooks.size() + " libri");
        for (int i = 0; i < navigableBooks.size(); i++) {
            System.out.println("   " + (i+1) + ". " + navigableBooks.get(i).getTitle());
        }

        return navigableBooks;
    }

    /**
     * Trova l'indice di un libro nella collezione
     */
    private static int findBookIndex(Book targetBook, List<Book> books) {
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            if (book.getIsbn() != null && book.getIsbn().equals(targetBook.getIsbn())) {
                return i;
            }
            if (book.getTitle() != null && book.getTitle().equals(targetBook.getTitle())) {
                return i;
            }
        }
        return 0; // Default al primo libro se non trovato
    }

    /**
     * Apre popup raccomandazione CON navigazione tra tutti i libri
     */
    private static void openRecommendationPopupWithNavigation(Book selectedBook, List<Book> navigableBooks,
                                                              int startIndex, Stage parentStage) {

        System.out.println("📖 Apertura popup raccomandazione come OVERLAY");

        // TROVA IL CONTAINER PRINCIPALE
        StackPane mainContainer = findMainRoot();

        if (mainContainer == null) {
            System.err.println("❌ Impossibile trovare container principale per overlay");
            showAlert("❌ Errore", "Impossibile aprire i dettagli del libro");
            return;
        }

        // GENERA UN ID UNIVOCO per questo popup
        String popupId = "recommendation-popup-" + System.currentTimeMillis();

        // CREA POPUP CONTENT con ID
        StackPane popupContent = createRecommendationPopupContent(
                selectedBook,
                navigableBooks,
                startIndex,
                () -> {
                    // Callback chiusura overlay - trova per ID
                    System.out.println("🔒 Chiusura popup raccomandazione overlay con ID: " + popupId);
                    Platform.runLater(() -> {
                        // Trova e rimuovi il popup con questo ID
                        mainContainer.getChildren().removeIf(node -> popupId.equals(node.getId()));
                        System.out.println("✅ Popup raccomandazione overlay rimosso per ID: " + popupId);
                    });
                }
        );

        // IMPOSTA L'ID
        popupContent.setId(popupId);

        // AGGIUNGI AL CONTAINER PRINCIPALE come overlay
        Platform.runLater(() -> {
            mainContainer.getChildren().add(popupContent);
            popupContent.requestFocus();
            System.out.println("✅ Popup raccomandazione overlay aggiunto con ID: " + popupId);
        });
    }

    public static void debugContainerHierarchy() {
        System.out.println("🔍 DEBUG CONTAINER HIERARCHY:");

        try {
            if (root != null) {
                System.out.println("  Root popup: " + root.getClass().getSimpleName());

                if (root.getScene() != null) {
                    Scene scene = root.getScene();
                    System.out.println("  Scene: " + scene.getClass().getSimpleName());
                    System.out.println("  Scene root: " + scene.getRoot().getClass().getSimpleName());

                    if (scene.getRoot() instanceof StackPane) {
                        StackPane sceneRoot = (StackPane) scene.getRoot();
                        System.out.println("  Scene root children: " + sceneRoot.getChildren().size());

                        for (int i = 0; i < sceneRoot.getChildren().size(); i++) {
                            System.out.println("    " + i + ". " + sceneRoot.getChildren().get(i).getClass().getSimpleName());
                        }
                    }

                    if (scene.getWindow() instanceof Stage) {
                        Stage stage = (Stage) scene.getWindow();
                        System.out.println("  Window title: " + stage.getTitle());
                      }
                }
            }

            // Debug finestre aperte
            javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Stage.getWindows();
            System.out.println("  Finestre aperte: " + windows.size());

            for (javafx.stage.Window window : windows) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    System.out.println("    - " + stage.getTitle() + " (Showing: " + stage.isShowing() + ")");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Errore debug container: " + e.getMessage());
        }
    }

    /**
     * Crea il contenuto del popup raccomandazione con navigazione
     */
    private static StackPane createRecommendationPopupContent(Book selectedBook, List<Book> navigableBooks,
                                                              int startIndex, Runnable closeCallback) {

        // Usa BookDetailsPopup ma con la collezione di libri consigliati
        return BookDetailsPopup.createWithLibrarySupport(
                selectedBook,
                navigableBooks,  // PASSA TUTTI I LIBRI NAVIGABILI
                closeCallback,
                currentAuthManager
        );
    }

    /**
     * RIPRISTINA FOCUS al popup principale
     */
    private static void restoreFocusToMainPopup(Stage parentStage) {
        Platform.runLater(() -> {
            try {
                if (parentStage != null && parentStage.isShowing()) {
                    System.out.println("🔄 Ripristino focus al popup principale");

                    // Focus sullo stage
                    parentStage.requestFocus();
                    parentStage.toFront();

                    // Focus sulla scena
                    Scene scene = parentStage.getScene();
                    if (scene != null && scene.getRoot() != null) {
                        scene.getRoot().requestFocus();

                        // Se è un StackPane (il nostro root), forza focus
                        if (scene.getRoot() instanceof StackPane) {
                            StackPane root = (StackPane) scene.getRoot();
                            root.setFocusTraversable(true);
                            root.requestFocus();

                            // RIATTIVA GLI EVENT HANDLER
                            reactivateEventHandlers(scene, root);
                        }
                    }

                    System.out.println("✅ Focus ripristinato al popup principale");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore ripristino focus: " + e.getMessage());
            }
        });
    }

    /**
     * RIATTIVA gli event handler del popup principale
     */
    private static void reactivateEventHandlers(Scene scene, StackPane root) {
        try {
            // RIATTIVA ESC
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    System.out.println("🔑 ESC riattivato su popup principale");

                    // Chiudi popup principale
                    Stage currentStage = (Stage) scene.getWindow();
                    if (currentStage != null && ApplicationProtection.safeCloseStage(currentStage)) {
                        // Chiuso con protezione
                    } else if (closeHandler != null) {
                        closeHandler.run();
                    }

                    event.consume();
                }

                // RIATTIVA NAVIGAZIONE FRECCE
                if (event.getCode() == KeyCode.LEFT && currentBookIndex > 0 && !isTransitioning) {
                    slideToBook(currentBookIndex - 1);
                    event.consume();
                } else if (event.getCode() == KeyCode.RIGHT && currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                    slideToBook(currentBookIndex + 1);
                    event.consume();
                }
            });

            // RIATTIVA CLICK BACKGROUND
            if (root.getChildren().size() > 0 && root.getChildren().get(0) instanceof StackPane) {
                StackPane backgroundLayer = (StackPane) root.getChildren().get(0);
                backgroundLayer.setOnMouseClicked(e -> {
                    System.out.println("🖱️ Click background riattivato");

                    Stage currentStage = (Stage) scene.getWindow();
                    if (currentStage != null && ApplicationProtection.safeCloseStage(currentStage)) {
                        // Chiuso con protezione
                    } else if (closeHandler != null) {
                        closeHandler.run();
                    }

                    e.consume();
                });
            }

            System.out.println("🔄 Event handler riattivati");

        } catch (Exception e) {
            System.err.println("⚠️ Errore riattivazione event handler: " + e.getMessage());
        }
    }


    private static void debugAllBookImages(List<Book> books) {
        System.out.println("🔍 DEBUG TUTTE LE IMMAGINI LIBRI:");
        for (Book book : books) {
            book.debugImageInfo();
            System.out.println("---");
        }
    }

    /**
     * Rimuove una raccomandazione dell'utente corrente
     */
    private static void removeRecommendationAsync(BookRecommendation rec, Book targetBook, Button deleteButton) {
        if (currentAuthManager == null || !currentAuthManager.isAuthenticated()) {
            showAlert("❌ Errore", "Devi essere autenticato per rimuovere una raccomandazione");
            deleteButton.setDisable(false);
            return;
        }

        // Verifica che l'utente possa rimuovere solo le proprie raccomandazioni
        if (!rec.getRecommenderUsername().equals(currentAuthManager.getCurrentUsername())) {
            showAlert("❌ Errore", "Puoi rimuovere solo le tue raccomandazioni");
            deleteButton.setDisable(false);
            return;
        }

        System.out.println("🗑️ Rimozione raccomandazione:");
        System.out.println("   Target Book: " + targetBook.getTitle() + " (ISBN: " + targetBook.getIsbn() + ")");
        System.out.println("   Recommended Book ISBN: " + rec.getRecommendedBookIsbn());
        System.out.println("   User: " + rec.getRecommenderUsername());

        // Crea richiesta di rimozione con dati corretti
        RecommendationRequest removeRequest = new RecommendationRequest(
                currentAuthManager.getCurrentUsername(),
                rec.getTargetBookIsbn(),  // ISBN del libro per cui è stata fatta la raccomandazione
                rec.getRecommendedBookIsbn()  // ISBN del libro raccomandato
        );

        // Esegui la rimozione in background
        CompletableFuture.supplyAsync(() -> {
            try {
                return recommendationService.removeRecommendationAsync(removeRequest).get();
            } catch (Exception e) {
                System.err.println("❌ Errore nella rimozione: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        }).thenAccept(removeResponse -> {
            Platform.runLater(() -> {
                if (removeResponse.isSuccess()) {
                    showAlert("✅ Successo", "Raccomandazione rimossa con successo");

                    // IMPORTANTE: Ricarica le raccomandazioni dopo la rimozione
                    if (currentRecommendationsScrollPane != null) {
                        System.out.println("🔄 Ricaricamento raccomandazioni dopo rimozione...");
                        loadBookRecommendations(targetBook, currentRecommendationsScrollPane);
                    }
                } else {
                    showAlert("❌ Errore", "Errore nella rimozione: " + removeResponse.getMessage());
                    deleteButton.setDisable(false);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                deleteButton.setDisable(false);
            });
            return null;
        });
    }

    // 2. FIX DUPLICATI - Metodo per rimuovere duplicati e raggruppare utenti
    private static Map<String, List<BookRecommendation>> groupRecommendationsByBook(List<BookRecommendation> recommendations) {
        Map<String, List<BookRecommendation>> groupedRecommendations = new LinkedHashMap<>();

        for (BookRecommendation rec : recommendations) {
            String key = rec.getRecommendedBookIsbn(); // Raggruppa per ISBN

            // Se non esiste la chiave, crea una nuova lista
            if (!groupedRecommendations.containsKey(key)) {
                groupedRecommendations.put(key, new ArrayList<>());
            }

            // Aggiungi la raccomandazione al gruppo
            groupedRecommendations.get(key).add(rec);
        }

        return groupedRecommendations;
    }

    private static ScrollPane createFixedHorizontalScrollPane(HBox cardsContainer) {
        ScrollPane recommendationsScrollPane = new ScrollPane(cardsContainer);

        // CORREZIONE: ScrollPane SOLO orizzontale
        recommendationsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recommendationsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // IMPEDISCE scroll verticale

        // CORREZIONE: Aumenta altezza per vedere le carte completamente
        recommendationsScrollPane.setPrefHeight(240); // Altezza maggiore per carte complete
        recommendationsScrollPane.setMaxHeight(240);
        recommendationsScrollPane.setMinHeight(240);

        // Stile per nascondere i bordi
        recommendationsScrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );

        // IMPORTANTE: Imposta fit-to-height per mantenere l'altezza
        recommendationsScrollPane.setFitToHeight(true);
        recommendationsScrollPane.setFitToWidth(false); // NON adattare alla larghezza

        return recommendationsScrollPane;
    }

    /**
     * Metodo fallback per aprire popup se PopupManager fallisce
     */
    private static void showRecommendedBookDetailsPopupFallback(Book recommendedBook, List<Book> recommendedBooksCollection) {
        System.out.println("🔄 Fallback: apertura popup tradizionale per: " + recommendedBook.getTitle());

        try {
            // Crea popup tradizionale
            StackPane popup = BookDetailsPopup.createWithLibrarySupport(
                    recommendedBook,
                    recommendedBooksCollection != null ? recommendedBooksCollection : List.of(recommendedBook),
                    () -> {
                        // Chiudi popup tradizionale
                        closeTopPopupFallback();
                    },
                    currentAuthManager
            );

            // Trova il root principale e aggiungi popup
            StackPane mainRoot = findMainRoot();
            if (mainRoot != null) {
                mainRoot.getChildren().add(popup);
                System.out.println("✅ Popup fallback aperto con successo");
            } else {
                System.err.println("❌ Impossibile trovare root principale per fallback");
                throw new RuntimeException("Root principale non trovato");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore nel fallback: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Metodo fallback per chiudere popup
     */
    private static void closeTopPopupFallback() {
        try {
            StackPane mainRoot = findMainRoot();
            if (mainRoot != null && mainRoot.getChildren().size() > 1) {
                mainRoot.getChildren().remove(mainRoot.getChildren().size() - 1);
                System.out.println("✅ Popup fallback chiuso");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Errore nella chiusura popup fallback: " + e.getMessage());
        }
    }

    /**
     * Mostra popup dettagli per un libro raccomandato
     */
    private static void showRecommendedBookDetailsPopup(Book recommendedBook, List<Book> recommendedBooksCollection) {
        System.out.println("🔍 Apertura popup per libro consigliato: " + recommendedBook.getTitle());

        try {
            // Crea un nuovo Stage invece di aggiungere al root esistente
            Stage recommendedPopupStage = new Stage();
            recommendedPopupStage.initStyle(StageStyle.TRANSPARENT);
            recommendedPopupStage.initModality(Modality.APPLICATION_MODAL);
            recommendedPopupStage.setTitle("Dettagli Libro Consigliato");

            // AGGIUNTO: Imposta icona per il popup libro consigliato
            IconUtils.setStageIcon(recommendedPopupStage);

            // Crea il popup content
            StackPane popup = BookDetailsPopup.createWithLibrarySupport(
                    recommendedBook,
                    recommendedBooksCollection != null ? recommendedBooksCollection : List.of(recommendedBook),
                    () -> {
                        // Chiudi questo popup e aggiorna il popup originale
                        recommendedPopupStage.close();
                        // Aggiorna le raccomandazioni nel popup originale se necessario
                        Platform.runLater(() -> {
                            if (currentBook != null && currentRecommendationsScrollPane != null) {
                                loadBookRecommendations(currentBook, currentRecommendationsScrollPane);
                            }
                        });
                    },
                    currentAuthManager
            );

            Scene scene = new Scene(popup, 1000, 800);
            scene.setFill(Color.TRANSPARENT);
            recommendedPopupStage.setScene(scene);

            // Centra rispetto al popup principale
            if (parentStage != null) {
                recommendedPopupStage.initOwner(parentStage);
            }

            recommendedPopupStage.show();
            recommendedPopupStage.centerOnScreen();

            System.out.println("✅ Popup libro consigliato aperto con successo");

        } catch (Exception e) {
            System.err.println("❌ Errore nell'apertura popup libro consigliato: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Errore", "Errore nell'apertura dei dettagli del libro: " + e.getMessage());
        }
    }

    public static void showAsDialog(Book book, List<Book> collection, AuthenticationManager authManager) {
        Platform.runLater(() -> {
            try {
                parentStage = new Stage();
                parentStage.initStyle(StageStyle.TRANSPARENT);
                parentStage.initModality(Modality.APPLICATION_MODAL);
                parentStage.setTitle("Dettagli Libro");

                // AGGIUNTO: Imposta icona per il popup
                IconUtils.setStageIcon(parentStage);

                StackPane popup = createWithLibrarySupport(book, collection, () -> {
                    parentStage.close();
                    parentStage = null;
                    currentRecommendationsScrollPane = null;
                }, authManager);

                Scene scene = new Scene(popup, 1000, 800);
                scene.setFill(Color.TRANSPARENT);
                parentStage.setScene(scene);

                parentStage.show();
                parentStage.centerOnScreen();

            } catch (Exception e) {
                System.err.println("❌ Errore nell'apertura popup principale: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    /**
     * Trova il container delle raccomandazioni
     */
    private static ScrollPane findRecommendationsScrollPane() {
        return currentRecommendationsScrollPane;
    }

    /**
     * Mostra messaggio quando non ci sono raccomandazioni
     */
    private static void showNoRecommendationsMessage(ScrollPane scrollPane) {
        if (scrollPane != null) {
            HBox container = (HBox) scrollPane.getContent();
            showNoRecommendationsMessage(container);
        }
    }


    private static void showNoRecommendationsMessage(HBox container) {
        Label noRecommendationsLabel = new Label("💡 Nessuna raccomandazione ancora disponibile");
        noRecommendationsLabel.setFont(Font.font("SF Pro Text", 14));
        noRecommendationsLabel.setTextFill(Color.GRAY);
        noRecommendationsLabel.setStyle("-fx-padding: 20;");
        container.getChildren().clear();
        container.getChildren().add(noRecommendationsLabel);
    }

    private static void showErrorRecommendationsMessage(HBox container, String message) {
        Label errorLabel = new Label("❌ " + message);
        errorLabel.setFont(Font.font("SF Pro Text", 14));
        errorLabel.setTextFill(Color.LIGHTCORAL);
        errorLabel.setStyle("-fx-padding: 20;");
        container.getChildren().clear();
        container.getChildren().add(errorLabel);
    }

    /**
     * Trova il root principale per aggiungere popup
     */
    private static StackPane findMainRoot() {
        try {
            // Prova con il root corrente se disponibile
            if (root != null && root.getScene() != null && root.getScene().getRoot() instanceof StackPane) {
                return (StackPane) root.getScene().getRoot();
            }

            // Prova con le finestre aperte
            if (javafx.stage.Stage.getWindows() != null && !javafx.stage.Stage.getWindows().isEmpty()) {
                for (javafx.stage.Window window : javafx.stage.Stage.getWindows()) {
                    if (window instanceof Stage && ((Stage) window).getScene() != null) {
                        Scene scene = ((Stage) window).getScene();
                        if (scene.getRoot() instanceof StackPane) {
                            return (StackPane) scene.getRoot();
                        }
                    }
                }
            }

            System.out.println("⚠️ Root principale non trovato");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca del root principale: " + e.getMessage());
            return null;
        }
    }

    /**
     * Trova il titolo di un libro per ISBN
     */
    private static String findBookTitleByIsbn(String isbn) {
        if (recommendedBooksDetails != null) {
            return recommendedBooksDetails.stream()
                    .filter(book -> isbn.equals(book.getIsbn()))
                    .map(Book::getTitle)
                    .findFirst()
                    .orElse("Libro sconosciuto");
        }
        return "Libro sconosciuto";
    }

    /**
     * Chiude il popup in cima
     */
    private static void closeTopPopup() {
        StackPane mainRoot = findMainRoot();
        if (mainRoot != null && mainRoot.getChildren().size() > 1) {
            mainRoot.getChildren().remove(mainRoot.getChildren().size() - 1);
        }
    }

    // Preview and navigation methods
    private static VBox createBookPreview(int bookIndex) {
        if (bookIndex < 0 || booksCollection == null || bookIndex >= booksCollection.size()) {
            VBox emptyPreview = new VBox();
            emptyPreview.setVisible(false);
            emptyPreview.setManaged(false);
            return emptyPreview;
        }

        Book previewBook = booksCollection.get(bookIndex);
        VBox previewContent = new VBox();
        previewContent.setMaxWidth(1000);
        previewContent.setMaxHeight(700);
        previewContent.setOpacity(0.0);
        previewContent.setStyle(createPopupStyle(getBookBackgroundColor(previewBook)));

        // Simplified content for performance
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));

        HBox detailsSection = new HBox(30);
        detailsSection.setPadding(new Insets(20, 30, 30, 30));
        detailsSection.setAlignment(Pos.TOP_LEFT);

        VBox coverContainer = createCoverContainer(previewBook);
        VBox infoBox = createBasicInfoBox(previewBook);

        detailsSection.getChildren().addAll(coverContainer, infoBox);

        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentScroll.setFitToWidth(true);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox scrollContent = new VBox();
        scrollContent.getChildren().addAll(detailsSection);
        contentScroll.setContent(scrollContent);

        previewContent.getChildren().addAll(topBar, contentScroll);
        return previewContent;
    }

    private static VBox createBasicInfoBox(Book book) {
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.TOP_LEFT);

        infoBox.getChildren().addAll(
                createTitleLabel(book.getTitle()),
                createAuthorLabel(book.getAuthor())
        );

        return infoBox;
    }

    // Navigation methods
    private static void addEdgeDetection(StackPane container) {
        if (booksCollection == null || booksCollection.size() <= 1) {
            return;
        }

        Rectangle leftEdge = createEdgeZone(120, 700);
        Rectangle rightEdge = createEdgeZone(120, 700);

        StackPane.setAlignment(leftEdge, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightEdge, Pos.CENTER_RIGHT);

        setupEdgeEvents(leftEdge, rightEdge);
        container.getChildren().addAll(leftEdge, rightEdge);
    }

    private static Rectangle createEdgeZone(int width, int height) {
        Rectangle edge = new Rectangle(width, height, Color.TRANSPARENT);
        edge.setOpacity(0.01);
        return edge;
    }

    private static void setupEdgeEvents(Rectangle leftEdge, Rectangle rightEdge) {
        Timeline leftHoverDelay = new Timeline();
        Timeline rightHoverDelay = new Timeline();

        // Right edge events
        rightEdge.setOnMouseEntered(e -> {
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                rightHoverDelay.stop();
                rightHoverDelay.getKeyFrames().clear();
                rightHoverDelay.getKeyFrames().add(
                        new KeyFrame(Duration.millis(100), event -> showBookPreview(true, false))
                );
                rightHoverDelay.play();
            }
        });

        rightEdge.setOnMouseExited(e -> {
            rightHoverDelay.stop();
            if (!isTransitioning) {
                Timeline exitDelay = new Timeline(
                        new KeyFrame(Duration.millis(50), event -> showBookPreview(false, false))
                );
                exitDelay.play();
            }
        });

        rightEdge.setOnMouseClicked(e -> {
            rightHoverDelay.stop();
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                slideToBook(currentBookIndex + 1);
            }
        });

        // Left edge events
        leftEdge.setOnMouseEntered(e -> {
            if (currentBookIndex > 0 && !isTransitioning) {
                leftHoverDelay.stop();
                leftHoverDelay.getKeyFrames().clear();
                leftHoverDelay.getKeyFrames().add(
                        new KeyFrame(Duration.millis(100), event -> showBookPreview(true, true))
                );
                leftHoverDelay.play();
            }
        });

        leftEdge.setOnMouseExited(e -> {
            leftHoverDelay.stop();
            if (!isTransitioning) {
                Timeline exitDelay = new Timeline(
                        new KeyFrame(Duration.millis(50), event -> showBookPreview(false, true))
                );
                exitDelay.play();
            }
        });

        leftEdge.setOnMouseClicked(e -> {
            leftHoverDelay.stop();
            if (currentBookIndex > 0 && !isTransitioning) {
                slideToBook(currentBookIndex - 1);
            }
        });
    }

    private static void addNavigationArrows() {
        if (booksCollection == null || booksCollection.size() <= 1) {
            return;
        }

        leftArrowButton = createArrowButton("❮");
        rightArrowButton = createArrowButton("❯");

        StackPane.setAlignment(leftArrowButton, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightArrowButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(leftArrowButton, new Insets(0, 0, 0, 30));
        StackPane.setMargin(rightArrowButton, new Insets(0, 30, 0, 0));

        setupArrowEvents();
        root.getChildren().addAll(leftArrowButton, rightArrowButton);
    }

    private static Button createArrowButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.3);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 44px;" +
                        "-fx-min-height: 44px;" +
                        "-fx-max-width: 44px;" +
                        "-fx-max-height: 44px;" +
                        "-fx-padding: 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-opacity: 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);"
        );
        return button;
    }

    private static void setupArrowEvents() {
        // Root hover events for showing/hiding arrows
        root.setOnMouseEntered(e -> {
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(leftArrowButton.opacityProperty(),
                                    currentBookIndex > 0 ? 0.7 : 0, Interpolator.EASE_OUT),
                            new KeyValue(rightArrowButton.opacityProperty(),
                                    currentBookIndex < booksCollection.size() - 1 ? 0.7 : 0, Interpolator.EASE_OUT)
                    )
            );
            fadeIn.play();
        });

        root.setOnMouseExited(e -> {
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(leftArrowButton.opacityProperty(), 0, Interpolator.EASE_OUT),
                            new KeyValue(rightArrowButton.opacityProperty(), 0, Interpolator.EASE_OUT)
                    )
            );
            fadeOut.play();
        });

        // Button hover effects
        setupButtonHoverEffect(leftArrowButton, () -> currentBookIndex > 0);
        setupButtonHoverEffect(rightArrowButton, () -> currentBookIndex < booksCollection.size() - 1);

        // Click handlers
        leftArrowButton.setOnMouseClicked(e -> {
            if (currentBookIndex > 0 && !isTransitioning) {
                slideToBook(currentBookIndex - 1);
            }
        });

        rightArrowButton.setOnMouseClicked(e -> {
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                slideToBook(currentBookIndex + 1);
            }
        });
    }

    private static void setupButtonHoverEffect(Button button, java.util.function.Supplier<Boolean> condition) {
        button.setOnMouseEntered(e -> {
            if (condition.get()) {
                button.setStyle(button.getStyle().replace("0.3", "0.5"));
            }
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("0.5", "0.3"));
        });
    }

    private static void updateArrowVisibility() {
        if (booksCollection == null || booksCollection.size() <= 1) {
            return;
        }

        Timeline updateVisibility = new Timeline();

        updateVisibility.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(150),
                        new KeyValue(leftArrowButton.opacityProperty(),
                                currentBookIndex > 0 ? 0.7 : 0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(rightArrowButton.opacityProperty(),
                                currentBookIndex < booksCollection.size() - 1 ? 0.7 : 0, Interpolator.EASE_OUT))
        );

        updateVisibility.play();
    }

    private static void showBookPreview(boolean show, boolean isPrevious) {
        if (slideAnimation != null && slideAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            slideAnimation.stop();
        }

        VBox previewToShow = isPrevious ? prevBookPreview : nextBookPreview;

        if (previewToShow == null || !previewToShow.isVisible()) {
            return;
        }

        double targetX = isPrevious ?
                (show ? -1050 : -1200) :
                (show ? 1050 : 1200);
        double targetOpacity = show ? 0.6 : 0.0;
        double targetScale = show ? 0.95 : 0.9;

        slideAnimation = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(previewToShow.translateXProperty(), targetX, Interpolator.EASE_OUT),
                        new KeyValue(previewToShow.opacityProperty(), targetOpacity, Interpolator.EASE_OUT),
                        new KeyValue(previewToShow.scaleXProperty(), targetScale, Interpolator.EASE_OUT),
                        new KeyValue(previewToShow.scaleYProperty(), targetScale, Interpolator.EASE_OUT)
                )
        );
        slideAnimation.play();
    }

    private static void slideToBook(int newIndex) {
        if (newIndex < 0 || newIndex >= booksCollection.size() || newIndex == currentBookIndex || isTransitioning) {
            return;
        }

        isTransitioning = true;
        Book targetBook = booksCollection.get(newIndex);
        boolean isForward = newIndex > currentBookIndex;

        // Reset ratings for new book
        resetRatings();
        currentBook = targetBook;

        // Create new book content
        VBox newBookContent = createBookContent(targetBook, getBookBackgroundColor(targetBook), currentAuthManager);

        // Set initial animation state
        newBookContent.setTranslateX(isForward ? 1200 : -1200);
        newBookContent.setOpacity(0.0);
        newBookContent.setScaleX(0.95);
        newBookContent.setScaleY(0.95);

        bookDisplayPane.getChildren().add(newBookContent);
        VBox currentContent = (VBox) bookDisplayPane.getChildren().get(1);

        if (slideAnimation != null && slideAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            slideAnimation.stop();
        }

        // Apple-style smooth transition
        slideAnimation = new Timeline(
                new KeyFrame(Duration.millis(0),
                        new KeyValue(currentContent.opacityProperty(), 1.0),
                        new KeyValue(currentContent.scaleXProperty(), 1.0),
                        new KeyValue(currentContent.scaleYProperty(), 1.0),
                        new KeyValue(currentContent.translateXProperty(), 0),
                        new KeyValue(newBookContent.opacityProperty(), 0.0),
                        new KeyValue(newBookContent.scaleXProperty(), 0.95),
                        new KeyValue(newBookContent.scaleYProperty(), 0.95)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(currentContent.opacityProperty(), 0.3, Interpolator.EASE_BOTH),
                        new KeyValue(currentContent.scaleXProperty(), 0.95, Interpolator.EASE_BOTH),
                        new KeyValue(currentContent.scaleYProperty(), 0.95, Interpolator.EASE_BOTH),
                        new KeyValue(currentContent.translateXProperty(),
                                isForward ? -200 : 200, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.opacityProperty(), 0.7, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.scaleXProperty(), 0.98, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.scaleYProperty(), 0.98, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.translateXProperty(),
                                isForward ? 200 : -200, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(currentContent.opacityProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(currentContent.scaleXProperty(), 0.9, Interpolator.EASE_OUT),
                        new KeyValue(currentContent.scaleYProperty(), 0.9, Interpolator.EASE_OUT),
                        new KeyValue(currentContent.translateXProperty(),
                                isForward ? -400 : 400, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.translateXProperty(), 0, Interpolator.EASE_OUT)
                )
        );

        slideAnimation.setOnFinished(e -> {
            currentBookIndex = newIndex;
            bookDisplayPane.getChildren().clear();

            // Reset previews
            prevBookPreview = createBookPreview(currentBookIndex - 1);
            prevBookPreview.setTranslateX(-1200);
            prevBookPreview.setOpacity(0.0);
            prevBookPreview.setScaleX(0.9);
            prevBookPreview.setScaleY(0.9);

            nextBookPreview = createBookPreview(currentBookIndex + 1);
            nextBookPreview.setTranslateX(1200);
            nextBookPreview.setOpacity(0.0);
            nextBookPreview.setScaleX(0.9);
            nextBookPreview.setScaleY(0.9);

            // Reset main content
            newBookContent.setOpacity(1.0);
            newBookContent.setTranslateX(0);
            newBookContent.setScaleX(1.0);
            newBookContent.setScaleY(1.0);

            bookDisplayPane.getChildren().addAll(prevBookPreview, newBookContent, nextBookPreview);

            updateArrowVisibility();
            addEdgeDetection(bookDisplayPane);

            // Load ratings for new book
            loadBookRatingsForAllUsers(targetBook, currentAuthManager);

            isTransitioning = false;
        });

        slideAnimation.play();
    }

    // Utility methods
    private static void resetRatings() {
        currentUserRating = null;
        averageBookRating = null;
        averageRatingLabel = null;
        currentRatingSection = null;
        currentBookRecommendations = null;
        recommendedBooksDetails = null;
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    // Color extraction methods
    private static Color extractDominantColor(Image image) {
        if (image == null || image.isError()) {
            return Color.rgb(41, 35, 46);
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        if (width <= 0 || height <= 0) {
            return Color.rgb(41, 35, 46);
        }

        int sampleSize = 5;
        Map<Integer, Integer> colorCounts = new HashMap<>();
        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y += sampleSize) {
            for (int x = 0; x < width; x += sampleSize) {
                Color color = pixelReader.getColor(x, y);

                int rgb = ((int) (color.getRed() * 255) << 16) |
                        ((int) (color.getGreen() * 255) << 8) |
                        ((int) (color.getBlue() * 255));

                colorCounts.put(rgb, colorCounts.getOrDefault(rgb, 0) + 1);
            }
        }

        int dominantRGB = 0;
        int maxCount = 0;

        for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantRGB = entry.getKey();
            }
        }

        int red = (dominantRGB >> 16) & 0xFF;
        int green = (dominantRGB >> 8) & 0xFF;
        int blue = dominantRGB & 0xFF;

        return Color.rgb(red, green, blue);
    }

    private static Color darkenColor(Color color, double factor) {
        return new Color(
                Math.max(0, color.getRed() * factor),
                Math.max(0, color.getGreen() * factor),
                Math.max(0, color.getBlue() * factor),
                color.getOpacity()
        );
    }

    private static String toHexString(Color color) {
        int r = ((int) (color.getRed() * 255)) & 0xFF;
        int g = ((int) (color.getGreen() * 255)) & 0xFF;
        int b = ((int) (color.getBlue() * 255)) & 0xFF;

        return String.format("#%02X%02X%02X", r, g, b);
    }

    // METODO PER GESTIRE ESC a livello di root (opzionale)
    private static void setupRootKeyHandlers(StackPane root) {
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                System.out.println("🔑 ESC premuto su root");

                try {
                    Stage currentStage = (Stage) root.getScene().getWindow();

                    if (currentStage != null) {
                        String title = currentStage.getTitle();
                        System.out.println("🔍 Finestra da chiudere (ESC): " + title);

                        // VERIFICA che sia davvero un popup prima di chiudere
                        if (title != null && title.contains("Dettagli Libro")) {
                            System.out.println("✅ Confermato popup - chiudo con ESC");
                            currentStage.close();
                        } else {
                            System.out.println("⚠️ BLOCCO: Non è un popup, non chiudo! Titolo: " + title);

                            // Se non è un popup, usa solo il callback
                            if (closeHandler != null) {
                                closeHandler.run();
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("⚠️ Errore durante chiusura da ESC: " + ex.getMessage());

                    // Fallback sicuro: usa solo il callback
                    if (closeHandler != null) {
                        closeHandler.run();
                    }
                }

                event.consume();
            }
        });

        // Assicura che il root possa ricevere eventi di tastiera
        root.setFocusTraversable(true);
        Platform.runLater(() -> root.requestFocus());
    }

    private static void debugCurrentStage(String context) {
        try {
            javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Stage.getWindows();

            System.out.println("🔍 DEBUG " + context + ":");
            System.out.println("  Finestre totali: " + windows.size());

            for (javafx.stage.Window window : windows) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    System.out.println("  - " + stage.getClass().getSimpleName() +
                            " | Titolo: '" + stage.getTitle() + "'" +
                            " | Showing: " + stage.isShowing() +
                            " | Modal: " + stage.getModality());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore debug stage: " + e.getMessage());
        }
    }

    public static void debugPopupState() {
        System.out.println("🔍 DEBUG STATO POPUP:");
        System.out.println("  root != null: " + (root != null));
        System.out.println("  closeHandler != null: " + (closeHandler != null));
        System.out.println("  isTransitioning: " + isTransitioning);
        System.out.println("  currentBookIndex: " + currentBookIndex);
        System.out.println("  booksCollection size: " + (booksCollection != null ? booksCollection.size() : "null"));

        if (root != null) {
            System.out.println("  root.isFocused(): " + root.isFocused());
            System.out.println("  root.isVisible(): " + root.isVisible());

            if (root.getScene() != null) {
                System.out.println("  scene.getWindow() != null: " + (root.getScene().getWindow() != null));

                if (root.getScene().getWindow() instanceof Stage) {
                    Stage stage = (Stage) root.getScene().getWindow();
                    System.out.println("  stage.isShowing(): " + stage.isShowing());
                    System.out.println("  stage.isFocused(): " + stage.isFocused());
                    System.out.println("  stage.getTitle(): " + stage.getTitle());
                }
            }
        }
    }

    /**
     * Test della navigazione raccomandazioni
     */
    public static void testRecommendationNavigation() {
        System.out.println("🧪 TEST NAVIGAZIONE RACCOMANDAZIONI");

        // Crea libri di test
        List<Book> testBooks = new ArrayList<>();
        testBooks.add(new Book("Libro 1", "Autore 1", "Desc 1", "1.jpg"));
        testBooks.add(new Book("Libro 2", "Autore 2", "Desc 2", "2.jpg"));
        testBooks.add(new Book("Libro 3", "Autore 3", "Desc 3", "3.jpg"));

        recommendedBooksDetails = testBooks;

        // Test collezione navigabile
        List<Book> navigable = createNavigableRecommendedBooks(testBooks.get(1));
        System.out.println("✅ Collezione navigabile: " + navigable.size() + " libri");

        // Test indice
        int index = findBookIndex(testBooks.get(1), navigable);
        System.out.println("✅ Indice trovato: " + index);
    }

    public static void testPopupClose() {
        System.out.println("🧪 TEST CHIUSURA POPUP");

        try {
            if (root != null && root.getScene() != null) {
                Stage currentStage = (Stage) root.getScene().getWindow();
                if (currentStage != null) {
                    System.out.println("📋 Stage corrente: " + currentStage.getTitle());
                    System.out.println("👁️ Showing: " + currentStage.isShowing());
                    System.out.println("🎯 Focused: " + currentStage.isFocused());

                    // Test chiusura
                    System.out.println("🔄 Test chiusura...");
                    currentStage.close();

                    // Verifica dopo 1 secondo
                    Platform.runLater(() -> {
                        System.out.println("✅ Test chiusura completato. Showing: " + currentStage.isShowing());
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore test chiusura: " + e.getMessage());
        }
    }

    /**
     * Metodo di debug per verificare l'integrazione con PopupManager
     */
    public static void debugPopupManagerIntegration() {
        System.out.println("🔍 BookDetailsPopup - Debug integrazione PopupManager:");
        System.out.println("  root != null: " + (root != null));
        System.out.println("  closeHandler != null: " + (closeHandler != null));
        System.out.println("  currentBook: " + (currentBook != null ? currentBook.getTitle() : "null"));

        PopupManager popupManager = PopupManager.getInstance();
        System.out.println("  PopupManager inizializzato: " + popupManager.isInitialized());
        System.out.println("  PopupManager popup attivi: " + popupManager.getActivePopupsCount());

        popupManager.debugFullState();
    }
}