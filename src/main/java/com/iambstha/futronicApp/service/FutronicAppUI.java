package com.iambstha.futronicApp.service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class FutronicAppUI {

    private static JLabel imageLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Fingerprint Desktop App");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();

            JTextField userNameTextField = new JTextField(20);
            JButton enrollButton = new JButton("Enroll");

            imageLabel = new JLabel();
            panel.add(userNameTextField);
            panel.add(enrollButton);
            panel.add(imageLabel);

            enrollButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enrollFingerprint(userNameTextField.getText());
                }
            });

            frame.getContentPane().add(panel);
            frame.setSize(500, 300);
            frame.setVisible(true);
        });
    }

    private static void enrollFingerprint(String userName) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://localhost:8080/enroll");
            httpPost.setEntity(new StringEntity(userName));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println("Response: " + EntityUtils.toString(response.getEntity()));
                
                byte[] imageData = response.getEntity().getContent().readAllBytes();
                displayImage(imageData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void displayImage(byte[] imageData) {
        try {
            ImageIcon imageIcon = new ImageIcon(imageData);
            Image image = imageIcon.getImage();
            Image scaledImage = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImage);
            imageLabel.setIcon(imageIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
