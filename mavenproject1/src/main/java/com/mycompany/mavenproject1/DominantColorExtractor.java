package com.mycompany.mavenproject1;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class DominantColorExtractor {

    // Metodo per estrarre il colore dominante da un'immagine
    public static Color extractDominantColor(String imageFileName) {
        Image image;
        try (InputStream stream = AppleBooksClone.class.getResourceAsStream("/books_covers/" + imageFileName)) {
            // Controlla se il file esiste
            if (stream == null) {
                System.out.println("DominantColorExtractor: File non trovato: " + imageFileName);
                // Usa un'immagine di placeholder se il file non è trovato
                image = new Image("https://via.placeholder.com/100x150");
            } else {
                // Carica l'immagine dal file
                image = new Image(stream);
            }
        } catch (Exception e) {
            // Gestisce eventuali errori durante il caricamento dell'immagine
            System.out.println("DominantColorExtractor: Errore nel caricamento immagine: " + imageFileName);
            image = new Image("https://via.placeholder.com/100x150");
        }

        // Ottiene il lettore di pixel dall'immagine
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) return Color.LIGHTGRAY; // Ritorna un colore di default se non è possibile leggere i pixel

        // Mappa per contare la frequenza di ogni colore
        Map<Integer, Integer> colorMap = new HashMap<>();
        int width = (int) image.getWidth(); // Larghezza dell'immagine
        int height = (int) image.getHeight(); // Altezza dell'immagine

        // Scorre i pixel dell'immagine con un passo di 3 per ridurre il carico computazionale
        for (int y = 0; y < height; y += 3) {
            for (int x = 0; x < width; x += 3) {
                Color color = pixelReader.getColor(x, y);

                // Ignora i pixel troppo trasparenti o troppo chiari
                if (color.getOpacity() < 0.5 || color.getBrightness() > 0.95) continue;

                // Converte il colore in un valore RGB intero
                int r = (int) (color.getRed() * 255);
                int g = (int) (color.getGreen() * 255);
                int b = (int) (color.getBlue() * 255);

                int key = (r << 16) | (g << 8) | b; // Codifica RGB in un unico intero
                // Incrementa il conteggio del colore nella mappa
                colorMap.put(key, colorMap.getOrDefault(key, 0) + 1);
            }
        }

        // Trova il colore con la frequenza massima
        int dominantKey = 0;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : colorMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantKey = entry.getKey();
            }
        }

        // Decodifica il colore dominante dall'intero RGB
        int r = (dominantKey >> 16) & 0xFF;
        int g = (dominantKey >> 8) & 0xFF;
        int b = dominantKey & 0xFF;

        // Scurisce leggermente il colore per evitare che sia troppo acceso
        Color dominant = Color.rgb(r, g, b);
        return dominant.interpolate(Color.BLACK, 0.3); // Scurisce del 30%
    }
}