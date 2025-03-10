package com.example.touchtyped.controller;

import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.model.TypingPlan;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ClassroomViewController {

    @FXML
    private VBox joinCreateContainer;

    @FXML
    private VBox joinForm;

    @FXML
    private ImageView gamesButton;

    @FXML
    private Label joinFormDescription;

    @FXML
    private TextField classroomIDField;

    @FXML
    private TextField studentNameField;

    @FXML
    private StackPane stackPane;

    private static final String file_path = "user_info.txt";

    public void initialize() {

        joinCreateContainer.setVisible(true);
        joinForm.setVisible(false);

        if (localDataExists()) {
            displayClassroomInfo();
        } else {
            showJoinCreateButtons();
        }
    }

    private boolean localDataExists() {
        File file = new File(file_path);
        return file.exists() && file.length() > 0;
    }

    private void displayClassroomInfo() {
        try (Scanner scanner = new Scanner(new File(file_path))) {
            String classroomID = scanner.nextLine();
            String username = scanner.nextLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showJoinCreateButtons() {
    }

    @FXML
    public void onJoinButtonClick() {
    }

    @FXML
    public void onCreateButtonClick() {
    }

    @FXML
    public void onJoinAction() {
//        String classroomID = classroomIDField.getText();
//        String username = usernameField.getText();
//
//        try (FileWriter writer = new FileWriter(file_path)) {
//            writer.write(classroomID + "\n");
//            writer.write(username + "\n");
//            displayClassroomInfo();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @FXML
    public void displayJoinForm() {
        joinCreateContainer.setVisible(false);
        joinForm.setVisible(true);
        joinForm.requestFocus();
        joinFormDescription.setText("Enter the ID of the classroom you want to join, and choose a username!");
        joinFormDescription.setMaxWidth(350);
        joinFormDescription.setTextAlignment(TextAlignment.CENTER);
        joinFormDescription.setWrapText(true);
    }

    @FXML
    public void joinClassroom() {
        String classroomID = classroomIDField.getText();
        String username = studentNameField.getText();

        // check to ensure classroom exists.
        try {
            if (!ClassroomDAO.classroomExists(classroomID)) {
                joinFormDescription.setText("That classroom doesn't exist! Please ensure you entered the ID correctly.");
                joinFormDescription.setTextFill(Color.RED);
            } else {
                // join classroom - either creating a new student account for the classroom, or logging in to an existing account.
                if (ClassroomDAO.usernameExistsInClassroom(classroomID, username)) {
                    // logging in to existing account

                } else {
                    // creating new account
                    ClassroomDAO.addStudentToClassroom(classroomID, username);
                    ClassroomDAO.saveUserCache(classroomID, username);
                }
            }
        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to join classroom.");
            joinFormDescription.setText("An error occurred while joining the classroom. Please try again later...");
            joinFormDescription.setTextFill(Color.RED);
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


}
