package com.example.touchtyped.interfaces;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ComputerVisionInterface {
    private Process p;
    private JFrame popup;
    public ComputerVisionInterface(){

    }
    public void runCVProgramWithPopups(){
        Thread thread = new Thread(() -> {
            boolean placementIsCorrect = true;
            String s = null;
            boolean left_hand_correct = false;
            boolean right_hand_correct = false;
            popup = new JFrame();
            popup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            popup.setSize(600, 100);
            popup.setLayout(new BorderLayout());
            popup.setLocationRelativeTo(null);
            popup.setFocusableWindowState(false);
            JLabel label = new JLabel(" ");
            label.setFont(new Font("Consola",Font.PLAIN,20));
            label.setHorizontalAlignment(JLabel.CENTER);
            popup.add(label, BorderLayout.CENTER);
            try{
                p = Runtime.getRuntime().exec("python MediaPipe/src/final2.py");

                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));

                // read the output from the command
                while ((s = stdInput.readLine()) != null) {
                    if (s.equals("right hand correct")){
                        right_hand_correct = true;
                    } else if (s.equals("right hand incorrect")) {
                        right_hand_correct = false;
                    } else if (s.equals("left hand correct")) {
                        left_hand_correct = true;
                    } else if (s.equals("left hand incorrect")){
                        left_hand_correct = false;
                    }
                    System.out.println(s + " " + left_hand_correct + " " + right_hand_correct);
                    if (left_hand_correct && right_hand_correct) {
                        label.setVisible(false);
                        popup.setVisible(false);
                    } else if (left_hand_correct) {
                        label.setText("Your right hand is not in the correct position on the keyboard.");
                        label.setVisible(true);
                        popup.setVisible(true);
                    } else if (right_hand_correct) {
                        label.setText("Your left hand is not in the correct position on the keyboard.");
                        label.setVisible(true);
                        popup.setVisible(true);
                    } else {
                        label.setText("Your hands are not in their correct positions on the keyboard.");
                        label.setVisible(true);
                        popup.setVisible(true);
                    }
                }


            } catch (Exception e){
                System.out.println("didn't work");
            }

        });
        thread.start();
    }
    public void closeCVProgram(){
        p.destroy();
        popup.dispose();
    }



}
