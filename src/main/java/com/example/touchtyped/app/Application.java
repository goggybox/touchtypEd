package com.example.touchtyped.app;

import com.example.touchtyped.controller.LearnViewController;
import com.example.touchtyped.controller.PlayerNameDialog;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.ExampleKeypressListener;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.model.TypingPlanManager;
import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.model.UserProfile;
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
     * 提交游戏结束时的排名数据到全球排名服务器
     * @param ranking 要提交的排名数据
     */
    public static void submitGameRanking(PlayerRanking ranking) {
        try {
            // 直接提交到全球排名服务器
            GlobalRankingService globalRankingService = GlobalRankingService.getInstance();
            CompletableFuture<Boolean> future = globalRankingService.submitRanking(ranking);
            
            future.thenAccept(success -> {
                if (success) {
                    System.out.println("排名已成功提交到全球服务器");
                    
                    // 获取玩家在全球排名中的位置
                    globalRankingService.getPlayerPosition(ranking.getPlayerName())
                        .thenAccept(position -> {
                            if (position > 0) {
                                System.out.println("玩家 " + ranking.getPlayerName() + 
                                                  " 在全球排名中的位置: " + position);
                            } else {
                                System.out.println("无法获取玩家在全球排名中的位置");
                            }
                        });
                } else {
                    System.out.println("提交到全球服务器失败");
                }
            });
        } catch (Exception e) {
            System.err.println("提交排名时出错: " + e.getMessage());
        }
    }
}