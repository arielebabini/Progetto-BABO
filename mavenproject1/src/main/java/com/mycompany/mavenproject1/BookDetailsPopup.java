package com.mycompany.mavenproject1;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

public class BookDetailsPopup {
    
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
    
    public static StackPane create(Book book, List<Book> collection, Runnable onClose) {
        // Store the book collection for pagination
        booksCollection = collection;
        closeHandler = onClose;
        
        // Find current book index
        currentBookIndex = collection.indexOf(book);
        if (currentBookIndex == -1) currentBookIndex = 0;
        
        // MAIN CONTAINER
        root = new StackPane();
        
        // Load the cover image for color extraction
        Image coverImage = loadCoverImage(book.imageUrl);
        Color dominantColor = extractDominantColor(coverImage);
        Color darkenedColor = darkenColor(dominantColor, 0.7);
        
        String backgroundColor = toHexString(darkenedColor);
        
        // --- BACKGROUND ---
        StackPane blurLayer = new StackPane();
        blurLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        blurLayer.setEffect(new BoxBlur(20, 20, 3));
        blurLayer.setOnMouseClicked((MouseEvent e) -> {
            // Close popup when clicking on background
            if (closeHandler != null) {
                closeHandler.run();
            }
        });
        
        // Book display pane (for current and book previews sliding)
        bookDisplayPane = new StackPane();
        
        // --- CURRENT BOOK CONTAINER ---
        VBox currentBookContent = createBookContent(book, backgroundColor);
        
        // --- PREVIEW CONTAINERS ---
        nextBookPreview = createBookPreview(currentBookIndex + 1);
        nextBookPreview.setTranslateX(1200); // Start off-screen right
        
        prevBookPreview = createBookPreview(currentBookIndex - 1);
        prevBookPreview.setTranslateX(-1200); // Start off-screen left
        
        // Add books to the display pane
        bookDisplayPane.getChildren().addAll(prevBookPreview, currentBookContent, nextBookPreview);
        
        // Add edge detection zones for previewing
        addEdgeDetection(bookDisplayPane);
        
        // Add navigation controls
        addNavigationArrows();
        
        // Add everything to root container
        root.getChildren().addAll(blurLayer, bookDisplayPane);
        
        return root;
    }
    
    private static VBox createBookContent(Book book, String backgroundColor) {
        // --- POPUP CONTAINER ---
        VBox popupContent = new VBox();
        popupContent.setMaxWidth(1000);
        popupContent.setMaxHeight(700);
        popupContent.setStyle(
            "-fx-background-color: " + backgroundColor + ";" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);"
        );
        
        // --- TOP BAR WITH CLOSE BUTTON ---
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button closeButton = new Button("×");
        closeButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #999999;" +
            "-fx-font-size: 20;" +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> {
            // Execute close handler
            if (closeHandler != null) {
                closeHandler.run();
            }
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("+");
        addButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #999999;" +
            "-fx-font-size: 20;" +
            "-fx-cursor: hand;"
        );
        
        Button shareButton = new Button("⇧");
        shareButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #999999;" +
            "-fx-font-size: 16;" +
            "-fx-cursor: hand;"
        );
        
        Button moreButton = new Button("⋯");
        moreButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #999999;" +
            "-fx-font-size: 16;" +
            "-fx-cursor: hand;"
        );
        
        topBar.getChildren().addAll(closeButton, spacer, addButton, shareButton, moreButton);
        
        // --- BOOK DETAILS SECTION ---
        HBox detailsSection = new HBox(30);
        detailsSection.setPadding(new Insets(20, 30, 30, 30));
        detailsSection.setAlignment(Pos.TOP_LEFT);
        
        // Book cover with rounded corners
        ImageView cover = createSafeImageView(book.imageUrl, 180, 270);
        Rectangle coverClip = new Rectangle(180, 270);
        coverClip.setArcWidth(8);
        coverClip.setArcHeight(8);
        cover.setClip(coverClip);
        
        VBox coverContainer = new VBox(cover);
        coverContainer.setAlignment(Pos.TOP_CENTER);
        
        // Book info
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.TOP_LEFT);
        
        // Category badge
        Label categoryBadge = new Label("#1, BAD ROMANCE ❯");
        categoryBadge.setStyle(
            "-fx-background-color: #444;" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 5 10;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12;"
        );
        HBox badgeBox = new HBox(categoryBadge);
        badgeBox.setPadding(new Insets(0, 0, 5, 0));
        
        // Title
        Label title = new Label(book.getTitle());
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        
        // Author
        Label author = new Label(book.getAuthor());
        author.setFont(Font.font("SF Pro Text", 18));
        author.setTextFill(Color.LIGHTGRAY);
        
        // Rating
        HBox ratingBox = new HBox(5);
        ratingBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label ratingValue = new Label("3,9 (190)");
        ratingValue.setTextFill(Color.WHITE);
        ratingValue.setFont(Font.font("SF Pro Text", 14));
        
        Label ratingCategory = new Label("• Romanzi rosa");
        ratingCategory.setTextFill(Color.LIGHTGRAY);
        ratingCategory.setFont(Font.font("SF Pro Text", 14));
        ratingCategory.setPadding(new Insets(0, 0, 0, 5));
        
        ratingBox.getChildren().addAll(ratingValue, ratingCategory);
        
        // Book info box
        VBox bookInfoBox = new VBox(15);
        bookInfoBox.setPadding(new Insets(20, 0, 0, 0));
        
        Label infoLabel = new Label("Libro");
        infoLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        infoLabel.setTextFill(Color.WHITE);
        
        Label pagesInfo = new Label(" pagine");
        pagesInfo.setFont(Font.font("SF Pro Text", 14));
        pagesInfo.setTextFill(Color.LIGHTGRAY);
        
        bookInfoBox.getChildren().addAll(infoLabel, pagesInfo);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button getButton = new Button("Ottieni");
        getButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 8 25;"
        );
        
        Button previewButton = new Button("Estratto");
        previewButton.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 8 25;"
        );
        
        buttonBox.getChildren().addAll(getButton, previewButton);
        
        infoBox.getChildren().addAll(badgeBox, title, author, ratingBox, bookInfoBox, buttonBox);
        detailsSection.getChildren().addAll(coverContainer, infoBox);
        
        // --- PUBLISHER SECTION ---
        VBox publisherSection = new VBox(15);
        publisherSection.setPadding(new Insets(0, 30, 30, 30));
        
        Label publisherHeader = new Label("Dall'editore");
        publisherHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        publisherHeader.setTextFill(Color.WHITE);
        
        Text publisherText = new Text(book.getDescription());
        publisherText.setWrappingWidth(940);
        publisherText.setFill(Color.WHITE);
        publisherText.setFont(Font.font("SF Pro Text", 14));
        
        publisherSection.getChildren().addAll(publisherHeader, publisherText);
        
        // --- REVIEWS SECTION ---
        VBox reviewsSection = new VBox(15);
        reviewsSection.setPadding(new Insets(0, 30, 30, 30));
        
        HBox reviewHeaderBox = new HBox();
        Label reviewsHeader = new Label("Recensioni dei lettori");
        reviewsHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        reviewsHeader.setTextFill(Color.WHITE);
        
        Label seeMoreReviews = new Label("❯");
        seeMoreReviews.setFont(Font.font("SF Pro Text", 16));
        seeMoreReviews.setTextFill(Color.GRAY);
        
        Region reviewSpacer = new Region();
        HBox.setHgrow(reviewSpacer, Priority.ALWAYS);
        reviewHeaderBox.getChildren().addAll(reviewsHeader, reviewSpacer, seeMoreReviews);
        
        // Review cards
        HBox reviewCards = new HBox(20);
        reviewCards.setPadding(new Insets(10, 0, 10, 0));
        
        // First review
        VBox review1 = createReviewCard(
            "Bello ed intrigante",
            "Si legge tutto d'un fiato, molto bello come storia",
            "⭐⭐⭐⭐⭐",
            "20 apr, alessio_apples"
        );
        
        // Second review
        VBox review2 = createReviewCard(
            "Non saprei",
            "Il libro è scritto abbastanza bene, ma la storia è un po' banale. Lui il classico bad boy e lei la ragazza con una carattere forte, ma che ha dei demoni interiori...",
            "⭐⭐⭐☆☆",
            "26 nov 2024, vi_violet"
        );
        
        reviewCards.getChildren().addAll(review1, review2);
        reviewsSection.getChildren().addAll(reviewHeaderBox, reviewCards);
        
        // --- SIMILAR BOOKS SECTION ---
        VBox similarBooksSection = new VBox(15);
        similarBooksSection.setPadding(new Insets(0, 30, 30, 30));
        
        HBox similarHeaderBox = new HBox();
        Label similarHeader = new Label("Altri libri di questa serie");
        similarHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        similarHeader.setTextFill(Color.WHITE);
        
        Label seeMoreBooks = new Label("❯");
        seeMoreBooks.setFont(Font.font("SF Pro Text", 16));
        seeMoreBooks.setTextFill(Color.GRAY);
        
        Region similarSpacer = new Region();
        HBox.setHgrow(similarSpacer, Priority.ALWAYS);
        similarHeaderBox.getChildren().addAll(similarHeader, similarSpacer, seeMoreBooks);
        
        // Content area with scrollbar
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentScroll.setFitToWidth(true);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox scrollContent = new VBox();
        scrollContent.getChildren().addAll(detailsSection, publisherSection, reviewsSection, similarBooksSection);
        contentScroll.setContent(scrollContent);
        
        // Prevent clicks on the content from closing the popup
        contentScroll.setOnMouseClicked(e -> e.consume());
        
        // Add everything to main popup
        popupContent.getChildren().addAll(topBar, contentScroll);
        
        return popupContent;
    }
    
    private static VBox createBookPreview(int bookIndex) {
        // Validate book index
        if (bookIndex < 0 || booksCollection == null || bookIndex >= booksCollection.size()) {
            VBox emptyPreview = new VBox();
            emptyPreview.setVisible(false); // Make invisible if invalid
            return emptyPreview;
        }
        
        // Get the book
        Book previewBook = booksCollection.get(bookIndex);
        
        // Create a simplified preview of the book
        Image coverImage = loadCoverImage(previewBook.imageUrl);
        Color dominantColor = extractDominantColor(coverImage);
        Color darkenedColor = darkenColor(dominantColor, 0.7);
        String backgroundColor = toHexString(darkenedColor);
        
        VBox previewContent = new VBox();
        previewContent.setMaxWidth(1000);
        previewContent.setMaxHeight(700);
        previewContent.setOpacity(0.95);
        previewContent.setStyle(
            "-fx-background-color: " + backgroundColor + ";" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);"
        );
        
        // Top bar
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        
        // Details section (simplified)
        HBox detailsSection = new HBox(30);
        detailsSection.setPadding(new Insets(20, 30, 30, 30));
        detailsSection.setAlignment(Pos.TOP_LEFT);
        
        // Book cover with rounded corners
        ImageView cover = createSafeImageView(previewBook.imageUrl, 180, 270);
        Rectangle coverClip = new Rectangle(180, 270);
        coverClip.setArcWidth(8);
        coverClip.setArcHeight(8);
        cover.setClip(coverClip);
        
        VBox coverContainer = new VBox(cover);
        coverContainer.setAlignment(Pos.TOP_CENTER);
        
        // Book info (simplified)
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.TOP_LEFT);
        
        // Title
        Label title = new Label(previewBook.getTitle());
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        
        // Author
        Label author = new Label(previewBook.getAuthor());
        author.setFont(Font.font("SF Pro Text", 18));
        author.setTextFill(Color.LIGHTGRAY);
        
        // Add minimal info
        infoBox.getChildren().addAll(title, author);
        detailsSection.getChildren().addAll(coverContainer, infoBox);
        
        // Scrollable content
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentScroll.setFitToWidth(true);
        
        VBox scrollContent = new VBox();
        scrollContent.getChildren().addAll(detailsSection);
        contentScroll.setContent(scrollContent);
        
        // Add to container
        previewContent.getChildren().addAll(topBar, contentScroll);
        return previewContent;
    }
    
    private static void addEdgeDetection(StackPane container) {
        // Create transparent edge detection regions
        Rectangle leftEdge = new Rectangle(100, 700, Color.TRANSPARENT);
        leftEdge.setOpacity(0.01); // Almost invisible but still catches mouse events
        
        Rectangle rightEdge = new Rectangle(100, 700, Color.TRANSPARENT);
        rightEdge.setOpacity(0.01);
        
        // Position them at the edges
        StackPane.setAlignment(leftEdge, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightEdge, Pos.CENTER_RIGHT);
        
        // Set hover behaviors for right edge (next book)
        rightEdge.setOnMouseEntered(e -> {
            // Preview next book when hovering on right edge
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                showBookPreview(true, false);
            }
        });
        
        rightEdge.setOnMouseExited(e -> {
            // Hide preview if not transitioning
            if (!isTransitioning) {
                showBookPreview(false, false);
            }
        });
        
        // Set hover behaviors for left edge (previous book)
        leftEdge.setOnMouseEntered(e -> {
            // Preview previous book when hovering on left edge
            if (currentBookIndex > 0 && !isTransitioning) {
                showBookPreview(true, true);
            }
        });
        
        leftEdge.setOnMouseExited(e -> {
            // Hide preview if not transitioning
            if (!isTransitioning) {
                showBookPreview(false, true);
            }
        });
        
        // Add click behaviors
        rightEdge.setOnMouseClicked(e -> {
            // Navigate to next book when clicking right edge
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                slideToBook(currentBookIndex + 1);
            }
        });
        
        leftEdge.setOnMouseClicked(e -> {
            // Navigate to previous book when clicking left edge
            if (currentBookIndex > 0 && !isTransitioning) {
                slideToBook(currentBookIndex - 1);
            }
        });
        
        // Add the edge detection regions
        container.getChildren().addAll(leftEdge, rightEdge);
    }
    
    private static void addNavigationArrows() {
        // Create arrow buttons
        leftArrowButton = new Button("❮");
        leftArrowButton.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.5);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 24px;" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 50px;" +
            "-fx-min-height: 50px;" +
            "-fx-max-width: 50px;" +
            "-fx-max-height: 50px;" +
            "-fx-padding: 0;" +
            "-fx-cursor: hand;" +
            "-fx-opacity: 0.8;"
        );
        
        rightArrowButton = new Button("❯");
        rightArrowButton.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.5);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 24px;" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 50px;" +
            "-fx-min-height: 50px;" +
            "-fx-max-width: 50px;" +
            "-fx-max-height: 50px;" +
            "-fx-padding: 0;" +
            "-fx-cursor: hand;" +
            "-fx-opacity: 0.8;"
        );
        
        // Position them at the left and right centers
        StackPane.setAlignment(leftArrowButton, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightArrowButton, Pos.CENTER_RIGHT);
        
        // Set margins to keep them away from the edge
        StackPane.setMargin(leftArrowButton, new Insets(0, 0, 0, 20));
        StackPane.setMargin(rightArrowButton, new Insets(0, 20, 0, 0));
        
        // Initially invisible
        leftArrowButton.setOpacity(0);
        rightArrowButton.setOpacity(0);
        
        // Show them on container hover
        root.setOnMouseEntered(e -> {
            updateArrowVisibility();
        });
        
        root.setOnMouseExited(e -> {
            leftArrowButton.setOpacity(0);
            rightArrowButton.setOpacity(0);
        });
        
        // Add click behavior - correct directions
        leftArrowButton.setOnMouseClicked(e -> {
            // Left arrow navigates to previous book
            if (currentBookIndex > 0 && !isTransitioning) {
                slideToBook(currentBookIndex - 1);
            }
        });
        
        rightArrowButton.setOnMouseClicked(e -> {
            // Right arrow navigates to next book
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                slideToBook(currentBookIndex + 1);
            }
        });
        
        // Prevent the click from reaching the background
        leftArrowButton.setOnMousePressed(e -> e.consume());
        rightArrowButton.setOnMousePressed(e -> e.consume());
        
        // Add the arrows to the root container
        root.getChildren().addAll(leftArrowButton, rightArrowButton);
    }
    
    private static void updateArrowVisibility() {
        // Show/hide arrows based on current position in collection
        if (currentBookIndex > 0) {
            leftArrowButton.setOpacity(0.8);
        } else {
            leftArrowButton.setOpacity(0);
        }
        
        if (currentBookIndex < booksCollection.size() - 1) {
            rightArrowButton.setOpacity(0.8);
        } else {
            rightArrowButton.setOpacity(0);
        }
    }
    
    private static void showBookPreview(boolean show, boolean isPrevious) {
        // Stop any running animation
        if (slideAnimation != null && slideAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            slideAnimation.stop();
        }
        
        // Get the preview to show
        VBox previewToShow = isPrevious ? prevBookPreview : nextBookPreview;
        
        // Check if we have a valid preview
        if (previewToShow == null || !previewToShow.isVisible()) {
            return;
        }
        
        // Target X position
        double targetX;
        if (isPrevious) {
            // For previous book, peek from left
            targetX = show ? -950 : -1200;
        } else {
            // For next book, peek from right
            targetX = show ? 950 : 1200;
        }
        
        // Create and play the animation
        slideAnimation = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(previewToShow.translateXProperty(), targetX)
            )
        );
        slideAnimation.play();
    }
    
    private static void slideToBook(int newIndex) {
        if (newIndex < 0 || newIndex >= booksCollection.size() || newIndex == currentBookIndex || isTransitioning) {
            return; // Invalid index, same book, or already transitioning
        }
        
        // Mark that we're transitioning to prevent multiple animations
        isTransitioning = true;
        
        // Get the target book
        Book targetBook = booksCollection.get(newIndex);
        
        // Determine if we're going forward or backward
        boolean isForward = newIndex > currentBookIndex;
        
        // Create content for the target book
        Image coverImage = loadCoverImage(targetBook.imageUrl);
        Color dominantColor = extractDominantColor(coverImage);
        Color darkenedColor = darkenColor(dominantColor, 0.7);
        String backgroundColor = toHexString(darkenedColor);
        
        VBox newBookContent = createBookContent(targetBook, backgroundColor);
        
        // Position the new content off-screen in the correct direction
        newBookContent.setTranslateX(isForward ? 1200 : -1200);
        
        // Add it to the display pane
        bookDisplayPane.getChildren().add(newBookContent);
        
        // Get current content (middle child)
        VBox currentContent = (VBox) bookDisplayPane.getChildren().get(1);
        
        // Stop any running animation
        if (slideAnimation != null && slideAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            slideAnimation.stop();
        }
        
        // Create and play the slide animation
        slideAnimation = new Timeline(
            // Slide current content out
            new KeyFrame(Duration.millis(500), 
                new KeyValue(currentContent.translateXProperty(), 
                             isForward ? -1200 : 1200)
            ),
            // Slide new content in
            new KeyFrame(Duration.millis(500), 
                new KeyValue(newBookContent.translateXProperty(), 0)
            )
        );
        
        // When animation completes, clean up and update state
        slideAnimation.setOnFinished(e -> {
            // Update current book index
            currentBookIndex = newIndex;
            
            // Clear display pane and add the new content centered
            bookDisplayPane.getChildren().clear();
            
            // Create new previews for adjacent books
            prevBookPreview = createBookPreview(currentBookIndex - 1);
            prevBookPreview.setTranslateX(-1200); // Off-screen left
            
            nextBookPreview = createBookPreview(currentBookIndex + 1);
            nextBookPreview.setTranslateX(1200); // Off-screen right
            
            // Add all to display pane in correct order
            bookDisplayPane.getChildren().addAll(prevBookPreview, newBookContent, nextBookPreview);
            
            // Update navigation arrows
            updateArrowVisibility();
            
            // Add edge detection again
            addEdgeDetection(bookDisplayPane);
            
            // Transition is complete
            isTransitioning = false;
        });
        
        slideAnimation.play();
    }

    
    private static VBox createReviewCard(String title, String content, String rating, String date) {
        VBox card = new VBox(8);
        card.setStyle(
            "-fx-background-color: #3a3a3c;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 15;"
        );
        card.setPrefWidth(450);
        
        Label reviewTitle = new Label(title);
        reviewTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        reviewTitle.setTextFill(Color.WHITE);
        
        Text reviewContent = new Text(content);
        reviewContent.setFont(Font.font("SF Pro Text", 14));
        reviewContent.setFill(Color.LIGHTGRAY);
        reviewContent.setWrappingWidth(420);
        
        HBox footer = new HBox(10);
        Label ratingLabel = new Label(rating);
        ratingLabel.setTextFill(Color.WHITE);
        
        Label dateLabel = new Label(date);
        dateLabel.setTextFill(Color.GRAY);
        dateLabel.setFont(Font.font("SF Pro Text", 12));
        
        footer.getChildren().addAll(ratingLabel, dateLabel);
        
        card.getChildren().addAll(reviewTitle, reviewContent, footer);
        return card;
    }
    
// Helper method to load cover image and handle errors
private static Image loadCoverImage(String isbnFileName) {
    Image image;
    try {
        InputStream stream = AppleBooksClone.class.getResourceAsStream("/books_covers/" + isbnFileName);
        if (stream == null) {
            System.out.println("File non trovato: " + isbnFileName);
            return new Image("https://via.placeholder.com/140x210", 180, 270, true, true);
        }
        image = new Image(stream);
        if (image.isError()) throw new Exception("Errore immagine.");
    } catch (Exception e) {
        System.out.println("Errore caricamento immagine: fallback.");
        image = new Image("https://via.placeholder.com/140x210", 180, 270, true, true);
    }
    return image;
}

// Helper method to create an image view with error handling
public static ImageView createSafeImageView(String isbnFileName, double width, double height) {
    Image image = loadCoverImage(isbnFileName);
    ImageView imageView = new ImageView(image);
    imageView.setFitWidth(width);
    imageView.setFitHeight(height);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    return imageView;
}

// Helper method to extract dominant color from an image
private static Color extractDominantColor(Image image) {
    if (image == null || image.isError()) {
        return Color.rgb(41, 35, 46); // Default color if image not available
    }
    
    int width = (int) image.getWidth();
    int height = (int) image.getHeight();
    
    if (width <= 0 || height <= 0) {
        return Color.rgb(41, 35, 46);
    }
    
    // Use sampling for better performance
    int sampleSize = 5; // Sample every 5 pixels
    
    Map<Integer, Integer> colorCounts = new HashMap<>();
    PixelReader pixelReader = image.getPixelReader();
    
    // Sample pixels from the image
    for (int y = 0; y < height; y += sampleSize) {
        for (int x = 0; x < width; x += sampleSize) {
            Color color = pixelReader.getColor(x, y);
            
            // Convert color to an integer for easier storage
            int rgb = ((int) (color.getRed() * 255) << 16) |
                      ((int) (color.getGreen() * 255) << 8) |
                      ((int) (color.getBlue() * 255));
            
            colorCounts.put(rgb, colorCounts.getOrDefault(rgb, 0) + 1);
        }
    }
    
    // Find the most frequent color
    int dominantRGB = 0;
    int maxCount = 0;
    
    for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
        if (entry.getValue() > maxCount) {
            maxCount = entry.getValue();
            dominantRGB = entry.getKey();
        }
    }
    
    // Convert from integer RGB to JavaFX Color
    int red = (dominantRGB >> 16) & 0xFF;
    int green = (dominantRGB >> 8) & 0xFF;
    int blue = dominantRGB & 0xFF;
    
    return Color.rgb(red, green, blue);
}

// Helper method to darken a color
private static Color darkenColor(Color color, double factor) {
    return new Color(
        Math.max(0, color.getRed() * factor),
        Math.max(0, color.getGreen() * factor),
        Math.max(0, color.getBlue() * factor),
        color.getOpacity()
    );
}

// Helper method to convert Color to hex string
private static String toHexString(Color color) {
    int r = ((int) (color.getRed() * 255)) & 0xFF;
    int g = ((int) (color.getGreen() * 255)) & 0xFF;
    int b = ((int) (color.getBlue() * 255)) & 0xFF;
    
    return String.format("#%02X%02X%02X", r, g, b);
}
}