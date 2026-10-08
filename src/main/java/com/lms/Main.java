package com.lms;

import com.lms.database.DatabaseInitializer;
import com.lms.gui.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // Initialize database and create default accounts
        DatabaseInitializer.initialize();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}