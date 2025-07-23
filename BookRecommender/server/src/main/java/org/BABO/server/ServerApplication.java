package org.BABO.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Applicazione server Spring Boot per Apple Books Clone
 * CORS configuration corretta per Spring Boot 3.x
 */
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Avvio Apple Books Server...");
        SpringApplication.run(ServerApplication.class, args);
        System.out.println("✅ Server avviato su http://localhost:8080");
        System.out.println("📚 API disponibili:");
        System.out.println("  GET /api/books - Tutti i libri");
        System.out.println("  GET /api/books/{id} - Libro specifico");
        System.out.println("  GET /api/books/featured - Libri in evidenza");
        System.out.println("  GET /api/books/free - Libri gratuiti");
        System.out.println("  GET /api/books/new-releases - Nuove uscite");
        System.out.println("  GET /api/books/search?q={query} - Ricerca libri");
    }

    /**
     * Configurazione CORS corretta per Spring Boot 3.x
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")  // Usa allowedOriginPatterns invece di allowedOrigins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);  // Disabilita credentials per evitare conflitti
            }
        };
    }

    /**
     * Configurazione CORS alternativa (bean based)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Usa allowedOriginPatterns invece di allowedOrigins per Spring Boot 3.x
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Non impostare allowCredentials=true quando usi "*" come origin
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}