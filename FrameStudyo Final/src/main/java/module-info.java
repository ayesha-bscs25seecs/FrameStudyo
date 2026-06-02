module com.framestudyo.framestudyo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.media;


    opens com.framestudyo.framestudyo to javafx.fxml;
    exports com.framestudyo.framestudyo;
}