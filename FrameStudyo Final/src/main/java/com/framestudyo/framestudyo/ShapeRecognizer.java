package com.framestudyo.framestudyo;

import java.util.ArrayList;
import java.util.List;


public class ShapeRecognizer {

    private final List<double[]> points = new ArrayList<>(); // [x, y] pairs

    // ── Stroke recording ───────────────────────────────────────────────────

    public void startStroke(double x, double y) {
        points.clear();
        points.add(new double[]{x, y});
    }

    public void addPoint(double x, double y) {
        // Sub-sample: only add if moved >3px to avoid noise
        if (!points.isEmpty()) {
            double[] last = points.get(points.size() - 1);
            double dx = x - last[0], dy = y - last[1];
            if (dx*dx + dy*dy < 9) return;
        }
        points.add(new double[]{x, y});
    }

    /** Draws the current freehand trail using the provided GraphicsContext. */
    public void drawStroke(javafx.scene.canvas.GraphicsContext gc) {
        if (points.size() < 2) return;
        gc.setStroke(javafx.scene.paint.Color.web("#3ddcf8"));
        gc.setLineWidth(1.5);
        gc.setLineDashes(0);
        gc.beginPath();
        gc.moveTo(points.get(0)[0], points.get(0)[1]);
        for (int i = 1; i < points.size(); i++) {
            gc.lineTo(points.get(i)[0], points.get(i)[1]);
        }
        gc.stroke();
    }

    // ── Recognition ────────────────────────────────────────────────────────

    /**
     * Analyzes recorded points and returns the best-matching DesignElement,
     * or null if the stroke is too short to recognize.
     */
    public DesignElement recognize(String fillColor, String strokeColor, double strokeWidth) {
        if (points.size() < 4) return null;

        double[] bbox = boundingBox();
        double bX = bbox[0], bY = bbox[1], bW = bbox[2], bH = bbox[3];

        // Need a meaningful stroke
        if (bW < 10 && bH < 10) return null;

        String shape = classify(bW, bH);

        DesignElement el;
        switch (shape) {
            case "line":
                double[] first = points.get(0);
                double[] last  = points.get(points.size() - 1);
                el = new LineElement(first[0], first[1], last[0], last[1]);
                break;
            case "circle":
                el = new CircleElement(bX, bY, bW, bH);
                el.setFillColor(fillColor);
                break;
            case "triangle":
                el = new TriangleElement(bX, bY, bW, bH);
                el.setFillColor(fillColor);
                break;
            case "star":
                el = new StarElement(bX, bY, bW, bH);
                el.setFillColor(fillColor);
                break;
            default: // rectangle
                el = new RectangleElement(bX, bY, bW, bH);
                el.setFillColor(fillColor);
                break;
        }
        el.setStrokeColor(strokeColor);
        el.setStrokeWidth(strokeWidth);
        return el;
    }

    /** Returns the recognized shape name as a human-readable label. */
    public String lastRecognizedLabel() {
        if (points.size() < 4) return "";
        double[] bbox = boundingBox();
        return classify(bbox[2], bbox[3]);
    }

    // ── Internal classification ────────────────────────────────────────────

    /**
     * Classifies the stroke based on:
     * - Linearity  → line
     * - Closure    → closed shape
     * - Aspect ratio + corner count → rectangle vs circle vs triangle
     */
    private String classify(double bW, double bH) {
        // 1. Line check: very narrow bounding box in one direction
        double aspectRatio = (bW + bH == 0) ? 1 : Math.max(bW, bH) / Math.max(1, Math.min(bW, bH));
        if (aspectRatio > 4.0 && isLinear()) return "line";

        // 2. Closure check
        boolean closed = isClosed();

        // 3. Corner detection
        int corners = countCorners();

        if (!closed) {
            // Treat open strokes with narrow bbox as lines
            if (aspectRatio > 2.5) return "line";
            return "rectangle"; // fallback
        }

        // 4. Circularity: how close to a circle?
        double circularity = computeCircularity(bW, bH);
        if (circularity > 0.90 && corners < 4)
            return "circle";

        // 5. Corner count
        if (corners >= 2 && corners <= 5)
            return "triangle";

        //6.Detect Star
        if (corners >= 8)
            return "star";

        // 7. Default → rectangle
        return "rectangle";


    }

    /** True if start and end points are within 15% of the bounding box diagonal. */
    private boolean isClosed() {
        if (points.size() < 6) return false;
        double[] first = points.get(0);
        double[] last  = points.get(points.size() - 1);
        double[] bbox  = boundingBox();
        double diag    = Math.sqrt(bbox[2]*bbox[2] + bbox[3]*bbox[3]);
        double dist    = Math.sqrt(Math.pow(last[0]-first[0],2) + Math.pow(last[1]-first[1],2));
        return dist < diag * 0.25;
    }

    /** True if points form an approximately straight line (low deviation from linear fit). */
    private boolean isLinear() {
        if (points.size() < 3) return true;
        double[] first = points.get(0);
        double[] last  = points.get(points.size() - 1);
        double dx = last[0] - first[0], dy = last[1] - first[1];
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return true;
        double maxDev = 0;
        for (double[] p : points) {
            // Distance from point to the line first→last
            double dev = Math.abs(dy*p[0] - dx*p[1] + last[0]*first[1] - last[1]*first[0]) / len;
            if (dev > maxDev) maxDev = dev;
        }
        return maxDev < 12; // within 12px of the line
    }

    /**
     * Counts approximate corners by detecting direction changes > 40°.
     * Returns 0-6.
     */
    private int countCorners() {
        if (points.size() < 5) return 0;
        int corners = 0;
        int step = Math.max(1, points.size() / 20);
        for (int i = step; i < points.size() - step; i += step) {
            double[] prev = points.get(i - step);
            double[] curr = points.get(i);
            double[] next = points.get(Math.min(i + step, points.size()-1));
            double ax = curr[0]-prev[0], ay = curr[1]-prev[1];
            double bx = next[0]-curr[0], by = next[1]-curr[1];
            double dot   = ax*bx + ay*by;
            double cross = ax*by - ay*bx;
            double angle = Math.toDegrees(Math.atan2(Math.abs(cross), dot));
            if (angle > 40) corners++;
        }
        return corners;
    }

    /**
     * Measures how circular the stroke is.
     * Compares perimeter to what a circle of the same bounding-box size would have.
     * Returns 0 (not circular) to 1 (perfect circle).
     */
    private double computeCircularity(double bW, double bH) {
        if (bW <= 0 || bH <= 0) return 0;
        double strokeLen = strokeLength();
        if (strokeLen == 0) return 0;
        double r = (bW + bH) / 4.0;
        double idealPerimeter = 2 * Math.PI * r;
        return Math.min(1.0, idealPerimeter / strokeLen);
    }

    private double strokeLength() {
        double len = 0;
        for (int i = 1; i < points.size(); i++) {
            double dx = points.get(i)[0] - points.get(i-1)[0];
            double dy = points.get(i)[1] - points.get(i-1)[1];
            len += Math.sqrt(dx*dx + dy*dy);
        }
        return len;
    }

    /** Returns [minX, minY, width, height] of the stroke bounding box. */
    private double[] boundingBox() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (double[] p : points) {
            if (p[0] < minX) minX = p[0];
            if (p[1] < minY) minY = p[1];
            if (p[0] > maxX) maxX = p[0];
            if (p[1] > maxY) maxY = p[1];
        }
        return new double[]{minX, minY, maxX-minX, maxY-minY};
    }
}