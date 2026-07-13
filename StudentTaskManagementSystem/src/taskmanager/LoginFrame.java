package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.prefs.Preferences;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister;
    private JCheckBox chkRememberMe;
    private JLabel lblForgotPassword;

    private static final Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);

    public LoginFrame() {
        setTitle("Task Management System - Login");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblTitle = new JLabel("📚 Student Task Management");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        add(txtUsername, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        add(txtPassword, gbc);

        gbc.gridy = 3;
        gbc.gridx = 1;
        chkRememberMe = new JCheckBox("Remember Me");
        chkRememberMe.setFont(new Font("Arial", Font.PLAIN, 12));
        add(chkRememberMe, gbc);

        gbc.gridy = 4;
        gbc.gridx = 1;
        lblForgotPassword = new JLabel("<HTML><U>Forgot Password?</U></HTML>");
        lblForgotPassword.setForeground(Color.BLUE);
        lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(lblForgotPassword, gbc);

        btnLogin = new JButton("🔑 Login");
        btnLogin.setBackground(new Color(0, 153, 76));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));

        btnRegister = new JButton("📝 Register");
        btnRegister.setBackground(new Color(0, 102, 204));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panelButtons = new JPanel(new FlowLayout());
        panelButtons.add(btnLogin);
        panelButtons.add(btnRegister);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(panelButtons, gbc);

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        lblForgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new ForgotPasswordFrame().setVisible(true);
            }
        });

        getRootPane().setDefaultButton(btnLogin);
        loadRememberMe();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id, full_name, role FROM users WHERE username = ? AND password = ?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");

                if (chkRememberMe.isSelected()) {
                    prefs.put("username", username);
                    prefs.put("password", password);
                } else {
                    prefs.remove("username");
                    prefs.remove("password");
                }

                JOptionPane.showMessageDialog(this, "Welcome " + fullName + "!", "Success", JOptionPane.INFORMATION_MESSAGE);

                if (role.equals("admin")) {
                    new AdminDashboardFrame().setVisible(true);
                } else {
                    new DashboardFrame(userId, fullName, role).setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRememberMe() {
        String savedUsername = prefs.get("username", "");
        String savedPassword = prefs.get("password", "");
        if (!savedUsername.isEmpty()) {
            txtUsername.setText(savedUsername);
            txtPassword.setText(savedPassword);
            chkRememberMe.setSelected(true);
        }
    }

    public static void main(String[] args) {
    System.out.println("بدء التطبيق...");
    SwingUtilities.invokeLater(() -> {
        System.out.println("جاري إنشاء LoginFrame...");
        new LoginFrame().setVisible(true);
        System.out.println("تم إنشاء LoginFrame");
    });
}
}