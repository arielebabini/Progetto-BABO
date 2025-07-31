package org.BABO.client.ui;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * VERSIONE DEFINITIVA - Carica SOLO il tuo logo.png
 * Cross-platform: Windows + macOS
 * Nessun fallback, nessuna opzione
 */
public class IconUtils {

    private static Image cachedAppIcon = null;

    /**
     * Imposta l'icona per uno stage
     */
    public static void setStageIcon(Stage stage) {
        if (stage == null) return;

        try {
            Image appIcon = getApplicationIcon();
            if (appIcon != null) {
                stage.getIcons().clear();
                stage.getIcons().add(appIcon);
                System.out.println("✅ Icona impostata per: " + stage.getTitle());
            } else {
                System.err.println("❌ Impossibile caricare icona per: " + stage.getTitle());
            }
        } catch (Exception e) {
            System.err.println("❌ Errore setting icona: " + e.getMessage());
        }
    }

    /**
     * Ottiene l'icona dell'applicazione (con cache)
     */
    public static Image getApplicationIcon() {
        if (cachedAppIcon != null) {
            return cachedAppIcon;
        }

        cachedAppIcon = loadPNGIcon();
        return cachedAppIcon;
    }

    /**
     * Carica SOLO il logo.png - semplice e diretto
     */
    private static Image loadPNGIcon() {
        System.out.println("🔍 Caricamento /logo/logo.png...");

        try {
            // Carica direttamente il PNG
            java.io.InputStream iconStream = IconUtils.class.getResourceAsStream("/logo/logo.png");

            if (iconStream == null) {
                System.err.println("❌ File /logo/logo.png NON TROVATO");
                System.err.println("   Verifica: src/main/resources/logo/logo.png");
                return null;
            }

            // Carica l'immagine PNG
            Image icon = new Image(iconStream);

            // Chiudi stream
            iconStream.close();

            // Verifica validità
            if (icon.isError()) {
                System.err.println("❌ Errore PNG: " + icon.getException().getMessage());
                return null;
            }

            if (icon.getWidth() <= 0 || icon.getHeight() <= 0) {
                System.err.println("❌ Dimensioni PNG non valide: " + icon.getWidth() + "x" + icon.getHeight());
                return null;
            }

            System.out.println("✅ PNG caricato: " + icon.getWidth() + "x" + icon.getHeight());
            return icon;

        } catch (Exception e) {
            System.err.println("❌ Eccezione caricamento PNG:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Imposta dock/taskbar icon (cross-platform)
     */
    public static void setSystemTrayIcon() {
        try {
            Image icon = getApplicationIcon();
            if (icon == null) return;

            String os = System.getProperty("os.name").toLowerCase();
            System.out.println("🖥️ OS: " + os);

            if (os.contains("mac")) {
                setMacDockIcon(icon);
            } else if (os.contains("windows")) {
                System.out.println("🪟 Windows: Icona taskbar gestita da JavaFX");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore system icon: " + e.getMessage());
        }
    }

    /**
     * Dock macOS
     */
    private static void setMacDockIcon(Image fxIcon) {
        try {
            if (!java.awt.Taskbar.isTaskbarSupported()) {
                System.out.println("⚠️ macOS Taskbar non supportato");
                return;
            }

            java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
            if (!taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                System.out.println("⚠️ macOS dock icon non supportato");
                return;
            }

            // Converti in BufferedImage
            java.awt.image.BufferedImage awtImage = fxToAWT(fxIcon);
            if (awtImage != null) {
                taskbar.setIconImage(awtImage);
                System.out.println("✅ Dock icon macOS impostata");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore dock macOS: " + e.getMessage());
        }
    }

    /**
     * Conversione JavaFX -> AWT (per macOS dock)
     */
    private static java.awt.image.BufferedImage fxToAWT(Image fxImage) {
        try {
            int w = (int) fxImage.getWidth();
            int h = (int) fxImage.getHeight();

            java.awt.image.BufferedImage awtImage = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            javafx.scene.image.PixelReader pixelReader = fxImage.getPixelReader();

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    javafx.scene.paint.Color fxColor = pixelReader.getColor(x, y);
                    int r = (int)(fxColor.getRed() * 255);
                    int g = (int)(fxColor.getGreen() * 255);
                    int b = (int)(fxColor.getBlue() * 255);
                    int a = (int)(fxColor.getOpacity() * 255);
                    int argb = (a << 24) | (r << 16) | (g << 8) | b;
                    awtImage.setRGB(x, y, argb);
                }
            }

            return awtImage;

        } catch (Exception e) {
            System.err.println("❌ Errore conversione FX->AWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Info icona
     */
    public static String getIconInfo() {
        Image icon = getApplicationIcon();
        if (icon == null) return "Nessuna icona";
        if (icon.isError()) return "Errore icona";
        return String.format("PNG: %.0fx%.0f", icon.getWidth(), icon.getHeight());
    }

    /**
     * Disponibilità icona
     */
    public static boolean isIconAvailable() {
        Image icon = getApplicationIcon();
        return icon != null && !icon.isError() && icon.getWidth() > 0;
    }

    /**
     * Pulisce cache
     */
    public static void clearCache() {
        cachedAppIcon = null;
    }
}