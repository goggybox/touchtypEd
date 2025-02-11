package com.example.touchtyped.app;

import com.example.touchtyped.controller.LearnViewController;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.ExampleKeypressListener;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.model.TypingPlanManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load custom fonts
        Font customFont = Font.loadFont(
            getClass().getResourceAsStream("/fonts/consola.ttf"),
            28
        );
        System.out.println("Loaded font: " + (customFont != null ? customFont.getName() : "Failed to load"));
        
        Font customBoldFont = Font.loadFont(
            getClass().getResourceAsStream("/fonts/consolab.ttf"),
            28
        );
        System.out.println("Loaded bold font: " + (customBoldFont != null ? customBoldFont.getName() : "Failed to load"));

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/com/example/touchtyped/learn-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        // set properties of stage
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setTitle("TouchTypEd");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        // shutdown hook to save TypingPlan if it has been modified
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            TypingPlanManager manager = TypingPlanManager.getInstance();
            if (manager.getModified()) {
                manager.saveTypingPlan();
                System.out.println("SAVING TYPING PLAN");
            } else {
                System.out.println("TypingPlan not changed. Not saving.");
            }
        }));

        launch();
    }
}