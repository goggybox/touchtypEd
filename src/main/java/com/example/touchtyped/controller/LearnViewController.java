package com.example.touchtyped.controller;

import com.example.touchtyped.app.Application;
import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserAccount;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.*;
import com.example.touchtyped.model.Module;
import com.example.touchtyped.serialisers.TypingPlanDeserialiser;
import com.example.touchtyped.service.AppSettingsService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LearnViewController {

    /**
     * reference to the learnButton in learn-view.fxml
     */
    @FXML
    private Button learnButton;

    /**
     * reference to the gamesButton in learn-view.fxml
     */
    @FXML
    private ImageView gamesButton;

    /**
     * reference to the classroomButton in learn-view.fxml
     */
    @FXML
    private ImageView classroomButton;

    /**
     * reference to the optionsButton in learn-view.fxml
     */
    @FXML
    private ImageView optionsButton;

    @FXML private Label typingPlanToggleLabel;

    /**
     * reference to the VBox in learn-view.fxml
     */
    @FXML
    private VBox vbox;


    @FXML private HBox typingPlanToggleContainer;

    @FXML private ImageView typingPlanToggleButton;

    @FXML private StackPane loadingOverlay;

    private KeyboardInterface keyboardInterface;
    private AppSettingsService settingsService;

    private final Font primary_font = Font.loadFont(this.getClass().getResourceAsStream("/fonts/Antipasto_extrabold.otf"), 48);
    private final Font secondary_font = Font.loadFont(this.getClass().getResourceAsStream("/fonts/Manjari.ttf"), 22);

    public void initialize() {
        // Get settings service
        settingsService = AppSettingsService.getInstance();

        // Only create a new KeyboardInterface if one hasn't been set
        if (keyboardInterface == null) {
            keyboardInterface = Application.keyboardInterface;
        }

        if (TypingPlanManager.getInstance().isDisplayingPersonalisedPlan()) {
            // we are now displaying the personalised plan.
            typingPlanToggleLabel.setText("Displaying personalised typing plan.");
            typingPlanToggleButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/learn-content/tick.png").toExternalForm()));
        } else {
            // we are now displaying the default plan.
            typingPlanToggleLabel.setText("Displaying default typing plan.");
            typingPlanToggleButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/learn-content/cross.png").toExternalForm()));
        }

        // Attach keyboard interface to scene, when scene is available
        Platform.runLater(() -> {
            Scene scene = vbox.getScene(); // Use vbox's scene instead of buttonGrid's
            if (scene != null) {
                // Apply settings
                settingsService.applySettingsToScene(scene);

                keyboardInterface.attachToScene(scene);
                keyboardInterface.stopHaptic();
                // Example keypress listener
                new ExampleKeypressListener(keyboardInterface);
            } else {
                System.err.println("Scene is not available yet.");
            }
        });

        // check cached information (if it exists), and ensure the details are valid
        Task<Void> initialisation = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Map<String, String> userCache = ClassroomDAO.loadUserCache();
                    if (userCache != null) {
                        String classroomID = userCache.get("classroomID");

                        // check if the classroom exists
                        if (!ClassroomDAO.classroomExists(classroomID)) {
                            // go to Classroom page and change label to notify user.
                            Platform.runLater(() -> {
                                ClassroomDAO.deleteUserCache(); // delete the cache since it is now invalid
                                TypingPlanManager.getInstance().clearTypingPlans();
                                String error = "You were logged out because your classroom no longer exists!";
                                goToClassroomWithError(error);
                            });
                        }

                        String username = userCache.get("username");
                        String password = userCache.getOrDefault("password", null);
                        UserAccount user = UserDAO.getAccount(classroomID, username, password);

                        if (user == null) {
                            // either the user doesn't exist anymore, or their password has changed.
                            // go to Classroom page and change label to notify user.
                            System.out.println("User credentials invalid: going to classroom...");
                            Platform.runLater(() -> {
                                ClassroomDAO.deleteUserCache(); // delete the cache since it is now invalid.
                                String error = "You were logged out because either your account was deleted, or your password has changed.";
                                goToClassroomWithError(error);
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // successfully confirmed cached user details to be valid
                return null;
            }
        };
        new Thread(initialisation).start();

        typingPlanToggleLabel.setFont(secondary_font);
        typingPlanToggleLabel.setTextFill(Color.web(StyleConstants.GREY_COLOUR));

        // load TypingPlan from JSON and display.
        TypingPlan typingPlan = TypingPlanManager.getInstance().getTypingPlan();

        // if saved typing plan exists, display a button to toggle it
        if (TypingPlanManager.getInstance().personalisedPlanExists()) {
            System.out.println("Display toggle");
            typingPlanToggleContainer.setManaged(true);
            typingPlanToggleContainer.setVisible(true);
        } else {
            System.out.println("Do not display toggle");
            typingPlanToggleContainer.setManaged(false);
            typingPlanToggleContainer.setVisible(false);
        }


        typingPlan.display(vbox);
        HBox divider = DividerLine.createDividerLineWithText("");
        vbox.getChildren().add(divider);

//        ClassroomDAO.saveUserCache("C123456", "johndoe");
//
//        Map<String, String> cache = ClassroomDAO.loadUserCache();
//        if (cache != null) {
//            System.out.println(cache.get("classroomID"));
//            System.out.println(cache.get("username"));
//            System.out.println(cache.getOrDefault("password", "NO PASSWORD SAVED."));
//        }

//        try {
//            UserDAO.createUser("CA3B93D", "aidancheung", new TypingPlan(), "hello123");
//            System.out.println("USER CREATED");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            UserAccount account = UserDAO.getAccountByID("UYV16IY", "hello123");
//            if (account != null) { System.out.println("Got account "+account.getUsername()); }
//            else {
//                System.out.println("PASSWORD INCORRECT.");
//            }
//        } catch (Exception e) {
//            System.out.println("FAILED TO GET ACCOUNT");
//        }

    }

    private void goToClassroomWithError(String error) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/classroom-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);

            // 应用主题设置到场景
            settingsService.applySettingsToScene(scene);

            // Access the controller of the Classroom page
            ClassroomViewController classroomController = loader.getController();

            // Pass the error message to the Classroom controller
            classroomController.displayUserDetailsChangedError(error);

            // Navigate to the Classroom page
            Stage stage = (Stage) classroomButton.getScene().getWindow();
            
            boolean wasFullScreen = stage.isFullScreen();
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleTypingPlan() {
        TypingPlanManager.getInstance().toggleTypingPlan();
        TypingPlan typingPlan = TypingPlanManager.getInstance().getTypingPlan();
        vbox.getChildren().clear();
        typingPlan.display(vbox);
        HBox divider = DividerLine.createDividerLineWithText("");
        vbox.getChildren().add(divider);

        if (TypingPlanManager.getInstance().isDisplayingPersonalisedPlan()) {
            // we are now displaying the personalised plan.
            typingPlanToggleLabel.setText("Displaying personalised typing plan.");
            typingPlanToggleButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/learn-content/tick.png").toExternalForm()));
        } else {
            // we are now displaying the default plan.
            typingPlanToggleLabel.setText("Displaying default typing plan.");
            typingPlanToggleButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/learn-content/cross.png").toExternalForm()));
        }
    }

    public void setKeyboardInterface(KeyboardInterface keyboardInterface){
        this.keyboardInterface = keyboardInterface;
    }

    @FXML
    public void onGamesButtonClick() {
        System.out.println("onGamesButtonClick method called");
        try {
            System.out.println("Attempting to load game-view.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            System.out.println("Starting loader.load()");
            Scene scene = new Scene(loader.load(), 1200, 700);
            System.out.println("loader.load() completed");

            System.out.println("Getting GameViewController");
            GameViewController gameViewController = loader.getController();
            System.out.println("Setting keyboardInterface");
            gameViewController.setKeyboardInterface(keyboardInterface);

            System.out.println("Applying scene settings");
            settingsService.applySettingsToScene(scene);

            System.out.println("Getting current window and switching scene");
            Stage stage = (Stage) gamesButton.getScene().getWindow();

            boolean wasFullScreen = stage.isFullScreen();
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
            
            System.out.println("Scene switch completed");
        } catch (IOException e) {
            System.out.println("IO Exception occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Other exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onOptionsButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/options-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);

            Stage stage = (Stage) optionsButton.getScene().getWindow();
            boolean wasFullScreen = stage.isFullScreen();
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClassroomButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/classroom-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);
            Stage stage = (Stage) classroomButton.getScene().getWindow();
            boolean wasFullScreen = stage.isFullScreen();
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}