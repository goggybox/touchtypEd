package com.example.touchtyped.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/com/example/touchtyped/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        stage.setMinWidth(1200);
        stage.setMinHeight(700);

        stage.setTitle("TouchTypEd");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}