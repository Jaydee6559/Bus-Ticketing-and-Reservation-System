package dao;

import model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    
    // Get pending payments for a user
    public List<Object[]> getPendingPayments(int userId) {
        List<Object[]> payments = new ArrayList<>();
        String query = "SELECT " +
                      "b.booking_id, " +
                      "bus.plate_number, " +
                      "pickup.station_name as pickup_point, " +
                      "dropoff.station_name as arrival_point, " +
                      "bus.bus_type, " +
                      "DATE(s.departure_time) as departure_date, " +
                      "GROUP_CONCAT(seat.seat_code) as seats, " +
                      "b.total_fare, " +
                      "p.payment_status " +
                      "FROM bookings b " +
                      "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                      "JOIN buses bus ON s.bus_id = bus.bus_id " +
                      "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
                      "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
                      "JOIN booking_seats bs ON b.booking_id = bs.booking_id " +
                      "JOIN seats seat ON bs.seat_id = seat.seat_id " +
                      "JOIN payments p ON b.booking_id = p.booking_id " +
                      "WHERE b.user_id = ? AND b.status = 'confirmed' AND p.payment_status = 'pending' " +
                      "GROUP BY b.booking_id " +
                      "ORDER BY b.booking_date DESC";

        System.out.println(" DEBUG: Getting pending payments for user: " + userId);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Object[] paymentData = {
                    rs.getString("plate_number"),
                    rs.getString("pickup_point"),
                    rs.getString("arrival_point"),
                    rs.getString("bus_type"),
                    rs.getDate("departure_date"),
                    rs.getString("seats"),
                    rs.getDouble("total_fare"),
                    "Pay Now", 
                    "Cancel",  
                    rs.getInt("booking_id") // Hidden booking ID
                };
                payments.add(paymentData);
                count++;
                System.out.println(" Found pending payment - Booking ID: " + rs.getInt("booking_id") + 
                                 ", Plate: " + rs.getString("plate_number") +
                                 ", Payment Status: " + rs.getString("payment_status"));
            }
            System.out.println(" Total pending payments found: " + count);
            
        } catch (SQLException e) {
            System.err.println(" Error getting pending payments: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    // Get payment history/receipts for a user - Only show completed payments
    public List<Object[]> getPaymentHistory(int userId) {
        List<Object[]> receipts = new ArrayList<>();
        String query = "SELECT " +
                      "bus.plate_number, " +
                      "DATE(s.departure_time) as departure_date, " +
                      "pickup.station_name as pickup_point, " +
                      "dropoff.station_name as arrival_point, " +
                      "p.payment_status, " +
                      "b.booking_id " +
                      "FROM payments p " +
                      "JOIN bookings b ON p.booking_id = b.booking_id " +
                      "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                      "JOIN buses bus ON s.bus_id = bus.bus_id " +
                      "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
                      "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
                      "WHERE b.user_id = ? AND p.payment_status = 'completed' " +
                      "ORDER BY p.payment_date DESC";

        System.out.println("  DEBUG: Getting payment history for user: " + userId);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Object[] receiptData = {
                    rs.getString("plate_number"),
                    rs.getDate("departure_date"),
                    rs.getString("pickup_point"),
                    rs.getString("arrival_point"),
                    rs.getString("payment_status"),
                    "View Receipt", // Receipt button
                    rs.getInt("booking_id") // Hidden booking ID
                };
                receipts.add(receiptData);
                count++;
                System.out.println(" Found receipt - Booking ID: " + rs.getInt("booking_id") + 
                                 ", Status: " + rs.getString("payment_status"));
            }
            System.out.println(" Total receipts found: " + count);
            
        } catch (SQLException e) {
            System.err.println(" Error getting payment history: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    // Process payment
    public boolean processPayment(int bookingId) {
        String checkQuery = "SELECT payment_status FROM payments WHERE booking_id = ?";
        String updateQuery = "UPDATE payments SET payment_status = 'completed', payment_date = NOW() WHERE booking_id = ? AND payment_status = 'pending'";
        
        System.out.println(" DEBUG: Processing payment for booking: " + bookingId);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Check current status
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, bookingId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    String currentStatus = rs.getString("payment_status");
                    System.out.println(" Current payment status: " + currentStatus);
                    
                    if (!"pending".equals(currentStatus)) {
                        System.out.println(" Payment is not pending, current status: " + currentStatus);
                        return false;
                    }
                } else {
                    System.out.println("No payment record found for booking: " + bookingId);
                    return false;
                }
            }
            
            // Process payment
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, bookingId);
                boolean result = updateStmt.executeUpdate() > 0;
                System.out.println("Payment processed: " + result);
                
                if (result) {
                    // Verify the update
                    try (PreparedStatement verifyStmt = conn.prepareStatement(checkQuery)) {
                        verifyStmt.setInt(1, bookingId);
                        ResultSet verifyRs = verifyStmt.executeQuery();
                        if (verifyRs.next()) {
                            System.out.println(" Verified new payment status: " + verifyRs.getString("payment_status"));
                        }
                    }
                }
                
                return result;
            }
            
        } catch (SQLException e) {
            System.err.println(" Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get receipt details - Only for completed payments
    public Object[] getReceiptDetails(int bookingId) {
        String query = "SELECT " +
                      "b.booking_id, " +
                      "bus.plate_number, " +
                      "pickup.station_name as pickup_point, " +
                      "dropoff.station_name as arrival_point, " +
                      "bus.bus_type, " +
                      "DATE(s.departure_time) as departure_date, " +
                      "TIME(s.departure_time) as departure_time, " +
                      "GROUP_CONCAT(seat.seat_code) as seats, " +
                      "b.passenger_num, " +
                      "b.total_fare, " +
                      "p.payment_method, " +
                      "p.payment_date, " +
                      "b.booking_date, " +
                      "p.payment_status " +
                      "FROM bookings b " +
                      "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                      "JOIN buses bus ON s.bus_id = bus.bus_id " +
                      "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
                      "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
                      "JOIN booking_seats bs ON b.booking_id = bs.booking_id " +
                      "JOIN seats seat ON bs.seat_id = seat.seat_id " +
                      "JOIN payments p ON b.booking_id = p.booking_id " +
                      "WHERE b.booking_id = ? AND p.payment_status = 'completed' " +
                      "GROUP BY b.booking_id";

        System.out.println(" DEBUG: Getting receipt details for booking: " + bookingId);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Object[] receiptDetails = {
                    rs.getInt("booking_id"),
                    rs.getString("plate_number"),
                    rs.getString("pickup_point"),
                    rs.getString("arrival_point"),
                    rs.getString("bus_type"),
                    rs.getDate("departure_date"),
                    rs.getTime("departure_time"),
                    rs.getString("seats"),
                    rs.getInt("passenger_num"),
                    rs.getDouble("total_fare"),
                    rs.getString("payment_method"),
                    rs.getTimestamp("payment_date"),
                    rs.getTimestamp("booking_date"),
                    rs.getString("payment_status")
                };
                System.out.println("  Receipt details loaded for booking: " + bookingId + 
                                 ", Payment Status: " + rs.getString("payment_status"));
                return receiptDetails;
            } else {
                System.out.println(" No completed payment found for booking: " + bookingId);
            }
        } catch (SQLException e) {
            System.err.println(" Error getting receipt details: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Debug method to check payment status
    public void debugPaymentStatus(int bookingId) {
        String query = "SELECT b.booking_id, b.status as booking_status, p.payment_status " +
                      "FROM bookings b LEFT JOIN payments p ON b.booking_id = p.booking_id " +
                      "WHERE b.booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("=== DEBUG PAYMENT STATUS ===");
                System.out.println("Booking ID: " + rs.getInt("booking_id"));
                System.out.println("Booking Status: " + rs.getString("booking_status"));
                System.out.println("Payment Status: " + rs.getString("payment_status"));
                System.out.println("============================");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper methods with connection parameter support
    public String getPaymentStatus(int bookingId) {
        return getPaymentStatus(bookingId, null);
    }

    private String getPaymentStatus(int bookingId, Connection existingConn) {
        String sql = "SELECT payment_status FROM payments WHERE booking_id = ?";
        Connection conn = null;
        boolean shouldClose = false;
        
        try {
            if (existingConn != null) {
                conn = existingConn;
            } else {
                conn = DatabaseConnection.getConnection();
                shouldClose = true;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, bookingId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getString("payment_status");
                }
            }
            
        } catch (SQLException e) {
            System.err.println(" Error getting payment status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println(" Error closing connection in getPaymentStatus: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public boolean isWithinThreeDaysBeforeDeparture(int bookingId) {
        return isWithinThreeDaysBeforeDeparture(bookingId, null);
    }

    private boolean isWithinThreeDaysBeforeDeparture(int bookingId, Connection existingConn) {
        String sql = "SELECT s.departure_time FROM bookings b " +
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "WHERE b.booking_id = ?";
        
        Connection conn = null;
        boolean shouldClose = false;
        
        try {
            if (existingConn != null) {
                conn = existingConn;
            } else {
                conn = DatabaseConnection.getConnection();
                shouldClose = true;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, bookingId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Timestamp departureTime = rs.getTimestamp("departure_time");
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    
                    long diff = departureTime.getTime() - now.getTime();
                    double daysDiff = (double) diff / (1000 * 60 * 60 * 24);
                    
                    System.out.println(" Days until departure: " + daysDiff);
                    return daysDiff <= 3 && daysDiff >= 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println(" Error checking departure time: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println(" Error closing connection in isWithinThreeDaysBeforeDeparture: " + e.getMessage());
                }
            }
        }
        return false;
    }

    public boolean canCancelCompletedPayment(int bookingId) {
        String sql = "SELECT s.departure_time FROM bookings b " +
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "WHERE b.booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Timestamp departureTime = rs.getTimestamp("departure_time");
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long diff = departureTime.getTime() - now.getTime();
                double daysDiff = (double) diff / (1000 * 60 * 60 * 24);
                
                System.out.println("Days until departure for completed payment: " + daysDiff);
                return daysDiff > 3;
            }
            
        } catch (SQLException e) {
            System.err.println(" Error checking departure time for completed payment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDepartureInFuture(int bookingId) {
        String sql = "SELECT s.departure_time FROM bookings b " +
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "WHERE b.booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Timestamp departureTime = rs.getTimestamp("departure_time");
                Timestamp now = new Timestamp(System.currentTimeMillis());
                return departureTime.after(now);
            }
            
        } catch (SQLException e) {
            System.err.println(" Error checking if departure is in future: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Create penalty payment
    public boolean createPenaltyPayment(Connection conn, int userId, int originalBookingId, String reason, double amount) {
        String sql = "INSERT INTO payments (booking_id, payment_date, payment_method, payment_status) VALUES (?, NOW(), ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, originalBookingId);
            String penaltyType = amount == 50 ? "Penalty: Late Cancellation" : "Penalty: Post-Payment Cancellation";
            stmt.setString(2, penaltyType);
            stmt.setString(3, "penalty");
            
            boolean result = stmt.executeUpdate() > 0;
            System.out.println(" Penalty payment created: " + result);
            
            if (result) {
            
                String updateUserSql = "UPDATE users SET user_status = 'blacklisted' WHERE user_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateUserSql)) {
                    updateStmt.setInt(1, userId);
                    int rowsAffected = updateStmt.executeUpdate();
                    System.out.println(" User blacklisted - Rows affected: " + rowsAffected);
                }
            }
            
            return result;
            
        } catch (SQLException e) {
            System.err.println(" Error creating penalty payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get pending penalty payments for user
    public List<Object[]> getPendingPenalties(int userId) {
        List<Object[]> penalties = new ArrayList<>();
        
        String sql = "SELECT p.payment_id, p.payment_method, p.booking_id, " +
                    "s.departure_time, bus.plate_number " +
                    "FROM payments p " +
                    "JOIN bookings b ON p.booking_id = b.booking_id " + 
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "JOIN buses bus ON s.bus_id = bus.bus_id " +
                    "WHERE b.user_id = ? AND p.payment_status = 'penalty' " +
                    "ORDER BY p.payment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String paymentMethod = rs.getString("payment_method");
                double amount = 0.0;
                String reason = "";
                
                if (paymentMethod.contains("Late Cancellation")) {
                    amount = 50.00;
                    reason = "Late cancellation (within 3 days of departure) - ₱50.00";
                } else if (paymentMethod.contains("Post-Payment Cancellation")) {
                    amount = 100.00;
                    reason = "Cancellation after payment - ₱100.00";
                }
                
                // Use Timestamp instead of Date to get full departure time
                Timestamp departureTimestamp = rs.getTimestamp("departure_time");
                java.util.Date departureDate = new java.util.Date(departureTimestamp.getTime());
                
                // Format the date for display
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a");
                String formattedDeparture = dateFormat.format(departureDate);
                
                Object[] penaltyData = {
                    formattedDeparture,
                    reason,
                    "Pay Penalty",
                    rs.getInt("payment_id")
                };
                penalties.add(penaltyData);
                
                System.out.println(" Penalty - Departure: " + formattedDeparture + ", Reason: " + reason);
            }
            
            System.out.println(" Total pending penalties found: " + penalties.size());
            
        } catch (SQLException e) {
            System.err.println(" Error getting pending penalties: " + e.getMessage());
            e.printStackTrace();
        }
        return penalties;
    }

    // Process penalty payment - FIXED: Don't overwrite payment_date
    public boolean processPenaltyPayment(int paymentId) {
        String sql = "UPDATE payments SET payment_status = 'penalty_paid' WHERE payment_id = ? AND payment_status = 'penalty'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, paymentId);
            boolean result = stmt.executeUpdate() > 0;
            
            if (result) {
                System.out.println(" Penalty payment processed successfully - Payment ID: " + paymentId);
                
                // Get user ID to update their status
                String getUserSql = "SELECT b.user_id FROM payments p " +
                                  "JOIN bookings b ON p.booking_id = b.booking_id " +
                                  "WHERE p.payment_id = ?";
                try (PreparedStatement getUserStmt = conn.prepareStatement(getUserSql)) {
                    getUserStmt.setInt(1, paymentId);
                    ResultSet rs = getUserStmt.executeQuery();
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        if (!hasPendingPenalties(userId)) {
                            UserDAO userDAO = new UserDAO();
                            userDAO.updateUserStatus(userId, "whitelisted");
                            System.out.println(" User whitelisted after paying all penalties - User ID: " + userId);
                        } else {
                            System.out.println(" User still has pending penalties - User ID: " + userId);
                        }
                    }
                }
            } else {
                System.out.println(" Failed to process penalty payment - Payment ID: " + paymentId);
            }
            
            return result;
            
        } catch (SQLException e) {
            System.err.println(" Error processing penalty payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Check if user has pending penalties
    public boolean hasPendingPenalties(int userId) {
        String sql = "SELECT COUNT(*) FROM payments p " +
                    "JOIN bookings b ON p.booking_id = b.booking_id " +
                    "WHERE b.user_id = ? AND p.payment_status = 'penalty'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println(" Error checking pending penalties: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Debug method for cancellation
    public void debugCancellationStatus(int bookingId) {
        debugCancellationStatus(bookingId, null);
    }

    private void debugCancellationStatus(int bookingId, Connection existingConn) {
        String sql = "SELECT " +
                    "b.booking_id, b.status as booking_status, " +
                    "p.payment_status, s.departure_time, " +
                    "DATEDIFF(s.departure_time, NOW()) as days_until_departure " +
                    "FROM bookings b " +
                    "JOIN payments p ON b.booking_id = p.booking_id " +
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "WHERE b.booking_id = ?";
        
        Connection conn = null;
        boolean shouldClose = false;
        
        try {
            if (existingConn != null) {
                conn = existingConn;
            } else {
                conn = DatabaseConnection.getConnection();
                shouldClose = true;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, bookingId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    System.out.println("=== CANCELLATION DEBUG ===");
                    System.out.println("Booking ID: " + rs.getInt("booking_id"));
                    System.out.println("Booking Status: " + rs.getString("booking_status"));
                    System.out.println("Payment Status: " + rs.getString("payment_status"));
                    System.out.println("Departure Time: " + rs.getTimestamp("departure_time"));
                    System.out.println("Days until departure: " + rs.getInt("days_until_departure"));
                    System.out.println("=== END DEBUG ===");
                }
            }
            
        } catch (SQLException e) {
            System.err.println(" Error debugging cancellation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println(" Error closing connection in debugCancellationStatus: " + e.getMessage());
                }
            }
        }
    }

    // MAIN FIX: Completely rewritten cancelBookingWithPenalty method
    public boolean cancelBookingWithPenalty(int bookingId, int userId) {
        System.out.println(" DEBUG: Starting cancellation process for booking: " + bookingId);
        
        Connection conn = null;
        
        try {
            // Get a single connection for the entire transaction
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            System.out.println(" Database connection established for cancellation");
            
            // Use the same connection for all queries within this transaction
            debugCancellationStatus(bookingId, conn);
            
            String paymentStatus = getPaymentStatus(bookingId, conn);
            boolean withinThreeDays = isWithinThreeDaysBeforeDeparture(bookingId, conn);
            boolean isPaid = "completed".equals(paymentStatus);
            
            System.out.println(" Payment Status: " + paymentStatus);
            System.out.println(" Within 3 days: " + withinThreeDays);
            System.out.println(" Is Paid: " + isPaid);
            
            // Update booking status
            String bookingQuery = "UPDATE bookings SET status = 'cancelled' WHERE booking_id = ?";
            boolean bookingUpdated = false;
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery)) {
                bookingStmt.setInt(1, bookingId);
                int rowsAffected = bookingStmt.executeUpdate();
                bookingUpdated = rowsAffected > 0;
                System.out.println(" Booking update - Rows affected: " + rowsAffected);
            }
            
            // Update payment status if pending
            boolean paymentUpdated = false;
            if ("pending".equals(paymentStatus)) {
                String paymentQuery = "UPDATE payments SET payment_status = 'cancelled' WHERE booking_id = ? AND payment_status = 'pending'";
                try (PreparedStatement paymentStmt = conn.prepareStatement(paymentQuery)) {
                    paymentStmt.setInt(1, bookingId);
                    int rowsAffected = paymentStmt.executeUpdate();
                    paymentUpdated = rowsAffected > 0;
                    System.out.println(" Payment update - Rows affected: " + rowsAffected);
                }
            } else {
                paymentUpdated = true; // Payment is already completed or cancelled
                System.out.println("ℹ️ Payment not updated (status: " + paymentStatus + ")");
            }
            
            // Apply penalties only if both updates were successful
            if (bookingUpdated && paymentUpdated) {
                boolean penaltyCreated = false;
                
                if (isPaid) {
                    // Paid booking cancellation - always apply penalty
                    penaltyCreated = createPenaltyPayment(conn, userId, bookingId, "Cancellation after payment", 100.00);
                    System.out.println(" Applied 100 pesos penalty for cancellation after payment");
                } else if (withinThreeDays) {
                    // Unpaid booking within 3 days - apply penalty
                    penaltyCreated = createPenaltyPayment(conn, userId, bookingId, "Late cancellation (within 3 days of departure)", 50.00);
                    System.out.println(" Applied 50 pesos penalty for late cancellation");
                } else {
                    System.out.println(" Free cancellation - no penalty applied");
                }
                
                // Commit the transaction
                conn.commit();
                System.out.println(" Transaction committed successfully");
                System.out.println(" Booking cancelled successfully");
                return true;
            } else {
                // Rollback if updates failed
                conn.rollback();
                System.out.println(" Failed to cancel booking - bookingUpdated: " + bookingUpdated + ", paymentUpdated: " + paymentUpdated);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println(" Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.rollback();
                    System.out.println(" Transaction rolled back due to error");
                }
            } catch (SQLException ex) {
                System.err.println("  Error during rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            // Properly close the connection
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                    System.out.println("  Database connection closed");
                }
            } catch (SQLException e) {
                System.err.println(" Error closing connection: " + e.getMessage());
            }
        }
    }

    // Auto-cancel pending payments that are within 3 days of departure
    public void autoCancelPendingPayments() {
        String sql = "SELECT b.booking_id, b.user_id FROM bookings b " +
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "JOIN payments p ON b.booking_id = p.booking_id " +
                    "WHERE p.payment_status = 'pending' " +
                    "AND s.departure_time <= DATE_ADD(NOW(), INTERVAL 3 DAY) " +
                    "AND s.departure_time > NOW()";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("  Auto-cancelling pending payments within 3 days");
            
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                int userId = rs.getInt("user_id");
                
                System.out.println("  Auto-cancelling booking: " + bookingId);
                cancelBookingWithPenalty(bookingId, userId);
            }
            
        } catch (SQLException e) {
            System.err.println(" Error auto-cancelling pending payments: " + e.getMessage());
            e.printStackTrace();
        }
    }
}