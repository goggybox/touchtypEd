package com.example.touchtyped.controller;

import com.example.touchtyped.firestore.Classroom;
import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserAccount;
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
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class ClassroomViewController {

    @FXML
    private VBox joinCreateContainer;

    @FXML
    private VBox studentTeacherContainer;

    @FXML
    private VBox teacherSelectionForm;

    @FXML
    private VBox userAccountDisplayContainer;

    @FXML
    private VBox joinForm;

    @FXML
    private VBox createForm;

    @FXML
    private VBox loadingContainer;

    @FXML
    private ImageView gamesButton;

    @FXML
    private ImageView learnButton;

    @FXML
    private Label joinFormDescription;

    @FXML
    private Label teacherSelectionFormDescription;

    @FXML
    private Label createFormDescription;

    @FXML
    private Label userGreeting;

    @FXML
    private Label userDescription;

    @FXML
    private TextField classroomIDField;

    @FXML
    private TextField studentNameField;

    @FXML
    private TextField classroomNameField;

    @FXML
    private TextField teacherNameField;

    @FXML
    private TextField passwordField;

    @FXML
    private StackPane stackPane;

    private static final String file_path = "user_cache.txt";

    /**
     * if the user has logged in before, and there account information is stored in the cache, load this information
     * otherwise, display "Student" or "Teacher" buttons to allow them to join or create a class.
     */
    public void initialize() {
        hideAllForms();
        loadingContainer.setVisible(true);

        Task<Void> initialisation = new Task<>() {
            @Override
            protected Void call() {
                try {

                    if (localDataExists()) {
                        Map<String, String> userDetails = ClassroomDAO.loadUserCache();
                        String classroomID = userDetails.get("classroomID");
                        String username = userDetails.get("username");
                        if (userDetails.containsKey("password")) {
                            String password = userDetails.get("password");
                            // check if the password matches the one saved in the database. try to get the UserAccount
                            // - if it returns null, the password doesn't match
                            UserAccount userAccount = UserDAO.getAccount(classroomID, username, password);
                            if (userAccount == null) {
                                // TODO: force user to log in again.
                            } else {
                                Platform.runLater(() -> {
                                    displayUserAccount(userAccount);
                                });
                            }
                        } else {
                            UserAccount userAccount = UserDAO.getAccount(classroomID, username);
                            if (userAccount == null) {
                                // TODO: force user to log in again.
                            } else {
                                Platform.runLater(() -> {
                                   displayUserAccount(userAccount);
                                });
                            }
                        }
                    } else {
                        displayStudentTeacherContainer();
                    }

                } catch (Exception e) {
                    System.out.println("DATABASE FAILURE. Failed to initialise.");
                    Platform.runLater(() -> {
                        displayStudentTeacherContainer();
                    });
                }
                return null;
            }
        };
        new Thread(initialisation).start();
    }

    public void displayUserAccount(UserAccount userAccount) {
        hideAllForms();
        userAccountDisplayContainer.setVisible(true);
        userGreeting.setText("Hello, "+userAccount.getUsername());
        Classroom classroom = ClassroomDAO.getClassroom(userAccount.getClassroomID());
        boolean teacher = Objects.equals(classroom.getTeacherID(), userAccount.getUserID()); // is this user the owner of the classroom?
        if (teacher) {
            userDescription.setText("You are the owner of the classroom '" + classroom.getClassroomName() + "'. You can view your students' progress below.");
        } else {
            userDescription.setText("You are already a part of the classroom '" + classroom.getClassroomName() + "'. Your progress will be sent to your teacher.");
        }
    }

    /**
     * check if the user has account information cached
     * @return whether cached information exists
     */
    private boolean localDataExists() {
        File file = new File(file_path);
        return file.exists() && file.length() > 0;
    }


    /**
     * hide all forms
     */
    private void hideAllForms() {
        studentTeacherContainer.setVisible(false);
        teacherSelectionForm.setVisible(false);
        userAccountDisplayContainer.setVisible(false);
        loadingContainer.setVisible(false);
        joinForm.setVisible(false);
        createForm.setVisible(false);
    }

    /**
     * display the "Student" or "Teacher" buttons
     */
    @FXML
    private void displayStudentTeacherContainer() {
        hideAllForms();
        studentTeacherContainer.setVisible(true);
    }

    /**
     * if "Student" button is clicked, display the student form to allow them to join a classroom.
     */
    @FXML
    public void displayStudent() {
        hideAllForms();
        joinForm.setVisible(true);
        joinForm.requestFocus();
        joinFormDescription.setText("Enter the ID of the classroom you want to join, and choose a username!");
        joinFormDescription.setTextFill(Color.BLACK);
        joinFormDescription.setMaxWidth(350);
        joinFormDescription.setPrefHeight(50);
        joinFormDescription.setTextAlignment(TextAlignment.CENTER);
        joinFormDescription.setWrapText(true);
    }

    @FXML
    public void displayTeacher() {
        hideAllForms();
        teacherSelectionForm.setVisible(true);
        teacherSelectionForm.requestFocus();
        joinFormDescription.setMaxWidth(350);
        joinFormDescription.setPrefHeight(50);
        joinFormDescription.setTextAlignment(TextAlignment.CENTER);
        joinFormDescription.setWrapText(true);
    }

    @FXML
    public void displayCreateForm() {
        hideAllForms();
        createForm.setVisible(true);
        createForm.requestFocus();
        createFormDescription.setMaxWidth(450);
        createFormDescription.setPrefHeight(100);
        createFormDescription.setTextAlignment(TextAlignment.CENTER);
        createFormDescription.setWrapText(true);
    }

    /**
     * take inputted information from the student form and add them to the classroom. cache account information.
     */
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
    public void createClassroom() {
        String classroomName = classroomNameField.getText();
        String teacherName = teacherNameField.getText();
        String password = passwordField.getText();

        // ensure fields are not empty
        if (classroomName.isBlank() || teacherName.isBlank() || password.isBlank()) {
            createFormDescription.setText("Please ensure all fields are filled in!");
            createFormDescription.setTextFill(Color.RED);
            return;
        }

        // ensure password is at least 6 characters and contains a number.
        if (password.length() >= 6 && password.length() <= 16 && password.chars().anyMatch(Character::isDigit)) {
            // do nothing
        } else {
            createFormDescription.setText("Password must be between 6 and 16 characters, and contain at least one digit.");
            return;
        }

        // display loading message
        createFormDescription.setText("Creating classroom...");
        createFormDescription.setTextFill(Color.BLACK);
        createFormDescription.setAlignment(Pos.CENTER);

        Task<Void> createTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    String userID = UserDAO.generateUserID();
                    String classroomID = ClassroomDAO.createClassroom(userID, classroomName);
                    UserDAO.createUser(classroomID, userID, teacherName, new TypingPlan(), password);
                    ClassroomDAO.saveUserCache(classroomID, teacherName, password);
                    Platform.runLater(() -> {
                        createFormDescription.setText("Classroom created! Classroom ID is: "+classroomID);
                        createFormDescription.setTextFill(Color.GREEN);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.out.println("DATABASE FAILURE. Failed to create classroom.");
                        createFormDescription.setText("An error occurred while creating the classroom. Please try again later...");
                        createFormDescription.setTextFill(Color.RED);
                    });
                }
                return null;
            }

        };
        new Thread(createTask).start();

    }


    @FXML
    public void onLearnButtonClick(){
        try{
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene=new Scene(loader.load(),1200,700);
            Stage stage= (Stage) learnButton.getScene().getWindow();
            stage.setScene(scene);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * close scene and go to games scene
     */
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
