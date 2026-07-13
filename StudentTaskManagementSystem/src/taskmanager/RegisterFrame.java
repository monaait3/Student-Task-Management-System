package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {
    private JTextField txtUsername, txtEmail, txtFullName;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnRegister, btnBack;
    private JLabel lblPasswordStrength;

    public RegisterFrame() {
        setTitle("Task Management System - Register");
        setSize(500, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        int row = 0;

        JLabel lblTitle = new JLabel("📝 Create New Account");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Username:*"), gbc);
        txtUsername = new JTextField(15);
        gbc.gridx = 1;
        add(txtUsername, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Email:*"), gbc);
        txtEmail = new JTextField(15);
        gbc.gridx = 1;
        add(txtEmail, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Full Name:*"), gbc);
        txtFullName = new JTextField(15);
        gbc.gridx = 1;
        add(txtFullName, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Password:*"), gbc);
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        add(txtPassword, gbc);

        lblPasswordStrength = new JLabel("Weak");
        lblPasswordStrength.setFont(new Font("Arial", Font.BOLD, 12));
        lblPasswordStrength.setForeground(Color.RED);
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        add(lblPasswordStrength, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Confirm Password:*"), gbc);
        txtConfirmPassword = new JPasswordField(15);
        gbc.gridx = 1;
        add(txtConfirmPassword, gbc);

        btnRegister = new JButton("✅ Register");
        btnRegister.setBackground(new Color(0, 153, 76));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));

        btnBack = new JButton("🔙 Back");
        btnBack.setBackground(new Color(204, 0, 0));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panelButtons = new JPanel(new FlowLayout());
        panelButtons.add(btnRegister);
        panelButtons.add(btnBack);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(panelButtons, gbc);

        btnRegister.addActionListener(e -> register());
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkPasswordStrength();
            }
        });

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPasswordStrength() {
        String password = new String(txtPassword.getPassword());
        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+].*")) strength++;

        if (strength <= 2) {
            lblPasswordStrength.setText("Weak");
            lblPasswordStrength.setForeground(Color.RED);
        } else if (strength == 3 || strength == 4) {
            lblPasswordStrength.setText("Medium");
            lblPasswordStrength.setForeground(Color.ORANGE);
        } else {
            lblPasswordStrength.setText("Strong");
            lblPasswordStrength.setForeground(Color.GREEN);
        }
    }

    private void register() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, email, full_name) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, fullName);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "Username or email already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}