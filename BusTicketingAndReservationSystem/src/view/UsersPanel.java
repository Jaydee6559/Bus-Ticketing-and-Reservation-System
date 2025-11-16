package view;

import model.User;
import dao.UserDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 *
 * @author jayde
 */
public class UsersPanel extends javax.swing.JPanel {
    private UserDAO userDAO;
    private DefaultTableModel tableModel;

    /**
     * Creates new form Users
     */
    public UsersPanel() {
        initComponents();
        userDAO = new UserDAO();
        initializeTable();
        loadUsersData();
    }

    private void initializeTable() {
        tableModel = (DefaultTableModel) jTable1.getModel();
        // Make table non-editable and non-selectable
        jTable1.setDefaultEditor(Object.class, null);
        jTable1.setRowSelectionAllowed(false);
    }

    private void loadUsersData() {
        try {
            List<User> users = userDAO.getAllUsersForAdmin();
            tableModel.setRowCount(0); // Clear existing data
            
            for (User user : users) {
                Object[] rowData = {
                    user.getUserId(),
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getPhone(),
                    user.getUserStatus()
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading users: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getUserIdFromInput() {
        String userIdStr = JOptionPane.showInputDialog(this,
            "Enter User ID:",
            "User ID Input",
            JOptionPane.QUESTION_MESSAGE);
        
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            return -1; 
        }
        
        try {
            return Integer.parseInt(userIdStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid numeric User ID",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(1275, 800));
        setLayout(null);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "User Id", "Email", "Full Name", "Number", "User Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1);
        jScrollPane1.setBounds(100, 240, 690, 200);

        jLabel1.setBackground(new java.awt.Color(36, 106, 112));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Users");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.setOpaque(true);
        add(jLabel1);
        jLabel1.setBounds(100, 200, 690, 43);

        jButton35.setBackground(new java.awt.Color(36, 106, 112));
        jButton35.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton35.setForeground(new java.awt.Color(255, 255, 255));
        jButton35.setText("Whitelist User");
        jButton35.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        jButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton35ActionPerformed(evt);
            }
        });
        add(jButton35);
        jButton35.setBounds(220, 480, 120, 40);

        jButton36.setBackground(new java.awt.Color(36, 106, 112));
        jButton36.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton36.setForeground(new java.awt.Color(255, 255, 255));
        jButton36.setText("Blacklist User");
        jButton36.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        jButton36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton36ActionPerformed(evt);
            }
        });
        add(jButton36);
        jButton36.setBounds(390, 480, 110, 40);

        jButton37.setBackground(new java.awt.Color(36, 106, 112));
        jButton37.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton37.setForeground(new java.awt.Color(255, 255, 255));
        jButton37.setText("Delete User");
        jButton37.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });
        add(jButton37);
        jButton37.setBounds(550, 480, 130, 40);

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator1);
        jSeparator1.setBounds(0, 134, 1269, 22);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 29)); // NOI18N
        jLabel11.setText("Users");
        add(jLabel11);
        jLabel11.setBounds(66, 44, 75, 40);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
        int userId = getUserIdFromInput();
        if (userId == -1) return;

        boolean success = userDAO.updateUserStatus(userId, "whitelisted");
        if (success) {
            JOptionPane.showMessageDialog(this,
                "User whitelisted successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadUsersData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to whitelist user - User ID may not exist",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton35ActionPerformed

    private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
          int userId = getUserIdFromInput();
        if (userId == -1) return;

        boolean success = userDAO.updateUserStatus(userId, "blacklisted");
        if (success) {
            JOptionPane.showMessageDialog(this,
                "User blacklisted successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadUsersData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to blacklist user - User ID may not exist",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton36ActionPerformed

    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
        int userId = getUserIdFromInput();
        if (userId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user with ID: " + userId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userDAO.deleteUser(userId);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "User deleted successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadUsersData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete user - User ID may not exist",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton37ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
