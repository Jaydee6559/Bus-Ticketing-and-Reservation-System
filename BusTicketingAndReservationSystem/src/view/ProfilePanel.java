package view;

import dao.PaymentDAO;
import dao.UserDAO;
import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfilePanel extends javax.swing.JPanel {

    private PaymentDAO paymentDAO;
    private UserDAO userDAO;
    private DefaultTableModel paymentTableModel;
    private DefaultTableModel receiptTableModel;
    private DefaultTableModel penaltyTableModel;
    private int currentUserId;
    private User currentUser;

    public ProfilePanel() {
        initComponents();
        initializePanel();
    }

    private void initializePanel() {
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        this.currentUserId = getCurrentUserId();
        this.currentUser = userDAO.getUserById(currentUserId);
        
        System.out.println(" ProfilePanel initialized for user: " + currentUserId);
        
        paymentDAO.autoCancelPendingPayments();
        
        setupPaymentTable();
        setupReceiptTable();
        setupPenaltyTable();
        
        loadUserData();
        loadPaymentData();
        loadReceiptData();
        loadPenaltyData();
        
        setupTableListeners();
    }
    
    
    private void updateFirstName() {
        try {
            String firstName = jTextField2.getText().trim();

            if (firstName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userDAO.updateUserFirstName(currentUserId, firstName);
            if (success) {
                currentUser.setFirstName(firstName);
                JOptionPane.showMessageDialog(this, "First name updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update first name", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateLastName() {
        try {
            String lastName = jTextField1.getText().trim();

            if (lastName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userDAO.updateUserLastName(currentUserId, lastName);
            if (success) {
                currentUser.setLastName(lastName);
                JOptionPane.showMessageDialog(this, "Last name updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update last name", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmail() {
        try {
            String email = jTextField3.getText().trim();

            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userDAO.updateUserEmail(currentUserId, email);
            if (success) {
                currentUser.setEmail(email);
                JOptionPane.showMessageDialog(this, "Email updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update email", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePhone() {
        try {
            String phone = jTextField4.getText().trim();

            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Phone cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userDAO.updateUserPhone(currentUserId, phone);
            if (success) {
                currentUser.setPhone(phone);
                JOptionPane.showMessageDialog(this, "Phone updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update phone", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeUserPassword() {
        try {
            // Show input dialog for new password
            JPasswordField passwordField = new JPasswordField();
            JPasswordField confirmPasswordField = new JPasswordField();

            Object[] message = {
                "New Password:", passwordField,
                "Confirm New Password:", confirmPasswordField
            };

            int option = JOptionPane.showConfirmDialog(this,
                message,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                String newPassword = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                // Validation
                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Please fill in both password fields.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this,
                        "Passwords do not match. Please try again.",
                        "Password Mismatch",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update password in database
                boolean success = userDAO.updateUserPassword(currentUserId, newPassword);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Password changed successfully!",
                        "Password Updated",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Update the display
                    jPasswordField1.setText("********");

                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to change password. Please try again.",
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println(" Error changing password: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error changing password: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    

    

    private void loadUserData() {
        if (currentUser != null) {
            jLabel1.setText("Hi " + currentUser.getFirstName() + "!");
            
            jTextField1.setText(currentUser.getLastName());
            jTextField2.setText(currentUser.getFirstName());
            jTextField3.setText(currentUser.getEmail());
            jTextField4.setText(currentUser.getPhone());
            jPasswordField1.setText("********");
            
            jLabel12.setText("Status: " + currentUser.getUserStatus());
            if ("blacklisted".equals(currentUser.getUserStatus())) {
                jLabel12.setForeground(Color.RED);
            } else {
                jLabel12.setForeground(new Color(0, 128, 0));
            }
        }
    }

    private void setupPaymentTable() {
        paymentTableModel = (DefaultTableModel) paymentTable.getModel();
        paymentTableModel.setColumnIdentifiers(new Object[]{
            "Bus Plate", "Pickup Point", "Arrival Point", "Bus Type", 
            "Departure Date", "Seats", "Total", "Pay Now", "Cancel", "BookingID"
        });
        hidePaymentTableIdColumn();
    }

    private void setupReceiptTable() {
        receiptTableModel = (DefaultTableModel) receiptTable.getModel();
        receiptTableModel.setColumnIdentifiers(new Object[]{
            "Bus Plate", "Departure Date", "Pickup Point", "Arrival Point", 
            "Status", "Receipt", "Cancel", "BookingID"
        });
        hideReceiptTableIdColumn();
    }

    private void setupPenaltyTable() {
        penaltyTableModel = (DefaultTableModel) penaltyTable.getModel();
        penaltyTableModel.setColumnIdentifiers(new Object[]{
            "Departure Time", "Reason", "Pay Penalty", "PaymentID"
        });
        hidePenaltyTableIdColumn();
    }

    private void loadPaymentData() {
        paymentTableModel.setRowCount(0);
        java.util.List<Object[]> payments = paymentDAO.getPendingPayments(currentUserId);
        
        System.out.println(" Loading " + payments.size() + " pending payments into table");
        
        for (Object[] payment : payments) {
            paymentTableModel.addRow(payment);
        }
        hidePaymentTableIdColumn();
    }

    private void loadReceiptData() {
    receiptTableModel.setRowCount(0);
    java.util.List<Object[]> receipts = paymentDAO.getPaymentHistory(currentUserId);
    
    System.out.println("   Loading " + receipts.size() + " receipts into table");
    
    for (Object[] receipt : receipts) {
        
        if (receipt.length < 7) {
            System.err.println(" Invalid receipt data length: " + receipt.length);
            continue;
        }
        
        int bookingId = (int) receipt[6]; // BookingID is at index 6 in DAO data
        boolean canCancel = paymentDAO.canCancelCompletedPayment(bookingId);
        
        // Create a new array with 8 elements for our table
        Object[] newReceipt = {
            receipt[0], // Bus Plate
            receipt[1], // Departure Date
            receipt[2], // Pickup Point
            receipt[3], // Arrival Point
            receipt[4], // Status
            "View Receipt", // Receipt button
            canCancel ? "Cancel" : "Cannot Cancel", // Cancel button
            bookingId  // BookingID (hidden)
        };
        
        receiptTableModel.addRow(newReceipt);
    }
    hideReceiptTableIdColumn();
}

    private void loadPenaltyData() {
        penaltyTableModel.setRowCount(0);
        java.util.List<Object[]> penalties = paymentDAO.getPendingPenalties(currentUserId);
        
        System.out.println(" Loading " + penalties.size() + " penalties into table");
        
        for (Object[] penalty : penalties) {
            // Create simplified penalty data with only 3 columns
            Object[] simplifiedPenalty = {
                penalty[0], // Departure Time
                penalty[1], // Reason
                "Pay Penalty", // Pay Penalty button
                penalty[3]  // PaymentID (hidden)
            };
            penaltyTableModel.addRow(simplifiedPenalty);
        }
        hidePenaltyTableIdColumn();
    }

    private void setupTableListeners() {
        paymentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = paymentTable.rowAtPoint(evt.getPoint());
                int col = paymentTable.columnAtPoint(evt.getPoint());
                
                System.out.println(" Payment table clicked - Row: " + row + ", Col: " + col);
                
                if (row >= 0) {
                    int modelRow = paymentTable.convertRowIndexToModel(row);
                    
                    if (col == 7) {
                        handlePayNow(modelRow);
                    } else if (col == 8) {
                        handleCancelBooking(modelRow);
                    }
                }
            }
        });

        receiptTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = receiptTable.rowAtPoint(evt.getPoint());
                int col = receiptTable.columnAtPoint(evt.getPoint());
                
                System.out.println(" Receipt table clicked - Row: " + row + ", Col: " + col);
                
                if (row >= 0) {
                    int modelRow = receiptTable.convertRowIndexToModel(row);
                    
                    if (col == 5) {
                        handleViewReceipt(modelRow);
                    } else if (col == 6) {
                        handleCancelCompletedBooking(modelRow);
                    }
                }
            }
        });

        penaltyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = penaltyTable.rowAtPoint(evt.getPoint());
                int col = penaltyTable.columnAtPoint(evt.getPoint());
                
                System.out.println(" Penalty table clicked - Row: " + row + ", Col: " + col);
                
                if (row >= 0 && col == 2) { // Pay Penalty column is now index 2
                    int modelRow = penaltyTable.convertRowIndexToModel(row);
                    handlePayPenalty(modelRow);
                }
            }
        });
    }

    private void handlePayNow(int modelRow) {
        try {
            int bookingId = (int) paymentTableModel.getValueAt(modelRow, 9);
            
            System.out.println("  rocessing payment for booking: " + bookingId);
            
            if (paymentDAO.isWithinThreeDaysBeforeDeparture(bookingId)) {
                JOptionPane.showMessageDialog(this,
                    "  Cannot process payment!\n\n" +
                    "It's within 3 days of departure time.\n" +
                    "Your booking will be automatically cancelled and you will receive a penalty.\n" +
                    "Please check your penalty payments section.",
                    "Payment Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to process this payment?",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = paymentDAO.processPayment(bookingId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Payment processed successfully!",
                        "Payment Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadPaymentData();
                    loadReceiptData();
                    
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to process payment. Payment may have already been processed.",
                        "Payment Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println("  Error in handlePayNow: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error processing payment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelBooking(int modelRow) {
    try {
        int bookingId = (int) paymentTableModel.getValueAt(modelRow, 9);
        
        System.out.println("  Attempting to cancel booking: " + bookingId);
        
        // Debug current state
        paymentDAO.debugCancellationStatus(bookingId);
        
        // Check if departure is in the past
        if (!paymentDAO.isDepartureInFuture(bookingId)) {
            JOptionPane.showMessageDialog(this,
                "  Cannot cancel booking!\n\n" +
                "This trip has already departed.\n" +
                "Cancellations are only allowed before departure time.",
                "Cancellation Not Allowed",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String paymentStatus = paymentDAO.getPaymentStatus(bookingId);
        boolean withinThreeDays = paymentDAO.isWithinThreeDaysBeforeDeparture(bookingId);
        boolean isPaid = "completed".equals(paymentStatus);
        
        System.out.println("UI - Payment Status: " + paymentStatus);
        System.out.println("UI - Within 3 days: " + withinThreeDays);
        System.out.println("UI - Is Paid: " + isPaid);
        
        // Simple cancellation logic
        String message;
        String title;
        int messageType = JOptionPane.WARNING_MESSAGE;
        
        if (isPaid) {
            message = "   PAID BOOKING CANCELLATION\n\n" +
                     "• You have already paid for this booking\n" +
                     "• Cancellation will incur a ₱100 penalty\n" +
                     "• Your account will be blacklisted until penalty is paid\n\n" +
                     "Are you sure you want to cancel?";
            title = "Cancellation - ₱100 Penalty";
        } else if (withinThreeDays) {
            message = "   LATE CANCELLATION\n\n" +
                     "• It's within 3 days of departure\n" +
                     "• Cancellation will incur a ₱50 penalty\n" +
                     "• Your account will be blacklisted until penalty is paid\n\n" +
                     "Are you sure you want to cancel?";
            title = "Cancellation - ₱50 Penalty";
        } else {
            message = "   FREE CANCELLATION\n\n" +
                     "• Payment is still pending\n" +
                     "• It's more than 3 days before departure\n" +
                     "• Cancellation is free (no penalty)\n\n" +
                     "Are you sure you want to cancel?";
            title = "Confirm Cancellation";
            messageType = JOptionPane.QUESTION_MESSAGE;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            messageType);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = paymentDAO.cancelBookingWithPenalty(bookingId, currentUserId);
            if (success) {
                String successMessage = "  Booking cancelled successfully!";
                if (isPaid || withinThreeDays) {
                    successMessage += "\n\n️  Please check your penalty payments and pay to restore your account status.";
                }
                
                JOptionPane.showMessageDialog(this,
                    successMessage,
                    "Cancellation Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh all data
                loadPaymentData();
                loadReceiptData();
                loadPenaltyData();
                loadUserData();
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to cancel booking. Please try again.",
                    "Cancellation Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (Exception e) {
        System.err.println("Error in handleCancelBooking: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Error cancelling booking: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

    private void handleCancelCompletedBooking(int modelRow) {
        try {
            int bookingId = (int) receiptTableModel.getValueAt(modelRow, 7);
            
            System.out.println("  Cancelling COMPLETED booking: " + bookingId);
            
            boolean canCancel = paymentDAO.canCancelCompletedPayment(bookingId);
            
            if (!canCancel) {
                JOptionPane.showMessageDialog(this,
                    "  Cancellation Not Allowed\n\n" +
                    "• It's within 3 days of departure time\n" +
                    "• Completed payments cannot be cancelled within 3 days of departure\n" +
                    "• You can only cancel completed payments 4+ days before departure",
                    "Cancellation Not Allowed",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String message = "   Cancellation Policy - COMPLETED PAYMENT (4+ days before departure)\n\n" +
                           "• You have already paid for this booking\n" +
                           "• It's 4+ days before departure\n" +
                           "• Cancellation will incur a ₱100 penalty\n" +
                           "• Your account will be blacklisted until penalty is paid\n\n" +
                           "Are you sure you want to cancel?";
            
            int confirm = JOptionPane.showConfirmDialog(this,
                message,
                "Cancellation - Penalty Applied",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = paymentDAO.cancelBookingWithPenalty(bookingId, currentUserId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Booking cancelled successfully!\n\nPlease check your penalty payments and pay to restore your account status.",
                        "Cancellation Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadPaymentData();
                    loadReceiptData();
                    loadPenaltyData();
                    loadUserData();
                    
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to cancel booking. Please try again.",
                        "Cancellation Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println(" Error in handleCancelCompletedBooking: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error cancelling booking: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePayPenalty(int modelRow) {
        try {
            int paymentId = (int) penaltyTableModel.getValueAt(modelRow, 3); // PaymentID is now index 3
            
            // Get amount from the reason text (extract ₱ amount)
            String reason = (String) penaltyTableModel.getValueAt(modelRow, 1);
            double amount = 0.0;
            if (reason.contains("₱100")) {
                amount = 100.00;
            } else if (reason.contains("₱50")) {
                amount = 50.00;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to pay the penalty of ₱" + amount + "?",
                "Confirm Penalty Payment",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = paymentDAO.processPenaltyPayment(paymentId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Penalty paid successfully!\nYour account status has been restored.",
                        "Payment Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadPenaltyData();
                    loadUserData();
                    
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to process penalty payment. Please try again.",
                        "Payment Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println(" Error in handlePayPenalty: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error processing penalty payment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewReceipt(int modelRow) {
        try {
            int bookingId = (int) receiptTableModel.getValueAt(modelRow, 7);
            System.out.println(" Viewing receipt for booking: " + bookingId);
            
            paymentDAO.debugPaymentStatus(bookingId);
            showReceiptDetails(bookingId);
        } catch (Exception e) {
            System.err.println(" Error in handleViewReceipt: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading receipt: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReceiptDetails(int bookingId) {
        try {
            Object[] receiptDetails = paymentDAO.getReceiptDetails(bookingId);
            
            if (receiptDetails != null) {
                String receiptText = String.format(
                    "?  BOOKING RECEIPT\n\n" +
                    "Booking ID: %d\n" +
                    "Bus Plate: %s\n" +
                    "Route: %s → %s\n" +
                    "Bus Type: %s\n" +
                    "Departure Date: %s\n" +
                    "Departure Time: %s\n" +
                    "Seats: %s\n" +
                    "Passengers: %d\n" +
                    "Total Fare: ₱%.2f\n" +
                    "Payment Method: %s\n" +
                    "Payment Status: %s\n" +
                    "Payment Date: %s\n" +
                    "Booking Date: %s\n\n" +
                    "Thank you for choosing our service!",
                    receiptDetails[0],
                    receiptDetails[1],
                    receiptDetails[2],
                    receiptDetails[3],
                    receiptDetails[4],
                    receiptDetails[5],
                    receiptDetails[6],
                    receiptDetails[7],
                    receiptDetails[8],
                    receiptDetails[9],
                    receiptDetails[10],
                    receiptDetails[13],
                    receiptDetails[11],
                    receiptDetails[12]
                );
                
                JOptionPane.showMessageDialog(this,
                    receiptText,
                    "Booking Receipt",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No completed payment found for this booking.\n" +
                    "Please complete the payment first to view the receipt.",
                    "Payment Not Completed",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println(" Error in showReceiptDetails: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading receipt details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hidePaymentTableIdColumn() {
        try {
            paymentTable.removeColumn(paymentTable.getColumnModel().getColumn(9));
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void hideReceiptTableIdColumn() {
        try {
            receiptTable.removeColumn(receiptTable.getColumnModel().getColumn(7));
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void hidePenaltyTableIdColumn() {
        try {
            penaltyTable.removeColumn(penaltyTable.getColumnModel().getColumn(3)); 
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private int getCurrentUserId() {
        util.UserSession session = util.UserSession.getInstance();
        if (session.isLoggedIn()) {
            int userId = session.getCurrentUserId();
            System.out.println("  Current user ID from session: " + userId);
            return userId;
        }
        System.err.println("  No user logged in!");
        return -1;
    }
    
    

    public void refreshData() {
        System.out.println("  Refreshing ProfilePanel data");
        paymentDAO.autoCancelPendingPayments();
        
        currentUser = userDAO.getUserById(currentUserId);
        loadUserData();
        loadPaymentData();
        loadReceiptData();
        loadPenaltyData();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        changeEmail = new javax.swing.JButton();
        changePassword = new javax.swing.JButton();
        changeEmail1 = new javax.swing.JButton();
        changeEmail2 = new javax.swing.JButton();
        changeEmail3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        paymentTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        receiptTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        penaltyTable = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(235, 235, 235));
        setPreferredSize(new java.awt.Dimension(1300, 719));

        jPanel1.setBackground(new java.awt.Color(224, 234, 235));
        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Last Name");

        jTextField1.setText("jTextField1");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("First Name");

        jTextField2.setText("jTextField1");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Email");

        jTextField3.setText("jTextField1");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Password");

        jPasswordField1.setText("jPasswordField1");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Number");

        jTextField4.setText("jTextField1");

        changeEmail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit (1) (1).png"))); // NOI18N
        changeEmail.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        changeEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeEmailActionPerformed(evt);
            }
        });

        changePassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit (1) (1).png"))); // NOI18N
        changePassword.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        changePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePasswordActionPerformed(evt);
            }
        });

        changeEmail1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit (1) (1).png"))); // NOI18N
        changeEmail1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        changeEmail1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeEmail1ActionPerformed(evt);
            }
        });

        changeEmail2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit (1) (1).png"))); // NOI18N
        changeEmail2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        changeEmail2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeEmail2ActionPerformed(evt);
            }
        });

        changeEmail3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit (1) (1).png"))); // NOI18N
        changeEmail3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        changeEmail3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeEmail3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changeEmail2)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changeEmail3))
                            .addComponent(jLabel3))
                        .addContainerGap(13, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(changeEmail))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel4))
                            .addComponent(jLabel6)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPasswordField1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(changePassword)
                                    .addComponent(changeEmail1))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeEmail2)
                    .addComponent(changeEmail3))
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeEmail1))
                .addGap(7, 7, 7)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeEmail, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(changePassword)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57))
        );

        jLabel1.setText("Hi ");

        jPanel2.setBackground(new java.awt.Color(224, 234, 235));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel2.setLayout(null);

        jLabel8.setBackground(new java.awt.Color(36, 106, 112));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("PAYMENT");
        jLabel8.setOpaque(true);
        jPanel2.add(jLabel8);
        jLabel8.setBounds(20, 30, 720, 40);

        paymentTable.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        paymentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Bus Plate ", "Pickup Point", "Ariival Point", "Bus Type", "Departure Date", "Seats", "Total", "Pay Now", "Cancel", "BookingID"
            }
        ));
        jScrollPane1.setViewportView(paymentTable);

        jPanel2.add(jScrollPane1);
        jScrollPane1.setBounds(20, 70, 720, 110);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel7.setText("PROFILE");

        jPanel3.setBackground(new java.awt.Color(224, 234, 235));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel3.setLayout(null);

        jLabel9.setBackground(new java.awt.Color(36, 106, 112));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("RECEIPTS/HISTORY");
        jLabel9.setOpaque(true);
        jPanel3.add(jLabel9);
        jLabel9.setBounds(20, 30, 720, 40);

        receiptTable.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        receiptTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Bus Plate", "Departure Date", "Pickup Point", "Arrival Point", "Payment Status", "Receipt", "Cancel", "Booking ID"
            }
        ));
        jScrollPane2.setViewportView(receiptTable);

        jPanel3.add(jScrollPane2);
        jScrollPane2.setBounds(20, 70, 720, 110);

        jPanel4.setBackground(new java.awt.Color(224, 234, 235));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel4.setLayout(null);

        jLabel10.setBackground(new java.awt.Color(36, 106, 112));
        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Penalty Payment");
        jLabel10.setOpaque(true);
        jPanel4.add(jLabel10);
        jLabel10.setBounds(20, 10, 340, 22);

        penaltyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Departure Date", "Reason", "Pay Penalty", "PaymentID"
            }
        ));
        jScrollPane3.setViewportView(penaltyTable);

        jPanel4.add(jScrollPane3);
        jScrollPane3.setBounds(20, 30, 340, 59);

        jLabel12.setText("jLabel12");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(182, 182, 182)
                        .addComponent(jLabel12)
                        .addGap(390, 390, 390)
                        .addComponent(jLabel7))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 768, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(64, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(183, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void changeEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeEmailActionPerformed
        updateEmail();
    }//GEN-LAST:event_changeEmailActionPerformed

    private void changePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordActionPerformed
        changeUserPassword();
    }//GEN-LAST:event_changePasswordActionPerformed

    private void changeEmail1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeEmail1ActionPerformed
        updatePhone();
    }//GEN-LAST:event_changeEmail1ActionPerformed

    private void changeEmail2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeEmail2ActionPerformed
        updateLastName();
    }//GEN-LAST:event_changeEmail2ActionPerformed

    private void changeEmail3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeEmail3ActionPerformed
        updateFirstName();
    }//GEN-LAST:event_changeEmail3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeEmail;
    private javax.swing.JButton changeEmail1;
    private javax.swing.JButton changeEmail2;
    private javax.swing.JButton changeEmail3;
    private javax.swing.JButton changePassword;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTable paymentTable;
    private javax.swing.JTable penaltyTable;
    private javax.swing.JTable receiptTable;
    // End of variables declaration//GEN-END:variables
}
