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
        // 初始化用户配置文件
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
     * 初始化用户配置文件，如果没有用户名则请求输入
     */
    private void initUserProfile() {
        UserProfile userProfile = UserProfile.getInstance();
        System.out.println("正在检查用户配置文件...");
        System.out.println("是否已有用户名: " + userProfile.hasPlayerName());
        
        if (!userProfile.hasPlayerName()) {
            System.out.println("需要请求用户输入名称");
            
            // 删除现有的配置文件（如果存在），确保重新请求输入名称
            try {
                java.io.File profileFile = new java.io.File("user_profile.dat");
                if (profileFile.exists()) {
                    profileFile.delete();
                    System.out.println("已删除现有配置文件");
                }
            } catch (Exception e) {
                System.err.println("删除配置文件时出错: " + e.getMessage());
            }
            
            // 直接显示对话框，不使用Platform.runLater
            String playerName = PlayerNameDialog.showDialog();
            System.out.println("用户输入的名称: " + playerName);
            
            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "Anonymous";
                System.out.println("使用默认名称: Anonymous");
            }
            
            userProfile.setPlayerName(playerName);
            System.out.println("已设置用户名称: " + playerName);
        } else {
            System.out.println("已有用户名称: " + userProfile.getPlayerName());
        }
    }

    public static void main(String[] args) {
        // 测试RankingService
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
     * 测试RankingService
     */
    private static void testRankingService() {
        try {
            System.out.println("测试RankingService...");
            RankingService rankingService = RankingService.getInstance();
            
            // 测试添加新排名
            PlayerRanking ranking1 = new PlayerRanking("TestUser", 50, 90.0, "Timed Mode");
            rankingService.addRanking(ranking1);
            
            // 测试添加相同名字但更好的排名
            PlayerRanking ranking2 = new PlayerRanking("TestUser", 60, 95.0, "Timed Mode");
            rankingService.addRanking(ranking2);
            
            // 测试添加相同名字但更差的排名
            PlayerRanking ranking3 = new PlayerRanking("TestUser", 40, 85.0, "Timed Mode");
            rankingService.addRanking(ranking3);
            
            // 打印所有排名
            List<PlayerRanking> rankings = rankingService.getRankings();
            System.out.println("当前排名列表:");
            for (int i = 0; i < rankings.size(); i++) {
                PlayerRanking ranking = rankings.get(i);
                System.out.println((i + 1) + ". " + ranking.getPlayerName() + 
                                  " - WPM: " + ranking.getWpm() + 
                                  " - 准确率: " + ranking.getAccuracy() + "% - " + 
                                  ranking.getGameMode());
            }
            
            System.out.println("RankingService测试完成");
        } catch (Exception e) {
            System.err.println("RankingService测试出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}