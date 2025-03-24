package com.example.touchtyped.controller;

import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.service.GlobalRankingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the ranking view
 */
public class RankingViewController {
    @FXML private TableView<PlayerRanking> rankingTable;
    @FXML private TableColumn<PlayerRanking, Integer> rankColumn;
    @FXML private TableColumn<PlayerRanking, String> nameColumn;
    @FXML private TableColumn<PlayerRanking, Integer> wpmColumn;
    @FXML private TableColumn<PlayerRanking, Double> accuracyColumn;
    @FXML private TableColumn<PlayerRanking, String> gameModeColumn;
    @FXML private TableColumn<PlayerRanking, LocalDateTime> dateColumn;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up the table columns
        rankColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        wpmColumn.setCellValueFactory(new PropertyValueFactory<>("wpm"));
        
        accuracyColumn.setCellValueFactory(new PropertyValueFactory<>("accuracy"));
        accuracyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                }
            }
        });
        
        gameModeColumn.setCellValueFactory(new PropertyValueFactory<>("gameMode"));
        
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(item));
                }
            }
        });
        
        // Load the rankings
        loadRankings();
    }
    
    /**
     * Load the rankings from the service
     */
    private void loadRankings() {
        // Show loading indicator
        ObservableList<PlayerRanking> emptyList = FXCollections.observableArrayList();
        PlayerRanking loadingIndicator = new PlayerRanking("Loading global rankings...", 0, 0, "");
        emptyList.add(loadingIndicator);
        rankingTable.setItems(emptyList);
        
        // Get rankings from global server
        GlobalRankingService globalRankingService = GlobalRankingService.getInstance();
        globalRankingService.getGlobalRankings().thenAccept(rankings -> {
            javafx.application.Platform.runLater(() -> {
                if (rankings != null && !rankings.isEmpty()) {
                    ObservableList<PlayerRanking> observableRankings = FXCollections.observableArrayList(rankings);
                    rankingTable.setItems(observableRankings);
                } else {
                    ObservableList<PlayerRanking> noDataList = FXCollections.observableArrayList();
                    PlayerRanking noData = new PlayerRanking("Unable to get global ranking data", 0, 0, "");
                    noDataList.add(noData);
                    rankingTable.setItems(noDataList);
                }
            });
        });
    }
    
    /**
     * Navigate to learn view
     */
    @FXML
    public void onLearnButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) rankingTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Return to game view
     */
    @FXML
    public void onReturnButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene gameScene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) rankingTable.getScene().getWindow();
            stage.setScene(gameScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 