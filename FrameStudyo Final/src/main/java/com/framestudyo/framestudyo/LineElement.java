package com.framestudyo.framestudyo;

import javafx.scene.canvas.GraphicsContext;

public class LineElement extends DesignElement {

    private double endX;
    private double endY;

    public LineElement(double startX, double startY, double endX, double endY) {
        super(startX, startY, 0, 0);
        this.endX = endX;
        this.endY = endY;
    }
    @Override
        public void draw(GraphicsContext gc) {
            gc.save();
        gc.translate((x + endX) / 2, (y + endY) / 2);
        gc.rotate(angle);
        gc.translate(-((x + endX) / 2), -((y + endY) / 2));

            gc.setGlobalAlpha(opacity);
            gc.setStroke(getStrokeColorAsColor());
            gc.setLineWidth(strokeWidth);
            gc.strokeLine(x, y, endX, endY);
            gc.setGlobalAlpha(1.0);

            gc.restore();
        }

    @Override
    public boolean contains(double px, double py) {
        // Check if the point is close enough to the line (within 5 pixels)
        double tolerance = 5.0;

        // Calculate distance from point to line segment
        double dx = endX - x;
        double dy = endY - y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) return false;

        // Project the point onto the line
        double t = ((px - x) * dx + (py - y) * dy) / (length * length);
        t = Math.max(0, Math.min(1, t));

        double closestX = x + t * dx;
        double closestY = y + t * dy;

        double distance = Math.sqrt(
                (px - closestX) * (px - closestX) +
                        (py - closestY) * (py - closestY)
        );

        return distance <= tolerance;
    }

    public double getEndX() { return endX; }
    public void setEndX(double endX) { this.endX = endX; }

    public double getEndY() { return endY; }
    public void setEndY(double endY) { this.endY = endY; }
    @Override
    public DesignElement duplicate() {
        LineElement copy = new LineElement(x, y, endX, endY);
        copy.setFillColor(fillColor);
        copy.setStrokeColor(strokeColor);
        copy.setStrokeWidth(strokeWidth);
        copy.setOpacity(opacity);
        copy.setAngle(angle);
        return copy;
    }
}