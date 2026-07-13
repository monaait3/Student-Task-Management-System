package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ForgotPasswordFrame extends JFrame {
    private JTextField txtUsername, txtEmail;
    private JButton btnReset, btnBack;

    public ForgotPasswordFrame() {
        setTitle("Forgot Password");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        int row = 0;

        JLabel lblTitle = new JLabel("🔑 Forgot Password?");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        txtUsername = new JTextField(15);
        gbc.gridx = 1;
        add(txtUsername, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(15);
        gbc.gridx = 1;
        add(txtEmail, gbc);

        btnReset = new JButton("✅ Reset Password");
        btnReset.setBackground(new Color(0, 153, 76));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(new Font("Arial", Font.BOLD, 14));

        btnBack = new JButton("🔙 Back");
        btnBack.setBackground(new Color(204, 0, 0));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panelButtons = new JPanel(new FlowLayout());
        panelButtons.add(btnReset);
        panelButtons.add(btnBack);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(panelButtons, gbc);

        btnReset.addActionListener(e -> resetPassword());
        btnBack.addActionListener(e -> dispose());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetPassword() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT password FROM users WHERE username = ? AND email = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String password = rs.getString("password");
                JOptionPane.showMessageDialog(this,
                        "Your password is: " + password + "\nPlease change it after login.",
                        "Password Found",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Username or email not found!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}