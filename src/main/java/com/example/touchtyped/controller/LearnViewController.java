package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserAccount;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.*;
import com.example.touchtyped.model.Module;
import com.example.touchtyped.serialisers.TypingPlanDeserialiser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Button optionsButton;

    @FXML private Label typingPlanToggleLabel;

    /**
     * reference to the VBox in learn-view.fxml
     */
    @FXML
    private VBox vbox;

    @FXML private HBox typingPlanToggleContainer;

    @FXML private ImageView typingPlanToggleButton;

    private KeyboardInterface keyboardInterface = new KeyboardInterface();

    private final Font primary_font = Font.loadFont(this.getClass().getResourceAsStream("/fonts/Antipasto_extrabold.otf"), 48);
    private final Font secondary_font = Font.loadFont(this.getClass().getResourceAsStream("/fonts/Manjari.ttf"), 22);

    private boolean displayPersonalisedPlan = true;

    public void initialize() {
        // Attach keyboard interface to scene, when scene is available
        Platform.runLater(() -> {
            Scene scene = vbox.getScene(); // Use vbox's scene instead of buttonGrid's
            if (scene != null) {
                keyboardInterface.attachToScene(scene);
                keyboardInterface.stopHaptic();
                // Example keypress listener
                new ExampleKeypressListener(keyboardInterface);
            } else {
                System.err.println("Scene is not available yet.");
            }
        });

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

    @FXML
    public void toggleTypingPlan() {
        TypingPlanManager.getInstance().toggleTypingPlan();
        TypingPlan typingPlan = TypingPlanManager.getInstance().getTypingPlan();
        vbox.getChildren().clear();
        typingPlan.display(vbox);

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

    @FXML
    public void onGamesButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onOptionsButtonClick() {
        System.out.println("Options button clicked!");
        // Navigate to the "Options" screen
    }

    @FXML
    public void onClassroomButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/classroom-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) classroomButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}