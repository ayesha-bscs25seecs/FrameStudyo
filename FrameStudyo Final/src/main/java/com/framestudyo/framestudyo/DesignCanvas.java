package com.framestudyo.framestudyo;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class DesignCanvas {

    private Canvas canvas;
    private GraphicsContext gc;
    private List<DesignElement> elements;
    private String backgroundColor = "#2b2b2b";
    private boolean showGrid = false;

    public DesignCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.elements = new ArrayList<>();
    }
    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; redraw(); }
    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String color) { this.backgroundColor = color; redraw(); }

    // Add a shape to the canvas
    public void addElement(DesignElement element) {
        elements.add(element);
        redraw();
    }
    public void updateSelectedAngle(double angle) {
        if (selectedElement != null) {
            selectedElement.setAngle(angle);
            redraw();
        }
    }

    // Redraw everything from scratch
    public void redraw() {

        // Clear the canvas
       // gc.setFill(Color.web(backgroundColor));
       // gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (!backgroundColor.equals("transparent")) {

            gc.setFill(Color.web(backgroundColor));

            gc.fillRect(0, 0,
                    canvas.getWidth(),
                    canvas.getHeight());
        }

        // Draw grid if enabled
        if (showGrid) {
            gc.setStroke(Color.web("#444444"));
            gc.setLineWidth(0.5);
            double gridSize = 20;
            for (double x = 0; x < canvas.getWidth(); x += gridSize) {
                gc.strokeLine(x, 0, x, canvas.getHeight());
            }
            for (double y = 0; y < canvas.getHeight(); y += gridSize) {
                gc.strokeLine(0, y, canvas.getWidth(), y);
            }
        }

        // Draw every shape in the list
        // This is polymorphism in action!
        for (DesignElement element : elements) {
            element.draw(gc);
        }
    }

    public List<DesignElement> getElements() { return elements; }
    private DesignElement selectedElement = null;
    public Canvas getCanvas() { return canvas; }

    public DesignElement getSelectedElement() { return selectedElement; }
    public void setSelectedElement(DesignElement element) { this.selectedElement = element; }

    public void deleteSelected() {
        System.out.println("deleteSelected called, selectedElement = " + selectedElement);
        if (selectedElement != null) {
            elements.remove(selectedElement);
            selectedElement = null;
            redraw();
        }
    }
    public void updateSelectedFillColor(String color) {
        if (selectedElement != null) {
            selectedElement.setFillColor(color);
            redraw();
        }
    }

    public void updateSelectedStrokeColor(String color) {
        if (selectedElement != null) {
            selectedElement.setStrokeColor(color);
            redraw();
        }
    }

    public void updateSelectedStrokeWidth(double width) {
        if (selectedElement != null) {
            selectedElement.setStrokeWidth(width);
            redraw();
        }
    }
    public void bringForward() {
        if (selectedElement != null) {
            int index = elements.indexOf(selectedElement);
            if (index < elements.size() - 1) {
                elements.remove(index);
                elements.add(index + 1, selectedElement);
                redraw();
            }
        }
    }

    public void sendBackward() {
        if (selectedElement != null) {
            int index = elements.indexOf(selectedElement);
            if (index > 0) {
                elements.remove(index);
                elements.add(index - 1, selectedElement);
                redraw();
            }
        }
    }

    public void bringToFront() {
        if (selectedElement != null) {
            elements.remove(selectedElement);
            elements.add(selectedElement);
            redraw();
        }
    }

    public void sendToBack() {
        if (selectedElement != null) {
            elements.remove(selectedElement);
            elements.add(0, selectedElement);
            redraw();
        }
    }
}
