package com.framestudyo.framestudyo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.io.Serializable;

public class ImageElement extends DesignElement {
    private String imagePath;
    private transient Image image;

    public ImageElement(double x, double y, double width, double height, String imagePath) {
        super(x, y, width, height);
        this.imagePath = imagePath;
        this.image = new Image("file:" + imagePath);
    }

    private void loadImage() {
        if (image == null && imagePath != null)
            image = new Image("file:" + imagePath);
    }

    @Override
    public void draw(GraphicsContext gc) {
        loadImage();
        gc.save();
        gc.translate(x + width/2, y + height/2);
        gc.rotate(angle);
        gc.translate(-(x + width/2), -(y + height/2));
        gc.setGlobalAlpha(opacity);
        if (image != null) gc.drawImage(image, x, y, width, height);
        gc.setGlobalAlpha(1.0);
        gc.restore();
    }

    @Override
    public DesignElement duplicate() {
        ImageElement copy = new ImageElement(x, y, width, height, imagePath);
        copy.setOpacity(opacity);
        copy.setAngle(angle);
        return copy;
    }

    public String getImagePath() { return imagePath; }
}