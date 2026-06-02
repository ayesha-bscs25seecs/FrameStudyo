package com.framestudyo.framestudyo;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.GridPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.TextInputDialog;

public class FRAMESTUDYO extends Application {
    private String selectedTool = "select";
    private double startX, startY;
    private double dragOffsetX, dragOffsetY;
    private boolean isResizing = false;
    private String resizeHandle = "";
    private double originalX, originalY, originalWidth, originalHeight;
    private Button activeButton = null;
    private String activeBg = "#3e7e9f";
    private String activeHover = "#74d8eb";
    private boolean hasUnsavedChanges = false;
    AlignmentEngine alignmentEngine = new AlignmentEngine();
    ShapeRecognizer shapeRecognizer = new ShapeRecognizer();

    private String btn(String bg, String text) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + text + ";" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 6 14 6 14;" +
                "-fx-cursor: hand;";
    }

    private String activeStyle(String borderColor) {
        return "-fx-background-color: #ffffff;" +
                "-fx-text-fill: #000000;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 6 14 6 14;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;";
    }

    private Button styledBtn(String label, String bg, String hoverBg) {
        Button b = new Button(label);
        b.setStyle(btn(bg, "#ffffff"));
        b.setOnMouseEntered(e -> { if (b != activeButton) b.setStyle(btn(hoverBg, "#ffffff")); });
        b.setOnMouseExited(e -> { if (b != activeButton) b.setStyle(btn(bg, "#ffffff")); });
        return b;
    }

    private void setActive(Button b, String bg, String hoverBg) {
        if (activeButton != null) {
            final String oldBg = activeBg;
            final String oldHover = activeHover;
            activeButton.setStyle(btn(oldBg, "#ffffff"));
            Button prev = activeButton;
            prev.setOnMouseEntered(e -> { if (prev != activeButton) prev.setStyle(btn(oldHover, "#ffffff")); });
            prev.setOnMouseExited(e -> { if (prev != activeButton) prev.setStyle(btn(oldBg, "#ffffff")); });
        }
        activeButton = b;
        activeBg = bg;
        activeHover = hoverBg;
        activeButton.setStyle(activeStyle(bg));
        activeButton.setOnMouseEntered(e -> {});
        activeButton.setOnMouseExited(e -> {});
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private void drawSelectionHighlight(Canvas canvas, DesignCanvas designCanvas) {
        if (designCanvas.getSelectedElement() != null) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.web("#3ddcf8"));
            gc.setLineWidth(2);
            gc.setLineDashes(5);
            gc.strokeRect(
                    designCanvas.getSelectedElement().getX() - 4,
                    designCanvas.getSelectedElement().getY() - 4,
                    designCanvas.getSelectedElement().getWidth() + 8,
                    designCanvas.getSelectedElement().getHeight() + 8);
            gc.setLineDashes(0);
        }
    }

    private void drawHandles(Canvas canvas, DesignCanvas designCanvas) {
        if (designCanvas.getSelectedElement() != null) {
            DesignElement el = designCanvas.getSelectedElement();
            GraphicsContext gc = canvas.getGraphicsContext2D();
            double hSize = 8;
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.web("#3ddcf8"));
            gc.setLineWidth(1);
            double[][] handles = {
                    {el.getX() - hSize/2, el.getY() - hSize/2},
                    {el.getX() + el.getWidth() - hSize/2, el.getY() - hSize/2},
                    {el.getX() - hSize/2, el.getY() + el.getHeight() - hSize/2},
                    {el.getX() + el.getWidth() - hSize/2, el.getY() + el.getHeight() - hSize/2}
            };
            for (double[] handle : handles) {
                gc.fillRect(handle[0], handle[1], hSize, hSize);
                gc.strokeRect(handle[0], handle[1], hSize, hSize);
            }
        }
    }

    private void drawSelection(Canvas canvas, DesignCanvas designCanvas) {
        drawSelectionHighlight(canvas, designCanvas);
        drawHandles(canvas, designCanvas);
    }

    private String getResizeHandle(DesignElement el, double px, double py) {
        double tolerance = 10;
        if (Math.abs(px - el.getX()) < tolerance && Math.abs(py - el.getY()) < tolerance) return "TL";
        if (Math.abs(px - (el.getX() + el.getWidth())) < tolerance && Math.abs(py - el.getY()) < tolerance) return "TR";
        if (Math.abs(px - el.getX()) < tolerance && Math.abs(py - (el.getY() + el.getHeight())) < tolerance) return "BL";
        if (Math.abs(px - (el.getX() + el.getWidth())) < tolerance && Math.abs(py - (el.getY() + el.getHeight())) < tolerance) return "BR";
        return "";
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        Canvas canvas = new Canvas(860, 600);
        canvas.setStyle("-fx-background-color: transparent;");
        DesignCanvas designCanvas = new DesignCanvas(canvas);
        CommandManager commandManager = new CommandManager();
        designCanvas.setBackgroundColor("#ffffff");
        designCanvas.redraw();

        ScrollPane[] propScrollRef = {null};
        Scene[] mainScene = new Scene[1];

        Media media = new Media(
                getClass().getResource("/com/framestudyo/framestudyo/bg canvas.mp4").toExternalForm());
        MediaPlayer player = new MediaPlayer(media);
        MediaView mediaView = new MediaView(player);
        mediaView.setPreserveRatio(false);
        mediaView.fitWidthProperty().bind(stage.widthProperty());
        mediaView.fitHeightProperty().bind(stage.heightProperty());
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.play();

        StackPane canvasHolder = new StackPane(canvas);
        canvasHolder.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

        ColorPicker colorPicker = new ColorPicker(Color.web("#ac7cc3"));
        ColorPicker strokeColorPicker = new ColorPicker(Color.BLACK);
        Slider thicknessSlider = new Slider(1, 20, 2);
        thicknessSlider.setShowTickLabels(true);
        thicknessSlider.setMajorTickUnit(5);
        thicknessSlider.setPrefWidth(80);

        MenuButton shapesMenu = new MenuButton("✦ Shapes");
        shapesMenu.setStyle(btn("#eb6891", "#ffffff") + "-fx-background-radius: 20;");
        shapesMenu.setTooltip(new Tooltip("Choose a shape to draw"));

        String[] shapeNames = {"Rectangle", "Circle", "Triangle", "Star", "Arrow", "Line"};
        String[] shapeKeys  = {"rectangle", "circle",  "triangle", "star", "arrow",  "line"};
        for (int i = 0; i < shapeNames.length; i++) {
            final String key  = shapeKeys[i];
            final String name = shapeNames[i];
            MenuItem item = new MenuItem(name);
            item.setOnAction(e -> {
                selectedTool = key;
                shapesMenu.setText("✦ " + name);
                if (activeButton != null) {
                    final String oldBg = activeBg;
                    activeButton.setStyle(btn(oldBg, "#ffffff"));
                    activeButton = null;
                }
                shapesMenu.setStyle(activeStyle("#eb6891") + "-fx-background-radius: 20;");
            });
            shapesMenu.getItems().add(item);
        }

        // ── Buttons ───────────────────────────────────────────────────────
        Button selectBtn    = styledBtn("⬡ Select",     "#3e7e9f", "#74d8eb");
        Button textBtn      = styledBtn("T Text",        "#ac7cc3", "#e2a9f1");
        Button deleteBtn    = styledBtn("✕ Delete",      "#bc4156", "#fb6c6a");
        Button duplicateBtn = styledBtn("⧉ Duplicate",  "#f19e4f", "#fbbb3e");
        Button clearBtn     = styledBtn("⊘ Clear",       "#bc4156", "#fb6c6a");
        Button undoBtn      = styledBtn("↩ Undo",        "#2b588b", "#3e7e9f");
        Button redoBtn      = styledBtn("↪ Redo",        "#2b588b", "#3e7e9f");
        Button paletteBtn   = styledBtn("🎨 Palette",   "#6a3d8f", "#9b59b6");
        Button analyzeBtn   = styledBtn("📊 Analyze",   "#2b588b", "#3e7e9f");
        Button drawBtn      = styledBtn("✏ Draw",        "#3e8f5a", "#56c97a");

        selectBtn.setTooltip(new Tooltip("Select and move elements"));
        textBtn.setTooltip(new Tooltip("Add text"));
        deleteBtn.setTooltip(new Tooltip("Delete selected element"));
        duplicateBtn.setTooltip(new Tooltip("Duplicate selected element"));
        clearBtn.setTooltip(new Tooltip("Clear entire canvas"));
        undoBtn.setTooltip(new Tooltip("Undo (Ctrl+Z)"));
        redoBtn.setTooltip(new Tooltip("Redo (Ctrl+Y)"));
        paletteBtn.setTooltip(new Tooltip("Smart color palette suggestions"));
        analyzeBtn.setTooltip(new Tooltip("Analyze your design score"));
        drawBtn.setTooltip(new Tooltip("Freehand draw — auto-detects shape"));

        // ── Button actions ────────────────────────────────────────────────
        selectBtn.setOnAction(e -> {
            selectedTool = "select";
            setActive(selectBtn, "#3e7e9f", "#74d8eb");
            shapesMenu.setStyle(btn("#eb6891", "#ffffff") + "-fx-background-radius: 20;");
        });

        textBtn.setOnAction(e -> {
            selectedTool = "text";
            setActive(textBtn, "#ac7cc3", "#e2a9f1");
            shapesMenu.setStyle(btn("#eb6891", "#ffffff") + "-fx-background-radius: 20;");
        });

        drawBtn.setOnAction(e -> {
            selectedTool = "freehand";
            setActive(drawBtn, "#3e8f5a", "#56c97a");
            shapesMenu.setStyle(btn("#eb6891", "#ffffff") + "-fx-background-radius: 20;");
        });

        paletteBtn.setOnAction(e -> SmartColorAdvisor.show(stage, designCanvas));

        analyzeBtn.setOnAction(e ->
                DesignAnalyzer.show(stage, designCanvas, canvas.getWidth(), canvas.getHeight()));

        deleteBtn.setOnAction(e -> {
            if (designCanvas.getSelectedElement() != null) {
                commandManager.executeCommand(
                        new DeleteShapeCommand(designCanvas, designCanvas.getSelectedElement()));
                SessionTracker.getInstance().logAction("Deleted element");
                hasUnsavedChanges = true;
                designCanvas.setSelectedElement(null);
                root.setRight(null);
            }
        });

        duplicateBtn.setOnAction(e -> {
            if (designCanvas.getSelectedElement() != null) {
                DesignElement original = designCanvas.getSelectedElement();
                DesignElement copy = original.duplicate();
                copy.setX(original.getX() + 20);
                copy.setY(original.getY() + 20);
                commandManager.executeCommand(new AddShapeCommand(designCanvas, copy));
                SessionTracker.getInstance().logAction("Duplicated element");
                hasUnsavedChanges = true;
            }
        });

        clearBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear Canvas");
            alert.setHeaderText("Clear everything?");
            alert.setContentText("This will remove all elements. This action can be undone.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    commandManager.executeCommand(new ClearCanvasCommand(designCanvas));
                    SessionTracker.getInstance().logAction("Cleared canvas");
                    designCanvas.setSelectedElement(null);
                    root.setRight(null);
                }
            });
        });

        undoBtn.setOnAction(e -> {
            commandManager.undo();
            SessionTracker.getInstance().logAction("Undo");
            designCanvas.redraw();
        });

        redoBtn.setOnAction(e -> {
            commandManager.redo();
            SessionTracker.getInstance().logAction("Redo");
            designCanvas.redraw();
        });

        // ── Status bar ────────────────────────────────────────────────────
        Label statusLabel = new Label("X: 0 Y: 0 | Tool: Select | Selected: None");
        statusLabel.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; " +
                "-fx-padding: 5 12 5 12; -fx-font-size: 11px;");
        // ── Circular Stats button (bottom-left) ─────────────────────────
        Button statsBtn = new Button("✦");
        statsBtn.setTooltip(new Tooltip("Session stats & history"));
        String statsBtnNormal =
                "-fx-background-color: #ac7cc3;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 32px; -fx-min-height: 32px;" +
                        "-fx-max-width: 32px; -fx-max-height: 32px;" +
                        "-fx-padding: 0; -fx-cursor: hand;";
        String statsBtnHover =
                "-fx-background-color: #3ddcf8;" +
                        "-fx-text-fill: #1a1a2e;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 32px; -fx-min-height: 32px;" +
                        "-fx-max-width: 32px; -fx-max-height: 32px;" +
                        "-fx-padding: 0; -fx-cursor: hand;";
        statsBtn.setStyle(statsBtnNormal);
        statsBtn.setOnMouseEntered(e -> statsBtn.setStyle(statsBtnHover));
        statsBtn.setOnMouseExited(e  -> statsBtn.setStyle(statsBtnNormal));

        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);

        HBox statusBar = new HBox(6);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(4, 8, 4, 8));
        statusBar.setStyle("-fx-background-color: rgba(43,70,114,0.75);");
        statusBar.getChildren().addAll(statsBtn, statusLabel, statusSpacer);

        canvas.setOnMouseMoved(e -> {
            String selected = designCanvas.getSelectedElement() != null ?
                    designCanvas.getSelectedElement().getClass().getSimpleName().replace("Element", "") : "None";
            statusLabel.setText("X: " + (int)e.getX() + " Y: " + (int)e.getY() +
                    " | Tool: " + selectedTool.substring(0, 1).toUpperCase() + selectedTool.substring(1) +
                    " | Selected: " + selected);
        });

        // ── Mouse pressed ─────────────────────────────────────────────────
        canvas.setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();

            // Clear snap guides on every new press
            alignmentEngine.clearGuides(canvas, designCanvas);

            if (e.getClickCount() == 2 &&
                    designCanvas.getSelectedElement() instanceof TextElement txt) {

                TextInputDialog dialog =
                        new TextInputDialog(txt.getText());

                dialog.setTitle("Edit Text");

                dialog.setHeaderText(null);

                dialog.setContentText("Text:");

                dialog.showAndWait().ifPresent(newText -> {

                    txt.setText(newText);

                    hasUnsavedChanges = true;

                    designCanvas.redraw();

                    drawSelection(canvas, designCanvas);
                });
            }

            if (selectedTool.equals("freehand")) {
                shapeRecognizer.startStroke(startX, startY);
                return;
            }

            if (selectedTool.equals("select")) {
                isResizing = false;
                if (designCanvas.getSelectedElement() != null) {
                    resizeHandle = getResizeHandle(designCanvas.getSelectedElement(), startX, startY);
                    if (!resizeHandle.isEmpty()) {
                        isResizing = true;
                        DesignElement el = designCanvas.getSelectedElement();
                        originalX      = el.getX();
                        originalY      = el.getY();
                        originalWidth  = el.getWidth();
                        originalHeight = el.getHeight();
                        return;
                    }
                }
                designCanvas.setSelectedElement(null);
                root.setRight(null);
                java.util.List<DesignElement> elements = designCanvas.getElements();
                for (int i = elements.size() - 1; i >= 0; i--) {
                    if (elements.get(i).contains(startX, startY)) {
                        designCanvas.setSelectedElement(elements.get(i));
                        dragOffsetX = startX - designCanvas.getSelectedElement().getX();
                        dragOffsetY = startY - designCanvas.getSelectedElement().getY();
                        root.setRight(propScrollRef[0]);
                        break;
                    }
                }
                designCanvas.redraw();
                drawSelection(canvas, designCanvas);
            }
        });

        // ── Mouse dragged ─────────────────────────────────────────────────
        canvas.setOnMouseDragged(e -> {
            double endX = e.getX();
            double endY = e.getY();

            if (selectedTool.equals("freehand")) {
                shapeRecognizer.addPoint(endX, endY);
                designCanvas.redraw();
                shapeRecognizer.drawStroke(canvas.getGraphicsContext2D());
                return;
            }

            if (selectedTool.equals("select")) {
                if (isResizing && designCanvas.getSelectedElement() != null) {
                    DesignElement el = designCanvas.getSelectedElement();
                    double dx = endX - startX, dy = endY - startY;
                    switch (resizeHandle) {
                        case "BR":
                            el.setWidth(Math.max(10, originalWidth + dx));
                            el.setHeight(Math.max(10, originalHeight + dy));
                            break;
                        case "TR":
                            el.setWidth(Math.max(10, originalWidth + dx));
                            el.setY(originalY + dy);
                            el.setHeight(Math.max(10, originalHeight - dy));
                            break;
                        case "BL":
                            el.setX(originalX + dx);
                            el.setWidth(Math.max(10, originalWidth - dx));
                            el.setHeight(Math.max(10, originalHeight + dy));
                            break;
                        case "TL":
                            el.setX(originalX + dx);
                            el.setY(originalY + dy);
                            el.setWidth(Math.max(10, originalWidth - dx));
                            el.setHeight(Math.max(10, originalHeight - dy));
                            break;
                    }
                    designCanvas.redraw();
                    drawSelection(canvas, designCanvas);
                } else if (designCanvas.getSelectedElement() != null) {
                    // ── Smart snap drag ───────────────────────────────────
                    DesignElement dragEl = designCanvas.getSelectedElement();
                    double[] snapped = alignmentEngine.snap(
                            dragEl,
                            endX - dragOffsetX,
                            endY - dragOffsetY,
                            designCanvas.getElements());
                    dragEl.setX(snapped[0]);
                    dragEl.setY(snapped[1]);
                    designCanvas.redraw();
                    alignmentEngine.drawGuides(canvas, designCanvas);
                    drawSelection(canvas, designCanvas);
                }
            } else {
                // Shape preview while dragging
                double width  = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                double x = Math.min(startX, endX);
                double y = Math.min(startY, endY);
                designCanvas.redraw();
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(Color.web("#3ddcf8"));
                gc.setLineWidth(1.5);
                gc.setLineDashes(6);
                switch (selectedTool) {
                    case "rectangle":
                        gc.strokeRect(x, y, width, height);
                        break;
                    case "circle":
                        gc.strokeOval(x, y, width, height);
                        break;
                    case "line":
                        gc.strokeLine(startX, startY, endX, endY);
                        break;
                    case "text":
                        gc.strokeRect(x, y, 200, 50);
                        break;
                    case "triangle":
                        double[] px = {x + width/2, x, x + width};
                        double[] py = {y, y + height, y + height};
                        gc.strokePolygon(px, py, 3);
                        break;
                    case "star":
                        double cx = x + width/2, cy = y + height/2;
                        double oR = Math.min(width, height)/2, iR = oR * 0.4;
                        double[] sx = new double[10], sy = new double[10];
                        for (int i = 0; i < 10; i++) {
                            double angle = Math.PI/ 5 * i - Math.PI/ 2;
                            double r = (i % 2 == 0) ? oR : iR;
                            sx[i] = cx + r * Math.cos(angle);
                            sy[i] = cy + r * Math.sin(angle);
                        }
                        gc.strokePolygon(sx, sy, 10);
                        break;
                    case "arrow":
                        double sH = height * 0.4, sY = y + height * 0.3, hW = width * 0.4;
                        double[] ax = {x, x+width-hW, x+width-hW, x+width, x+width-hW, x+width-hW, x};
                        double[] ay = {sY, sY, y, y+height/2, y+height, sY+sH, sY+sH};
                        gc.strokePolygon(ax, ay, 7);
                        break;
                }
                gc.setLineDashes(0);
            }
        });

        // ── Mouse released ────────────────────────────────────────────────
        canvas.setOnMouseReleased(e -> {
            // Clear snap guides on release
            alignmentEngine.clearGuides(canvas, designCanvas);

            if (selectedTool.equals("select")) {
                isResizing = false;
                return;
            }

            double endX   = e.getX(), endY = e.getY();
            double width  = Math.abs(endX - startX), height = Math.abs(endY - startY);
            double x = Math.min(startX, endX),        y = Math.min(startY, endY);

            DesignElement element = null;

            switch (selectedTool) {
                case "freehand":
                    DesignElement recognized = shapeRecognizer.recognize(
                            colorToHex(colorPicker.getValue()),
                            colorToHex(strokeColorPicker.getValue()),
                            thicknessSlider.getValue());
                    if (recognized != null) {
                        commandManager.executeCommand(new AddShapeCommand(designCanvas, recognized));
                        SessionTracker.getInstance().logAction(
                                "Drew (recognized as " + shapeRecognizer.lastRecognizedLabel() + ")");
                        hasUnsavedChanges = true;
                        statusLabel.setText("✦ Recognized: " +
                                shapeRecognizer.lastRecognizedLabel().toUpperCase() +
                                " — click Select to move it");
                    }
                    designCanvas.redraw();
                    return;

                case "rectangle":
                    element = new RectangleElement(x, y, width, height);
                    element.setFillColor(colorToHex(colorPicker.getValue()));
                    element.setStrokeColor(colorToHex(strokeColorPicker.getValue()));
                    element.setStrokeWidth(thicknessSlider.getValue());
                    break;
                case "circle":
                    element = new CircleElement(x, y, width, height);
                    element.setFillColor(colorToHex(colorPicker.getValue()));
                    element.setStrokeColor(colorToHex(strokeColorPicker.getValue()));
                    element.setStrokeWidth(thicknessSlider.getValue());
                    break;
                case "triangle":
                    element = new TriangleElement(x, y, width, height);
                    element.setFillColor(colorToHex(colorPicker.getValue()));
                    element.setStrokeColor(colorToHex(strokeColorPicker.getValue()));
                    element.setStrokeWidth(thicknessSlider.getValue());
                    break;
                case "star":
                    element = new StarElement(x, y, width, height);
                    element.setFillColor(colorToHex(colorPicker.getValue()));
                    element.setStrokeColor(colorToHex(strokeColorPicker.getValue()));
                    element.setStrokeWidth(thicknessSlider.getValue());
                    break;
                case "arrow":
                    element = new ArrowElement(x, y, width, height);
                    element.setFillColor(colorToHex(colorPicker.getValue()));
                    element.setStrokeColor(colorToHex(strokeColorPicker.getValue()));
                    element.setStrokeWidth(thicknessSlider.getValue());
                    break;
                case "line":
                    element = new LineElement(startX, startY, endX, endY);
                    element.setStrokeColor(colorToHex(strokeColorPicker.getValue()));
                    element.setStrokeWidth(thicknessSlider.getValue());
                    break;
                case "text":
                    Dialog<String[]> dialog = new Dialog<>();
                    dialog.setTitle("Add Text");
                    dialog.setHeaderText("Enter your text details");
                    ButtonType okButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    TextField textInput = new TextField("Your text here");
                    ComboBox<String> fontPicker = new ComboBox<>();
                    fontPicker.getItems().addAll("Arial", "Times New Roman", "Verdana",
                            "Georgia", "Courier New", "Comic Sans MS", "Impact", "Trebuchet MS");
                    fontPicker.setValue("Arial");
                    ComboBox<String> sizePicker = new ComboBox<>();
                    sizePicker.getItems().addAll("12", "16", "20", "24", "32", "40", "48", "64", "72");
                    sizePicker.setValue("24");
                    grid.add(new Label("Text:"), 0, 0);
                    grid.add(textInput, 1, 0);
                    grid.add(new Label("Font:"), 0, 1);
                    grid.add(fontPicker, 1, 1);
                    grid.add(new Label("Size:"), 0, 2);
                    grid.add(sizePicker, 1, 2);
                    dialog.getDialogPane().setContent(grid);
                    dialog.setResultConverter(db -> db == okButton ?
                            new String[]{textInput.getText(), fontPicker.getValue(), sizePicker.getValue()} : null);
                    Optional<String[]> result = dialog.showAndWait();
                    result.ifPresent(values -> {
                        TextElement te = new TextElement(x, y, values[0]);
                        te.setFillColor(colorToHex(colorPicker.getValue()));
                        te.setStrokeColor(colorToHex(strokeColorPicker.getValue()));
                        te.setFontFamily(values[1]);
                        te.setFontSize(Double.parseDouble(values[2]));
                        commandManager.executeCommand(new AddShapeCommand(designCanvas, te));
                        SessionTracker.getInstance().logAction("Added Text");
                        hasUnsavedChanges = true;
                    });
                    return;
            }

            if (element != null) {
                commandManager.executeCommand(new AddShapeCommand(designCanvas, element));
                SessionTracker.getInstance().logAction("Added " +
                        element.getClass().getSimpleName().replace("Element", ""));
                hasUnsavedChanges = true;
            }
        });

        // ── Properties Panel ──────────────────────────────────────────────
        ColorPicker propFillPicker     = new ColorPicker(Color.web("#ac7cc3"));
        ColorPicker propStrokePicker   = new ColorPicker(Color.BLACK);
        Slider propThicknessSlider     = new Slider(1, 20, 2);
        propThicknessSlider.setShowTickLabels(true);
        propThicknessSlider.setMajorTickUnit(5);
        Slider propOpacitySlider = new Slider(0.0, 1.0, 1.0);
        propOpacitySlider.setShowTickLabels(true);
        propOpacitySlider.setMajorTickUnit(0.5);
        Slider propRotationSlider = new Slider(0, 360, 0);
        propRotationSlider.setShowTickLabels(true);
        propRotationSlider.setMajorTickUnit(90);

        propFillPicker.setOnAction(e -> {
            if (designCanvas.getSelectedElement() != null) {
                designCanvas.updateSelectedFillColor(colorToHex(propFillPicker.getValue()));
                drawSelection(canvas, designCanvas);
            }
        });
        propStrokePicker.setOnAction(e -> {
            if (designCanvas.getSelectedElement() != null) {
                designCanvas.updateSelectedStrokeColor(colorToHex(propStrokePicker.getValue()));
                drawSelection(canvas, designCanvas);
            }
        });
        propThicknessSlider.valueProperty().addListener((obs, o, n) -> {
            if (designCanvas.getSelectedElement() != null) {
                designCanvas.updateSelectedStrokeWidth(n.doubleValue());
                drawSelection(canvas, designCanvas);
            }
        });
        propOpacitySlider.valueProperty().addListener((obs, o, n) -> {
            if (designCanvas.getSelectedElement() != null) {
                designCanvas.getSelectedElement().setOpacity(n.doubleValue());
                designCanvas.redraw();
                drawSelection(canvas, designCanvas);
            }
        });
        propRotationSlider.valueProperty().addListener((obs, o, n) -> {
            if (designCanvas.getSelectedElement() != null) {
                designCanvas.getSelectedElement().setAngle(n.doubleValue());
                designCanvas.redraw();
                drawSelection(canvas, designCanvas);
            }
        });

        Button gridToggleBtn = styledBtn("Show Grid", "#3e7e9f", "#74d8eb");
        gridToggleBtn.setMaxWidth(Double.MAX_VALUE);
        gridToggleBtn.setOnAction(e -> {
            boolean ns = !designCanvas.isShowGrid();
            designCanvas.setShowGrid(ns);
            gridToggleBtn.setText(ns ? "Hide Grid" : "Show Grid");
        });

        Button bringToFrontBtn = styledBtn("Bring to Front", "#2b588b", "#3e7e9f");
        Button bringForwardBtn = styledBtn("Bring Forward",  "#2b588b", "#3e7e9f");
        Button sendBackwardBtn = styledBtn("Send Backward",  "#2b4672", "#2b588b");
        Button sendToBackBtn   = styledBtn("Send to Back",   "#2b4672", "#2b588b");
        bringToFrontBtn.setMaxWidth(Double.MAX_VALUE);
        bringForwardBtn.setMaxWidth(Double.MAX_VALUE);
        sendBackwardBtn.setMaxWidth(Double.MAX_VALUE);
        sendToBackBtn.setMaxWidth(Double.MAX_VALUE);

        bringForwardBtn.setOnAction(e -> { designCanvas.bringForward();  drawSelection(canvas, designCanvas); });
        sendBackwardBtn.setOnAction(e -> { designCanvas.sendBackward();  drawSelection(canvas, designCanvas); });
        bringToFrontBtn.setOnAction(e -> { designCanvas.bringToFront();  drawSelection(canvas, designCanvas); });
        sendToBackBtn.setOnAction(e ->   { designCanvas.sendToBack();    drawSelection(canvas, designCanvas); });

        String labelStyle = "-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 12px;";
        String titleStyle = "-fx-text-fill: #3ddcf8; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 14px;";

        Label propTitle    = new Label("✦ Properties"); propTitle.setStyle(titleStyle);
        Label fillLbl      = new Label("Fill Color:");   fillLbl.setStyle(labelStyle);
        Label strokeLbl    = new Label("Stroke Color:"); strokeLbl.setStyle(labelStyle);
        Label thickLbl     = new Label("Stroke Width:"); thickLbl.setStyle(labelStyle);
        Label opacityLbl   = new Label("Opacity:");      opacityLbl.setStyle(labelStyle);
        Label rotationLbl  = new Label("Rotation:");     rotationLbl.setStyle(labelStyle);
        Label layersLbl    = new Label("✦ Layers:");    layersLbl.setStyle(titleStyle);
        Label gridLbl      = new Label("✦ Canvas:");    gridLbl.setStyle(titleStyle);

        VBox propertiesPanel = new VBox(10);
        propertiesPanel.setStyle("-fx-background-color: #471f3f; -fx-padding: 15;");
        propertiesPanel.setPrefWidth(200);
        propertiesPanel.setMinWidth(200);
        propertiesPanel.getChildren().addAll(
                propTitle, new Separator(),
                fillLbl, propFillPicker,
                strokeLbl, propStrokePicker,
                thickLbl, propThicknessSlider,
                opacityLbl, propOpacitySlider,
                rotationLbl, propRotationSlider,
                new Separator(),
                gridLbl, gridToggleBtn,
                new Separator(),
                layersLbl,
                bringToFrontBtn, bringForwardBtn,
                sendBackwardBtn, sendToBackBtn
        );

        propScrollRef[0] = new ScrollPane(propertiesPanel);
        propScrollRef[0].setFitToWidth(true);
        propScrollRef[0].setPrefWidth(220);
        propScrollRef[0].setStyle("-fx-background-color: #471f3f; -fx-background: #471f3f;");

        // ── File menu ─────────────────────────────────────────────────────
        MenuButton fileMenu = new MenuButton(" File");
        fileMenu.setStyle(btn("#3ddcf8", "#ffffff") + "-fx-background-radius: 20;");
        fileMenu.setTooltip(new Tooltip("File operations"));

        MenuItem homeItem   = new MenuItem(" Home");
        MenuItem saveItem   = new MenuItem(" Save");
        MenuItem loadItem   = new MenuItem(" Load");
        MenuItem exportItem = new MenuItem("↑ Export PNG");

        homeItem.setOnAction(e -> {
            if (hasUnsavedChanges) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes.");
                alert.setContentText("Going home will discard current progress. Continue?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) return;
            }
            //SessionTracker.getInstance().pauseSession();
            player.pause();

            stage.setScene(
                    WelcomeScreen.create(

                            stage,

                            () -> {

                                designCanvas.getElements().clear();

                                designCanvas.setSelectedElement(null);

                                selectedTool = "select";

                                root.setRight(null);

                                hasUnsavedChanges = false;

                                designCanvas.redraw();
                               // SessionTracker.getInstance().resumeSession();
                                player.play();
                                stage.setScene(mainScene[0]);
                            },

                            (loaded) -> {

                                designCanvas.getElements().clear();

                                designCanvas.getElements().addAll(loaded);

                                designCanvas.setSelectedElement(null);

                                designCanvas.redraw();
                              //  SessionTracker.getInstance().resumeSession();
                                player.play();
                                stage.setScene(mainScene[0]);
                            }
                    )
            );
        });

        saveItem.setOnAction(e -> {

            FileChooser fc = new FileChooser();

            fc.setTitle("Save Design");

            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "FrameStudyo Files",
                            "*.fsd"
                    )
            );

            File file = fc.showSaveDialog(stage);

            if (file != null) {

                FileManager.save(
                        designCanvas.getElements(),
                        file.getAbsolutePath()
                );

                Alert success = new Alert(Alert.AlertType.INFORMATION);

                success.setTitle("Saved!");

                success.setHeaderText(null);

                success.setContentText("Design saved successfully!");

                success.showAndWait();

                hasUnsavedChanges = false;

                SessionTracker.getInstance()
                        .logAction("Saved design");
            }
        });

        loadItem.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Load Design");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("FrameStudyo Files", "*.fsd"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                java.util.List<DesignElement> loaded = FileManager.load(file.getAbsolutePath());
                if (loaded != null) {
                    designCanvas.getElements().clear();
                    designCanvas.getElements().addAll(loaded);
                    SessionTracker.getInstance().logAction("Loaded design from file");
                    designCanvas.redraw();
                }
            }
        });

        exportItem.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export as PNG");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(
                        (int)canvas.getWidth(), (int)canvas.getHeight());
                canvas.snapshot(null, image);
                try {
                    java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                            (int)canvas.getWidth(), (int)canvas.getHeight(),
                            java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    for (int iy = 0; iy < (int)canvas.getHeight(); iy++)
                        for (int ix = 0; ix < (int)canvas.getWidth(); ix++) {
                            javafx.scene.paint.Color fx = image.getPixelReader().getColor(ix, iy);
                            bi.setRGB(ix, iy, new java.awt.Color(
                                    (float)fx.getRed(), (float)fx.getGreen(),
                                    (float)fx.getBlue(), (float)fx.getOpacity()).getRGB());
                        }
                    javax.imageio.ImageIO.write(bi, "PNG", file);
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Export Successful");
                    success.setHeaderText(null);
                    success.setContentText("Design exported successfully as PNG!");
                    success.showAndWait();
                    SessionTracker.getInstance().logAction("Exported as PNG");
                } catch (java.io.IOException ex) {
                    System.out.println("Export failed: " + ex.getMessage());
                }
            }
        });

        fileMenu.getItems().addAll(homeItem, saveItem, loadItem, exportItem);

        // ── Top toolbar extras ────────────────────────────────────────────
        Label bgLabel = new Label("Canvas BG:");
        bgLabel.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");
        ColorPicker topBgPicker = new ColorPicker(Color.WHITE);
        topBgPicker.setTooltip(new Tooltip("Change canvas background color"));
        topBgPicker.setOnAction(e -> designCanvas.setBackgroundColor(colorToHex(topBgPicker.getValue())));

        Button topGridBtn = styledBtn("⊞ Grid", "#3e7e9f", "#74d8eb");
        topGridBtn.setTooltip(new Tooltip("Toggle canvas grid"));
        topGridBtn.setOnAction(e -> {
            boolean ns = !designCanvas.isShowGrid();
            designCanvas.setShowGrid(ns);
            topGridBtn.setText(ns ? "⊞ Hide Grid" : "⊞ Grid");
        });

        Button importBtn = styledBtn("🖼 Image", "#2b588b", "#3e7e9f");
        importBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Import Image");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                ImageElement img = new ImageElement(100, 100, 200, 200, file.getAbsolutePath());
                commandManager.executeCommand(new AddShapeCommand(designCanvas, img));
                SessionTracker.getInstance().logAction("Imported image");
                hasUnsavedChanges = true;
            }
        });

        // ── Toolbar ───────────────────────────────────────────────────────
        ToolBar toolbar = new ToolBar(
                fileMenu, new Separator(),
                shapesMenu, selectBtn, textBtn, drawBtn, new Separator(), importBtn,
                deleteBtn, duplicateBtn, clearBtn, new Separator(),
                undoBtn, redoBtn, new Separator(),
                paletteBtn, analyzeBtn, new Separator(),
                bgLabel, topBgPicker, topGridBtn
        );
        toolbar.setStyle("-fx-background-color: rgba(43,70,114,0.75); -fx-padding: 6 10 6 10;");

        VBox toolbars = new VBox(toolbar);
        toolbars.setStyle("-fx-background-color: transparent;");

        root.setTop(toolbars);
        root.setCenter(canvasHolder);
        root.setRight(null);
        // ── Stats panel action ────────────────────────────────────────────
        statsBtn.setOnAction(e -> {
            SessionTracker.SessionStats stats =
                    SessionTracker.getInstance().computeStats(designCanvas);
            java.util.List<SessionTracker.ActionEntry> log =
                    SessionTracker.getInstance().getRecentHistory(12);

            Stage panel = new Stage();
            panel.initOwner(stage);
            panel.initModality(javafx.stage.Modality.NONE);
            panel.initStyle(javafx.stage.StageStyle.UNDECORATED);

            Label hdr = new Label("✦  Stats & History");
            hdr.setStyle("-fx-text-fill: #3ddcf8; -fx-font-family: 'Segoe UI';" +
                    "-fx-font-weight: bold; -fx-font-size: 15px;");

            VBox timeTile = statTile("🕒 Time", stats.sessionTime);

            Label timeLabel =
                    (Label) timeTile.getChildren().get(0);

            HBox tiles = new HBox(8,
                    timeTile,
                    statTile("✦ Elements", String.valueOf(stats.totalElements)),
                    statTile("📋 Actions", String.valueOf(stats.totalActions)),
                    statTile("📐 Coverage", String.format("%.0f%%", stats.coveragePercent))
            );
            tiles.setAlignment(Pos.CENTER_LEFT);

            javafx.animation.Timeline timerUpdater =
                    new javafx.animation.Timeline(

                            new javafx.animation.KeyFrame(
                                    javafx.util.Duration.seconds(1),

                                    ev -> {

                                        long elapsed =
                                                SessionTracker.getInstance()
                                                        .elapsedMs();

                                        long minutes = elapsed / 60000;

                                        long seconds =
                                                (elapsed % 60000) / 1000;

                                        timeLabel.setText(
                                                minutes + "m " + seconds + "s"
                                        );
                                    }
                            )
                    );

            timerUpdater.setCycleCount(
                    javafx.animation.Animation.INDEFINITE
            );

            timerUpdater.play();

            Label shapesHdr = new Label("Shapes used:");
            shapesHdr.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI';" +
                    "-fx-font-weight: bold; -fx-font-size: 11px;");
            HBox shapeChips = new HBox(6);
            shapeChips.setAlignment(Pos.CENTER_LEFT);
            if (stats.shapeCounts.isEmpty()) {
                Label none = new Label("No shapes yet");
                none.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
                shapeChips.getChildren().add(none);
            } else {
                stats.shapeCounts.entrySet().stream()
                        .sorted(java.util.Map.Entry.<String,Long>comparingByValue().reversed())
                        .forEach(entry -> {
                            Label chip = new Label(entry.getKey() + " x" + entry.getValue());
                            chip.setStyle("-fx-background-color: #2b2b4a; -fx-text-fill: #3ddcf8;" +
                                    "-fx-font-family: 'Segoe UI'; -fx-font-size: 11px;" +
                                    "-fx-background-radius: 10; -fx-padding: 3 8 3 8;");
                            shapeChips.getChildren().add(chip);
                        });
            }

            Label colorLbl = new Label("Top color:");
            colorLbl.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;");
            javafx.scene.shape.Rectangle swatch = new javafx.scene.shape.Rectangle(22, 22);
            swatch.setArcWidth(6); swatch.setArcHeight(6);
            try { swatch.setFill(Color.web(stats.mostUsedColor)); }
            catch (Exception ex) { swatch.setFill(Color.GRAY); }
            swatch.setStroke(Color.web("#3ddcf8")); swatch.setStrokeWidth(1);
            Label colorHex = new Label(stats.mostUsedColor);
            colorHex.setStyle("-fx-text-fill: #ccc; -fx-font-family: 'Segoe UI'; -fx-font-size: 11px;");
            HBox colorRow = new HBox(8, colorLbl, swatch, colorHex);
            colorRow.setAlignment(Pos.CENTER_LEFT);

            Label histHdr = new Label("Recent actions:");
            histHdr.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI';" +
                    "-fx-font-weight: bold; -fx-font-size: 11px;");
            VBox histList = new VBox(3);
            histList.setStyle("-fx-background-color: #12102a; -fx-background-radius: 8; -fx-padding: 8;");
            java.util.List<SessionTracker.ActionEntry> reversed = new java.util.ArrayList<>(log);
            java.util.Collections.reverse(reversed);
            for (SessionTracker.ActionEntry entry : reversed) {
                Label row = new Label(entry.toString());
                row.setStyle("-fx-text-fill: #aaa; -fx-font-family: 'Segoe UI'; -fx-font-size: 10px;");
                row.setMaxWidth(Double.MAX_VALUE);
                histList.getChildren().add(row);
            }
            if (log.isEmpty()) {
                Label empty = new Label("No actions recorded yet.");
                empty.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");
                histList.getChildren().add(empty);
            }
            ScrollPane histScroll = new ScrollPane(histList);
            histScroll.setFitToWidth(true);
            histScroll.setPrefHeight(160);
            histScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            histScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            histScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            Label startLbl = new Label("Session started: " + stats.sessionStart);
            startLbl.setStyle("-fx-text-fill: #555; -fx-font-family: 'Segoe UI'; -fx-font-size: 10px;");

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #ac7cc3; -fx-text-fill: white;" +
                    "-fx-font-family: 'Segoe UI'; -fx-font-size: 11px;" +
                    "-fx-background-radius: 20; -fx-padding: 5 18 5 18; -fx-cursor: hand;");
            closeBtn.setOnMouseEntered(ev -> closeBtn.setStyle(
                    "-fx-background-color: #3ddcf8; -fx-text-fill: #1a1a2e;" +
                            "-fx-font-family: 'Segoe UI'; -fx-font-size: 11px;" +
                            "-fx-background-radius: 20; -fx-padding: 5 18 5 18; -fx-cursor: hand;"));
            closeBtn.setOnMouseExited(ev -> closeBtn.setStyle(
                    "-fx-background-color: #ac7cc3; -fx-text-fill: white;" +
                            "-fx-font-family: 'Segoe UI'; -fx-font-size: 11px;" +
                            "-fx-background-radius: 20; -fx-padding: 5 18 5 18; -fx-cursor: hand;"));
            closeBtn.setOnAction(ev -> {

                timerUpdater.stop();

                panel.close();
            });
            HBox closeRow = new HBox(closeBtn);
            closeRow.setAlignment(Pos.CENTER_RIGHT);

            VBox panelRoot = new VBox(12,
                    hdr, new Separator(),
                    tiles, new Separator(),
                    shapesHdr, shapeChips,
                    colorRow, new Separator(),
                    histHdr, histScroll,
                    startLbl, closeRow
            );
            panelRoot.setPadding(new Insets(18));
            panelRoot.setPrefWidth(380);
            panelRoot.setStyle(
                    "-fx-background-color: #1e1235;" +
                            "-fx-border-color: #ac7cc3;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 14;" +
                            "-fx-background-radius: 14;");

            panel.setScene(new javafx.scene.Scene(panelRoot));
            panel.show();
            panel.setX(stage.getX() + 12);
            panel.setY(stage.getY() + stage.getHeight() - 520);
        });

        root.setBottom(statusBar);

        StackPane mainLayer = new StackPane(mediaView, root);
        Scene scene = new Scene(mainLayer, 1200, 750);
        mainScene[0] = scene;

        // ── Keyboard shortcuts ────────────────────────────────────────────
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Z:
                    if (e.isControlDown()) { commandManager.undo(); designCanvas.redraw(); }
                    break;
                case Y:
                    if (e.isControlDown()) { commandManager.redo(); designCanvas.redraw(); }
                    break;
                case S:
                    if (e.isControlDown()) {
                        FileChooser fc = new FileChooser();
                        fc.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("FrameStudyo Files", "*.fsd"));
                        File file = fc.showSaveDialog(stage);
                        if (file != null) FileManager.save(designCanvas.getElements(), file.getAbsolutePath());
                    }
                    break;
                case BACK_SPACE:
                    if (designCanvas.getSelectedElement() != null) {
                        commandManager.executeCommand(
                                new DeleteShapeCommand(designCanvas, designCanvas.getSelectedElement()));
                        SessionTracker.getInstance().logAction("Deleted element (backspace)");
                        hasUnsavedChanges = true;
                        designCanvas.setSelectedElement(null);
                        root.setRight(null);
                    }
                    break;
                default:
                    break;
            }
        });

        // ── Welcome screen ────────────────────────────────────────────────
        setActive(selectBtn, "#3e7e9f", "#74d8eb");

        stage.setTitle("FrameStudyo");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setMaximized(true);
        stage.setScene(

                WelcomeScreen.create(

                        stage,

                        () -> {

                            designCanvas.getElements().clear();

                            designCanvas.setSelectedElement(null);

                            selectedTool = "select";

                            root.setRight(null);

                            hasUnsavedChanges = false;

                            designCanvas.redraw();

                            stage.setScene(mainScene[0]);
                            player.play();
                        },

                        (loaded) -> {

                            designCanvas.getElements().clear();

                            designCanvas.getElements().addAll(loaded);

                            designCanvas.setSelectedElement(null);

                            designCanvas.redraw();

                            stage.setScene(mainScene[0]);
                            player.play();
                        }
                )
        );
        stage.show();

        // Start session tracking after the app is fully loaded
        // Pause immediately — user is on welcome screen, not designing yet
        SessionTracker.getInstance().startSession();
        //SessionTracker.getInstance().pauseSession();
    }

    private static VBox statTile(String label, String value) {
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: #3ddcf8; -fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold; -fx-font-size: 16px;");
        Label keyLbl = new Label(label);
        keyLbl.setStyle("-fx-text-fill: #e2a9f1; -fx-font-family: 'Segoe UI'; -fx-font-size: 10px;");
        VBox tile = new VBox(2, valLbl, keyLbl);
        tile.setAlignment(javafx.geometry.Pos.CENTER);
        tile.setStyle("-fx-background-color: #2b2b4a; -fx-background-radius: 10; -fx-padding: 8 14 8 14;");
        tile.setPrefWidth(80);
        return tile;
    }

    public static void main(String[] args) {
        launch(args);
    }
}


