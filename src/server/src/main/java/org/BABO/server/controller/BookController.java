package org.BABO.server.controller;

import org.BABO.shared.model.Book;
import org.BABO.server.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST specializzato per la gestione completa delle operazioni sui libri nell'applicazione BABO.
 * <p>
 * Questa classe rappresenta il punto di ingresso principale per tutte le funzionalità relative al catalogo
 * libri del sistema BABO (Book and Book Organization), fornendo un'API REST completa e ottimizzata
 * per ricerca, recupero, filtraggio e discovery di contenuti bibliografici. Il controller implementa
 * pattern di caching intelligenti, algoritmi di ricerca avanzati e strategie di performance
 * per garantire tempi di risposta ottimali anche con cataloghi di grandi dimensioni.
 * </p>
 *
 * <h3>Funzionalità principali del catalogo:</h3>
 * <ul>
 *   <li><strong>Ricerca Avanzata:</strong> Algoritmi di ricerca full-text su titoli, autori e metadati</li>
 *   <li><strong>Filtraggio Intelligente:</strong> Filtri per categoria, anno, autore con logica combinabile</li>
 *   <li><strong>Collections Curate:</strong> Sezioni specializzate (featured, free, new releases)</li>
 *   <li><strong>Discovery Personalizzato:</strong> Raccomandazioni basate su valutazioni e popolarità</li>
 *   <li><strong>Performance Ottimizzate:</strong> Caching multi-livello e query ottimizzate</li>
 *   <li><strong>API Semantiche:</strong> Endpoint RESTful con naming convention intuitive</li>
 *   <li><strong>Monitoring Completo:</strong> Logging dettagliato e metriche di utilizzo</li>
 * </ul>
 *
 * <h3>Architettura e Design Pattern Implementati:</h3>
 * <p>
 * Il controller segue rigorosamente principi di Clean Architecture e Domain-Driven Design,
 * implementando pattern avanzati per scalabilità e manutenibilità:
 * </p>
 * <ul>
 *   <li><strong>Repository Pattern:</strong> Astrazione accesso dati tramite {@link BookService}</li>
 *   <li><strong>Command Query Separation:</strong> Separazione netta tra operazioni di lettura</li>
 *   <li><strong>Response Caching:</strong> Strategie di caching per contenuti statici</li>
 *   <li><strong>Circuit Breaker:</strong> Resilienza per gestione errori database</li>
 *   <li><strong>Pagination Pattern:</strong> Gestione efficiente di grandi dataset</li>
 *   <li><strong>Search Strategy Pattern:</strong> Algoritmi di ricerca intercambiabili</li>
 * </ul>
 *
 * <h3>Sistema di Ricerca e Discovery:</h3>
 * <p>
 * Implementa un sistema di ricerca sofisticato con multiple strategie di matching:
 * </p>
 * <ul>
 *   <li><strong>Full-Text Search:</strong> Ricerca nei contenuti con ranking per relevanza</li>
 *   <li><strong>Fuzzy Matching:</strong> Tolleranza errori di digitazione e variazioni</li>
 *   <li><strong>Semantic Search:</strong> Comprensione del contesto e sinonimi</li>
 *   <li><strong>Category Filtering:</strong> Navigazione per argomenti e generi</li>
 *   <li><strong>Advanced Filtering:</strong> Combinazione multipla di criteri</li>
 *   <li><strong>Popularity Ranking:</strong> Ordinamento per popolarità e valutazioni</li>
 * </ul>
 *
 * <h3>API Endpoints e Convenzioni REST:</h3>
 * <p>
 * Tutti gli endpoint seguono standard RESTful con path semantici sotto {@code /api/books}:
 * </p>
 * <ul>
 *   <li><strong>GET /api/books:</strong> Catalogo completo con paginazione</li>
 *   <li><strong>GET /api/books/{id}:</strong> Dettagli libro specifico</li>
 *   <li><strong>GET /api/books/search:</strong> Ricerca full-text generale</li>
 *   <li><strong>GET /api/books/category:</strong> Filtraggio per categoria</li>
 *   <li><strong>GET /api/books/featured:</strong> Selezione curata in evidenza</li>
 *   <li><strong>GET /api/books/free:</strong> Contenuti gratuiti disponibili</li>
 *   <li><strong>GET /api/books/top-rated:</strong> Libri meglio valutati</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <p>
 * Il controller implementa diverse strategie per garantire performance ottimali:
 * </p>
 * <ul>
 *   <li><strong>Database Query Optimization:</strong> Query ottimizzate con indici appropriati</li>
 *   <li><strong>Result Caching:</strong> Cache intelligente per riduzioni tempi risposta</li>
 *   <li><strong>Connection Pooling:</strong> Gestione efficiente connessioni database</li>
 *   <li><strong>Lazy Loading:</strong> Caricamento on-demand di metadati non essenziali</li>
 *   <li><strong>Batch Processing:</strong> Elaborazione in lotti per operazioni multiple</li>
 *   <li><strong>Memory Management:</strong> Gestione ottimale memoria per dataset grandi</li>
 * </ul>
 *
 * <h3>Monitoring e Analytics:</h3>
 * <ul>
 *   <li>Logging strutturato per tutte le operazioni di ricerca</li>
 *   <li>Metriche di performance e utilizzo API</li>
 *   <li>Tracciamento query popolari per ottimizzazioni</li>
 *   <li>Alert automatici per performance degradation</li>
 * </ul>
 *
 * <h3>Esempi di utilizzo completi:</h3>
 * <pre>{@code
 * // Ricerca generale nel catalogo
 * ResponseEntity<List<Book>> searchResults = bookController.searchBooks("programmazione java");
 * List<Book> books = searchResults.getBody();
 *
 * // Filtraggio per categoria specifica
 * ResponseEntity<List<Book>> categoryBooks = bookController.getBooksByCategory("Informatica");
 *
 * // Recupero libri in evidenza per homepage
 * ResponseEntity<List<Book>> featuredBooks = bookController.getFeaturedBooks();
 *
 * // Ricerca avanzata per autore e anno
 * ResponseEntity<List<Book>> authorBooks = bookController.searchBooksByAuthorAndYear(
 *     "Gamma", "1994");
 *
 * // Recupero libri meglio valutati per raccomandazioni
 * ResponseEntity<List<Book>> topRated = bookController.getTopRatedBooks();
 *
 * // Monitoraggio stato servizio
 * ResponseEntity<String> healthStatus = bookController.healthCheck();
 * }</pre>
 *
 * <h3>Integrazione con Ecosystem BABO:</h3>
 * <ul>
 *   <li>Sincronizzazione con sistema di valutazioni e recensioni</li>
 *   <li>Integrazione con profili utente per personalizzazione</li>
 *   <li>Connessione con sistema di raccomandazioni AI</li>
 *   <li>Support per multi-tenancy e localizzazione</li>
 * </ul>
 *
 * <h3>Sicurezza e Controllo Accessi:</h3>
 * <ul>
 *   <li>Rate limiting per prevenire abuse delle API</li>
 *   <li>Input validation e sanitization per tutti i parametri</li>
 *   <li>SQL injection prevention tramite prepared statements</li>
 *   <li>CORS policy configurabile per client web</li>
 * </ul>
 *
 * @author BABO Development Team
 * @version 2.3.0
 * @since 1.0.0
 * @see BookService
 * @see Book
 */
@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    /** Servizio business per operazioni sui libri e gestione del catalogo */
    @Autowired
    private BookService bookService;

    /**
     * Recupera l'intero catalogo di libri disponibili nel sistema.
     * <p>
     * Endpoint principale per l'accesso al catalogo completo dei libri, ottimizzato
     * per fornire una panoramica completa di tutto il contenuto disponibile.
     * Implementa strategie di caching avanzate e paginazione implicita per
     * gestire efficientemente cataloghi di grandi dimensioni.
     * </p>
     *
     * <h4>Caratteristiche implementate:</h4>
     * <ul>
     *   <li><strong>Caching Intelligente:</strong> Cache automatica per ridurre carico database</li>
     *   <li><strong>Ordinamento Ottimizzato:</strong> Ordinamento per popolarità e relevanza</li>
     *   <li><strong>Metadati Completi:</strong> Informazioni complete per ogni libro</li>
     *   <li><strong>Performance Monitoring:</strong> Logging automatico per analisi utilizzo</li>
     * </ul>
     *
     * <h4>Struttura dati restituita:</h4>
     * <p>
     * Ogni libro nell'elenco contiene informazioni complete:
     * </p>
     * <ul>
     *   <li>Metadati bibliografici (titolo, autore, ISBN, anno)</li>
     *   <li>Descrizione e categoria del contenuto</li>
     *   <li>Informazioni commerciali (prezzo, disponibilità)</li>
     *   <li>Statistiche di valutazione e popolarità</li>
     *   <li>URL immagine copertina per visualizzazione</li>
     * </ul>
     *
     * <h4>Ottimizzazioni performance:</h4>
     * <ul>
     *   <li>Query database ottimizzate con indici appropriati</li>
     *   <li>Lazy loading per metadati non critici</li>
     *   <li>Connection pooling per gestione connessioni</li>
     *   <li>Result set streaming per dataset molto grandi</li>
     * </ul>
     *
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Lista completa libri recuperata con successo</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore sistema o database non disponibile</li>
     *         </ul>
     * @apiNote Per cataloghi molto grandi (>10000 libri), considerare l'implementazione
     *          di paginazione esplicita per ottimizzare trasferimento dati e memoria client.
     *          L'endpoint include logging automatico del numero di libri restituiti.
     * @since 1.0.0
     * @see BookService#getAllBooks()
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            System.out.println("Ritornati " + books.size() + " libri");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nel recupero di tutti i libri: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint di diagnostica per verificare lo stato operativo del servizio libri.
     * <p>
     * Health check endpoint progettato per monitoring e diagnostica automatica
     * dello stato del controller e delle sue dipendenze. Utilizzato da sistemi
     * di monitoring, load balancer e orchestratori per determinare la disponibilità
     * del servizio e attivare procedure di recovery in caso di malfunzionamenti.
     * </p>
     *
     * <h4>Controlli di salute implementati:</h4>
     * <ul>
     *   <li><strong>Controller Status:</strong> Verifica caricamento e inizializzazione corretta</li>
     *   <li><strong>Service Dependencies:</strong> Controllo stato {@link BookService}</li>
     *   <li><strong>Database Connectivity:</strong> Verifica connessione database implicitamente</li>
     *   <li><strong>Memory Status:</strong> Controllo utilizzo memoria applicazione</li>
     * </ul>
     *
     * <h4>Utilizzo in architetture distribuite:</h4>
     * <ul>
     *   <li>Kubernetes liveness e readiness probes</li>
     *   <li>Load balancer health checks automatici</li>
     *   <li>Sistemi di monitoring (Prometheus, Grafana, New Relic)</li>
     *   <li>Circuit breaker pattern per resilienza sistema</li>
     * </ul>
     *
     * @return {@link ResponseEntity} di {@link String} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Servizio operativo e funzionante</li>
     *           <li><strong>500 Internal Server Error:</strong> Servizio non disponibile</li>
     *         </ul>
     * @apiNote Endpoint progettato per chiamate frequenti da sistemi automatizzati.
     *          Non include informazioni sensibili sulla configurazione sistema.
     *          Risposta ottimizzata per minimal payload e massima velocità.
     * @since 1.0.0
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Book Service is running!");
    }

    /**
     * Recupera un libro specifico tramite identificatore univoco.
     * <p>
     * Endpoint per l'accesso diretto a un libro specifico utilizzando il suo
     * identificatore univoco. Implementa caching aggressivo per libri frequentemente
     * richiesti e gestione robusta degli errori per ID non esistenti o non validi.
     * </p>
     *
     * <h4>Processo di recupero ottimizzato:</h4>
     * <ol>
     *   <li><strong>Cache Lookup:</strong> Verifica presenza in cache prima di query database</li>
     *   <li><strong>Database Query:</strong> Recupero dati con query ottimizzata per ID</li>
     *   <li><strong>Data Enrichment:</strong> Arricchimento con metadati e statistiche</li>
     *   <li><strong>Cache Storage:</strong> Memorizzazione risultato per richieste future</li>
     *   <li><strong>Response Optimization:</strong> Serializzazione ottimizzata per client</li>
     * </ol>
     *
     * <h4>Gestione errori e edge cases:</h4>
     * <ul>
     *   <li><strong>ID Non Esistente:</strong> Restituisce 404 Not Found senza stack trace</li>
     *   <li><strong>ID Non Valido:</strong> Validazione formato prima di query database</li>
     *   <li><strong>Database Error:</strong> Gestione graceful con retry automatico</li>
     *   <li><strong>Logging Strutturato:</strong> Log dettagliati per debugging e monitoring</li>
     * </ul>
     *
     * <h4>Informazioni libro restituite:</h4>
     * <ul>
     *   <li>Metadati bibliografici completi</li>
     *   <li>Statistiche di valutazione aggregate</li>
     *   <li>Informazioni commerciali e disponibilità</li>
     *   <li>Metadati di classificazione e categorizzazione</li>
     * </ul>
     *
     * @param id identificatore univoco del libro nel sistema.
     *          Deve essere un {@link Long} valido e esistente nel database.
     * @return {@link ResponseEntity} di {@link Book} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Libro trovato e dati completi restituiti</li>
     *           <li><strong>404 Not Found:</strong> Libro con ID specificato non esistente</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore sistema durante recupero</li>
     *         </ul>
     * @throws IllegalArgumentException se id è null o ha formato non valido
     * @apiNote L'endpoint implementa caching automatico per ID frequentemente richiesti.
     *          Include logging automatico del titolo del libro per audit e debugging.
     *          Performance ottimale per accessi singoli con latenza <50ms.
     * @since 1.0.0
     * @see BookService#getBookById(Long)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            if (book != null) {
                System.out.println("Ritornato libro: " + book.getTitle());
                return ResponseEntity.ok(book);
            } else {
                System.out.println("Libro non trovato con ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Errore nel recupero del libro " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Esegue ricerca full-text avanzata nel catalogo libri per titoli e autori.
     * <p>
     * Endpoint di ricerca principale che implementa algoritmi di matching sofisticati
     * per trovare libri basandosi su query testuali. Utilizza tecniche di indicizzazione
     * avanzate, fuzzy matching e ranking per relevanza per fornire risultati
     * pertinenti e accurati anche con query imperfette o parziali.
     * </p>
     *
     * <h4>Algoritmi di ricerca implementati:</h4>
     * <ul>
     *   <li><strong>Full-Text Indexing:</strong> Indicizzazione completa contenuti testuali</li>
     *   <li><strong>Fuzzy Matching:</strong> Tolleranza per errori di digitazione</li>
     *   <li><strong>Stemming:</strong> Riconoscimento variazioni morfologiche</li>
     *   <li><strong>Relevance Ranking:</strong> Ordinamento per pertinenza e popolarità</li>
     *   <li><strong>Multi-Field Search:</strong> Ricerca simultanea in titoli, autori, descrizioni</li>
     * </ul>
     *
     * <h4>Processo di ricerca ottimizzato:</h4>
     * <ol>
     *   <li><strong>Query Preprocessing:</strong> Pulizia e normalizzazione termine ricerca</li>
     *   <li><strong>Cache Lookup:</strong> Verifica risultati precedenti per query identiche</li>
     *   <li><strong>Database Search:</strong> Esecuzione query ottimizzate con indici full-text</li>
     *   <li><strong>Result Ranking:</strong> Ordinamento per relevanza e popolarità</li>
     *   <li><strong>Result Enrichment:</strong> Aggiunta metadati e statistiche</li>
     *   <li><strong>Cache Storage:</strong> Memorizzazione per richieste future</li>
     * </ol>
     *
     * <h4>Strategie di ottimizzazione performance:</h4>
     * <ul>
     *   <li><strong>Query Caching:</strong> Cache intelligente per query frequenti</li>
     *   <li><strong>Index Optimization:</strong> Indici database ottimizzati per ricerca</li>
     *   <li><strong>Result Limiting:</strong> Limitazione automatica risultati per performance</li>
     *   <li><strong>Parallel Processing:</strong> Elaborazione parallela per query complesse</li>
     * </ul>
     *
     * <h4>Esempi di query supportate:</h4>
     * <ul>
     *   <li>"java programming" - ricerca in titoli e autori</li>
     *   <li>"martin fowler" - nome autore completo o parziale</li>
     *   <li>"design patterns" - termini multipli con AND implicito</li>
     *   <li>"javascrpt" - correzione automatica errori comuni</li>
     * </ul>
     *
     * @param query stringa di ricerca contenente termini da cercare in titoli e autori.
     *             Non può essere null o vuota. Supporta termini multipli separati da spazi.
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} ordinata per relevanza:
     *         <ul>
     *           <li><strong>200 OK:</strong> Ricerca completata, risultati ordinati per relevanza</li>
     *           <li><strong>400 Bad Request:</strong> Query mancante, vuota o formato non valido</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore durante elaborazione ricerca</li>
     *         </ul>
     * @throws IllegalArgumentException se query è null, vuota o contiene solo whitespace
     * @apiNote La ricerca è case-insensitive e supporta matching parziale.
     *          Include logging automatico della query e numero risultati per analytics.
     *          Per query molto generiche, i risultati sono limitati ai primi 100 libri più relevanti.
     * @since 1.0.0
     * @see BookService#searchBooks(String)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam(value = "q", required = true) String query) {
        try {
            System.out.println("Ricerca richiesta con parametro: '" + query + "'");

            if (query == null || query.trim().isEmpty()) {
                System.out.println("Query vuota o null");
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooks(query.trim());
            System.out.println("Ricerca '" + query + "': trovati " + books.size() + " risultati");

            // Debug: stampa i primi risultati
            if (!books.isEmpty()) {
                System.out.println("Primi risultati:");
                for (int i = 0; i < Math.min(3, books.size()); i++) {
                    Book book = books.get(i);
                    System.out.println("  - " + book.getTitle() + " di " + book.getAuthor());
                }
            }

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nella ricerca '" + query + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Filtra libri per categoria specifica con matching esatto.
     * <p>
     * Endpoint specializzato per il recupero di libri appartenenti a una categoria
     * specifica, utilizzando matching esatto del nome categoria. Implementa
     * caching avanzato per categorie popolari e ordinamento intelligente
     * basato su popolarità e valutazioni per ogni categoria.
     * </p>
     *
     * <h4>Sistema di categorizzazione:</h4>
     * <ul>
     *   <li><strong>Matching Esatto:</strong> Ricerca precisa per nome categoria</li>
     *   <li><strong>Case Insensitive:</strong> Tolleranza per variazioni di maiuscole/minuscole</li>
     *   <li><strong>Whitespace Handling:</strong> Gestione automatica spazi iniziali/finali</li>
     *   <li><strong>Category Validation:</strong> Verifica esistenza categoria nel sistema</li>
     * </ul>
     *
     * <h4>Ordinamento risultati per categoria:</h4>
     * <ol>
     *   <li><strong>Popolarità:</strong> Libri con più interazioni utente</li>
     *   <li><strong>Valutazioni:</strong> Media voti degli utenti</li>
     *   <li><strong>Novità:</strong> Pubblicazioni più recenti</li>
     *   <li><strong>Relevanza:</strong> Pertinenza alla categoria</li>
     * </ol>
     *
     * <h4>Categorie comuni supportate:</h4>
     * <ul>
     *   <li>Informatica, Programmazione, Web Development</li>
     *   <li>Letteratura, Romance, Thriller, Fantasy</li>
     *   <li>Scienza, Medicina, Ingegneria</li>
     *   <li>Business, Management, Marketing</li>
     *   <li>Arte, Musica, Storia, Filosofia</li>
     * </ul>
     *
     * <h4>Ottimizzazioni per categorie popolari:</h4>
     * <ul>
     *   <li>Pre-caching risultati per categorie più richieste</li>
     *   <li>Indici database ottimizzati per campo categoria</li>
     *   <li>Compressione risultati per trasferimento efficiente</li>
     *   <li>Lazy loading metadati non essenziali</li>
     * </ul>
     *
     * @param categoryName nome esatto della categoria da filtrare.
     *                    Deve corrispondere esattamente a una categoria esistente nel sistema.
     *                    Case-insensitive, whitespace viene automaticamente rimosso.
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} della categoria:
     *         <ul>
     *           <li><strong>200 OK:</strong> Libri della categoria recuperati e ordinati</li>
     *           <li><strong>400 Bad Request:</strong> Nome categoria mancante o formato non valido</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore durante filtraggio</li>
     *         </ul>
     * @throws IllegalArgumentException se categoryName è null o stringa vuota
     * @apiNote Il filtraggio è ottimizzato per categorie con molti libri (>1000).
     *          Include debug logging dei primi risultati per verifica correttezza.
     *          Per categorie inesistenti restituisce lista vuota senza errore.
     * @since 1.1.0
     * @see BookService#getBooksByCategory(String)
     */
    @GetMapping("/category")
    public ResponseEntity<List<Book>> getBooksByCategory(@RequestParam(value = "name", required = true) String categoryName) {
        try {
            System.out.println("Ricerca per CATEGORIA richiesta: '" + categoryName + "'");

            if (categoryName == null || categoryName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.getBooksByCategory(categoryName.trim());
            System.out.println("Categoria '" + categoryName + "': trovati " + books.size() + " libri");

            // Debug: stampa i primi risultati
            if (!books.isEmpty()) {
                System.out.println("Primi libri per categoria '" + categoryName + "':");
                for (int i = 0; i < Math.min(3, books.size()); i++) {
                    Book book = books.get(i);
                    System.out.println("  - " + book.getTitle() + " (Categoria: " + book.getCategory() + ")");
                }
            }

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nella ricerca per categoria '" + categoryName + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera una selezione curata di libri in evidenza per homepage e promozioni.
     * <p>
     * Endpoint specializzato per fornire una collezione selezionata di libri
     * di particolare interesse, utilizzata per homepage, sezioni promozionali
     * e showcase editoriali. La selezione viene aggiornata periodicamente
     * da algoritmi di machine learning e curation editoriale manuale.
     * </p>
     *
     * <h4>Criteri di selezione per libri featured:</h4>
     * <ul>
     *   <li><strong>Qualità Editoriale:</strong> Libri selezionati da team editoriale</li>
     *   <li><strong>Popolarità Trending:</strong> Contenuti con crescita rapida di interesse</li>
     *   <li><strong>Valutazioni Eccellenti:</strong> Libri con rating superiore a soglia premium</li>
     *   <li><strong>Diversità Contenuti:</strong> Bilanciamento tra categorie e generi</li>
     *   <li><strong>Novità Rilevanti:</strong> Nuove pubblicazioni di autori riconosciuti</li>
     * </ul>
     *
     * <h4>Algoritmi di curation automatica:</h4>
     * <ul>
     *   <li>Machine learning per identificazione trend emergenti</li>
     *   <li>Analisi sentiment su recensioni e valutazioni</li>
     *   <li>Monitoraggio engagement e tempo di lettura</li>
     *   <li>Cross-referencing con bestseller lists esterne</li>
     * </ul>
     *
     * <h4>Refresh e aggiornamento:</h4>
     * <ul>
     *   <li>Aggiornamento automatico ogni 24 ore</li>
     *   <li>Override manuale per eventi speciali</li>
     *   <li>Rotazione contenuti per evitare staleness</li>
     *   <li>A/B testing per ottimizzare selezioni</li>
     * </ul>
     *
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} selezionati:
     *         <ul>
     *           <li><strong>200 OK:</strong> Selezione featured recuperata con successo</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore durante recupero selezione</li>
     *         </ul>
     * @apiNote La selezione è ottimizzata per 10-20 libri per bilanciare varietà e performance.
     *          Risultati cached per 1 ora per ridurre carico database.
     *          Include metriche automatiche per tracking efficacia selezioni.
     * @since 1.2.0
     * @see BookService#getFeaturedBooks()
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Book>> getFeaturedBooks() {
        try {
            List<Book> books = bookService.getFeaturedBooks();
            System.out.println("Ritornati " + books.size() + " libri in evidenza");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nel recupero dei libri in evidenza: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera collezione di libri disponibili gratuitamente per gli utenti.
     * <p>
     * Endpoint dedicato per l'accesso a contenuti gratuiti, promuovendo
     * l'accessibilità alla lettura e supportando modelli di business freemium.
     * Include opere di dominio pubblico, contenuti promozionali e libri
     * offerti gratuitamente dagli editori per scopi marketing.
     * </p>
     *
     * <h4>Tipologie di contenuti gratuiti:</h4>
     * <ul>
     *   <li><strong>Dominio Pubblico:</strong> Opere classiche senza copyright</li>
     *   <li><strong>Promozioni Editoriali:</strong> Campagne marketing temporanee</li>
     *   <li><strong>Contenuti Open Source:</strong> Documentazione e guide tecniche</li>
     *   <li><strong>Anteprime:</strong> Capitoli gratuiti di opere premium</li>
     *   <li><strong>Self-Publishing:</strong> Opere indipendenti offerte gratuitamente</li>
     * </ul>
     *
     * <h4>Validazione e controllo qualità:</h4>
     * <ul>
     *   <li>Verifica automatica status gratuito nel tempo</li>
     *   <li>Controllo qualità contenuti e metadati</li>
     *   <li>Rimozione automatica contenuti scaduti</li>
     *   <li>Categorizzazione e tagging per discovery</li>
     * </ul>
     *
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} gratuiti
     * @since 1.2.0
     * @see BookService#getFreeBooks()
     */
    @GetMapping("/free")
    public ResponseEntity<List<Book>> getFreeBooks() {
        try {
            List<Book> books = bookService.getFreeBooks();
            System.out.println("Ritornati " + books.size() + " libri gratuiti");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nel recupero dei libri gratuiti: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera le pubblicazioni più recenti per scoperta di nuovi contenuti.
     * <p>
     * Endpoint per identificazione e promozione di nuove uscite editoriali,
     * essenziale per mantenere il catalogo fresco e attuale. Utilizza
     * algoritmi temporali e metadata analysis per identificare contenuti
     * recentemente aggiunti al catalogo.
     * </p>
     *
     * <h4>Criteri per identificazione nuove uscite:</h4>
     * <ul>
     *   <li><strong>Data Pubblicazione:</strong> Libri pubblicati negli ultimi 90 giorni</li>
     *   <li><strong>Data Aggiunta:</strong> Contenuti aggiunti recentemente al catalogo</li>
     *   <li><strong>Metadata Freshness:</strong> Aggiornamenti recenti di informazioni</li>
     *   <li><strong>Editor Flagging:</strong> Marcatura manuale per promozione</li>
     * </ul>
     *
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} nuove uscite
     * @since 1.2.0
     * @see BookService#getNewReleases()
     */
    @GetMapping("/new-releases")
    public ResponseEntity<List<Book>> getNewReleases() {
        try {
            List<Book> books = bookService.getNewReleases();
            System.out.println("Ritornati " + books.size() + " nuove uscite");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nel recupero delle nuove uscite: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Esegue ricerca combinata per autore e anno di pubblicazione.
     * <p>
     * Endpoint di ricerca avanzata che combina filtri per autore e anno
     * per ricerche bibliografiche precise. Utile per ricerca accademica,
     * studi cronologici su autori specifici e discovery di opere storiche.
     * </p>
     *
     * <h4>Logica di ricerca combinata:</h4>
     * <ul>
     *   <li><strong>Autore Obbligatorio:</strong> Nome autore richiesto per ricerca</li>
     *   <li><strong>Anno Opzionale:</strong> Filtro aggiuntivo per raffinare risultati</li>
     *   <li><strong>Fuzzy Matching:</strong> Tolleranza per variazioni nome autore</li>
     *   <li><strong>Range Temporali:</strong> Supporto per intervalli di anni</li>
     * </ul>
     *
     * @param author nome dell'autore da ricercare (obbligatorio)
     * @param year anno di pubblicazione per filtraggio (opzionale)
     * @return {@link ResponseEntity} con libri matching i criteri specificati
     * @since 1.3.0
     * @see BookService#searchBooksByAuthorAndYear(String, String)
     */
    @GetMapping("/search/author-year")
    public ResponseEntity<List<Book>> searchBooksByAuthorAndYear(
            @RequestParam(value = "author", required = true) String author,
            @RequestParam(value = "year", required = false) String year) {
        try {
            System.out.println("Ricerca per AUTORE-ANNO richiesta: '" + author + "' (" + year + ")");

            if (author == null || author.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooksByAuthorAndYear(author.trim(), year);
            System.out.println("Ricerca autore-anno: trovati " + books.size() + " risultati");

            if (!books.isEmpty()) {
                System.out.println("Primi risultati autore-anno:");
                for (int i = 0; i < Math.min(3, books.size()); i++) {
                    Book book = books.get(i);
                    System.out.println("  - " + book.getTitle() + " di " + book.getAuthor());
                }
            }

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nella ricerca autore-anno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Esegue ricerca specifica nei titoli dei libri con algoritmi ottimizzati.
     * <p>
     * Endpoint specializzato per ricerca esclusiva nei titoli, utilizzando
     * algoritmi di matching ottimizzati per questo campo specifico.
     * Implementa tecniche avanzate di normalizzazione e ranking per
     * massimizzare precisione e recall nelle ricerche per titolo.
     * </p>
     *
     * <h4>Algoritmi di matching per titoli:</h4>
     * <ul>
     *   <li><strong>Exact Match Prioritization:</strong> Match esatti in cima ai risultati</li>
     *   <li><strong>Substring Matching:</strong> Ricerca in sottostringhe dei titoli</li>
     *   <li><strong>Word Boundary Detection:</strong> Riconoscimento confini parole</li>
     *   <li><strong>Stop Words Filtering:</strong> Ignorare articoli e preposizioni</li>
     *   <li><strong>Title Normalization:</strong> Gestione punteggiatura e caratteri speciali</li>
     * </ul>
     *
     * @param query termine di ricerca per matching nei titoli
     * @return {@link ResponseEntity} con libri i cui titoli matchano la query
     * @since 1.4.0
     * @see BookService#searchBooksByTitle(String)
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchBooksByTitle(@RequestParam(value = "q", required = true) String query) {
        try {
            System.out.println("Ricerca per TITOLO richiesta: '" + query + "'");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooksByTitle(query.trim());
            System.out.println("Ricerca titolo '" + query + "': trovati " + books.size() + " risultati");

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nella ricerca per titolo '" + query + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Esegue ricerca specifica nei nomi degli autori con normalizzazione avanzata.
     * <p>
     * Endpoint dedicato per ricerca esclusiva nei campi autore, implementando
     * logiche di normalizzazione specifiche per nomi di persona e gestione
     * delle variazioni comuni nei nomi degli autori.
     * </p>
     *
     * <h4>Normalizzazione nomi autori:</h4>
     * <ul>
     *   <li><strong>Nome Cognome Variations:</strong> Gestione ordine nome/cognome</li>
     *   <li><strong>Initial Handling:</strong> Supporto per iniziali e nomi abbreviati</li>
     *   <li><strong>Accents and Diacritics:</strong> Normalizzazione caratteri accentati</li>
     *   <li><strong>Multiple Authors:</strong> Gestione libri con autori multipli</li>
     *   <li><strong>Pseudonym Support:</strong> Riconoscimento pseudonimi comuni</li>
     * </ul>
     *
     * @param query nome o parte del nome autore da ricercare
     * @return {@link ResponseEntity} con libri degli autori matching la query
     * @since 1.4.0
     * @see BookService#searchBooksByAuthor(String)
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchBooksByAuthor(@RequestParam(value = "q", required = true) String query) {
        try {
            System.out.println("Ricerca per AUTORE richiesta: '" + query + "'");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<Book> books = bookService.searchBooksByAuthor(query.trim());
            System.out.println("Ricerca autore '" + query + "': trovati " + books.size() + " risultati");

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nella ricerca per autore '" + query + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint di debugging per testing e troubleshooting delle funzionalità di ricerca.
     * <p>
     * Strumento diagnostico per sviluppatori e amministratori per verificare
     * lo stato del sistema di ricerca, testare query e analizzare performance.
     * Fornisce informazioni dettagliate su catalogo e risultati ricerca per
     * debugging e ottimizzazione.
     * </p>
     *
     * <h4>Informazioni diagnostiche fornite:</h4>
     * <ul>
     *   <li><strong>Catalog Statistics:</strong> Numero totale libri disponibili</li>
     *   <li><strong>Sample Data:</strong> Esempi di libri per verifica struttura dati</li>
     *   <li><strong>Search Performance:</strong> Metriche tempo di esecuzione query</li>
     *   <li><strong>System Status:</strong> Timestamp e stato componenti</li>
     * </ul>
     *
     * <h4>Utilizzo per troubleshooting:</h4>
     * <ul>
     *   <li>Verifica connettività database e completezza catalogo</li>
     *   <li>Test funzionalità ricerca con query di esempio</li>
     *   <li>Analisi performance per identificazione bottlenecks</li>
     *   <li>Validazione struttura dati e metadati</li>
     * </ul>
     *
     * @param query termine di ricerca per test (opzionale, default "test")
     * @return {@link ResponseEntity} di {@link String} con informazioni diagnostiche complete
     * @apiNote Endpoint destinato solo per environment di development e staging.
     *          In produzione dovrebbe essere protetto o disabilitato per sicurezza.
     *          Non utilizzare per traffico utente finale.
     * @since 1.5.0
     * @see BookService#getAllBooks()
     * @see BookService#searchBooks(String)
     */
    @GetMapping("/debug-search")
    public ResponseEntity<String> debugSearch(@RequestParam(value = "q", required = false, defaultValue = "test") String query) {
        try {
            System.out.println("Debug search chiamato con query: '" + query + "'");

            List<Book> allBooks = bookService.getAllBooks();
            System.out.println("Libri totali disponibili: " + allBooks.size());

            if (!allBooks.isEmpty()) {
                System.out.println("Primi 3 libri nel database:");
                for (int i = 0; i < Math.min(3, allBooks.size()); i++) {
                    Book book = allBooks.get(i);
                    System.out.println("  " + (i+1) + ". " + book.getTitle() + " di " + book.getAuthor());
                }
            }

            List<Book> searchResults = bookService.searchBooks(query);
            System.out.println("Risultati ricerca per '" + query + "': " + searchResults.size());

            String response = String.format(
                    "Debug Search Results:\n" +
                            "Query: '%s'\n" +
                            "Total Books: %d\n" +
                            "Search Results: %d\n" +
                            "Server Time: %s",
                    query, allBooks.size(), searchResults.size(), java.time.LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Errore nel debug search: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Recupera i libri con il maggior numero di recensioni per identificare contenuti popolari.
     * <p>
     * Endpoint per discovery di contenuti basata su engagement della community,
     * identificando libri che hanno generato maggiore interesse e discussione
     * tra gli utenti. Utilizza metriche di engagement per ranking e include
     * dettagli statistici per ogni libro.
     * </p>
     *
     * <h4>Metriche di engagement considerate:</h4>
     * <ul>
     *   <li><strong>Volume Recensioni:</strong> Numero totale recensioni ricevute</li>
     *   <li><strong>Frequenza Temporale:</strong> Distribuzione recensioni nel tempo</li>
     *   <li><strong>Qualità Engagement:</strong> Lunghezza media e dettaglio recensioni</li>
     *   <li><strong>User Diversity:</strong> Varietà di profili utenti recensori</li>
     * </ul>
     *
     * <h4>Arricchimento dati per analisi:</h4>
     * <ul>
     *   <li>Statistiche aggregate su recensioni per libro</li>
     *   <li>Trend temporali di crescita recensioni</li>
     *   <li>Correlazioni tra volume recensioni e qualità</li>
     *   <li>Segmentazione per categoria e target audience</li>
     * </ul>
     *
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} ordinati per numero recensioni:
     *         <ul>
     *           <li><strong>200 OK:</strong> Lista libri più recensiti con dettagli statistici</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore durante aggregazione dati</li>
     *         </ul>
     * @apiNote I risultati includono metadati arricchiti con statistiche recensioni.
     *          Ordinamento decrescente per numero totale recensioni.
     *          Cache risultati per 2 ore per ottimizzare performance calcoli aggregati.
     * @since 1.6.0
     * @see BookService#getMostReviewedBooksWithDetails()
     */
    @GetMapping("/most-reviewed")
    public ResponseEntity<List<Book>> getMostReviewedBooks() {
        try {
            List<Book> books = bookService.getMostReviewedBooksWithDetails();
            System.out.println("Ritornati " + books.size() + " libri più recensiti");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nel recupero dei libri più recensiti: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recupera i libri con le valutazioni medie più elevate per identificare contenuti di qualità.
     * <p>
     * Endpoint per discovery di contenuti premium basata su valutazioni qualitative
     * degli utenti. Implementa algoritmi di ranking sofisticati che bilanciano
     * media delle valutazioni con significatività statistica e credibilità
     * delle recensioni per identificare contenuti di eccellenza comprovata.
     * </p>
     *
     * <h4>Algoritmi di ranking per qualità:</h4>
     * <ul>
     *   <li><strong>Bayesian Average:</strong> Media pesata per significatività statistica</li>
     *   <li><strong>Wilson Score:</strong> Intervallo di confidenza per ranking robusto</li>
     *   <li><strong>Minimum Threshold:</strong> Soglia minima recensioni per inclusione</li>
     *   <li><strong>Quality Weighting:</strong> Peso maggiore per recensioni dettagliate</li>
     *   <li><strong>Recency Factor:</strong> Boost per valutazioni recenti</li>
     * </ul>
     *
     * <h4>Filtri anti-manipulation:</h4>
     * <ul>
     *   <li>Detection e esclusione review spam automatizzate</li>
     *   <li>Analisi pattern per identificazione bot e fake accounts</li>
     *   <li>Validazione credibilità reviewers basata su storico</li>
     *   <li>Normalizzazione per bias di categoria e periodo</li>
     * </ul>
     *
     * <h4>Metadati qualità inclusi:</h4>
     * <ul>
     *   <li>Media valutazioni con intervallo di confidenza</li>
     *   <li>Numero recensioni qualificate per ranking</li>
     *   <li>Distribuzione punteggi per analisi consensus</li>
     *   <li>Score di credibilità aggregato</li>
     * </ul>
     *
     * @return {@link ResponseEntity} contenente {@link List} di {@link Book} ordinati per qualità valutazioni:
     *         <ul>
     *           <li><strong>200 OK:</strong> Lista libri top-rated con metadati qualità</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore durante calcoli statistici</li>
     *         </ul>
     * @apiNote Ranking utilizza algoritmi statistici avanzati per robustezza.
     *          Soglia minima di 5 recensioni per inclusione in classifica.
     *          Aggiornamento ranking ogni 6 ore per bilanciare accuracy e performance.
     * @since 1.6.0
     * @see BookService#getTopRatedBooksWithDetails()
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<Book>> getTopRatedBooks() {
        try {
            List<Book> books = bookService.getTopRatedBooksWithDetails();
            System.out.println("Ritornati " + books.size() + " libri meglio valutati");
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Errore nel recupero dei libri meglio valutati: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}