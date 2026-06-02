package com.framestudyo.framestudyo;

import javafx.scene.canvas.GraphicsContext;

public class RectangleElement extends DesignElement {

    // Constructor
    public RectangleElement(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    // This is where polymorphism happens!
    // RectangleElement knows how to draw itself as a rectangle
    @Override
        public void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x + width / 2, y + height / 2); // move to center
            gc.rotate(angle);                              // rotate
            gc.translate(-(x + width / 2), -(y + height / 2)); // move back

            gc.setGlobalAlpha(opacity);
            gc.setFill(getFillColorAsColor());
            gc.fillRect(x, y, width, height);
            gc.setStroke(getStrokeColorAsColor());
            gc.setLineWidth(strokeWidth);
            gc.strokeRect(x, y, width, height);
            gc.setGlobalAlpha(1.0);

            gc.restore();
        }

    @Override
    public DesignElement duplicate() {
        RectangleElement copy = new RectangleElement(x, y, width, height);
        copy.setFillColor(fillColor);
        copy.setStrokeColor(strokeColor);
        copy.setStrokeWidth(strokeWidth);
        copy.setOpacity(opacity);
        copy.setAngle(angle);
        return copy;
    }
}
