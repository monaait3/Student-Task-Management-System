package taskmanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboardFrame extends JFrame {
    private JTable userTable, taskTable;
    private DefaultTableModel userTableModel, taskTableModel;

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("👑 Admin Dashboard");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        tablesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] userColumns = {"User ID", "Username", "Email", "Full Name", "Role", "Created At"};
        userTableModel = new DefaultTableModel(userColumns, 0);
        userTable = new JTable(userTableModel);
        userTable.setRowHeight(25);
        JScrollPane userScroll = new JScrollPane(userTable);
        userScroll.setBorder(BorderFactory.createTitledBorder("👤 Users"));
        tablesPanel.add(userScroll);

        String[] taskColumns = {"Task ID", "User ID", "Title", "Category", "Due Date", "Priority", "Status"};
        taskTableModel = new DefaultTableModel(taskColumns, 0);
        taskTable = new JTable(taskTableModel);
        taskTable.setRowHeight(25);
        JScrollPane taskScroll = new JScrollPane(taskTable);
        taskScroll.setBorder(BorderFactory.createTitledBorder("📋 All Tasks"));
        tablesPanel.add(taskScroll);

        add(tablesPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnRefresh = new JButton("🔄 Refresh");
        JButton btnDeleteUser = new JButton("🗑️ Delete User");
        JButton btnDeleteTask = new JButton("🗑️ Delete Task");

        btnRefresh.addActionListener(e -> loadData());
        btnDeleteUser.addActionListener(e -> deleteUser());
        btnDeleteTask.addActionListener(e -> deleteTask());

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnDeleteUser);
        bottomPanel.add(btnDeleteTask);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        loadUsers();
        loadTasks();
    }

    private void loadUsers() {
        userTableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, username, email, full_name, role, created_at FROM users")) {

            while (rs.next()) {
                userTableModel.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage());
        }
    }

    private void loadTasks() {
        taskTableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT task_id, user_id, title, task_category, due_date, priority_level, task_status FROM tasks")) {

            while (rs.next()) {
                taskTableModel.addRow(new Object[]{
                        rs.getInt("task_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("task_category"),
                        rs.getTimestamp("due_date"),
                        rs.getString("priority_level"),
                        rs.getString("task_status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + ex.getMessage());
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete!");
            return;
        }
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user and all their tasks?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User deleted!");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete!");
            return;
        }
        int taskId = (int) taskTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE task_id = ?")) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task deleted!");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}