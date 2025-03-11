package com.example.touchtyped.app;

import com.example.touchtyped.controller.LearnViewController;
import com.example.touchtyped.controller.PlayerNameDialog;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.ExampleKeypressListener;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.model.TypingPlanManager;
import com.example.touchtyped.model.UserProfile;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import com.fazecast.jSerialComm.*;
import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.service.RankingService;

import java.io.IOException;
import java.util.List;

public class Application extends javafx.application.Application {
    public static SerialPort ioPort;
    public static boolean keyboardConnected;
    public static KeyboardInterface keyboardInterface;
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize user profile
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

        // set properties of stage
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setTitle("TouchTypEd");
        stage.setScene(scene);
        stage.show();

        keyboardConnected = false;
        keyboardInterface = new KeyboardInterface();
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
     * Initialize user profile, prompt for input if no username exists
     */
    private void initUserProfile() {
        UserProfile userProfile = UserProfile.getInstance();
        System.out.println("Checking user profile...");
        System.out.println("Username exists: " + userProfile.hasPlayerName());
        
        if (!userProfile.hasPlayerName()) {
            System.out.println("Need to request user to input name");
            
            // Delete existing profile file (if exists) to ensure requesting name input again
            try {
                java.io.File profileFile = new java.io.File("user_profile.dat");
                if (profileFile.exists()) {
                    profileFile.delete();
                    System.out.println("Deleted existing profile file");
                }
            } catch (Exception e) {
                System.err.println("Error deleting profile file: " + e.getMessage());
            }
            
            // Show dialog directly, without using Platform.runLater
            String playerName = PlayerNameDialog.showDialog();
            System.out.println("User input name: " + playerName);
            
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
        // Test RankingService
        testRankingService();

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
     * Test the RankingService
     */
    private static void testRankingService() {
        try {
            System.out.println("Testing RankingService...");
            RankingService rankingService = RankingService.getInstance();
            
            // Test adding a new ranking
            PlayerRanking ranking1 = new PlayerRanking("TestUser", 50, 90.0, "Timed Mode");
            rankingService.addRanking(ranking1);
            
            // Test adding a ranking with the same name but better stats
            PlayerRanking ranking2 = new PlayerRanking("TestUser", 60, 95.0, "Timed Mode");
            rankingService.addRanking(ranking2);
            
            // Test adding a ranking with the same name but worse stats
            PlayerRanking ranking3 = new PlayerRanking("TestUser", 40, 85.0, "Timed Mode");
            rankingService.addRanking(ranking3);
            
            // Print all rankings
            List<PlayerRanking> rankings = rankingService.getRankings();
            System.out.println("Current rankings list:");
            for (int i = 0; i < rankings.size(); i++) {
                PlayerRanking ranking = rankings.get(i);
                System.out.println((i + 1) + ". " + ranking.getPlayerName() + 
                                  " - WPM: " + ranking.getWpm() + 
                                  " - Accuracy: " + ranking.getAccuracy() + "% - " + 
                                  ranking.getGameMode());
            }
            
            System.out.println("RankingService test completed");
        } catch (Exception e) {
            System.err.println("RankingService test error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}