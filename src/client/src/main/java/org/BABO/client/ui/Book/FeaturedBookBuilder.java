package org.BABO.client.ui.Book;

import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.shared.model.Book;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Builder specializzato per la creazione di contenuti di libri in evidenza nell'applicazione BABO.
 * <p>
 * Questa classe fornisce un'API dedicata per costruire layout premium per libri in evidenza,
 * con design ottimizzato per la presentazione prominente di singoli libri. Implementa
 * un layout ricco e dettagliato che differenzia i contenuti featured dal resto della libreria,
 * offrendo maggiore spazio per metadati, descrizioni e call-to-action.
 * </p>
 *
 * <h3>Caratteristiche del layout featured:</h3>
 * <ul>
 *   <li><strong>Layout Orizzontale:</strong> Copertina grande affiancata da informazioni dettagliate</li>
 *   <li><strong>Copertina Premium:</strong> Dimensioni maggiorate (180x270px) con clipping arrotondato</li>
 *   <li><strong>Informazioni Ricche:</strong> Titolo, autore, ISBN, anno di pubblicazione</li>
 *   <li><strong>Descrizione Estesa:</strong> Anteprima della descrizione con troncamento intelligente</li>
 *   <li><strong>Call-to-Action:</strong> Pulsanti per anteprima e azioni sui libri</li>
 *   <li><strong>Interattività:</strong> Click handling su copertina e pulsanti</li>
 * </ul>
 *
 * <h3>Design e Styling:</h3>
 * <p>
 * Il builder applica uno styling premium che include:
 * </p>
 * <ul>
 *   <li>Typography gerarchica con font size e weight differenziati</li>
 *   <li>Color scheme sofisticato con gradazioni di grigio</li>
 *   <li>Spacing ottimizzato per leggibilità e gerarchia visiva</li>
 *   <li>Effetti di clipping per copertine con corner radius</li>
 *   <li>Pulsanti con styling glassmorphism</li>
 * </ul>
 *
 * <h3>Architettura Modulare:</h3>
 * <p>
 * La classe è strutturata con metodi modulari per facilitare manutenzione e personalizzazione:
 * </p>
 * <ul>
 *   <li><strong>Main Factory:</strong> {@link #createFeaturedBookContent(Book)} - Entry point principale</li>
 *   <li><strong>Info Section:</strong> {@link #createBookInfoBox(Book)} - Metadati e titoli</li>
 *   <li><strong>Additional Info:</strong> {@link #createAdditionalInfoBox(Book)} - Dettagli tecnici</li>
 *   <li><strong>Actions:</strong> {@link #createButtonBox(Book)} - Pulsanti di azione</li>
 *   <li><strong>Description:</strong> {@link #createDescriptionSection(Book)} - Descrizione estesa</li>
 * </ul>
 *
 * <h3>Gestione Contenuti Dinamici:</h3>
 * <p>
 * Il builder gestisce intelligentemente contenuti variabili:
 * </p>
 * <ul>
 *   <li>Fallback per immagini mancanti tramite {@link ImageUtils}</li>
 *   <li>Troncamento automatico per descrizioni lunghe (150 caratteri)</li>
 *   <li>Visualizzazione condizionale di metadati opzionali</li>
 *   <li>Gestione null-safe per tutti i campi del libro</li>
 * </ul>
 *
 * <h3>Sistema di Interazione:</h3>
 * <p>
 * Implementa un sistema di callback unificato per gestire le interazioni:
 * </p>
 * <ul>
 *   <li>Click sulla copertina per visualizzazione dettagli</li>
 *   <li>Pulsante anteprima per accesso rapido</li>
 *   <li>Cursor styling per indicare elementi interattivi</li>
 *   <li>Event handling consistente tra diversi elementi</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Creazione del builder
 * FeaturedBookBuilder featuredBuilder = new FeaturedBookBuilder();
 *
 * // Configurazione callback per interazioni
 * featuredBuilder.setBookClickHandler(book -> {
 *     // Apri popup dettagli libro
 *     BookDetailsPopup popup = new BookDetailsPopup(book);
 *     popup.show();
 *
 *     // Track analytics
 *     analytics.trackFeaturedBookClick(book.getIsbn());
 * });
 *
 * // Creazione contenuto featured
 * Book featuredBook = bookService.getFeaturedBook();
 * VBox featuredContent = featuredBuilder.createFeaturedBookContent(featuredBook);
 *
 * // Integrazione in sezione featured
 * VBox featuredSection = new VBox(featuredContent);
 * featuredSection.setStyle("-fx-background-color: linear-gradient(...)");
 *
 * // Aggiunta a layout principale
 * mainContainer.getChildren().add(featuredSection);
 * }</pre>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Utilizzare sempre il callback handler per garantire interattività</li>
 *   <li>Verificare che i dati del libro siano validi prima del rendering</li>
 *   <li>Integrare con analytics per tracciare engagement</li>
 *   <li>Considerare localizzazione per etichette e formattazione</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see Book
 * @see ImageUtils
 * @see BookSectionFactory
 */
public class FeaturedBookBuilder {

    /** Callback per gestire i click sui libri featured */
    private Consumer<Book> bookClickHandler;

    /**
     * Configura il gestore per i click sui libri featured.
     * <p>
     * Imposta il callback che verrà eseguito quando un utente interagisce
     * con elementi cliccabili del libro featured (copertina, pulsanti).
     * Il callback riceve l'oggetto Book e deve gestire la navigazione
     * o visualizzazione dei dettagli.
     * </p>
     *
     * @param handler il {@link Consumer} da eseguire per i click sui libri.
     *               Riceve l'oggetto {@link Book} selezionato. Può essere {@code null}
     *               per disabilitare l'interattività.
     * @apiNote Il callback viene eseguito nel JavaFX Application Thread, quindi
     *          è sicuro aggiornare l'UI direttamente. Il callback viene applicato
     *          sia alla copertina che ai pulsanti di azione.
     */
    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
    }

    /**
     * Crea il contenuto completo per un libro in evidenza con layout premium.
     * <p>
     * Metodo principale per generare l'interfaccia completa di un libro featured,
     * combinando copertina grande, informazioni dettagliate, metadati tecnici
     * e sezione descrizione in un layout orizzontale ottimizzato.
     * </p>
     *
     * <h4>Struttura del layout generato:</h4>
     * <ol>
     *   <li><strong>Sezione principale (HBox):</strong> Copertina + Box informazioni</li>
     *   <li><strong>Sezione descrizione (VBox):</strong> Anteprima descrizione estesa</li>
     * </ol>
     *
     * <h4>Caratteristiche della copertina:</h4>
     * <ul>
     *   <li>Dimensioni premium: 180x270 pixel</li>
     *   <li>Clipping arrotondato con radius 10px</li>
     *   <li>Interattività con cursor pointer</li>
     *   <li>Gestione fallback per immagini mancanti</li>
     * </ul>
     *
     * <h4>Box informazioni include:</h4>
     * <ul>
     *   <li>Badge "IN EVIDENZA" per identificazione</li>
     *   <li>Titolo del libro con font bold 26pt</li>
     *   <li>Nome autore con styling secondario</li>
     *   <li>Metadati aggiuntivi (ISBN, anno)</li>
     *   <li>Pulsanti di azione per interazione</li>
     * </ul>
     *
     * @param featuredBook l'oggetto {@link Book} da renderizzare come featured
     * @return un {@link VBox} contenente il layout completo del libro featured
     * @throws IllegalArgumentException se featuredBook è {@code null}
     * @see #createBookInfoBox(Book)
     * @see #createDescriptionSection(Book)
     */
    public VBox createFeaturedBookContent(Book featuredBook) {
        if (featuredBook == null) {
            throw new IllegalArgumentException("Il libro featured non può essere null");
        }

        HBox featuredBox = new HBox(20);
        featuredBox.setPadding(new Insets(20));
        featuredBox.setAlignment(Pos.CENTER_LEFT);

        ImageView cover = ImageUtils.createSafeImageView(featuredBook.getImageUrl(), 180, 270);
        Rectangle clip = new Rectangle(180, 270);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        cover.setClip(clip);

        // Rendi cliccabile la copertina in evidenza
        if (bookClickHandler != null) {
            cover.setOnMouseClicked(e -> bookClickHandler.accept(featuredBook));
            cover.setStyle("-fx-cursor: hand;");
        }

        VBox infoBox = createBookInfoBox(featuredBook);
        featuredBox.getChildren().addAll(cover, infoBox);

        VBox descriptionSection = createDescriptionSection(featuredBook);

        VBox content = new VBox();
        content.getChildren().addAll(featuredBox, descriptionSection);

        return content;
    }

    /**
     * Crea la sezione informazioni principale del libro featured.
     * <p>
     * Costruisce un layout verticale contenente tutti i metadati primari
     * del libro, inclusi badge identificativo, titolo, autore, informazioni
     * aggiuntive e pulsanti di azione. Utilizza typography gerarchica
     * per guidare l'attenzione dell'utente.
     * </p>
     *
     * <h4>Elementi della sezione informazioni:</h4>
     * <ul>
     *   <li><strong>Badge:</strong> "⭐ IN EVIDENZA" con styling grigio</li>
     *   <li><strong>Titolo:</strong> Font bold 26pt, testo bianco, wrap automatico</li>
     *   <li><strong>Autore:</strong> Prefisso "di", font 16pt, grigio chiaro</li>
     *   <li><strong>Info aggiuntive:</strong> ISBN e anno se disponibili</li>
     *   <li><strong>Pulsanti:</strong> Call-to-action per interazione</li>
     * </ul>
     *
     * <h4>Spacing e layout:</h4>
     * <p>
     * La sezione utilizza spacing di 12px tra elementi per garantire
     * leggibilità ottimale e gerarchia visiva chiara. Allineamento
     * a sinistra per consistenza con pattern di lettura occidentali.
     * </p>
     *
     * @param book l'oggetto {@link Book} per cui creare la sezione informazioni
     * @return un {@link VBox} contenente le informazioni strutturate del libro
     * @throws IllegalArgumentException se book è {@code null}
     * @see #createAdditionalInfoBox(Book)
     * @see #createButtonBox(Book)
     */
    private VBox createBookInfoBox(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("L'oggetto Book non può essere null");
        }

        VBox infoBox = new VBox(12);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⭐ IN EVIDENZA");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setTextFill(Color.gray(0.7));

        Label bookTitle = new Label(book.getTitle() != null ? book.getTitle() : "Titolo non disponibile");
        bookTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        bookTitle.setTextFill(Color.WHITE);
        bookTitle.setWrapText(true);

        Label authorLabel = new Label("di " + (book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto"));
        authorLabel.setFont(Font.font("System", 16));
        authorLabel.setTextFill(Color.gray(0.8));

        // Aggiungi informazioni ISBN e anno se disponibili
        VBox additionalInfo = createAdditionalInfoBox(book);

        HBox buttonBox = createButtonBox(book);

        infoBox.getChildren().addAll(title, bookTitle, authorLabel, additionalInfo, buttonBox);
        return infoBox;
    }

    /**
     * Crea la sezione con metadati tecnici aggiuntivi del libro.
     * <p>
     * Genera un layout per informazioni supplementari come ISBN e anno
     * di pubblicazione, visualizzate solo se disponibili nei dati del libro.
     * Utilizza icone descrittive e styling secondario per non competere
     * con le informazioni primarie.
     * </p>
     *
     * <h4>Metadati supportati:</h4>
     * <ul>
     *   <li><strong>ISBN:</strong> Codice identificativo con icona 📄</li>
     *   <li><strong>Anno pubblicazione:</strong> Data con icona 📅</li>
     * </ul>
     *
     * <h4>Logica di visualizzazione:</h4>
     * <p>
     * Ogni metadato viene visualizzato solo se presente e non vuoto
     * nei dati del libro, garantendo un layout pulito e privo di
     * informazioni mancanti o placeholder.
     * </p>
     *
     * <h4>Styling applicato:</h4>
     * <ul>
     *   <li>Font size 12pt per aspetto discreto</li>
     *   <li>Colore grigio (0.6) per gerarchia secondaria</li>
     *   <li>Spacing 5px tra elementi</li>
     *   <li>Padding top 10px per separazione dalla sezione principale</li>
     * </ul>
     *
     * @param book l'oggetto {@link Book} da cui estrarre i metadati
     * @return un {@link VBox} contenente i metadati disponibili
     * @throws IllegalArgumentException se book è {@code null}
     */
    private VBox createAdditionalInfoBox(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("L'oggetto Book non può essere null");
        }

        VBox additionalInfo = new VBox(5);
        additionalInfo.setPadding(new Insets(10, 0, 0, 0));

        // Mostra ISBN se disponibile
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            Label isbnLabel = new Label("📄 ISBN: " + book.getIsbn());
            isbnLabel.setFont(Font.font("System", 12));
            isbnLabel.setTextFill(Color.gray(0.6));
            additionalInfo.getChildren().add(isbnLabel);
        }

        // Mostra anno di pubblicazione se disponibile
        if (book.getPublishYear() != null && !book.getPublishYear().trim().isEmpty()) {
            Label yearLabel = new Label("📅 Anno: " + book.getPublishYear());
            yearLabel.setFont(Font.font("System", 12));
            yearLabel.setTextFill(Color.gray(0.6));
            additionalInfo.getChildren().add(yearLabel);
        }

        return additionalInfo;
    }

    /**
     * Crea la sezione pulsanti di azione per il libro featured.
     * <p>
     * Genera i pulsanti call-to-action per permettere all'utente di interagire
     * con il libro featured. Attualmente include un pulsante anteprima con
     * styling glassmorphism e gestione click tramite callback configurato.
     * </p>
     *
     * <h4>Pulsanti implementati:</h4>
     * <ul>
     *   <li><strong>Anteprima:</strong> "👁️ ANTEPRIMA" per accesso rapido ai dettagli</li>
     * </ul>
     *
     * <h4>Styling pulsanti:</h4>
     * <ul>
     *   <li>Sfondo semi-trasparente (glassmorphism)</li>
     *   <li>Testo bianco per contrasto</li>
     *   <li>Border radius 20px per aspetto moderno</li>
     *   <li>Padding interno 10x25px per area click ottimale</li>
     *   <li>Cursor pointer per indicare interattività</li>
     * </ul>
     *
     * <h4>Gestione interazioni:</h4>
     * <p>
     * I pulsanti utilizzano il callback configurato tramite
     * {@link #setBookClickHandler(Consumer)} per gestire le azioni,
     * garantendo comportamento consistente con altri elementi cliccabili.
     * </p>
     *
     * @param book l'oggetto {@link Book} per cui creare i pulsanti di azione
     * @return un {@link HBox} contenente i pulsanti di azione
     * @throws IllegalArgumentException se book è {@code null}
     * @apiNote La sezione è progettata per essere estensibile con pulsanti
     *          aggiuntivi (acquisto, wishlist, condivisione) in future versioni.
     */
    private HBox createButtonBox(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("L'oggetto Book non può essere null");
        }

        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button sampleButton = new Button("👁️ ANTEPRIMA");
        sampleButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 25;" +
                        "-fx-cursor: hand;"
        );

        // Aggiungi azione al pulsante anteprima per aprire il BookDetailsPopup
        if (bookClickHandler != null) {
            sampleButton.setOnAction(e -> bookClickHandler.accept(book));
        }

        buttonBox.getChildren().addAll(sampleButton);
        return buttonBox;
    }

    /**
     * Crea la sezione descrizione estesa del libro featured.
     * <p>
     * Costruisce una sezione dedicata alla descrizione del libro con
     * header identificativo e anteprima del testo descrittivo. Implementa
     * troncamento intelligente per mantenere layout consistente e
     * incoraggiare l'utente ad approfondire tramite click.
     * </p>
     *
     * <h4>Struttura della sezione:</h4>
     * <ul>
     *   <li><strong>Header:</strong> "📝 DESCRIZIONE" con styling bold e grigio</li>
     *   <li><strong>Contenuto:</strong> Anteprima descrizione con wrap automatico</li>
     * </ul>
     *
     * <h4>Logica di troncamento:</h4>
     * <p>
     * Se la descrizione supera 150 caratteri, viene troncata e aggiunto
     * "..." per indicare contenuto aggiuntivo. Questo mantiene il layout
     * compatto e incoraggia l'interazione per visualizzare il testo completo.
     * </p>
     *
     * <h4>Gestione contenuti mancanti:</h4>
     * <p>
     * Se la descrizione è null o vuota, viene mostrato il messaggio
     * "Descrizione non disponibile" per mantenere consistenza visiva
     * e informare l'utente sullo stato del contenuto.
     * </p>
     *
     * <h4>Styling e layout:</h4>
     * <ul>
     *   <li>Header con font bold 14pt, colore grigio (0.7)</li>
     *   <li>Descrizione con testo bianco, wrap automatico</li>
     *   <li>Padding 20px sui lati per allineamento con layout principale</li>
     *   <li>Padding bottom per separazione da contenuti successivi</li>
     * </ul>
     *
     * @param book l'oggetto {@link Book} da cui estrarre la descrizione
     * @return un {@link VBox} contenente la sezione descrizione formattata
     * @throws IllegalArgumentException se book è {@code null}
     */
    private VBox createDescriptionSection(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("L'oggetto Book non può essere null");
        }

        Label previewLabel = new Label("📝 DESCRIZIONE");
        previewLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        previewLabel.setTextFill(Color.gray(0.7));
        previewLabel.setPadding(new Insets(0, 0, 8, 20));

        String descriptionPreview = book.getDescription();
        if (descriptionPreview != null && descriptionPreview.length() > 150) {
            descriptionPreview = descriptionPreview.substring(0, 150) + "...";
        }

        Label description = new Label(descriptionPreview != null ? descriptionPreview : "Descrizione non disponibile");
        description.setWrapText(true);
        description.setTextFill(Color.WHITE);
        description.setPadding(new Insets(0, 20, 20, 20));

        VBox descriptionSection = new VBox();
        descriptionSection.getChildren().addAll(previewLabel, description);
        return descriptionSection;
    }
}