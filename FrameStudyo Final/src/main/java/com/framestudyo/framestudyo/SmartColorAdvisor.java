package com.framestudyo.framestudyo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.*;

public class SmartColorAdvisor {

    // ── Public entry point ─────────────────────────────────────────────────

    public static void show(Stage owner, DesignCanvas designCanvas) {
        List<String> canvasColors = extractColors(designCanvas);
        List<String> suggested   = generateSuggestions(canvasColors);

        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.NONE);
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setTitle("Smart Palette");

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setStyle(
                "-fx-background-color: #1e1235;" +
                        "-fx-border-color: #3ddcf8;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );
        root.setPrefWidth(280);

        // Title
        Label title = new Label("✦ Smart Color Palette");
        title.setStyle("-fx-text-fill: #3ddcf8; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-weight: bold; -fx-font-size: 14px;");

        // Canvas colors section
        Label canvasLbl = new Label("Colors on canvas:");
        canvasLbl.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;");
        HBox canvasRow = buildColorRow(canvasColors.isEmpty()
                ? List.of("#ffffff") : canvasColors, "Canvas color");

        // Suggested colors section
        Label suggestLbl = new Label("Suggested complements:");
        suggestLbl.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;");
        HBox suggestRow = buildColorRow(suggested, "Suggested color");

        // Harmony type labels
        Label harmonyLbl = new Label("Harmony: Complementary + Triadic split");
        harmonyLbl.setStyle("-fx-text-fill: #888; -fx-font-family: 'Segoe UI'; -fx-font-size: 10px;");

        // Close button
        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: #3e7e9f; -fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 11px;" +
                        "-fx-background-radius: 20; -fx-padding: 4 14 4 14; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.close());
        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, canvasLbl, canvasRow,
                new javafx.scene.control.Separator(),
                suggestLbl, suggestRow, harmonyLbl, btnRow);

        popup.setScene(new Scene(root));
        popup.show();

        // Position near owner
        popup.setX(owner.getX() + owner.getWidth() - 310);
        popup.setY(owner.getY() + 80);
    }

    // ── Color extraction ───────────────────────────────────────────────────

    private static List<String> extractColors(DesignCanvas dc) {
        Set<String> seen = new LinkedHashSet<>();
        for (DesignElement el : dc.getElements()) {
            if (el.getFillColor() != null && !el.getFillColor().isEmpty()
                    && !el.getFillColor().equalsIgnoreCase("transparent")) {
                seen.add(el.getFillColor().toUpperCase());
            }
        }
        List<String> list = new ArrayList<>(seen);
        // Cap display to 6
        return list.size() > 6 ? list.subList(0, 6) : list;
    }

    // ── Suggestion engine ──────────────────────────────────────────────────

    private static List<String> generateSuggestions(List<String> hexColors) {
        List<String> suggestions = new ArrayList<>();

        if (hexColors.isEmpty()) {
            // Default pleasant palette when canvas is empty
            suggestions.addAll(List.of("#FF6B9D", "#C44DFF", "#4DAFFF", "#43E8A0", "#FFD166"));
            return suggestions;
        }

        Set<String> used = new HashSet<>(hexColors);

        for (String hex : hexColors) {
            double[] hsl = hexToHsl(hex);

            // Complementary (180° flip)
            String comp = hslToHex((hsl[0] + 180) % 360, hsl[1], hsl[2]);
            if (!used.contains(comp.toUpperCase())) {
                suggestions.add(comp);
                used.add(comp.toUpperCase());
            }

            // Triadic split +120°
            String tri1 = hslToHex((hsl[0] + 120) % 360, hsl[1], hsl[2]);
            if (!used.contains(tri1.toUpperCase())) {
                suggestions.add(tri1);
                used.add(tri1.toUpperCase());
            }

            // Triadic split +240°
            String tri2 = hslToHex((hsl[0] + 240) % 360, hsl[1], hsl[2]);
            if (!used.contains(tri2.toUpperCase())) {
                suggestions.add(tri2);
                used.add(tri2.toUpperCase());
            }

            // Slightly lighter tint
            String tint = hslToHex(hsl[0], Math.max(0, hsl[1] - 15),
                    Math.min(95, hsl[2] + 20));
            if (!used.contains(tint.toUpperCase())) {
                suggestions.add(tint);
                used.add(tint.toUpperCase());
            }

            if (suggestions.size() >= 6) break;
        }

        return suggestions.size() > 6 ? suggestions.subList(0, 6) : suggestions;
    }

    // ── UI helpers ─────────────────────────────────────────────────────────

    private static HBox buildColorRow(List<String> hexColors, String tooltipBase) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        for (String hex : hexColors) {
            Rectangle swatch = new Rectangle(36, 36);
            swatch.setArcWidth(8);
            swatch.setArcHeight(8);
            try {
                swatch.setFill(Color.web(hex));
            } catch (Exception ex) {
                swatch.setFill(Color.GRAY);
            }
            swatch.setStroke(Color.web("#3ddcf8"));
            swatch.setStrokeWidth(1);
            Tooltip.install(swatch, new Tooltip(tooltipBase + ": " + hex));

            // Click to copy hex to clipboard
            swatch.setOnMouseClicked(e -> {
                javafx.scene.input.Clipboard cb =
                        javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content =
                        new javafx.scene.input.ClipboardContent();
                content.putString(hex);
                cb.setContent(content);
            });
            swatch.setCursor(javafx.scene.Cursor.HAND);
            row.getChildren().add(swatch);
        }
        return row;
    }

    // ── Color math ─────────────────────────────────────────────────────────

    /** Converts #RRGGBB → [hue°, saturation%, lightness%] */
    private static double[] hexToHsl(String hex) {
        try {
            Color c = Color.web(hex);
            double r = c.getRed(), g = c.getGreen(), b = c.getBlue();
            double max = Math.max(r, Math.max(g, b));
            double min = Math.min(r, Math.min(g, b));
            double l = (max + min) / 2.0;
            double s = 0, h = 0;
            if (max != min) {
                double d = max - min;
                s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
                if (max == r)      h = (g - b) / d + (g < b ? 6 : 0);
                else if (max == g) h = (b - r) / d + 2;
                else               h = (r - g) / d + 4;
                h *= 60;
            }
            return new double[]{h, s * 100, l * 100};
        } catch (Exception e) {
            return new double[]{0, 0, 50};
        }
    }

    /** Converts [hue°, saturation%, lightness%] → #RRGGBB */
    private static String hslToHex(double h, double s, double l) {
        double sn = s / 100.0, ln = l / 100.0;
        double c = (1 - Math.abs(2 * ln - 1)) * sn;
        double x = c * (1 - Math.abs((h / 60) % 2 - 1));
        double m = ln - c / 2;
        double r = 0, g = 0, b = 0;
        int sector = (int)(h / 60) % 6;
        switch (sector) {
            case 0: r=c; g=x; break;
            case 1: r=x; g=c; break;
            case 2: g=c; b=x; break;
            case 3: g=x; b=c; break;
            case 4: r=x; b=c; break;
            case 5: r=c; b=x; break;
        }
        return String.format("#%02X%02X%02X",
                (int)((r+m)*255), (int)((g+m)*255), (int)((b+m)*255));
    }
}