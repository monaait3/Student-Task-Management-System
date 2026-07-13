package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddEditTaskFrame extends JFrame {
    private int userId;
    private DashboardFrame parent;
    private Integer taskId;

    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<String> cmbCategory;
    private JSpinner spinnerDate;
    private JComboBox<String> cmbPriority;
    private JComboBox<String> cmbStatus;

    public AddEditTaskFrame(int userId, DashboardFrame parent, Integer taskId) {
        this.userId = userId;
        this.parent = parent;
        this.taskId = taskId;

        setTitle(taskId == null ? "Add New Task" : "Edit Task");
        setSize(550, 500);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel(taskId == null ? "➕ Add New Task" : "✏️ Edit Task");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Task Title:*"), gbc);
        txtTitle = new JTextField(20);
        gbc.gridx = 1;
        add(txtTitle, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Description:"), gbc);
        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        gbc.gridx = 1;
        add(descScroll, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Category:"), gbc);
        cmbCategory = new JComboBox<>(new String[]{"General", "Homework", "Exam", "Project", "Lecture"});
        gbc.gridx = 1;
        add(cmbCategory, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Due Date (yyyy-MM-dd):"), gbc);
        SpinnerDateModel model = new SpinnerDateModel();
        spinnerDate = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerDate, "yyyy-MM-dd");
        spinnerDate.setEditor(editor);
        spinnerDate.setValue(new java.util.Date());
        gbc.gridx = 1;
        add(spinnerDate, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Priority:"), gbc);
        cmbPriority = new JComboBox<>(new String[]{"low", "medium", "high"});
        gbc.gridx = 1;
        add(cmbPriority, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        add(new JLabel("Status:"), gbc);
        cmbStatus = new JComboBox<>(new String[]{"pending", "completed"});
        gbc.gridx = 1;
        add(cmbStatus, gbc);

        JButton btnSave = new JButton("💾 Save");
        JButton btnClear = new JButton("🧹 Clear");
        JButton btnCancel = new JButton("❌ Cancel");

        JPanel panelButtons = new JPanel(new FlowLayout());
        panelButtons.add(btnSave);
        panelButtons.add(btnClear);
        panelButtons.add(btnCancel);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(panelButtons, gbc);

        btnSave.addActionListener(e -> saveTask());
        btnClear.addActionListener(e -> clearFields());
        btnCancel.addActionListener(e -> dispose());

        if (taskId != null) {
            loadTaskData();
        }
    }

    private void loadTaskData() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT title, description, task_category, due_date, priority_level, task_status FROM tasks WHERE task_id = ? AND user_id = ?")) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                txtTitle.setText(rs.getString("title"));
                txtDescription.setText(rs.getString("description"));
                cmbCategory.setSelectedItem(rs.getString("task_category"));

                Timestamp ts = rs.getTimestamp("due_date");
                if (ts != null) {
                    spinnerDate.setValue(java.util.Date.from(ts.toInstant()));
                }

                cmbPriority.setSelectedItem(rs.getString("priority_level"));
                cmbStatus.setSelectedItem(rs.getString("task_status"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading task: " + ex.getMessage());
        }
    }

    private void saveTask() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter task title!");
            return;
        }

        String description = txtDescription.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();
        String priority = (String) cmbPriority.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();

        Timestamp dueDate = null;
        try {
            java.util.Date selectedDate = (java.util.Date) spinnerDate.getValue();
            LocalDate localDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDateTime dateTime = localDate.atStartOfDay();
            dueDate = Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please select a valid date!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (taskId == null) {
                String query = "INSERT INTO tasks (user_id, title, description, task_category, due_date, priority_level, task_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, title);
                    stmt.setString(3, description);
                    stmt.setString(4, category);
                    stmt.setTimestamp(5, dueDate);
                    stmt.setString(6, priority);
                    stmt.setString(7, status);
                    stmt.executeUpdate();
                }
            } else {
                String query = "UPDATE tasks SET title = ?, description = ?, task_category = ?, due_date = ?, priority_level = ?, task_status = ? WHERE task_id = ? AND user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setString(3, category);
                    stmt.setTimestamp(4, dueDate);
                    stmt.setString(5, priority);
                    stmt.setString(6, status);
                    stmt.setInt(7, taskId);
                    stmt.setInt(8, userId);
                    stmt.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, taskId == null ? "Task added successfully!" : "Task updated successfully!");
            if (parent != null) parent.loadTasks();
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving task: " + ex.getMessage());
        }
    }

    private void clearFields() {
        txtTitle.setText("");
        txtDescription.setText("");
        cmbCategory.setSelectedIndex(0);
        spinnerDate.setValue(new java.util.Date());
        cmbPriority.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
    }
}