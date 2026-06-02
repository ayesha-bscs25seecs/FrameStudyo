package com.framestudyo.framestudyo;

import java.io.*;
import java.util.List;

public class FileManager {

    public static void save(List<DesignElement> elements, String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(elements);
            System.out.println("Saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    public static List<DesignElement> load(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            List<DesignElement> elements = (List<DesignElement>) in.readObject();
            System.out.println("Loaded successfully! Elements: " + elements.size());
            return elements;
        } catch (InvalidClassException e) {
            System.out.println("File format outdated — please save a new file: " + e.getMessage());
            showError("This file was saved with an older version of FrameStudyo and can't be loaded. Please start a new design.");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading: " + e.getMessage());
            showError("Could not load file: " + e.getMessage());
            return null;
        }
    }

    private static void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Load Error");
            alert.setHeaderText("Could not load design");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}