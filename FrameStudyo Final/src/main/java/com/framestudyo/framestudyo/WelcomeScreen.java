package com.framestudyo.framestudyo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;

public class WelcomeScreen {

    public static Scene create(Stage stage, Runnable onNewDesign, java.util.function.Consumer<List<DesignElement>> onLoadDesign) {

        // --- VIDEO BACKGROUND ---
        Media media = new Media(
                WelcomeScreen.class.getResource(
                        "/com/framestudyo/framestudyo/bg1.mp4"
                ).toExternalForm()
        );

        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setPreserveRatio(false);
        mediaView.fitWidthProperty().bind(stage.widthProperty());
        mediaView.fitHeightProperty().bind(stage.heightProperty());
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setMute(true);
        mediaPlayer.play();

        // --- BUTTONS ---
        Button newBtn  = new Button("NEW DESIGN");
        Button loadBtn = new Button("LOAD DESIGN");

        String baseStyle =
                "-fx-font-size: 15px;" +
                        "-fx-font-family: Georgia;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 35 12 35;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 2.5;";

        String normalStyle = baseStyle +
                "-fx-background-color: #3e7e9f;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: white;";

        String hoverStyle = baseStyle +
                "-fx-background-color: #bc4156;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: white;";

        newBtn.setStyle(normalStyle);
        loadBtn.setStyle(normalStyle);

        newBtn.setOnMouseEntered(e -> newBtn.setStyle(hoverStyle));
        newBtn.setOnMouseExited(e -> newBtn.setStyle(normalStyle));
        loadBtn.setOnMouseEntered(e -> loadBtn.setStyle(hoverStyle));
        loadBtn.setOnMouseExited(e -> loadBtn.setStyle(normalStyle));

        // --- BUTTON ACTIONS ---
        newBtn.setOnAction(e -> {
            mediaPlayer.pause();
            mediaPlayer.seek(javafx.util.Duration.ZERO);
            onNewDesign.run();
        });

        loadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Design");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("FrameStudyo Files", "*.fsd")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                List<DesignElement> loaded = FileManager.load(file.getAbsolutePath());
                if (loaded != null) {
                    mediaPlayer.pause();
                    mediaPlayer.seek(javafx.util.Duration.ZERO);
                    onLoadDesign.accept(loaded);
                }
            }
        });

        // --- TEMPLATE SECTION ---

        Button templateBtn = new Button("CHOOSE TEMPLATE");

        templateBtn.setStyle(normalStyle);

        templateBtn.setOnMouseEntered(e ->
                templateBtn.setStyle(hoverStyle));

        templateBtn.setOnMouseExited(e ->
                templateBtn.setStyle(normalStyle));

        templateBtn.setOnAction(e -> {

            Stage popup = new Stage();

            popup.initOwner(stage);

            VBox layout = new VBox(15);

            layout.setAlignment(Pos.CENTER);

            layout.setPadding(new Insets(25));

            layout.setStyle(
                    "-fx-background-color: #1a1a2e;"
            );

            Label title = new Label("Select Template");

            title.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 22px;" +
                            "-fx-font-weight: bold;"
            );

            layout.getChildren().add(title);

            for (TemplateEngine.Template t :
                    TemplateEngine.getTemplates()) {

                if (t == TemplateEngine.Template.BLANK)
                    continue;

                Button temp = new Button(
                        t.displayName()
                );

                temp.setStyle(normalStyle);

                temp.setMaxWidth(Double.MAX_VALUE);

                temp.setOnMouseEntered(ev ->
                        temp.setStyle(hoverStyle));

                temp.setOnMouseExited(ev ->
                        temp.setStyle(normalStyle));

                temp.setOnAction(ev -> {

                    mediaPlayer.pause();
                    mediaPlayer.seek(javafx.util.Duration.ZERO);

                    List<DesignElement> els =
                            TemplateEngine.build(t);

                    popup.close();

                    onLoadDesign.accept(
                            new ArrayList<>(els)
                    );
                });

                layout.getChildren().add(temp);
            }

            Scene popScene = new Scene(layout, 350, 500);

            popup.setScene(popScene);

            popup.setTitle("Templates");

            popup.show();
        });

        // --- BUTTON ROW ---
        HBox buttonRow = new HBox(200);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(newBtn, loadBtn);

        // --- OVERLAY LAYOUT ---
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.BOTTOM_CENTER);
        overlay.setSpacing(35);

        overlay.setPadding(new Insets(40));
        overlay.getChildren().addAll(
                templateBtn,
                buttonRow
        );
        overlay.prefWidthProperty().bind(stage.widthProperty());
        overlay.prefHeightProperty().bind(stage.heightProperty());

        // --- STACK: video + buttons, NO dark overlay ---
        StackPane root = new StackPane();
        root.getChildren().addAll(mediaView, overlay);

        Scene scene = new Scene(root);
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());

        return scene;
    }
}