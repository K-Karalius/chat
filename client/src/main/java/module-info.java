module com.karalius.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.karalius.client to javafx.fxml;
    exports com.karalius.client;
}