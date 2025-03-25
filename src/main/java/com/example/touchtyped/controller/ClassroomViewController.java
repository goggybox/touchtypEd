package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.firestore.Classroom;
import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserAccount;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.PDFCache;
import com.example.touchtyped.model.PDFViewer;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.service.RESTClient;
import com.example.touchtyped.service.RESTResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class ClassroomViewController {

    @FXML
    private VBox joinCreateContainer;

    @FXML
    private ScrollPane studentListContainerScrollPane;

    @FXML
    private ScrollPane studentKeyLogsContainerScrollPane;

    @FXML
    private ScrollPane logContainerScrollPane;

    @FXML
    private VBox studentTeacherContainer;

    @FXML
    private VBox studentKeyLogsContainer;

    @FXML
    private VBox teacherSelectionForm;

    @FXML
    private VBox userAccountDisplayContainer;

    @FXML
    private VBox loginForm;

    @FXML
    private VBox joinForm;

    @FXML
    private VBox studentInfoContainer;

    @FXML
    private VBox createForm;

    @FXML
    private VBox loadingContainer;

    @FXML
    private HBox teacherContainer;

    @FXML
    private HBox typingTestsContainer;

    @FXML
    private HBox selectorContainer;

    @FXML
    private VBox accountContainer;

    @FXML
    private VBox studentContainer;

    @FXML
    private VBox studentListContainer;

    @FXML
    private VBox keyLogContainer;

    @FXML
    private VBox studentAccountContainer;

    @FXML
    private VBox studentList;

    @FXML
    private VBox logContainer;

    @FXML
    private ImageView gamesButton;

    @FXML
    private ImageView learnButton;

    @FXML
    private ImageView accountButton;

    @FXML
    private ImageView statsButton;

    @FXML
    private ImageView optionsButton;

    @FXML
    private Label joinFormDescription;

    @FXML
    private Label studentListDescriptor;

    @FXML
    private Label studentKeyLogsDescriptor;

    @FXML
    private Label accountJoinedDate;

    @FXML
    private Label teacherSelectionFormDescription;

    @FXML
    private Label createFormDescription;

    @FXML
    private Label userGreeting;

    @FXML
    private Label accountUsername;

    @FXML
    private Label userDescription;

    @FXML
    private Label loginFormDescription;

    @FXML
    private Label keyLogDescriptor;

    @FXML
    private Label changeTo;

    @FXML
    private Label accountTitle;

    @FXML
    private TextField classroomIDField;

    @FXML
    private TextField loginClassroomID;

    @FXML
    private TextField loginPassword;

    @FXML
    private TextField studentNameField;

    @FXML
    private TextField classroomNameField;

    @FXML
    private TextField teacherNameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button accountChangeUsernameButton;

    @FXML private Label removeStudent;

    @FXML private TextField removeStudentInput;
    @FXML private Button removeStudentButton;

    @FXML private Label errorLabel;

    @FXML private Label detailsChangedErrorLabel;


    @FXML
    private TextField accountChangeUsernameInput;

    private static final String file_path = "user_cache.txt";

    private final Font primary_font = Font.loadFont(this.getClass().getResourceAsStream("/fonts/Antipasto_extrabold.otf"), 48);
    private final Font secondary_font = Font.loadFont(this.getClass().getResourceAsStream("/fonts/Manjari.ttf"), 22);

    private Label selectedStudentLabel = null;
    private Label selectedKeyLogLabel = null;

    private boolean isAccountSelected = true;
    private boolean isStatsSelected = false;

    private String ourClassroomID;
    private String selectedStudentUsername;

    /**
     * used to check if a PDF still needs to be displayed, or the user has clicked on a different keylog.
     */
    private Task<RESTResponseWrapper> currentPDFTask = null;

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
                        ourClassroomID = classroomID;
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

    public void displayUserDetailsChangedError(String message) {
        Platform.runLater(() -> {
            detailsChangedErrorLabel.setText(message);
            detailsChangedErrorLabel.setTextFill(Color.web(StyleConstants.RED_COLOUR));
            detailsChangedErrorLabel.setVisible(true);
            System.out.println("Displaying error that the user's details have changed.");
        });
    }



    /**
     * this function runs when the user has logged in previously, and their account information has been cached.
     * this function will display a simple message to student users, but will display complex information to a teacher
     * about all of their students and the students' typing tests results.
     * @param userAccount is the user's account loaded from the cache.
     */
    public void displayUserAccount(UserAccount userAccount) {
        hideAllForms();
        isAccountSelected = true;
        isStatsSelected = false;
        accountButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/classroom-content/accountSelected.png").toExternalForm()));
        statsButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/classroom-content/stats.png").toExternalForm()));
        teacherContainer.setVisible(false);
        studentContainer.setVisible(false);
        userAccountDisplayContainer.setVisible(true);
        userGreeting.setFont(primary_font);
        selectorContainer.setVisible(false);
        accountContainer.setVisible(false);
        typingTestsContainer.setVisible(false);
        keyLogContainer.setVisible(false);
        userDescription.setFont(secondary_font);
        userDescription.setWrapText(true);
        userGreeting.setText("Hello, "+userAccount.getUsername());
        Classroom classroom = ClassroomDAO.getClassroom(userAccount.getClassroomID());
        boolean teacher = Objects.equals(classroom.getTeacherID(), userAccount.getUserID()); // is this user the owner of the classroom?
        if (teacher) {
            userDescription.setText("You are the owner of the classroom '" + classroom.getClassroomName() + "', with " +
                    "ID '"+ classroom.getClassroomID() + "'. You can view your students' progress below.");
            teacherContainer.setVisible(true);
            populateStudentList(classroom);
        } else {
            userDescription.setText("You are already a part of the classroom '" + classroom.getClassroomName() + "'. Your progress will be sent to your teacher.");
        }
    }

    /**
     * this function displays to a teacher the list of students that are in their classroom.
     * @param classroom is the classroom to load the students from.
     */
    private void populateStudentList(Classroom classroom) {
        studentListContainer.getChildren().clear();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<String> students = classroom.getStudentUsernames();
                students.sort(String.CASE_INSENSITIVE_ORDER);
                Platform.runLater(() -> {
                    studentListDescriptor.setFont(secondary_font);
                    studentListDescriptor.setText("Students");
                    if (!students.isEmpty()) {
                        for (String student : students) {
                            Label studentLabel = new Label(student);
                            studentLabel.setFont(secondary_font);
                            studentLabel.getStyleClass().add("student");
                            studentLabel.setPrefWidth((students.size() > 10) ? 225 : 242);

                            // Add click handler
                            studentLabel.setOnMouseClicked(e -> {
                                // Reset previous selection
                                if (selectedStudentLabel != null) {
                                    selectedStudentLabel.getStyleClass().remove("student-selected");
                                }
                                if (selectedKeyLogLabel != null) {
                                    selectedKeyLogLabel.getStyleClass().remove("log-selected");
                                }

                                // Set new selection
                                studentLabel.getStyleClass().add("student-selected");
                                selectedStudentLabel = studentLabel;
                                selectedStudentUsername = selectedStudentLabel.getText();
                                System.out.println("Selected user: " + selectedStudentUsername);
                                // hide error message if visible
                                errorLabel.setVisible(false);

                                // load student's keyLogsStructures IF we have selected Stats
                                if (isStatsSelected) {
                                    System.out.println("Loading user's key logs");
                                    loadStudentKeyLogs(classroom.getClassroomID(), selectedStudentUsername);
                                } else {
                                    System.out.println("Loading user's account.");
                                    loadAccount(classroom.getClassroomID(), selectedStudentUsername);
                                }

                                // set selectorContainer to be visible
                                selectorContainer.setVisible(true);

                            });

                            studentListContainer.getChildren().add(studentLabel);
                        }
                    } else {
                        Label studentLabel = new Label("No students yet.");
                        studentLabel.setFont(secondary_font);
                        studentLabel.setStyle("-fx-font-size: 16px;");
                        studentLabel.setTextFill(Color.rgb(97,97,97));
                        studentLabel.setPrefWidth((students.size() > 10) ? 225 : 242);
                        studentLabel.setTextAlignment(TextAlignment.CENTER);
                        studentLabel.setAlignment(Pos.CENTER);
                        studentListContainer.getChildren().add(studentLabel);
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void loadAccount(String classroomID, String student) {
        typingTestsContainer.setVisible(false);
        typingTestsContainer.setManaged(false);
        accountChangeUsernameInput.setText("");
        removeStudentInput.setText("");
        accountContainer.setVisible(true);
        accountContainer.setManaged(true);

        accountTitle.setFont(secondary_font);
        accountTitle.setText("Account Details of "+student);

        accountUsername.setFont(secondary_font);
        accountUsername.setText("Student's Username: "+student);
        changeTo.setFont(secondary_font);

        // get user account
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    UserAccount user = UserDAO.getAccount(classroomID, student);
                    Platform.runLater(() -> {
                        String formattedTime = Instant.ofEpochMilli(user.getJoinedDate())
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("dd/MM '('EEE')' 'at' HH:mm"));
                        accountJoinedDate.setText("User joined classroom on "+formattedTime);
                        accountJoinedDate.setFont(secondary_font);
                    });
                } catch (Exception e) {
                    System.out.println("DATABASE FAILURE. Failed to fetch user.");
                }

                return null;
            }
        };
        new Thread(task).start();

    }

    @FXML
    private void changeUsername() {
        String newUsername = accountChangeUsernameInput.getText();
        if (newUsername.isBlank()) {
            showWarning("* You must first enter a new username!");
            return;
        }

        if (newUsername.length() < 5 || newUsername.length() > 25) {
            showWarning("* New username must be between 5 and 25 characters long.");
            return;
        }

        if (newUsername.equals(selectedStudentUsername)) {
            showWarning("* New username must be different!");
            return;
        }

        Task<Boolean> checkTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return ClassroomDAO.usernameExistsInClassroom(ourClassroomID, newUsername);
            }
        };

        checkTask.setOnSucceeded(event -> {
            boolean exists = checkTask.getValue();
            if (exists) {
                showWarning("* A user with that name already exists!");
            } else {
                // Proceed with username change
                performUsernameChange(newUsername);
            }
        });

        checkTask.setOnFailed(event -> {
            System.out.println("DATABASE FAILURE: Failed to check username existence");
            showWarning("* Failed to check username availability. Please try again.");
        });

        new Thread(checkTask).start();


    }

    private void performUsernameChange(String newUsername) {
        // get the user
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {

                    UserAccount user = UserDAO.getAccount(ourClassroomID, selectedStudentUsername);
                    Classroom classroom = ClassroomDAO.getClassroom(ourClassroomID);
                    Platform.runLater(() -> {
                        String userID = user.getUserID();

                        try {
                            // delete user from db, change UserAccount username, then save user.
                            UserDAO.deleteUser(ourClassroomID, selectedStudentUsername);
                            System.out.println("Deleted user from database.");
                            user.setUsername(newUsername);
                            UserDAO.addUserAccount(ourClassroomID, user);
                            System.out.println("Added user with new username to database.");

                            // update classroom's student list
                            List<String> students = classroom.getStudentUsernames();
                            int index = students.indexOf(selectedStudentUsername);
                            students.set(index, newUsername);
                            ClassroomDAO.replaceStudentList(ourClassroomID, students);
                            System.out.println("Replaced students list in classroom.");

                            // update UI
                            selectedStudentLabel.setText(newUsername);
                            showSuccess("* Successfully updated '"+selectedStudentUsername+"' to '"+newUsername+"'.");
                            selectedStudentUsername = newUsername;
                            accountTitle.setText("Account Details of "+selectedStudentUsername);
                            accountUsername.setText("Student's Username: "+selectedStudentUsername);

                            // empty accountChangeUsernameInput
                            accountChangeUsernameInput.setText("");

                        } catch (Exception e) {
                            System.out.println("DATABASE FAILURE: Failed to update user or classroom student list.");
                        }

                    });

                } catch (Exception e) {
                    System.out.println("DATABASE FAILURE: Failed to get user or classroom.");
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * used to display a warning message in the errorLabel in the Accounts view.
     * @param message is the message to display
     */
    private void showWarning(String message) {
        Platform.runLater(() -> {
            errorLabel.setTextFill(Color.web(StyleConstants.RED_COLOUR));
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(event -> errorLabel.setVisible(false));
            pause.play();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            errorLabel.setTextFill(Color.web(StyleConstants.BLUE_COLOUR));
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(event -> errorLabel.setVisible(false));
            pause.play();
        });
    }

    @FXML
    private void removeStudent() {
        String inputtedUsername = removeStudentInput.getText();

        if (!inputtedUsername.equals(selectedStudentUsername)) {
            showWarning("* Please confirm the student's username in order to remove them!");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    UserDAO.deleteUser(ourClassroomID, selectedStudentUsername);
                    System.out.println("Deleted user "+selectedStudentUsername);
                    List<String> studentsList = ClassroomDAO.getClassroom(ourClassroomID).getStudentUsernames();
                    studentsList.remove(selectedStudentUsername);
                    ClassroomDAO.replaceStudentList(ourClassroomID, studentsList);
                    System.out.println("Updated students list in classroom to remove student.");

                    Platform.runLater(() -> {
                        studentListContainer.getChildren().remove(selectedStudentLabel);

                        logContainer.getChildren().clear();
                        keyLogContainer.setVisible(false);

                        selectedStudentLabel = null;
                        selectedStudentUsername = null;

                        accountContainer.setVisible(false);
                        accountContainer.setManaged(false);

                        selectorContainer.setVisible(false);

                        showSuccess("Student '" + inputtedUsername + "' removed successfully.");

                        if (studentsList.isEmpty()) {
                            Label studentLabel = new Label("No students yet.");
                            studentLabel.setFont(secondary_font);
                            studentLabel.setStyle("-fx-font-size: 16px;");
                            studentLabel.setTextFill(Color.rgb(97,97,97));
                            studentLabel.setPrefWidth((studentsList.size() > 10) ? 225 : 242);
                            studentLabel.setTextAlignment(TextAlignment.CENTER);
                            studentLabel.setAlignment(Pos.CENTER);
                            studentListContainer.getChildren().add(studentLabel);
                        }
                    });

                } catch (Exception e) {
                    System.out.println("DATABASE FAILURE: Failed to remove student from classroom.");
                }
                return null;
            }
        };
        new Thread(task).start();

    }

    /**
     * this function loads a student's typing test results. this function is called when a teacher selects a student from
     * the student list.
     * @param classroomID is the classroomID to search for the student's account in.
     * @param username is the student's username
     */
    private void loadStudentKeyLogs(String classroomID, String username) {
        accountContainer.setVisible(false);
        accountContainer.setManaged(false);
        errorLabel.setVisible(false);
        studentInfoContainer.setVisible(true);
        typingTestsContainer.setVisible(true);
        typingTestsContainer.setManaged(true);
        studentKeyLogsContainer.getChildren().clear();
        logContainer.getChildren().clear();
        keyLogDescriptor.setText("");
        studentKeyLogsDescriptor.setText("User's Typing Tests (LOADING...)");
        Task<Void> task = new Task<>() {
            protected Void call() {
                Platform.runLater(() -> {
                    try {
                        UserAccount user = UserDAO.getAccount(classroomID, username);
                        List<KeyLogsStructure> logs = user.getKeyLogs();
                        if (logs != null && logs.size() > 0) {
                            for (KeyLogsStructure log : logs) {
                                long time = log.getTimeCreated();
                                String formattedTime = Instant.ofEpochMilli(time)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("dd/MM '('EEE')' 'at' HH:mm"));
                                Label logLabel = new Label(formattedTime);
                                logLabel.setFont(secondary_font);
                                logLabel.getStyleClass().add("log");
                                logLabel.setPrefWidth((logs.size() > 10) ? 225 : 242);

                                logLabel.setOnMouseClicked(e -> {

                                    // cancel previous task
                                    if (currentPDFTask != null && !currentPDFTask.isDone()) {
                                        currentPDFTask.cancel();
                                        System.out.println("Canceled previous PDF task.");
                                    }

                                    // clear previous selection
                                    if (selectedKeyLogLabel != null) {
                                        selectedKeyLogLabel.getStyleClass().remove("log-selected");
                                    }

                                    logLabel.getStyleClass().add("log-selected");
                                    selectedKeyLogLabel = logLabel;

                                    // load selected key log
                                    loadKeyLog(log, username);
                                });

                                studentKeyLogsContainer.getChildren().add(logLabel);
                            }
                        } else {
                            // student has no logged typing tests results
                            Label logLabel = new Label("None");
                            logLabel.setFont(secondary_font);
                            logLabel.setStyle("-fx-font-size: 16px;");
                            logLabel.setTextFill(Color.rgb(97,97,97));
                            logLabel.setTextAlignment(TextAlignment.CENTER);
                            logLabel.setAlignment(Pos.CENTER);
                            logLabel.setPrefWidth((logs.size() > 10) ? 225 : 242);
                            studentKeyLogsContainer.getChildren().add(logLabel);
                        }

                        studentKeyLogsDescriptor.setText("User's Typing Tests");
                    } catch (Exception e) {
                        System.out.println("DATABASE FAILURE. Failed to load student's key logs.");
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * this function loads the PDF results for a specific typing test. this is called when the teacher has already selected
     * a student and then selects one of the student's typing tests to view.
     * if a pdf is fetched from the REST service, it is cached temporarily so the teacher can view it again without a new
     * call to the REST service.
     * @param log is the specific typing test being reviewed.
     * @param username is the student's username
     */
    private void loadKeyLog(KeyLogsStructure log, String username) {
        keyLogContainer.setVisible(true);
        long time = log.getTimeCreated();
        logContainer.getChildren().clear();
        String formattedTime = Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd/MM '('EEE')' 'at' HH:mm"));

        // CHECK CACHE FIRST
        String cacheKey = username + time;
        byte[] cachedPDF = PDFCache.getInstance().getPDF(cacheKey);
        if (cachedPDF != null) {
            displayPDF(cachedPDF, log);
            System.out.println("Loaded PDF from cache instead of fetching from REST service.");
            keyLogDescriptor.setFont(secondary_font);
            keyLogDescriptor.setText("Results of " + username + "'s typing test on "+formattedTime+".");
            keyLogDescriptor.setAlignment(Pos.CENTER);
            return;
        }


        // show loading message
        keyLogDescriptor.setFont(secondary_font);
        keyLogDescriptor.setText("Loading results of typing test...");
        keyLogDescriptor.setAlignment(Pos.CENTER);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(log));
        } catch (IOException e) {
            System.out.println("oh dear");
        }

        // call REST service to get PDF
        Task<RESTResponseWrapper> restTask = new Task<>() {
            @Override
            protected RESTResponseWrapper call() throws Exception {

                // check for cancellation
                if (isCancelled()) { return null; }

                RESTClient restService = new RESTClient();
                return restService.getPDF(log, () -> {
                    if (!isCancelled()) {
                        Platform.runLater(() ->
                                keyLogDescriptor.setText("Failed to load. Trying again... (Please be patient!)"));
                    }
                });
            }
        };
        restTask.setOnSucceeded(event -> {
            try {
                if (restTask == currentPDFTask) {
                    RESTResponseWrapper response = restTask.getValue();

                    // handle PDF
                    if (response.getPdfData() != null) {
                        System.out.println("Successfully fetched PDF from REST service.");
                        // add the PDF to the pdf cache
                        PDFCache.getInstance().putPDF(cacheKey, response.getPdfData());
                        System.out.println("Added PDF to PDF cache.");
                        displayPDF(response.getPdfData(), log);

                    }

                    keyLogDescriptor.setFont(secondary_font);
                    keyLogDescriptor.setText("Results of " + username + "'s typing test on " + formattedTime + ".");
                    keyLogDescriptor.setAlignment(Pos.CENTER_RIGHT);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        restTask.setOnFailed(event -> {
            if (restTask == currentPDFTask) {
                Throwable exception = restTask.getException();
                System.err.println("An error occurred while communicating with the REST service.");
                exception.printStackTrace();

                // notify the user.
                keyLogDescriptor.setFont(secondary_font);
                keyLogDescriptor.setText("Failed to load analytics. Please try again later.");
            }
        });

        currentPDFTask = restTask;
        new Thread(restTask).start();

    }

    /**
     * this function displays a loaded PDF in the VBox using PDFViewer, which uses the PDFBox library.
     * @param pdfData is the pdf to display.
     */
    private void displayPDF(byte[] pdfData, KeyLogsStructure log) {

        Task<Void> pdfTask = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    logContainer.getChildren().clear();
                    PDFViewer pdfViewer = new PDFViewer(pdfData);
                    // load simple statistics
                    Label simpleDescriptor = new Label("Here is a simple analysis of the typing test:");
                    simpleDescriptor.setFont(secondary_font);
                    simpleDescriptor.getStyleClass().add("stats-descriptor");
                    Label wpmLabel = new Label("Words per Minute (wpm): " + log.getWpm());
                    wpmLabel.setFont(secondary_font);
                    Label keyStrokes = new Label("Correct/Incorrect Keystrokes: " + log.getCorrectKeystrokes() + "/" + log.getIncorrectKeystrokes());
                    keyStrokes.setFont(secondary_font);
                    Label totalKeyStrokes = new Label("Total Keystrokes: " + (log.getCorrectKeystrokes() + log.getIncorrectKeystrokes()));
                    totalKeyStrokes.setFont(secondary_font);
                    wpmLabel.getStyleClass().add("simple-stats");
                    keyStrokes.getStyleClass().add("simple-stats");
                    totalKeyStrokes.getStyleClass().add("simple-stats");
                    Label advancedDescriptor = new Label("Here is an advanced analysis of the typing test:");
                    advancedDescriptor.setFont(secondary_font);
                    advancedDescriptor.getStyleClass().add("stats-descriptor");
                    logContainer.getChildren().add(simpleDescriptor);
                    logContainer.getChildren().add(wpmLabel);
                    logContainer.getChildren().add(keyStrokes);
                    logContainer.getChildren().add(totalKeyStrokes);
                    logContainer.getChildren().add(advancedDescriptor);
                    logContainer.getChildren().add(pdfViewer);
                });
                return null;
            }
        };
        new Thread(pdfTask).start();
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
        detailsChangedErrorLabel.setVisible(false);
        loadingContainer.setVisible(false);
        joinForm.setVisible(false);
        createForm.setVisible(false);
        loginForm.setVisible(false);
        studentInfoContainer.setVisible(false);
        keyLogContainer.setVisible(false);
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
        classroomIDField.setText("");
        studentNameField.setText("");
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
        teacherSelectionFormDescription.setText("Login to an existing account, or create a new account and classroom.");
        teacherSelectionFormDescription.setTextFill(Color.BLACK);
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
        classroomNameField.setText("");
        teacherNameField.setText("");
        passwordField.setText("");
        createFormDescription.setText("Please fill in the details below. A unique classroom ID will then be generated for you to share with your students. Passwords must be between 6-16 characters and contain at least one digit.");
        createFormDescription.setTextFill(Color.BLACK);
        createFormDescription.setMaxWidth(450);
        createFormDescription.setPrefHeight(100);
        createFormDescription.setTextAlignment(TextAlignment.CENTER);
        createFormDescription.setWrapText(true);
    }

    @FXML
    public void displayLoginForm() {
        hideAllForms();
        loginForm.setVisible(true);
        loginForm.requestFocus();
        loginClassroomID.setText("");
        loginPassword.setText("");
        loginFormDescription.setMaxWidth(450);
        loginFormDescription.setText("Fill in the details below to log-in to your existing teacher account.");
        loginFormDescription.setTextFill(Color.BLACK);
        loginFormDescription.setPrefHeight(100);
        loginFormDescription.setTextAlignment(TextAlignment.CENTER);
        loginFormDescription.setWrapText(true);
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

        if (username.length() < 5 || username.length() > 25) {
            joinFormDescription.setText("Usernames must be between 5 and 25 characters long.");
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
                            ClassroomDAO.saveUserCache(classroomID, username);
                            Platform.runLater(() -> {
                                joinFormDescription.setText("Logged in successfully! Loading...");
                                try {
                                    UserAccount user = UserDAO.getAccount(classroomID, username);
                                    displayUserAccount(user);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                joinFormDescription.setTextFill(Color.GREEN);
                            });
                        } else {
                            // creating new account
                            ClassroomDAO.addStudentToClassroom(classroomID, username);
                            UserDAO.createUser(classroomID, username, new TypingPlan());
                            ClassroomDAO.saveUserCache(classroomID, username);
                            Platform.runLater(() -> {
                                joinFormDescription.setText("Joined classroom successfully! Loading...");
                                try {
                                    UserAccount user = UserDAO.getAccount(classroomID, username);
                                    displayUserAccount(user);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
        createFormDescription.setAlignment(Pos.CENTER);

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
                        try {
                            UserAccount teacher = UserDAO.getAccount(classroomID, teacherName);
                            displayUserAccount(teacher);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
    public void teacherLogin() {
        String classroomID = loginClassroomID.getText();
        String password = loginPassword.getText();
        loginFormDescription.setAlignment(Pos.CENTER);

        if (classroomID.isBlank() || password.isBlank()) {
            loginFormDescription.setText("Please ensure all the fields are filled in!");
            loginFormDescription.setTextFill(Color.RED);
            return;
        }

        // display loading message
        loginFormDescription.setText("Checking credentials...");
        loginFormDescription.setTextFill(Color.BLACK);
        Task<Void> loginTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Classroom classroom = ClassroomDAO.getClassroom(classroomID);
                    if (classroom != null) {
                        UserAccount teacher = UserDAO.getAccountByID(classroom.getTeacherID(), password);
                        if (teacher != null) {
                            // successfully logged in
                            Platform.runLater(() -> {
                                ClassroomDAO.saveUserCache(classroomID, teacher.getUsername(), password);
                                loginFormDescription.setText("Logged in successfully!");
                                loginFormDescription.setTextFill(Color.GREEN);
                                displayUserAccount(teacher);
                            });
                        } else {
                            // password was incorrect
                            Platform.runLater(() -> {
                                loginFormDescription.setText("Password is incorrect.");
                                loginFormDescription.setTextFill(Color.RED);
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            loginFormDescription.setText("No classroom exists with the provided ID.");
                            loginFormDescription.setTextFill(Color.RED);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.out.println("DATABASE FAILURE. Failed to log in to classroom as teacher");
                        loginFormDescription.setText("An error occurred while attempting to login. Please try again later.");
                        loginFormDescription.setTextFill(Color.RED);
                    });
                }
                return null;
            }
        };
        new Thread(loginTask).start();


    }

    @FXML
    public void onAccountButtonClick() {
        System.out.println("ACCOUNT BUTTON CLICKED");
        if (!isAccountSelected) {
            isStatsSelected = false;
            isAccountSelected = true;
            accountButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/classroom-content/accountSelected.png").toExternalForm()));
            statsButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/classroom-content/stats.png").toExternalForm()));
            System.out.println("'"+selectedStudentUsername+"' account selected for classroom "+ourClassroomID);
            loadAccount(ourClassroomID, selectedStudentUsername);
        }

    }

    @FXML
    public void onStatsButtonClick() {
        System.out.println("STATS BUTTON CLICKED");
        if (!isStatsSelected) {
            isStatsSelected = true;
            isAccountSelected = false;
            accountButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/classroom-content/account.png").toExternalForm()));
            statsButton.setImage(new Image(getClass().getResource("/com/example/touchtyped/images/classroom-content/statsSelected.png").toExternalForm()));
            System.out.println("'"+selectedStudentUsername+"' stats selected for classroom "+ourClassroomID);
            loadStudentKeyLogs(ourClassroomID, selectedStudentUsername);
        }
    }

    @FXML
    public void onLogoutButtonClicked() {
        ClassroomDAO.deleteUserCache();
        displayStudentTeacherContainer();
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

    @FXML
    public void onOptionsButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/options-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) optionsButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
