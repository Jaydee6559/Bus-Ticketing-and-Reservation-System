package view;

import dao.ScheduleDAO;
import dao.BusDAO;
import dao.DatabaseConnection;
import dao.StationDAO;
import java.sql.Connection;
import model.Schedule;
import model.Bus;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

/**
 *
 * @author jayde
 */
public class SchedulesPanel extends javax.swing.JPanel {

    private ScheduleDAO scheduleDAO;
    private BusDAO busDAO;
    private StationDAO stationDAO;
    

    public SchedulesPanel() {
        initComponents();
        scheduleDAO = new ScheduleDAO();
        busDAO = new BusDAO();
        stationDAO = new StationDAO();
        loadSchedulesToTable();
    }

    private void loadSchedulesToTable() {
        try {
            List<Schedule> schedules = scheduleDAO.getAllSchedules();
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0); // Clear existing rows

            for (Schedule schedule : schedules) {
                // Combine date and time for display
                String departureDateTime = formatDepartureDateTime(schedule.getTravelDate(), schedule.getDepartureTime());

                model.addRow(new Object[]{
                    schedule.getScheduleId(),
                    schedule.getPlateNumber(),
                    schedule.getPickupPoint(),
                    schedule.getArrivalPoint(),
                    departureDateTime, // Combined date and time
                    String.format("₱%.2f", schedule.getFare()),
                    schedule.getAvailableSeats(),
                    schedule.getStatus()
                });
            }

            System.out.println("Loaded " + schedules.size() + " schedules to admin table");

        } catch (Exception e) {
            System.err.println("Error loading schedules to table: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private String formatDepartureDateTime(java.sql.Date travelDate, java.sql.Time departureTime) {
        if (travelDate == null || departureTime == null) {
            return "N/A";
        }

        // Convert sql.Date and sql.Time to java.util.Date for formatting
        java.util.Date date = new java.util.Date(travelDate.getTime());
        java.util.Date time = new java.util.Date(departureTime.getTime());

        // Create a formatter for the date part
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy");

        // Create a formatter for the time part (12-hour format with AM/PM)
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a");

        return dateFormat.format(date) + " at " + timeFormat.format(time);
    }

        private String getScheduleStatusFromDatabase(int scheduleId) {
        String sql = "SELECT status FROM schedules WHERE schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            System.err.println("Error getting status for schedule " + scheduleId + ": " + e.getMessage());
        }

        return "ongoing"; // Default fallback
    }


    private void changeScheduleStatus() {
        try {
            int selectedRow = jTable1.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a schedule from the table first!", 
                    "Selection Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int scheduleId = Integer.parseInt(jTable1.getValueAt(selectedRow, 0).toString());
            String currentStatus = jTable1.getValueAt(selectedRow, 7).toString();
            String busPlate = jTable1.getValueAt(selectedRow, 1).toString();

            // Status flow: ongoing -> completed
            String newStatus;
            if (currentStatus.equals("ongoing")) {
                newStatus = "completed";
            } else {
                newStatus = "ongoing"; // Allow reverting if needed
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Change schedule #" + scheduleId + " (" + busPlate + ") status?\n\n" +
                "From: " + currentStatus + "\n" +
                "To: " + newStatus + "\n\n" +
                "Note: Completed schedules will be hidden from users.", 
                "Confirm Status Change", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = scheduleDAO.updateScheduleStatus(scheduleId, newStatus);

                if (success) {
                    jTable1.setValueAt(newStatus, selectedRow, 7);
                    JOptionPane.showMessageDialog(this, 
                        "Schedule status updated successfully!\n\n" +
                        "Schedule #" + scheduleId + " is now: " + newStatus + "\n" +
                        (newStatus.equals("completed") ? 
                         "This schedule will no longer be visible to users." : 
                         "This schedule is now visible to users."), 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    loadSchedulesToTable(); // Refresh to see changes
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update schedule status in database!", 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating status: " + e.getMessage(), 
                "System Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void showAddScheduleDialog() {
        try {
            // Get available buses from database
            List<Bus> availableBuses = busDAO.getAllBuses().stream()
                    .filter(bus -> "available".equalsIgnoreCase(bus.getStatus()))
                    .toList();
            
            if (availableBuses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available buses found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get stations from database
            List<String> stationList = stationDAO.getAllStations();
            if (stationList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No stations found in database!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create bus selection dropdown
            String[] busOptions = availableBuses.stream()
                    .map(bus -> bus.getPlateNumber() + " - " + bus.getBusType() + " (ID: " + bus.getBusId() + ")")
                    .toArray(String[]::new);
            
            JComboBox<String> busComboBox = new JComboBox<>(busOptions);
            
            // Station dropdowns from database
            String[] stations = stationList.toArray(new String[0]);
            JComboBox<String> pickupComboBox = new JComboBox<>(stations);
            JComboBox<String> dropoffComboBox = new JComboBox<>(stations);
            
            // Input fields with placeholders
            JTextField dateField = new JTextField();
            dateField.setText("2024-01-15"); // Example format
            JTextField timeField = new JTextField();
            timeField.setText("08:00:00"); // Example format
            JTextField fareField = new JTextField();
            fareField.setText("100.00");
            
            Object[] message = {
                "Select Bus:", busComboBox,
                "Pickup Station:", pickupComboBox,
                "Arrival Station:", dropoffComboBox,
                "Departure Date (YYYY-MM-DD):", dateField,
                "Departure Time (HH:MM:SS):", timeField,
                "Fare per Seat:", fareField
            };
            
            int option = JOptionPane.showConfirmDialog(this, message, "Add New Schedule", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
                // Validate inputs
                String pickupStation = (String) pickupComboBox.getSelectedItem();
                String dropoffStation = (String) dropoffComboBox.getSelectedItem();
                
                if (pickupStation.equals(dropoffStation)) {
                    JOptionPane.showMessageDialog(this, "Pickup and arrival stations cannot be the same!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Get selected bus
                int selectedBusIndex = busComboBox.getSelectedIndex();
                Bus selectedBus = availableBuses.get(selectedBusIndex);
                
                // Parse inputs
                Date travelDate = Date.valueOf(dateField.getText().trim());
                Time departureTime = Time.valueOf(timeField.getText().trim());
                double fare = Double.parseDouble(fareField.getText().trim());
                
                // Get station IDs
                int pickupStationId = stationDAO.getStationIdByName(pickupStation);
                int dropoffStationId = stationDAO.getStationIdByName(dropoffStation);
                
                if (pickupStationId == -1 || dropoffStationId == -1) {
                    JOptionPane.showMessageDialog(this, "Invalid station selected!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Create schedule in database
                boolean success = scheduleDAO.addSchedule(selectedBus.getBusId(), 
                        pickupStationId, dropoffStationId, travelDate, departureTime, fare);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Schedule added successfully!");
                    loadSchedulesToTable(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add schedule to database!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                    "Invalid input format!\nDate format: YYYY-MM-DD\nTime format: HH:MM:SS\nFare: number only", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "System Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(1275, 800));
        setLayout(null);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Schedule Id", "Bus Plate", "Pickup Station", "Arrival Station", "Departure Time", "Fare Per Seat", "Available Seats", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1);
        jScrollPane1.setBounds(50, 220, 800, 370);

        jButton1.setBackground(new java.awt.Color(36, 106, 112));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Add Schedule");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1);
        jButton1.setBounds(290, 610, 126, 48);

        jButton2.setBackground(new java.awt.Color(36, 106, 112));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Change Schedule Status");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        add(jButton2);
        jButton2.setBounds(470, 610, 192, 48);

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator1);
        jSeparator1.setBounds(0, 134, 1269, 22);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 29)); // NOI18N
        jLabel11.setText("Schedules");
        add(jLabel11);
        jLabel11.setBounds(66, 44, 136, 40);

        jLabel1.setBackground(new java.awt.Color(36, 106, 112));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Schedules");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.setOpaque(true);
        add(jLabel1);
        jLabel1.setBounds(50, 180, 800, 43);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showAddScheduleDialog();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        changeScheduleStatus();
    }//GEN-LAST:event_jButton2ActionPerformed
 
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
