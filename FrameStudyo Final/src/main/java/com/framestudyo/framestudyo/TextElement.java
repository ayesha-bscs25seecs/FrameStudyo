package com.framestudyo.framestudyo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

public class TextElement extends DesignElement {

    private String text;
    private String fontFamily;
    private double fontSize;

    public TextElement(double x, double y, String text) {
        super(x, y, 200, 50);
        this.text = text;
        this.fontFamily = "Arial";
        this.fontSize = 24;
    }
    @Override
        public void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x + width / 2, y + height / 2); // move to center
            gc.rotate(angle);                              // rotate
            gc.translate(-(x + width / 2), -(y + height / 2)); // move back

            gc.setGlobalAlpha(opacity);
            gc.setFill(getFillColorAsColor());
        gc.setFont(new Font(fontFamily, fontSize));
            gc.fillText(text, x, y);
            gc.setGlobalAlpha(1.0);

            gc.restore();
        }


    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
    @Override
    public DesignElement duplicate() {
        TextElement copy = new TextElement(x, y, text);
        copy.setFillColor(fillColor);
        copy.setStrokeColor(strokeColor);
        copy.setStrokeWidth(strokeWidth);
        copy.setFontFamily(fontFamily);
        copy.setFontSize(fontSize);
        copy.setOpacity(opacity);
        copy.setAngle(angle);
        return copy;
    }
}
