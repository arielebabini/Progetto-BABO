package org.BABO.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Punto di ingresso principale per l'applicazione server.
 * <p>
 * Questa classe avvia l'applicazione Spring Boot che funge da backend per il progetto.
 * È configurata per gestire le richieste web e include una configurazione
 * Cross-Origin Resource Sharing (CORS) per consentire le comunicazioni
 * con il client frontend. La configurazione CORS è essenziale per
 * prevenire errori di sicurezza del browser quando il client e il server
 * sono su origini diverse.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 * <li><strong>Avvio Applicazione:</strong> Esegue l'applicazione Spring Boot.</li>
 * <li><strong>Configurazione CORS:</strong> Abilita e configura i permessi CORS
 * per garantire che il client frontend possa comunicare con il server senza restrizioni.</li>
 * </ul>
 *
 * <h3>Configurazione CORS:</h3>
 * <p>
 * L'applicazione è configurata per accettare richieste da qualsiasi origine (wildcard *).
 * I metodi HTTP consentiti sono {@code GET}, {@code POST}, {@code PUT}, {@code DELETE},
 * e {@code OPTIONS}. Sono inoltre permessi tutti gli header HTTP.
 * Questa configurazione semplificata è adatta per l'ambiente di sviluppo e
 * dovrebbe essere rivista con politiche di sicurezza più restrittive in produzione.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see SpringApplication
 * @see SpringBootApplication
 * @see WebMvcConfigurer
 */
@SpringBootApplication
public class ServerApplication {

    /**
     * Il metodo principale che avvia l'applicazione Spring Boot.
     * <p>
     * Questo è il punto di partenza dell'intera applicazione server. Invoca
     * {@link SpringApplication#run(Class, String[])} per inizializzare il
     * contesto dell'applicazione, caricare i bean e avviare il server web integrato.
     * </p>
     *
     * @param args gli argomenti della riga di comando.
     */
    public static void main(String[] args) {
        System.out.println("🚀 Avvio Apple Books Server...");
        SpringApplication.run(ServerApplication.class, args);
        System.out.println("✅ Server avviato...");
    }

    /**
     * Fornisce la configurazione globale CORS.
     * <p>
     * Questo bean personalizza il comportamento di Cross-Origin Resource Sharing
     * per l'intera applicazione. Consente l'accesso da qualsiasi origine, con
     * credenziali supportate, e per tutti i metodi HTTP (GET, POST, PUT, ecc.)
     * e header.
     * </p>
     *
     * @return un'istanza di {@link WebMvcConfigurer} configurata per gestire CORS.
     *
     * @apiNote Questa configurazione è permissiva e serve a facilitare
     * lo sviluppo. In un ambiente di produzione, le origini e i metodi
     * dovrebbero essere specificati esplicitamente per una maggiore sicurezza.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}