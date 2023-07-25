module com.karalius.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens com.karalius.server to javafx.fxml;
    exports com.karalius.server;
}