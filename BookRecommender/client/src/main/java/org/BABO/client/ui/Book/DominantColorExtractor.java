package org.BABO.client.ui.Book;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.BABO.client.ui.AppleBooksClient;

/**
 * Utility per estrarre il colore dominante da un'immagine
 * Utilizzata nel client per generare sfondi dinamici
 */
public class DominantColorExtractor {

    /**
     * Metodo per estrarre il colore dominante da un'immagine
     */
    public static Color extractDominantColor(String imageFileName) {
        Image image;
        try (InputStream stream = AppleBooksClient.class.getResourceAsStream("/books_covers/" + imageFileName)) {
            // Controlla se il file esiste
            if (stream == null) {
                System.out.println("🔍 DominantColorExtractor: File non trovato: " + imageFileName);
                // Usa un'immagine di placeholder se il file non è trovato
                try {
                    // Prova placeholder locale
                    InputStream placeholderStream = AppleBooksClient.class.getResourceAsStream("/books_covers/placeholder.jpg");
                    if (placeholderStream != null) {
                        image = new Image(placeholderStream);
                    } else {
                        // Fallback su placeholder online
                        image = new Image("https://via.placeholder.com/100x150/333333/ffffff?text=Libro");
                    }
                } catch (Exception ex) {
                    // Ultimo fallback
                    return Color.rgb(41, 35, 46);
                }
            } else {
                // Carica l'immagine dal file
                image = new Image(stream);
            }
        } catch (Exception e) {
            // Gestisce eventuali errori durante il caricamento dell'immagine
            System.out.println("⚠️ DominantColorExtractor: Errore nel caricamento immagine: " + imageFileName);
            return Color.rgb(41, 35, 46); // Colore di fallback
        }

        // Verifica che l'immagine sia stata caricata correttamente
        if (image.isError()) {
            System.out.println("⚠️ DominantColorExtractor: Immagine corrotta: " + imageFileName);
            return Color.rgb(41, 35, 46);
        }

        // Ottiene il lettore di pixel dall'immagine
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            System.out.println("⚠️ DominantColorExtractor: Impossibile leggere pixel: " + imageFileName);
            return Color.rgb(41, 35, 46); // Ritorna un colore di default se non è possibile leggere i pixel
        }

        // Mappa per contare la frequenza di ogni colore
        Map<Integer, Integer> colorMap = new HashMap<>();
        int width = (int) image.getWidth(); // Larghezza dell'immagine
        int height = (int) image.getHeight(); // Altezza dell'immagine

        // Verifica dimensioni valide
        if (width <= 0 || height <= 0) {
            System.out.println("⚠️ DominantColorExtractor: Dimensioni immagine non valide: " + width + "x" + height);
            return Color.rgb(41, 35, 46);
        }

        // Ottimizzazione: usa un passo maggiore per immagini grandi
        int step = Math.max(1, Math.min(width, height) / 50); // Adatta il passo in base alla dimensione
        step = Math.max(step, 3); // Minimo 3 per performance

        System.out.println("🎨 DominantColorExtractor: Analizzando " + imageFileName + " (" + width + "x" + height + ") step=" + step);

        // Scorre i pixel dell'immagine con un passo variabile per ridurre il carico computazionale
        int sampleCount = 0;
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                try {
                    Color color = pixelReader.getColor(x, y);

                    // Ignora i pixel troppo trasparenti o troppo chiari/scuri
                    if (color.getOpacity() < 0.5 ||
                            color.getBrightness() > 0.95 ||
                            color.getBrightness() < 0.05) {
                        continue;
                    }

                    // Converte il colore in un valore RGB intero
                    int r = (int) (color.getRed() * 255);
                    int g = (int) (color.getGreen() * 255);
                    int b = (int) (color.getBlue() * 255);

                    int key = (r << 16) | (g << 8) | b; // Codifica RGB in un unico intero
                    // Incrementa il conteggio del colore nella mappa
                    colorMap.put(key, colorMap.getOrDefault(key, 0) + 1);
                    sampleCount++;
                } catch (Exception e) {
                    // Continua se c'è un errore nel leggere un pixel specifico
                    continue;
                }
            }
        }

        System.out.println("🔍 DominantColorExtractor: Campionati " + sampleCount + " pixel, trovati " + colorMap.size() + " colori unici");

        // Se non ci sono colori validi, ritorna il colore di default
        if (colorMap.isEmpty()) {
            System.out.println("⚠️ DominantColorExtractor: Nessun colore valido trovato");
            return Color.rgb(41, 35, 46);
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

        // Crea il colore dominante
        Color dominant = Color.rgb(r, g, b);

        // Scurisce leggermente il colore per evitare che sia troppo acceso
        Color finalColor = dominant.interpolate(Color.BLACK, 0.3); // Scurisce del 30%

        System.out.println("✅ DominantColorExtractor: Colore dominante per " + imageFileName + ": RGB(" +
                (int)(finalColor.getRed()*255) + "," +
                (int)(finalColor.getGreen()*255) + "," +
                (int)(finalColor.getBlue()*255) + ")");

        return finalColor;
    }

    /**
     * Versione alternativa che accetta direttamente un'immagine Image
     */
    public static Color extractDominantColor(Image image) {
        if (image == null || image.isError()) {
            return Color.rgb(41, 35, 46);
        }

        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            return Color.rgb(41, 35, 46);
        }

        Map<Integer, Integer> colorMap = new HashMap<>();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        if (width <= 0 || height <= 0) {
            return Color.rgb(41, 35, 46);
        }

        int step = Math.max(3, Math.min(width, height) / 50);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                try {
                    Color color = pixelReader.getColor(x, y);

                    if (color.getOpacity() < 0.5 ||
                            color.getBrightness() > 0.95 ||
                            color.getBrightness() < 0.05) {
                        continue;
                    }

                    int r = (int) (color.getRed() * 255);
                    int g = (int) (color.getGreen() * 255);
                    int b = (int) (color.getBlue() * 255);

                    int key = (r << 16) | (g << 8) | b;
                    colorMap.put(key, colorMap.getOrDefault(key, 0) + 1);
                } catch (Exception e) {
                    continue;
                }
            }
        }

        if (colorMap.isEmpty()) {
            return Color.rgb(41, 35, 46);
        }

        int dominantKey = 0;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : colorMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantKey = entry.getKey();
            }
        }

        int r = (dominantKey >> 16) & 0xFF;
        int g = (dominantKey >> 8) & 0xFF;
        int b = dominantKey & 0xFF;

        Color dominant = Color.rgb(r, g, b);
        return dominant.interpolate(Color.BLACK, 0.3);
    }
}