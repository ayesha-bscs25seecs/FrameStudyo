package com.framestudyo.framestudyo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

/**
 * DesignAnalyzer
 *
 * Analyzes the current canvas and gives a scored design report across:
 *   - Balance     : how centered is the visual weight?
 *   - Contrast    : how varied are the colors?
 *   - Density     : how much of the canvas is used?
 *   - Complexity  : is there too much or too little going on?
 *
 * Usage in HelloApplication:
 *   Button analyzeBtn = styledBtn("📊 Analyze", "#2b588b", "#3e7e9f");
 *   analyzeBtn.setOnAction(e ->
 *       DesignAnalyzer.show(stage, designCanvas, 860, 600));
 */
public class DesignAnalyzer {

    private static final double CANVAS_W = 860;
    private static final double CANVAS_H = 600;

    // ── Public entry point ─────────────────────────────────────────────────

    public static void show(Stage owner, DesignCanvas designCanvas,
                            double canvasW, double canvasH) {

        List<DesignElement> elements = designCanvas.getElements();

        // Compute scores (0–100)
        int balanceScore   = scoreBalance(elements, canvasW, canvasH);
        int contrastScore  = scoreContrast(elements);
        int densityScore   = scoreDensity(elements, canvasW, canvasH);
        int complexityScore = scoreComplexity(elements);
        int overallScore   = (balanceScore + contrastScore + densityScore + complexityScore) / 4;

        // Build the popup
        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setTitle("Design Analysis");

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(320);
        root.setStyle(
                "-fx-background-color: #1e1235;" +
                        "-fx-border-color: #3ddcf8;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;"
        );

        Label title = new Label("✦ Design Score");
        title.setStyle("-fx-text-fill: #3ddcf8; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-weight: bold; -fx-font-size: 15px;");

        // Overall score ring (text-based)
        Label overallLbl = new Label(overallScore + " / 100");
        overallLbl.setStyle(
                "-fx-text-fill: " + scoreColor(overallScore) + ";" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 28px;"
        );
        Label overallTag = new Label(overallTag(overallScore));
        overallTag.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");

        VBox overallBox = new VBox(2, overallLbl, overallTag);
        overallBox.setAlignment(Pos.CENTER);
        overallBox.setStyle(
                "-fx-background-color: rgba(61,220,248,0.08);" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;"
        );

        // Individual score rows
        VBox scores = new VBox(8);
        scores.getChildren().addAll(
                scoreRow("⚖  Balance",    balanceScore,   feedbackBalance(balanceScore)),
                scoreRow("🎨 Contrast",   contrastScore,  feedbackContrast(contrastScore)),
                scoreRow("📐 Density",    densityScore,   feedbackDensity(densityScore)),
                scoreRow("🔢 Complexity", complexityScore, feedbackComplexity(complexityScore, elements.size()))
        );

        // Element count summary
        Label summary = new Label(
                "Elements: " + elements.size() +
                        "  |  Canvas: " + (int)canvasW + "×" + (int)canvasH
        );
        summary.setStyle("-fx-text-fill: #888; -fx-font-family: 'Segoe UI'; -fx-font-size: 10px;");

        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: #3e7e9f; -fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 11px;" +
                        "-fx-background-radius: 20; -fx-padding: 5 16 5 16; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.close());
        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                title, overallBox,
                new javafx.scene.control.Separator(),
                scores,
                new javafx.scene.control.Separator(),
                summary, btnRow
        );

        popup.setScene(new Scene(root));
        popup.show();
        popup.setX(owner.getX() + (owner.getWidth()  - popup.getWidth())  / 2);
        popup.setY(owner.getY() + (owner.getHeight() - popup.getHeight()) / 2);
    }

    // ── Score computations ─────────────────────────────────────────────────

    /**
     * Balance: distance of visual center-of-mass from canvas center.
     * Perfect center = 100, far off = 0.
     */
    private static int scoreBalance(List<DesignElement> elements, double cW, double cH) {
        if (elements.isEmpty()) return 50;
        double cx = 0, cy = 0;
        for (DesignElement el : elements) {
            cx += el.getX() + el.getWidth()  / 2.0;
            cy += el.getY() + el.getHeight() / 2.0;
        }
        cx /= elements.size();
        cy /= elements.size();
        double maxDist = Math.sqrt(cW*cW + cH*cH) / 2.0;
        double dist    = Math.sqrt(Math.pow(cx - cW/2, 2) + Math.pow(cy - cH/2, 2));
        return (int) Math.max(0, Math.min(100, 100 - (dist / maxDist) * 100));
    }

    /**
     * Contrast: color variance across elements.
     * High variance = high contrast = good.
     */
    private static int scoreContrast(List<DesignElement> elements) {
        if (elements.size() < 2) return 40;
        double[] luminances = elements.stream()
                .filter(el -> el.getFillColor() != null && !el.getFillColor().isEmpty())
                .mapToDouble(el -> luminance(el.getFillColor()))
                .toArray();
        if (luminances.length < 2) return 40;
        double mean = 0;
        for (double l : luminances) mean += l;
        mean /= luminances.length;
        double variance = 0;
        for (double l : luminances) variance += Math.pow(l - mean, 2);
        variance /= luminances.length;
        // Map variance (0–0.25 typical) to 0–100
        return (int) Math.min(100, (variance / 0.25) * 100);
    }

    /**
     * Density: what percentage of the canvas is covered by elements.
     * Ideal range 20–60%, outside that loses points.
     */
    private static int scoreDensity(List<DesignElement> elements, double cW, double cH) {
        if (elements.isEmpty()) return 0;
        double totalArea  = elements.stream()
                .mapToDouble(el -> el.getWidth() * el.getHeight())
                .sum();
        double coverage = (totalArea / (cW * cH)) * 100;
        // Ideal: 20–60%
        if (coverage >= 20 && coverage <= 60) return 100;
        if (coverage < 20) return (int)(coverage / 20.0 * 100);
        // > 60%: penalise for crowding
        return (int) Math.max(0, 100 - (coverage - 60) * 2);
    }

    /**
     * Complexity: element count.
     * 3–12 elements = ideal, fewer or more loses points.
     */
    private static int scoreComplexity(List<DesignElement> elements) {
        int n = elements.size();
        if (n == 0) return 10;
        if (n >= 3 && n <= 12) return 100;
        if (n < 3)  return (int)(n / 3.0 * 100);
        return (int) Math.max(20, 100 - (n - 12) * 5);
    }

    // ── Feedback text ──────────────────────────────────────────────────────

    private static String feedbackBalance(int score) {
        if (score >= 80) return "Well balanced layout";
        if (score >= 50) return "Slightly off-center";
        return "Layout is visually heavy on one side";
    }

    private static String feedbackContrast(int score) {
        if (score >= 75) return "Good color variety";
        if (score >= 40) return "Moderate contrast";
        return "Colors are too similar — add contrast";
    }

    private static String feedbackDensity(int score) {
        if (score >= 80) return "Canvas coverage is ideal";
        if (score >= 40) return "Could use more or fewer elements";
        return score < 40 ? "Canvas feels sparse" : "Canvas feels crowded";
    }

    private static String feedbackComplexity(int score, int count) {
        if (score >= 80) return "Good element count (" + count + ")";
        if (count < 3)  return "Too few elements — add more";
        return "Too many elements — simplify";
    }

    private static String overallTag(int score) {
        if (score >= 85) return "Excellent — publication ready!";
        if (score >= 65) return "Good — minor tweaks suggested";
        if (score >= 45) return "Fair — room for improvement";
        return "Needs work — review feedback below";
    }

    // ── UI helpers ─────────────────────────────────────────────────────────

    private static HBox scoreRow(String label, int score, String feedback) {
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-weight: bold; -fx-font-size: 12px;");
        nameLbl.setPrefWidth(100);

        // Bar
        StackPane barBg = new StackPane();
        barBg.setPrefWidth(100);
        barBg.setPrefHeight(8);
        barBg.setStyle("-fx-background-color: #2b2b4a; -fx-background-radius: 4;");

        Rectangle fill = new Rectangle(score, 8);
        fill.setFill(Color.web(scoreColor(score)));
        fill.setArcWidth(4);
        fill.setArcHeight(4);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        barBg.getChildren().add(fill);

        Label scoreLbl = new Label(score + "%");
        scoreLbl.setStyle("-fx-text-fill: " + scoreColor(score) + "; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 11px;");
        scoreLbl.setPrefWidth(38);
        scoreLbl.setAlignment(Pos.CENTER_RIGHT);

        Label feedbackLbl = new Label(feedback);
        feedbackLbl.setStyle("-fx-text-fill: #888; -fx-font-family: 'Segoe UI'; -fx-font-size: 10px;");

        VBox left = new VBox(2, new HBox(6, nameLbl, barBg, scoreLbl), feedbackLbl);
        HBox row = new HBox(left);
        return row;
    }

    private static String scoreColor(int score) {
        if (score >= 75) return "#43e8a0";
        if (score >= 50) return "#ffd166";
        return "#fb6c6a";
    }

    private static double luminance(String hex) {
        try {
            Color c = Color.web(hex);
            return 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
        } catch (Exception e) {
            return 0.5;
        }
    }
}