package com.example.touchtyped.app;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.ExampleKeypressListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/com/example/touchtyped/learn-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        // set properties of stage
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setTitle("TouchTypEd");
        stage.setScene(scene);
        stage.show();

        // create the keyboard interface and attach to scene
        KeyboardInterface keyboardInterface = new KeyboardInterface();
        keyboardInterface.attachToScene(scene);

        // Example keypress listener
        new ExampleKeypressListener(keyboardInterface);
    }

    public static void main(String[] args) {
        launch();
    }
}