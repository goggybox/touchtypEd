package com.example.touchtyped.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

/**
 * 玩家名称输入对话框
 */
public class PlayerNameDialog {
    
    /**
     * 显示玩家名称输入对话框
     * @return 玩家输入的名称，如果取消则返回null
     */
    public static String showDialog() {
        // 创建对话框
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("输入玩家名称");
        dialog.setHeaderText("请输入您的名称，用于排名显示");
        
        // 设置按钮
        ButtonType confirmButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        // 创建网格布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // 添加文本输入框
        TextField nameField = new TextField();
        nameField.setPromptText("输入名称");
        
        // 将控件添加到网格
        grid.add(new Label("玩家名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        
        // 设置默认焦点
        Platform.runLater(nameField::requestFocus);
        
        // 将网格添加到对话框
        dialog.getDialogPane().setContent(grid);
        
        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return nameField.getText();
            }
            return null;
        });
        
        // 显示对话框并获取结果
        Optional<String> result = dialog.showAndWait();
        
        return result.orElse(null);
    }
} 