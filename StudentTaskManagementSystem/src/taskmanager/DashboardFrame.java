package taskmanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class DashboardFrame extends JFrame {
    private int userId;
    private String fullName;
    private String role;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;
    private JLabel lblCounter;

    public DashboardFrame(int userId, String fullName, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.role = role;

        setTitle("Dashboard - Task Management System");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblWelcome = new JLabel("Welcome " + fullName + " (" + role + ")");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(lblWelcome, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(20);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                applyFilter();
            }
        });
        searchPanel.add(txtSearch);

        searchPanel.add(new JLabel("Filter:"));
        cmbFilter = new JComboBox<>(new String[]{"All", "Pending", "Completed"});
        cmbFilter.addActionListener(e -> applyFilter());
        searchPanel.add(cmbFilter);

        lblCounter = new JLabel("Total: 0 | Pending: 0 | Completed: 0");
        lblCounter.setFont(new Font("Arial", Font.BOLD, 14));
        searchPanel.add(lblCounter);
        add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Title", "Category", "Due Date", "Priority", "Status", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(25);
        rowSorter = new TableRowSorter<>(tableModel);
        taskTable.setRowSorter(rowSorter);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnAdd = new JButton("➕ Add Task");
        JButton btnEdit = new JButton("✏️ Edit");
        JButton btnComplete = new JButton("✅ Complete");
        JButton btnDelete = new JButton("🗑️ Delete");
        JButton btnRefresh = new JButton("🔄 Refresh");
        JButton btnReports = new JButton("📊 Reports");

        btnAdd.addActionListener(e -> new AddEditTaskFrame(userId, this, null).setVisible(true));
        btnEdit.addActionListener(e -> editTask());
        btnComplete.addActionListener(e -> completeTask());
        btnDelete.addActionListener(e -> deleteTask());
        btnRefresh.addActionListener(e -> loadTasks());
        btnReports.addActionListener(e -> new ReportsFrame(userId).setVisible(true));

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnComplete);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnReports);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTasks();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadTasks() {
        tableModel.setRowCount(0);
        String query = "SELECT task_id, title, task_category, due_date, priority_level, task_status, created_at FROM tasks WHERE user_id = ? ORDER BY due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String dueDateStr = "";
                Timestamp tsDue = rs.getTimestamp("due_date");
                if (tsDue != null) {
                    dueDateStr = tsDue.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                String createdAtStr = "";
                Timestamp tsCreated = rs.getTimestamp("created_at");
                if (tsCreated != null) {
                    createdAtStr = tsCreated.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
                tableModel.addRow(new Object[]{
                    rs.getInt("task_id"),
                    rs.getString("title"),
                    rs.getString("task_category"),
                    dueDateStr,
                    rs.getString("priority_level"),
                    rs.getString("task_status"),
                    createdAtStr
                });
            }
            updateCounter();
            applyFilter();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        String filterStatus = (String) cmbFilter.getSelectedItem();
        RowFilter<DefaultTableModel, Object> rowFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String title = entry.getStringValue(1).toLowerCase();
                String status = entry.getStringValue(5);
                boolean matchesSearch = title.contains(searchText);
                boolean matchesStatus = true;
                if ("Pending".equals(filterStatus)) {
                    matchesStatus = "pending".equals(status);
                } else if ("Completed".equals(filterStatus)) {
                    matchesStatus = "completed".equals(status);
                }
                return matchesSearch && matchesStatus;
            }
        };
        rowSorter.setRowFilter(rowFilter);
        updateCounter();
    }

    private void updateCounter() {
        int total = tableModel.getRowCount();
        int pending = 0, completed = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String status = (String) tableModel.getValueAt(i, 5);
            if ("pending".equals(status)) pending++;
            else if ("completed".equals(status)) completed++;
        }
        lblCounter.setText("Total: " + total + " | Pending: " + pending + " | Completed: " + completed);
    }

    private void editTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to edit!");
            return;
        }
        int modelRow = taskTable.convertRowIndexToModel(selectedRow);
        int taskId = (int) tableModel.getValueAt(modelRow, 0);
        new AddEditTaskFrame(userId, this, taskId).setVisible(true);
    }

    private void completeTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to complete!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Mark this task as completed?", "Complete Task", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int modelRow = taskTable.convertRowIndexToModel(selectedRow);
        int taskId = (int) tableModel.getValueAt(modelRow, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tasks SET task_status = 'completed' WHERE task_id = ? AND user_id = ?")) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Task marked as completed!");
                loadTasks();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error completing task: " + ex.getMessage());
        }
    }

    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int modelRow = taskTable.convertRowIndexToModel(selectedRow);
        int taskId = (int) tableModel.getValueAt(modelRow, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE task_id = ? AND user_id = ?")) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Task deleted successfully!");
                loadTasks();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting task: " + ex.getMessage());
        }
    }
}