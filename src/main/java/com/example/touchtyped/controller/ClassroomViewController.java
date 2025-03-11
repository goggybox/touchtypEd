package com.example.touchtyped.controller;

import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.model.TypingPlan;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
    private VBox studentTeacherContainer;

    @FXML
    private VBox joinForm;

    @FXML
    private VBox createForm;

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

    private static final String file_path = "user_cache.txt";

    public void initialize() {

        hideAllForms();

        System.out.println(localDataExists());
        if (localDataExists()) {
            displayClassroomInfo();
        } else {
            displayStudentTeacherContainer();
        }
    }

    @FXML
    private void displayStudentTeacherContainer() {
        hideAllForms();
        studentTeacherContainer.setVisible(true);
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

    private void hideAllForms() {
        studentTeacherContainer.setVisible(false);
        joinForm.setVisible(false);
        createForm.setVisible(false);
    }

    private void showJoinCreateButtons() {
    }

    @FXML
    public void displayStudent() {
        hideAllForms();
        joinForm.setVisible(true);
        joinForm.requestFocus();
        joinFormDescription.setText("Enter the ID of the classroom you want to join, and choose a username!");
        joinFormDescription.setMaxWidth(350);
        joinFormDescription.setPrefHeight(50);
        joinFormDescription.setTextAlignment(TextAlignment.CENTER);
        joinFormDescription.setWrapText(true);
    }

    @FXML
    public void joinClassroom() {
        String classroomID = classroomIDField.getText();
        String username = studentNameField.getText();

        // check to ensure fields are not empty
        if (classroomID.isBlank() || username.isBlank()) {
            joinFormDescription.setText("Please enter the classroom ID and a username!");
            joinFormDescription.setTextFill(Color.BLACK);
            return;
        }

        // display loading message
        joinFormDescription.setText("Joining...");
        joinFormDescription.setTextFill(Color.BLACK);
        joinFormDescription.setAlignment(Pos.CENTER);

        // run database operation in separate thread to ensure "Joining..." message is displayed.
        Task<Void> joinTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    if (!ClassroomDAO.classroomExists(classroomID)) {
                        Platform.runLater(() -> {
                            joinFormDescription.setText("That classroom doesn't exist! Please ensure you entered the ID correctly.");
                            joinFormDescription.setTextFill(Color.RED);
                        });
                    } else {
                        if (ClassroomDAO.usernameExistsInClassroom(classroomID, username)) {
                            // logging in to existing account
                            Platform.runLater(() -> {
                                joinFormDescription.setText("Logged in successfully!");
                                joinFormDescription.setTextFill(Color.GREEN);
                            });
                        } else {
                            // creating new account
                            ClassroomDAO.addStudentToClassroom(classroomID, username);
                            UserDAO.createUser(classroomID, username, new TypingPlan());
                            ClassroomDAO.saveUserCache(classroomID, username);
                            Platform.runLater(() -> {
                                joinFormDescription.setText("Joined classroom successfully!");
                                joinFormDescription.setTextFill(Color.GREEN);
                            });
                        }
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.out.println("DATABASE FAILURE. Failed to join classroom.");
                        joinFormDescription.setText("An error occurred while joining the classroom. Please try again later...");
                        joinFormDescription.setTextFill(Color.RED);
                    });
                }
                return null;
            }
        };

        new Thread(joinTask).start();
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
