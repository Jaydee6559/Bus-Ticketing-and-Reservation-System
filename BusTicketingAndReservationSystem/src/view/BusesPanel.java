package view;

import model.Bus;
import dao.BusDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class BusesPanel extends javax.swing.JPanel {
    private BusDAO busDAO;
    private DefaultTableModel tableModel;

    public BusesPanel() {
        initComponents();
        busDAO = new BusDAO();
        initializeTable();
        loadBusesData();
    }

    private void initializeTable() {
        tableModel = (DefaultTableModel) jTable1.getModel();
        jTable1.setDefaultEditor(Object.class, null);
        jTable1.setRowSelectionAllowed(false);
    }

    private void loadBusesData() {
        try {
            List<Bus> buses = busDAO.getAllBuses();
            tableModel.setRowCount(0); // Clear existing data
            
            for (Bus bus : buses) {
                Object[] rowData = {
                    bus.getBusId(),
                    bus.getPlateNumber(),
                    bus.getBusType(),
                    bus.getCapacity(),
                    bus.getStatus()
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading buses: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getBusIdFromInput() {
        String busIdStr = JOptionPane.showInputDialog(this,
            "Enter Bus ID:",
            "Bus ID Input",
            JOptionPane.QUESTION_MESSAGE);
        
        if (busIdStr == null || busIdStr.trim().isEmpty()) {
            return -1;
        }
        
        try {
            return Integer.parseInt(busIdStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid numeric Bus ID",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    // Add Bus Button Action
    private void addBusButtonActionPerformed() {
        // Plate Number Input
        String plateNumber = JOptionPane.showInputDialog(this,
            "Enter Plate Number:",
            "Add New Bus",
            JOptionPane.QUESTION_MESSAGE);
        
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return;
        }
        plateNumber = plateNumber.trim().toUpperCase();
        
        // Check if plate number already exists
        if (busDAO.plateNumberExists(plateNumber)) {
            JOptionPane.showMessageDialog(this,
                "Plate number already exists!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Bus Type Selection
        String[] busTypes = {"Classic", "Premium"};
        String busType = (String) JOptionPane.showInputDialog(this,
            "Select Bus Type:",
            "Bus Type Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            busTypes,
            busTypes[0]);
        
        if (busType == null) {
            return;
        }
        
        // Confirm addition
        int confirm = JOptionPane.showConfirmDialog(this,
            "Add new bus?\nPlate: " + plateNumber + "\nType: " + busType + "\nCapacity: 31",
            "Confirm Add Bus",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = busDAO.addBus(plateNumber, busType);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Bus added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadBusesData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to add bus",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Change Status Button Action
    private void changeStatusButtonActionPerformed() {
        int busId = getBusIdFromInput();
        if (busId == -1) return;

        // Status Selection
        String[] statusOptions = {"available", "unavailable"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
            "Select new status for Bus ID " + busId + ":",
            "Change Bus Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statusOptions,
            statusOptions[0]);
        
        if (newStatus == null) {
            return;
        }
        
        boolean success = busDAO.updateBusStatus(busId, newStatus);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Bus status updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadBusesData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update bus status - Bus ID may not exist",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Delete Bus Button Action
    private void deleteBusButtonActionPerformed() {
        int busId = getBusIdFromInput();
        if (busId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete bus with ID: " + busId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = busDAO.deleteBus(busId);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Bus deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadBusesData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete bus - Bus ID may not exist",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

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
                "Bus Id", "Plate Number ", "Bus Type", "Capacity", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1);
        jScrollPane1.setBounds(50, 220, 820, 326);

        jButton35.setBackground(new java.awt.Color(36, 106, 112));
        jButton35.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton35.setForeground(new java.awt.Color(255, 255, 255));
        jButton35.setText("Add Bus");
        jButton35.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        jButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton35ActionPerformed(evt);
            }
        });
        add(jButton35);
        jButton35.setBounds(210, 570, 100, 39);

        jButton36.setBackground(new java.awt.Color(36, 106, 112));
        jButton36.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton36.setForeground(new java.awt.Color(255, 255, 255));
        jButton36.setText("Change Bus Status");
        jButton36.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        jButton36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton36ActionPerformed(evt);
            }
        });
        add(jButton36);
        jButton36.setBounds(370, 570, 150, 39);

        jButton37.setBackground(new java.awt.Color(36, 106, 112));
        jButton37.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton37.setForeground(new java.awt.Color(255, 255, 255));
        jButton37.setText("Delete Bus");
        jButton37.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });
        add(jButton37);
        jButton37.setBounds(570, 570, 150, 39);

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator1);
        jSeparator1.setBounds(0, 134, 1269, 22);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 29)); // NOI18N
        jLabel11.setText("Buses");
        add(jLabel11);
        jLabel11.setBounds(66, 44, 79, 40);

        jLabel1.setBackground(new java.awt.Color(36, 106, 112));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Buses");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.setOpaque(true);
        add(jLabel1);
        jLabel1.setBounds(50, 180, 820, 43);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
        addBusButtonActionPerformed();
    }//GEN-LAST:event_jButton35ActionPerformed

    private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
       changeStatusButtonActionPerformed();
    }//GEN-LAST:event_jButton36ActionPerformed

    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
        deleteBusButtonActionPerformed();
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
