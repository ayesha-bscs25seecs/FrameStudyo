package com.framestudyo.framestudyo;

import javafx.scene.canvas.GraphicsContext;


class TriangleElement extends DesignElement {

    public TriangleElement(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

        @Override
        public void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x + width / 2, y + height / 2); // move to center
            gc.rotate(angle);                              // rotate
            gc.translate(-(x + width / 2), -(y + height / 2)); // move back

            gc.setGlobalAlpha(opacity);
            double[] xPoints = { x + width / 2, x, x + width };
            double[] yPoints = { y, y + height, y + height };

            gc.setFill(getFillColorAsColor());
            gc.fillPolygon(xPoints, yPoints, 3);

            gc.setStroke(getStrokeColorAsColor());
            gc.setLineWidth(strokeWidth);
            gc.strokePolygon(xPoints, yPoints, 3);
            gc.setGlobalAlpha(1.0);

            gc.restore();
        }

    @Override
    public DesignElement duplicate() {
        TriangleElement copy = new TriangleElement(x, y, width, height);
        copy.setFillColor(fillColor);
        copy.setStrokeColor(strokeColor);
        copy.setStrokeWidth(strokeWidth);
        copy.setOpacity(opacity);
        copy.setAngle(angle);
        return copy;
    }
}


class StarElement extends DesignElement {

    public StarElement(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setGlobalAlpha(opacity);
            gc.save();
            gc.translate(x + width / 2, y + height / 2); // move to center
            gc.rotate(angle);                              // rotate
            gc.translate(-(x + width / 2), -(y + height / 2)); // move back

            gc.setGlobalAlpha(opacity);
            double cx = x + width / 2;
            double cy = y + height / 2;
            double outerR = Math.min(width, height) / 2;
            double innerR = outerR * 0.4;
            int points = 5;

            double[] xPoints = new double[points * 2];
            double[] yPoints = new double[points * 2];

            for (int i = 0; i < points * 2; i++) {
                double angle = Math.PI / points * i - Math.PI / 2;
                double r = (i % 2 == 0) ? outerR : innerR;
                xPoints[i] = cx + r * Math.cos(angle);
                yPoints[i] = cy + r * Math.sin(angle);
            }

            gc.setFill(getFillColorAsColor());
            gc.fillPolygon(xPoints, yPoints, points * 2);

            gc.setStroke(getStrokeColorAsColor());
            gc.setLineWidth(strokeWidth);
            gc.strokePolygon(xPoints, yPoints, points * 2);
            gc.setGlobalAlpha(1.0);

            gc.restore();
        }

    @Override
    public DesignElement duplicate() {
        StarElement copy = new StarElement(x, y, width, height);
        copy.setFillColor(fillColor);
        copy.setStrokeColor(strokeColor);
        copy.setStrokeWidth(strokeWidth);
        copy.setOpacity(opacity);
        copy.setAngle(angle);
        return copy;
    }
}


class ArrowElement extends DesignElement {

    public ArrowElement(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(GraphicsContext gc) {

            gc.save();
            gc.translate(x + width / 2, y + height / 2); // move to center
            gc.rotate(angle);                              // rotate
            gc.translate(-(x + width / 2), -(y + height / 2)); // move back

            gc.setGlobalAlpha(opacity);

            gc.setGlobalAlpha(opacity);
            double shaftH = height * 0.4;
            double shaftY = y + height * 0.3;
            double headW  = width * 0.4;

            double[] xPoints = {
                    x,                    // left of shaft
                    x + width - headW,    // where shaft meets head
                    x + width - headW,    // top of arrowhead
                    x + width,            // tip
                    x + width - headW,    // bottom of arrowhead
                    x + width - headW,    // where shaft meets head (bottom)
                    x                     // back to start
            };

            double[] yPoints = {
                    shaftY,               // top left
                    shaftY,               // top right of shaft
                    y,                    // top of arrowhead
                    y + height / 2,       // tip
                    y + height,           // bottom of arrowhead
                    shaftY + shaftH,      // bottom right of shaft
                    shaftY + shaftH       // bottom left
            };

            gc.setFill(getFillColorAsColor());
            gc.fillPolygon(xPoints, yPoints, 7);

            gc.setStroke(getStrokeColorAsColor());
            gc.setLineWidth(strokeWidth);
            gc.strokePolygon(xPoints, yPoints, 7);

            gc.setGlobalAlpha(1.0);

            gc.restore();
        }

    @Override
    public DesignElement duplicate() {
        ArrowElement copy = new ArrowElement(x, y, width, height);
        copy.setFillColor(fillColor);
        copy.setStrokeColor(strokeColor);
        copy.setStrokeWidth(strokeWidth);
        copy.setOpacity(opacity);
        copy.setAngle(angle);

        return copy;
    }
}