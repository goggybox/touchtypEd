package com.example.touchtyped.model;

import java.io.*;

/**
 * 用户配置文件，存储用户信息
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String PROFILE_FILE = "user_profile.dat";
    
    private static UserProfile instance;
    
    private String playerName;
    
    // 私有构造函数，防止外部实例化
    private UserProfile() {
        playerName = "";
    }
    
    /**
     * 获取单例实例
     * @return UserProfile实例
     */
    public static synchronized UserProfile getInstance() {
        if (instance == null) {
            instance = loadProfile();
        }
        return instance;
    }
    
    /**
     * 加载用户配置
     * @return 加载的UserProfile实例，如果加载失败则返回新实例
     */
    private static UserProfile loadProfile() {
        File profileFile = new File(PROFILE_FILE);
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile))) {
            Object obj = ois.readObject();
            if (obj instanceof UserProfile) {
                return (UserProfile) obj;
            } else {
                System.err.println("文件格式不正确，包含非UserProfile对象: " + obj.getClass().getName());
                // 删除损坏的文件
                if (profileFile.exists()) {
                    profileFile.delete();
                    System.out.println("已删除损坏的配置文件");
                }
                return new UserProfile();
            }
        } catch (FileNotFoundException e) {
            System.out.println("用户配置文件不存在，创建新配置");
            return new UserProfile();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("加载用户配置时出错: " + e.getMessage());
            // 删除损坏的文件
            if (profileFile.exists()) {
                profileFile.delete();
                System.out.println("已删除损坏的配置文件");
            }
            return new UserProfile();
        }
    }
    
    /**
     * 保存用户配置
     */
    public void saveProfile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROFILE_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("保存用户配置时出错: " + e.getMessage());
        }
    }
    
    /**
     * 获取玩家名称
     * @return 玩家名称
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * 设置玩家名称
     * @param playerName 玩家名称
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        saveProfile();
    }
    
    /**
     * 检查是否已有玩家名称
     * @return true如果已有玩家名称，false否则
     */
    public boolean hasPlayerName() {
        return playerName != null && !playerName.trim().isEmpty();
    }
} 