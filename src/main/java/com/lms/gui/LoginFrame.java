package com.lms.gui;

import com.lms.model.User;
import com.lms.service.UserService;
import com.lms.util.InputValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * LoginFrame — the application entry point GUI.
 *
 * Demonstrates:
 * - Java Swing JFrame, JPanel, JLabel, JTextField, JPasswordField, JButton
 * - Event handling via ActionListener / KeyListener
 * - Polymorphism: opens StudentDashboard or TeacherDashboard based on user role
 * - Custom exception handling: catches UserNotFoundException +
 * IllegalArgumentException
 */
public class LoginFrame extends JFrame {

    private final UserService userService;

    // Swing components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        this.userService = new UserService();
        initUI();
    }

    private void initUI() {
        setTitle("LMS — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 380);
        setLocationRelativeTo(null); // Centre on screen
        setResizable(false);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(30, 34, 45));
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // ---- Header ----
        JLabel titleLabel = new JLabel("Online LMS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(99, 179, 237));
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel subLabel = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLabel.setForeground(new Color(150, 160, 180));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 34, 45));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        // ---- Form Panel ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(30, 34, 45));
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;

        // Email field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel emailLabel = styledLabel("Email Address");
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        emailField = styledTextField("teacher@lms.com");
        formPanel.add(emailField, gbc);

        // Password field
        gbc.gridy = 2;
        formPanel.add(styledLabel("Password"), gbc);

        gbc.gridy = 3;
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        formPanel.add(passwordField, gbc);

        // Status / error label
        gbc.gridy = 4;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(252, 100, 100));
        formPanel.add(statusLabel, gbc);

        // Login button
        gbc.gridy = 5;
        loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setBackground(new Color(66, 135, 245));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(0, 40));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(this::handleLogin);
        formPanel.add(loginButton, gbc);

        // Allow Enter key to trigger login
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    handleLogin(null);
            }
        });

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Default credentials hint
        JLabel hint = new JLabel("Default: teacher@lms.com / teacher123", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(100, 110, 130));
        mainPanel.add(hint, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Handles login button click.
     * Demonstrates: custom exception handling, polymorphism (role-based dashboard).
     */
    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!InputValidator.isNotBlank(email) || !InputValidator.isNotBlank(password)) {
            showError("Please enter both email and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        // Run login on a background thread to keep UI responsive
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.login(email, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
                try {
                    User user = get();
                    openDashboard(user);
                } catch (Exception ex) {
                    // Unwrap ExecutionException to show friendly message
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Polymorphism: the same method handles both user types.
     * At runtime, the correct dashboard (Student or Teacher) is opened
     * based on the actual type of 'user'.
     */
    private void openDashboard(User user) {
        dispose(); // Close login window

        SwingUtilities.invokeLater(() -> {
            JFrame dashboard;
            if ("STUDENT".equals(user.getRole())) {
                dashboard = new StudentDashboard(user);
            } else {
                dashboard = new TeacherDashboard(user);
            }
            dashboard.setVisible(true);
        });
    }

    private void showError(String message) {
        statusLabel.setText(message);
    }

    // ---- Styling helpers ----

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(180, 190, 210));
        return lbl;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        styleTextField(tf);
        return tf;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBackground(new Color(45, 50, 65));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 75, 100)),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 36));
    }
}
