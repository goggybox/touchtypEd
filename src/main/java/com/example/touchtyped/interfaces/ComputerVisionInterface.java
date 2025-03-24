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
            popup = new JFrame();
            popup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            popup.setSize(600, 100);
            popup.setLayout(new BorderLayout());
            popup.setLocationRelativeTo(null);
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
                    if (s == "Both hands correct"){
                        popup.setVisible(false);
                    } else {
                        label.setText(s);
                        popup.setVisible(true);
                    }
                    System.out.println(s);
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
