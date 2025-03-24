package com.example.touchtyped.app;

import com.example.touchtyped.controller.LearnViewController;
import com.example.touchtyped.controller.PlayerNameDialog;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.ExampleKeypressListener;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.model.TypingPlanManager;
import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.model.UserProfile;
import com.example.touchtyped.service.AppSettingsService;
import com.example.touchtyped.service.GlobalRankingService;
import com.example.touchtyped.service.RankingService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import com.fazecast.jSerialComm.*;
import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.service.RankingService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Application extends javafx.application.Application {
    public static SerialPort ioPort;
    public static boolean keyboardConnected;
    public static KeyboardInterface keyboardInterface;
    
    @Override
    public void start(Stage stage) throws IOException {
        initUserProfile();

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
        
        // Apply settings to scene
        AppSettingsService settingsService = AppSettingsService.getInstance();
        settingsService.applySettingsToScene(scene);
        System.out.println("Applied settings to initial scene");


        // set properties of stage
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setTitle("TouchTypEd");
        stage.setScene(scene);
        stage.show();
        try {
            ioPort = SerialPort.getCommPort("/dev/ttyACM0");
            SerialPort[] ports = SerialPort.getCommPorts();
            int i = 0;
            while (!ioPort.openPort() && i < ports.length) {
                ioPort = ports[i];
                i++;
            }
            if (ioPort.isOpen()) {
                System.out.println("port opened successfully");
                keyboardConnected = true;
                ioPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
                ioPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
            } else {
                System.out.println("unable to open port");
            }
        } catch (Exception e) {
            System.out.println("no keyboard connected");
        }
    }
    
    /**
     * Initialize user profile, request input if no username exists
     */
    private void initUserProfile() {
        UserProfile userProfile = UserProfile.getInstance();
        System.out.println("Checking user profile...");
        System.out.println("Username exists: " + userProfile.hasPlayerName());
        
        if (!userProfile.hasPlayerName()) {
            System.out.println("Need to request username input");
            
            // Delete existing profile file (if any) to ensure username input is requested again
            try {
                java.io.File profileFile = new java.io.File("user_profile.dat");
                if (profileFile.exists()) {
                    profileFile.delete();
                    System.out.println("Deleted existing profile file");
                }
            } catch (Exception e) {
                System.err.println("Error deleting profile file: " + e.getMessage());
            }
            
            // Display dialog directly, don't use Platform.runLater
            String playerName = PlayerNameDialog.showDialog();
            System.out.println("User entered name: " + playerName);
            
            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "Anonymous";
                System.out.println("Using default name: Anonymous");
            }
            
            userProfile.setPlayerName(playerName);
            System.out.println("Username set: " + playerName);
        } else {
            System.out.println("Existing username: " + userProfile.getPlayerName());
        }
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
            if (keyboardConnected) {
                keyboardInterface.stopHaptic();
                ioPort.closePort();
                System.out.println(ioPort.isOpen());
            }
        }));

        launch();
    }

    /**
     * Submit game ranking data to global ranking server
     * @param ranking Ranking data to submit
     */
    public static void submitGameRanking(PlayerRanking ranking) {
        try {
            // Submit directly to global ranking server
            GlobalRankingService globalRankingService = GlobalRankingService.getInstance();
            CompletableFuture<Boolean> future = globalRankingService.submitRanking(ranking);
            
            future.thenAccept(success -> {
                if (success) {
                    System.out.println("Ranking successfully submitted to global server");
                    
                    // Get player position in global rankings
                    globalRankingService.getPlayerPosition(ranking.getPlayerName())
                        .thenAccept(position -> {
                            if (position > 0) {
                                System.out.println("Player " + ranking.getPlayerName() + 
                                                  " position in global rankings: " + position);
                            } else {
                                System.out.println("Unable to get player position in global rankings");
                            }
                        });
                } else {
                    System.out.println("Failed to submit to global server");
                }
            });
        } catch (Exception e) {
            System.err.println("Error submitting ranking: " + e.getMessage());
        }
    }
}