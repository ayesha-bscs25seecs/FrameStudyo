package com.framestudyo.framestudyo;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;


public class AlignmentEngine {

    private static final double SNAP_THRESHOLD = 8.0;   // pixels within which snap activates
    private static final Color  GUIDE_COLOR    = Color.web("#3ddcf8");
    private static final double GUIDE_WIDTH    = 1.0;

    // Guides to draw this frame
    private final List<double[]> hGuides = new ArrayList<>(); // [y, x1, x2]
    private final List<double[]> vGuides = new ArrayList<>(); // [x, y1, y2]

    // ── Snap logic ─────────────────────────────────────────────────────────

    public double[] snap(DesignElement dragged, double newX, double newY,
                         List<DesignElement> allElements) {
        hGuides.clear();
        vGuides.clear();

        double snappedX = newX;
        double snappedY = newY;

        // Candidate edges of the dragged element at proposed position
        double dL = newX;
        double dR = newX + dragged.getWidth();
        double dCX = newX + dragged.getWidth() / 2.0;
        double dT = newY;
        double dB = newY + dragged.getHeight();
        double dCY = newY + dragged.getHeight() / 2.0;

        double bestDX = SNAP_THRESHOLD + 1;
        double bestDY = SNAP_THRESHOLD + 1;

        for (DesignElement other : allElements) {
            if (other == dragged) continue;

            double oL  = other.getX();
            double oR  = other.getX() + other.getWidth();
            double oCX = other.getX() + other.getWidth() / 2.0;
            double oT  = other.getY();
            double oB  = other.getY() + other.getHeight();
            double oCY = other.getY() + other.getHeight() / 2.0;

            // ── Horizontal snap (X axis) ──────────────────────────────────
            // Left-to-left
            double d = Math.abs(dL - oL);
            if (d < bestDX) { bestDX = d; snappedX = oL; }
            // Right-to-right
            d = Math.abs(dR - oR);
            if (d < bestDX) { bestDX = d; snappedX = oR - dragged.getWidth(); }
            // Left-to-right
            d = Math.abs(dL - oR);
            if (d < bestDX) { bestDX = d; snappedX = oR; }
            // Right-to-left
            d = Math.abs(dR - oL);
            if (d < bestDX) { bestDX = d; snappedX = oL - dragged.getWidth(); }
            // Center-to-center X
            d = Math.abs(dCX - oCX);
            if (d < bestDX) { bestDX = d; snappedX = oCX - dragged.getWidth() / 2.0; }

            // ── Vertical snap (Y axis) ────────────────────────────────────
            // Top-to-top
            d = Math.abs(dT - oT);
            if (d < bestDY) { bestDY = d; snappedY = oT; }
            // Bottom-to-bottom
            d = Math.abs(dB - oB);
            if (d < bestDY) { bestDY = d; snappedY = oB - dragged.getHeight(); }
            // Top-to-bottom
            d = Math.abs(dT - oB);
            if (d < bestDY) { bestDY = d; snappedY = oB; }
            // Bottom-to-top
            d = Math.abs(dB - oT);
            if (d < bestDY) { bestDY = d; snappedY = oT - dragged.getHeight(); }
            // Center-to-center Y
            d = Math.abs(dCY - oCY);
            if (d < bestDY) { bestDY = d; snappedY = oCY - dragged.getHeight() / 2.0; }
        }

        // Build guide lines only when snap actually triggered
        buildGuideLines(dragged, snappedX, snappedY, allElements, bestDX, bestDY);

        return new double[]{snappedX, snappedY};
    }

    // ── Guide drawing ──────────────────────────────────────────────────────

    /**
     * Draws the recorded snap guide lines onto the canvas overlay.
     * Call AFTER designCanvas.redraw() and BEFORE drawSelection().
     */
    public void drawGuides(Canvas canvas, DesignCanvas designCanvas) {
        if (hGuides.isEmpty() && vGuides.isEmpty()) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(GUIDE_COLOR);
        gc.setLineWidth(GUIDE_WIDTH);
        gc.setLineDashes(4, 4);

        for (double[] g : hGuides) { // [y, x1, x2]
            gc.strokeLine(g[1], g[0], g[2], g[0]);
        }
        for (double[] g : vGuides) { // [x, y1, y2]
            gc.strokeLine(g[0], g[1], g[0], g[2]);
        }

        gc.setLineDashes(0);
    }

    /** Clears guides — call on mouse press and mouse released. */
    public void clearGuides(Canvas canvas, DesignCanvas designCanvas) {
        hGuides.clear();
        vGuides.clear();
        designCanvas.redraw();
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    private void buildGuideLines(DesignElement dragged, double snappedX, double snappedY,
                                 List<DesignElement> allElements, double bestDX, double bestDY) {
        double cW = 860, cH = 600; // canvas size — adjust if yours differs

        if (bestDX <= SNAP_THRESHOLD) {
            // Vertical guide line at snapped X edge
            vGuides.add(new double[]{snappedX, 0, cH});
            vGuides.add(new double[]{snappedX + dragged.getWidth(), 0, cH});
        }
        if (bestDY <= SNAP_THRESHOLD) {
            // Horizontal guide line at snapped Y edge
            hGuides.add(new double[]{snappedY, 0, cW});
            hGuides.add(new double[]{snappedY + dragged.getHeight(), 0, cW});
        }
    }
}