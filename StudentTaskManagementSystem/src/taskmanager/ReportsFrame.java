package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.io.*;

public class ReportsFrame extends JFrame {
    private int userId;
    private JLabel lblTotal, lblPending, lblCompleted, lblOverdue;
    private JTextArea txtReport;

    public ReportsFrame(int userId) {
        this.userId = userId;

        setTitle("Reports & Statistics");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("📊 Task Reports & Statistics");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblTotal = new JLabel("Total Tasks: 0");
        lblPending = new JLabel("Pending: 0");
        lblCompleted = new JLabel("Completed: 0");
        lblOverdue = new JLabel("Overdue: 0");

        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblPending.setFont(new Font("Arial", Font.BOLD, 14));
        lblCompleted.setFont(new Font("Arial", Font.BOLD, 14));
        lblOverdue.setFont(new Font("Arial", Font.BOLD, 14));

        statsPanel.add(lblTotal);
        statsPanel.add(lblPending);
        statsPanel.add(lblCompleted);
        statsPanel.add(lblOverdue);
        add(statsPanel, BorderLayout.NORTH);

        txtReport = new JTextArea();
        txtReport.setEditable(false);
        txtReport.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtReport);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Detailed Report"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnRefresh = new JButton("🔄 Refresh");
        JButton btnExport = new JButton("💾 Export Report");
        JButton btnClose = new JButton("❌ Close");

        btnRefresh.addActionListener(e -> loadReport());
        btnExport.addActionListener(e -> exportReport());
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);

        loadReport();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReport() {
        int total = 0, pending = 0, completed = 0, overdue = 0;
        StringBuilder report = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT task_id, title, task_category, due_date, priority_level, task_status, created_at FROM tasks WHERE user_id = ? ORDER BY due_date ASC")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            report.append("============ TASK REPORT ============\n\n");
            report.append("ID | Title | Category | Due Date | Priority | Status\n");
            report.append("--------------------------------------------------------\n");

            while (rs.next()) {
                total++;
                String status = rs.getString("task_status");
                if ("pending".equals(status)) {
                    pending++;
                } else if ("completed".equals(status)) {
                    completed++;
                }

                Timestamp dueDate = rs.getTimestamp("due_date");
                if (dueDate != null && dueDate.before(new java.util.Date()) && "pending".equals(status)) {
                    overdue++;
                }

                report.append(String.format("%-3d | %-15s | %-10s | %-12s | %-8s | %-9s\n",
                        rs.getInt("task_id"),
                        rs.getString("title").length() > 15 ? rs.getString("title").substring(0, 12) + "..." : rs.getString("title"),
                        rs.getString("task_category"),
                        dueDate != null ? dueDate.toString().substring(0, 10) : "N/A",
                        rs.getString("priority_level"),
                        status
                ));
            }

            report.append("\n========================================\n");
            report.append("📊 STATISTICS:\n");
            report.append("   Total Tasks: " + total + "\n");
            report.append("   Pending: " + pending + "\n");
            report.append("   Completed: " + completed + "\n");
            report.append("   Overdue: " + overdue + "\n");

            txtReport.setText(report.toString());

            lblTotal.setText("Total Tasks: " + total);
            lblPending.setText("Pending: " + pending);
            lblCompleted.setText("Completed: " + completed);
            lblOverdue.setText("Overdue: " + overdue);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading report: " + ex.getMessage());
        }
    }

    private void exportReport() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("task_report.txt"));
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.print(txtReport.getText());
                    JOptionPane.showMessageDialog(this, "Report exported successfully!\n" + file.getAbsolutePath());
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error exporting report: " + ex.getMessage());
        }
    }
}