package com.framestudyo.framestudyo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.io.Serializable;


public abstract class DesignElement implements Serializable {

    // Every shape has a position and size
    protected double x;
    protected double y;
    protected double width;
    protected double height;

    // Every shape has colors
    protected String fillColor;
    protected String strokeColor;
    protected double strokeWidth;
    protected double opacity = 1.0;
    protected double angle = 0.0;

    // Constructor
    public DesignElement(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fillColor = "#4A90D9";
        this.strokeColor = "#000000";
        this.strokeWidth = 2.0;
    }
    public double getOpacity() { return opacity; }
    public void setOpacity(double opacity) { this.opacity = opacity; }

    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }

    // Every shape MUST know how to draw itself
    // This is abstract - each subclass will implement it differently
    public abstract void draw(GraphicsContext gc);
    public abstract DesignElement duplicate();
    // --- Getters and Setters ---

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getFillColor() { return fillColor; }
    public void setFillColor(String fillColor) { this.fillColor = fillColor; }

    public Color getFillColorAsColor() { return Color.web(fillColor); }

    public String getStrokeColor() { return strokeColor; }
    public void setStrokeColor(String strokeColor) { this.strokeColor = strokeColor; }

    public Color getStrokeColorAsColor() { return Color.web(strokeColor); }

    public double getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(double strokeWidth) { this.strokeWidth = strokeWidth; }

    // Check if a point (like a mouse click) is inside this shape
    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    // Move the shape by a certain amount
    public void move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }
}