package com.mycompany.mavenproject1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Circle;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Clone di Apple Books implementato in JavaFX.
 * Questa classe riproduce l'interfaccia utente e le funzionalità principali
 * dell'applicazione Apple Books originale.
 */
public class AppleBooksClone extends Application {

    /**
     * Crea un ImageView sicuro che gestisce gli errori di caricamento delle immagini.
     * 
     * @param isbnFileName nome del file immagine
     * @param width larghezza desiderata
     * @param height altezza desiderata
     * @return ImageView configurato correttamente
     */
    public static ImageView createSafeImageView(String isbnFileName, double width, double height) {
        Image image;
        try {
            // Carica l'immagine dalla risorsa
            InputStream stream = AppleBooksClone.class.getResourceAsStream("/books_covers/" + isbnFileName);
            if (stream == null) {
                throw new IllegalArgumentException("File non trovato: " + isbnFileName);
            }
            image = new Image(stream, width, height, true, true);
            if (image.isError()) {
                throw new Exception("Errore nel caricamento dell'immagine.");
            }
        } catch (Exception e) {
            // Fallback su un'immagine placeholder in caso di errore
            System.err.println("Errore caricamento immagine, usando placeholder: " + isbnFileName);
            image = new Image("https://via.placeholder.com/100x150", width, height, true, true);
        }

        ImageView imageView = new ImageView(image);
        
        // Aggiungi un effetto di ombra per simulare lo stile Apple Books
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setOffsetY(3);
        shadow.setRadius(5);
        imageView.setEffect(shadow);
        
        return imageView;
    }

    /**
     * Crea una sezione di libri con stile Apple Books.
     * 
     * @param sectionTitle titolo della sezione
     * @param books lista di libri da visualizzare
     * @param onBookClick azione da eseguire al click su un libro
     * @return container VBox configurato con la sezione
     */
    private VBox createBookSection(String sectionTitle, List<Book> books, Consumer<Book> onBookClick) {
        // Titolo della sezione con stile Apple
        Label title = new Label(sectionTitle);
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        FlowPane bookGrid = new FlowPane(); // Griglia flessibile per i libri
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(10));
        bookGrid.setPrefWrapLength(750); // Larghezza preferita prima di andare a capo

        for (Book book : books) {
            VBox bookBox = new VBox(8); // Spaziatura aumentata tra cover e titolo
            bookBox.setAlignment(Pos.TOP_CENTER);
            bookBox.setMaxWidth(120);

            // Creazione della copertina del libro con angoli leggermente arrotondati
            ImageView cover = createSafeImageView(book.imageUrl, 110, 165);
            
            // Clip con angoli arrotondati per la copertina
            Rectangle clip = new Rectangle(110, 165);
            clip.setArcWidth(8);
            clip.setArcHeight(8);
            cover.setClip(clip);
            
            // Aggiunge un evento di click sulla copertina
            cover.setOnMouseClicked(e -> onBookClick.accept(book));
            cover.setStyle("-fx-cursor: hand;");

            // Titolo del libro in stile Apple (più leggibile)
            Label bookTitle = new Label(book.title);
            bookTitle.setWrapText(true);
            bookTitle.setMaxWidth(110);
            bookTitle.setTextFill(Color.WHITE);
            bookTitle.setFont(Font.font("SF Pro Text", FontWeight.NORMAL, 13));
            
            // Autore del libro in stile Apple (grigio e più piccolo)
            Label bookAuthor = new Label(book.author);
            bookAuthor.setWrapText(true);
            bookAuthor.setMaxWidth(110);
            bookAuthor.setTextFill(Color.gray(0.7));
            bookAuthor.setFont(Font.font("SF Pro Text", FontWeight.NORMAL, 12));

            bookBox.getChildren().addAll(cover, bookTitle, bookAuthor);
            bookGrid.getChildren().add(bookBox);
        }

        // ScrollPane per scorrere i libri con stile Apple (barra sottile)
        ScrollPane scroll = new ScrollPane(bookGrid);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(280); // Altezza aumentata per mostrare due righe
        scroll.getStyleClass().add("edge-to-edge");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Pulsante "Vedi tutti" in stile Apple
        Button seeAllBtn = new Button("Vedi tutti");
        seeAllBtn.getStyleClass().add("see-all-button");
        
        // Header con titolo e pulsante "Vedi tutti"
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, seeAllBtn);

        // Container della sezione con stile Apple Books
        VBox section = new VBox(5, headerBox, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));
        section.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
            "-fx-background-radius: 12;"
        );
        
        return section;
    }

    /**
     * Crea una sezione di categorie in stile Apple Books.
     * 
     * @param sectionTitle titolo della sezione
     * @param categories lista di categorie da visualizzare
     * @return container VBox configurato con la sezione
     */
    private VBox createCategorySection(String sectionTitle, List<Category> categories) {
        // Titolo della sezione con stile Apple
        Label title = new Label(sectionTitle);
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        // Pulsante "Vedi tutti" in stile Apple
        Button seeAllBtn = new Button("Vedi tutti");
        seeAllBtn.getStyleClass().add("see-all-button");
        
        // Header con titolo e pulsante "Vedi tutti"
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, seeAllBtn);

        HBox categoryRow = new HBox(20);
        categoryRow.setPadding(new Insets(10));
        categoryRow.setAlignment(Pos.CENTER_LEFT);

        for (Category category : categories) {
            // Creazione dell'immagine della categoria con qualità migliorata
            Image image = new Image(category.getImageUrl(), 290, 155, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(290);
            imageView.setFitHeight(155);
            imageView.setSmooth(true);

            // Aggiunge un clip con angoli arrotondati più pronunciati
            Rectangle clip = new Rectangle(290, 155);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            imageView.setClip(clip);

            // Testo di categoria sovrapposto all'immagine
            Label categoryLabel = new Label(category.getName());
            categoryLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
            categoryLabel.setTextFill(Color.WHITE);
            categoryLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
            
            // Contenitore per la card della categoria
            StackPane card = new StackPane(imageView, categoryLabel);
            StackPane.setAlignment(categoryLabel, Pos.BOTTOM_LEFT);
            StackPane.setMargin(categoryLabel, new Insets(0, 0, 15, 15));
            card.setPrefSize(290, 155);
            card.setMaxSize(290, 155);
            card.setStyle(
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0.3, 0, 3);" +
                "-fx-cursor: hand;"
            );

            categoryRow.getChildren().add(card);
        }

        // ScrollPane per scorrere le categorie con stile Apple
        ScrollPane scroll = new ScrollPane(categoryRow);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToHeight(true);
        scroll.setPrefHeight(185);
        scroll.getStyleClass().add("edge-to-edge");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox section = new VBox(10, headerBox, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));
        section.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
            "-fx-background-radius: 12;"
        );

        return section;
    }

    /**
     * Recupera i libri dal database
     * 
     * @return lista di oggetti Book recuperati dal database
     */
    private List<Book> fetchBooksFromDB() {
        List<Book> books = new ArrayList<>();
        String url = "jdbc:postgresql://localhost:5432/DataProva";
        String user = "postgres";
        String password = "postgress";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT \"isbn\", \"book_author\", \"description\" FROM books";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    String title = rs.getString("isbn");
                    String author = rs.getString("book_author");
                    String description = rs.getString("description");

                    // Costruisce il nome del file immagine
                    String fileName = title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg";

                    books.add(new Book(title, author, description, fileName));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero dei libri dal database:");
            e.printStackTrace();
            
            // Aggiungi alcuni libri di esempio se il database non è disponibile
            books.add(new Book("Il Nome della Rosa", "Umberto Eco", 
                "Un affascinante thriller medievale ambientato in un'abbazia benedettina.", "placeholder.jpg"));
            books.add(new Book("1984", "George Orwell", 
                "Un romanzo distopico sul totalitarismo e la sorveglianza di massa.", "placeholder.jpg"));
            books.add(new Book("Il Piccolo Principe", "Antoine de Saint-Exupéry", 
                "Una fiaba poetica che ha conquistato il cuore di lettori di tutte le età.", "placeholder.jpg"));
        }
        return books;
    }

    @Override
    public void start(Stage stage) {
        // Carica i font di sistema che più si avvicinano ai font Apple
        try {
            Font.loadFont(getClass().getResource("/fonts/NewYorkExtraLarge-Heavy.otf").toExternalForm(), 14);
            Font.loadFont(getClass().getResource("/fonts/NewYorkLarge-Medium.otf").toExternalForm(), 14);
        } catch (Exception e) {
            System.err.println("Impossibile caricare i font personalizzati");
        }

        BorderPane root = new BorderPane(); // Layout principale

        // === SIDEBAR con stile migliorato ===
        VBox sidebar = new VBox(15);
        sidebar.setId("sidebar");
        sidebar.setPrefWidth(200); // Leggermente più larga
        sidebar.setPrefHeight(700);
        sidebar.setStyle("-fx-background-color: #2c2c2e;");

        // Intestazione sidebar
        Label sidebarHeader = new Label("Libreria");
        sidebarHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        sidebarHeader.setTextFill(Color.WHITE);
        sidebarHeader.setPadding(new Insets(20, 0, 5, 20));

        // Menu items con icone
        VBox menuItemsBox = new VBox(5);
        menuItemsBox.setPadding(new Insets(10, 0, 0, 15));
        menuItemsBox.setStyle("-fx-background-color: transparent;");

        String[] menuItems = {"Home", "Book Store", "Audiobook Store", "Tutto", "Da leggere", "Letti", "PDF"};
        String[] menuIcons = {"home", "store", "audio", "books", "reading", "read", "pdf"};
        
        for (int i = 0; i < menuItems.length; i++) {
            HBox itemBox = new HBox(10);
            
            // In un'implementazione reale, caricheresti icone vere
            Label iconPlaceholder = new Label("•");
            iconPlaceholder.setTextFill(Color.LIGHTGRAY);
            
            Label label = new Label(menuItems[i]);
            label.setTextFill(Color.LIGHTGRAY);
            label.setFont(Font.font("SF Pro Text", 14));
            
            itemBox.getChildren().addAll(iconPlaceholder, label);
            itemBox.setAlignment(Pos.CENTER_LEFT);
            itemBox.setPadding(new Insets(5, 10, 5, 10));
            itemBox.getStyleClass().add("menu-item-box");
            itemBox.setStyle("-fx-cursor: hand;");
            
            menuItemsBox.getChildren().add(itemBox);
        }

        // Seleziona il primo elemento come attivo
        ((HBox)menuItemsBox.getChildren().get(0)).setStyle(
            "-fx-background-color: #3a3a3c; -fx-background-radius: 5; -fx-cursor: hand;");
        ((Label)((HBox)menuItemsBox.getChildren().get(0)).getChildren().get(1)).setTextFill(Color.WHITE);

        menuItemsBox.setAlignment(Pos.TOP_LEFT);

        // Avatar dell'utente migliorato
        Image avatarImage = new Image(getClass().getResource("/logReg/white_logo.png").toExternalForm());
        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(36);
        avatar.setFitHeight(36);
        avatar.setId("avatar");
        
        // Clip circolare più definito
        Circle clip = new Circle(18, 18, 18);
        avatar.setClip(clip);
        
        // Etichetta con il nome utente
        Label userName = new Label("Utente");
        userName.setTextFill(Color.WHITE);
        userName.setFont(Font.font("SF Pro Text", 13));
        
        // Avatar container
        HBox avatarBox = new HBox(12);
        avatarBox.getChildren().addAll(avatar, userName);
        avatarBox.setAlignment(Pos.CENTER_LEFT);
        avatarBox.setPadding(new Insets(10, 15, 15, 20));
        avatarBox.setStyle("-fx-cursor: hand;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(sidebarHeader, menuItemsBox, spacer, avatarBox);
        root.setLeft(sidebar);

        // === HEADER migliorato ===
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #1e1e1e;");
        
        Label header = new Label("Book Store");
        header.setId("header");
        header.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        header.setTextFill(Color.WHITE);
        
        // Aggiunta barra di ricerca
        TextField searchField = new TextField();
        searchField.setPromptText("Cerca");
        searchField.setPrefWidth(240);
        searchField.getStyleClass().add("search-field");
        searchField.setStyle(
            "-fx-background-color: #3a3a3c;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: gray;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12;" 
        );
        
        headerBox.getChildren().add(header);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(headerSpacer, searchField);

        // === CONTENUTO CENTRALE migliorato ===
        VBox content = new VBox(20);
        content.setId("content");
        content.setPadding(new Insets(15, 20, 30, 20));
        content.setStyle("-fx-background-color: #1e1e1e;");

        List<Book> books = fetchBooksFromDB();
        int splitIndex = Math.min(8, books.size());

        // Categorie con immagini più curate
        List<Category> categories = List.of(
            new Category("Thriller", "/categories/Thriller_Gialli.jpg", getClass().getResource("/categories/Thriller_Gialli.jpg").toExternalForm()),
            new Category("Romance", "/categories/Romanzi_Rosa.jpg", getClass().getResource("/categories/Romanzi_Rosa.jpg").toExternalForm()),
            new Category("Horror", "/categories/Narrativa_Letteratura.jpg", getClass().getResource("/categories/Narrativa_Letteratura.jpg").toExternalForm()),
            new Category("Sci-Fi", "/categories/Saggistica.jpg", getClass().getResource("/categories/Saggistica.jpg").toExternalForm()),
            new Category("Fantasy", "/categories/Fantascienza_Fantasy.jpg", getClass().getResource("/categories/Fantascienza_Fantasy.jpg").toExternalForm())
        );

        // === OVERLAY PANNELLO DETTAGLI LIBRO ===
        StackPane mainStack = new StackPane();
        VBox centerBox = new VBox(headerBox, new ScrollPane(content));
        centerBox.setStyle("-fx-background-color: #1e1e1e;");
        mainStack.getChildren().add(centerBox);

        Consumer<Book> clickHandler = selectedBook -> {
            BoxBlur blur = new BoxBlur(30, 30, 3);
            centerBox.setEffect(blur);

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
            overlay.setPickOnBounds(true);
            overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Creare una chiusura da usare per onClose
            Runnable closePopup = () -> {
                mainStack.getChildren().remove(overlay);
                centerBox.setEffect(null);
            };

            // Modificato per passare tre parametri: libro selezionato, collection completa e chiusura
            StackPane detailsPane = BookDetailsPopup.create(selectedBook, books, closePopup);
            
            overlay.setOnMouseClicked(e -> {
                if (!detailsPane.getBoundsInParent().contains(e.getX(), e.getY())) {
                    closePopup.run();
                }
            });

            overlay.getChildren().add(detailsPane);
            StackPane.setAlignment(detailsPane, Pos.CENTER);
            mainStack.getChildren().add(overlay);
        };

        // Sezioni principali
        VBox featuredBooks = createFeatureSection(books.subList(0, Math.min(1, books.size())));
        
        // === AGGIUNGI SEZIONI AL CONTENUTO ===
        content.getChildren().addAll(
            featuredBooks,
            createBookSection("Libri gratuiti", books.subList(0, splitIndex), clickHandler),
            createBookSection("Nuove uscite", books.subList(splitIndex > 0 ? splitIndex-1 : 0, books.size()), clickHandler),
            createCategorySection("Scopri per genere", categories)
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.getStyleClass().add("edge-to-edge");
        centerBox.getChildren().set(1, scrollPane);
        root.setCenter(mainStack);

        // === ACCOUNT POPUP ===
        Stage accountStage = new Stage();
        accountStage.initStyle(StageStyle.DECORATED);
        accountStage.initModality(Modality.NONE);
        accountStage.setTitle("Account");
        accountStage.setMinWidth(320);
        accountStage.setMinHeight(280);

        AccountPanel accountPanel = new AccountPanel();
        Scene accountScene = new Scene(accountPanel, 320, 280);
        accountStage.setScene(accountScene);

        avatar.setOnMouseClicked(e -> {
            if (accountStage.isShowing()) {
                accountStage.hide();
            } else {
                accountStage.show();
            }
        });

        // === SCENA PRINCIPALE ===
        Scene scene = new Scene(root, 1100, 750);
        scene.getStylesheets().add(getClass().getResource("/css/scrollbar.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        // Aggiungi CSS personalizzato
        String customCss = 
            ".see-all-button { -fx-background-color: transparent; -fx-text-fill: #0a84ff; -fx-border-color: transparent; }" +
            ".edge-to-edge { -fx-background-insets: 0; -fx-padding: 0; }" +
            ".menu-item-box:hover { -fx-background-color: #3a3a3c; -fx-background-radius: 5; }";
        scene.getStylesheets().add("data:text/css," + customCss.replaceAll(" ", "%20"));
        
        stage.setScene(scene);
        stage.setTitle("Apple Books");
        stage.show();
    }
    
    /**
     * Crea una sezione evidenziata con libro in primo piano.
     * 
     * @param books lista con un libro da evidenziare
     * @return VBox contenente la sezione
     */
    private VBox createFeatureSection(List<Book> books) {
        if (books.isEmpty()) return new VBox();
        
        Book featuredBook = books.get(0);
        
        // Container principale
        HBox featuredBox = new HBox(20);
        featuredBox.setPadding(new Insets(20));
        featuredBox.setAlignment(Pos.CENTER_LEFT);
        
        // Copertina grande
        ImageView cover = createSafeImageView(featuredBook.imageUrl, 180, 270);
        Rectangle clip = new Rectangle(180, 270);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        cover.setClip(clip);
        
        // Info del libro
        VBox infoBox = new VBox(12);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("IN EVIDENZA");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
        title.setTextFill(Color.gray(0.7));
        
        Label bookTitle = new Label(featuredBook.title);
        bookTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 26));
        bookTitle.setTextFill(Color.WHITE);
        bookTitle.setWrapText(true);
        
        Label authorLabel = new Label(featuredBook.author);
        authorLabel.setFont(Font.font("SF Pro Text", 16));
        authorLabel.setTextFill(Color.gray(0.8));
        
        // Pulsanti azione
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button getButton = new Button("OTTIENI");
        getButton.setStyle(
            "-fx-background-color: #0a84ff;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 10 25;"
        );
        
        Button sampleButton = new Button("CAMPIONE");
        sampleButton.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 10 25;"
        );
        
        buttonBox.getChildren().addAll(getButton, sampleButton);
        
        infoBox.getChildren().addAll(title, bookTitle, authorLabel, buttonBox);
        featuredBox.getChildren().addAll(cover, infoBox);
        
        // Estratto dalla descrizione
        Label previewLabel = new Label("ANTEPRIMA");
        previewLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
        previewLabel.setTextFill(Color.gray(0.7));
        previewLabel.setPadding(new Insets(0, 0, 8, 20));
        
        String descriptionPreview = featuredBook.getDescription();
        if (descriptionPreview.length() > 150) {
            descriptionPreview = descriptionPreview.substring(0, 150) + "...";
        }
        
        Label description = new Label(descriptionPreview);
        description.setWrapText(true);
        description.setTextFill(Color.WHITE);
        description.setPadding(new Insets(0, 20, 20, 20));
        
        VBox container = new VBox();
        container.getChildren().addAll(featuredBox, previewLabel, description);
        container.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3a3a3c, #2c2c2c);" +
            "-fx-background-radius: 12;"
        );
        
        return container;
    }

    /**
     * Metodo main per avviare l'applicazione
     */
    public static void main(String[] args) {
        launch(args);
    }
}