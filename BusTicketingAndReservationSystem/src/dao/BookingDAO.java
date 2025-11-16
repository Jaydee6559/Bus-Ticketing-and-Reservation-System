package dao;

import model.Booking;
import model.BookingSeat;
import model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDAO {
    private Connection connection;
    private Map<String, Integer> seatIdMap;

    public BookingDAO() {
        this.connection = DatabaseConnection.getConnection();
        this.seatIdMap = loadSeatIdsFromDatabase();
    }

    // Load seat IDs from database using seat_code
    private Map<String, Integer> loadSeatIdsFromDatabase() {
        Map<String, Integer> seatMap = new HashMap<>();
        String query = "SELECT seat_id, seat_code FROM seats";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                seatMap.put(rs.getString("seat_code"), rs.getInt("seat_id"));
            }
            System.out.println("Loaded " + seatMap.size() + " seats from database");
        } catch (SQLException e) {
            System.err.println("Error loading seat IDs: " + e.getMessage());
            createFallbackSeatMapping(seatMap);
        }
        return seatMap;
    }
    
    
    public boolean hasActiveBookingForSchedule(int userId, int scheduleId) {
        String query = "SELECT COUNT(*) FROM bookings WHERE user_id = ? AND schedule_id = ? AND status IN ('confirmed', 'pending')";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, scheduleId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Booking> findBookingsByStationAndDate(String station, java.sql.Date date) {
        List<Booking> matching = new ArrayList<>();
        String sql = 
            "SELECT b.* FROM bookings b " +
            "JOIN schedules s ON b.schedule_id = s.schedule_id " +
            "WHERE (s.pickup_point = ? OR s.arrival_point = ?) " +
            "AND DATE(s.travel_date) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, station);
            stmt.setString(2, station);
            stmt.setDate(3, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setScheduleId(rs.getInt("schedule_id"));
                booking.setPassengerNum(rs.getInt("passenger_num"));
                booking.setBookingDate(rs.getTimestamp("booking_date"));
                booking.setStatus(rs.getString("status"));
                booking.setTotalFare(rs.getDouble("total_fare"));
                matching.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matching;
    }

    // Fallback mapping if seats table is empty
    private void createFallbackSeatMapping(Map<String, Integer> seatMap) {
        String[] seatCodes = {"A1", "A2", "A3", "B1", "B2", "B3", "B4", "B5", "C1", "C2", 
                             "C3", "C4", "C5", "D1", "D2", "D3", "D4", "D5", "D6", "D7", 
                             "D8", "E1", "E2", "E3", "E4", "E5", "F1", "F2", "F3", "F4", "F5"};
        
        String insertQuery = "INSERT IGNORE INTO seats (bus_id, seat_code) VALUES (1, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            for (String seatCode : seatCodes) {
                stmt.setString(1, seatCode);
                stmt.addBatch();
            }
            stmt.executeBatch();
            
            // Reload from database
            String selectQuery = "SELECT seat_id, seat_code FROM seats";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                 ResultSet rs = selectStmt.executeQuery()) {
                
                while (rs.next()) {
                    seatMap.put(rs.getString("seat_code"), rs.getInt("seat_id"));
                }
            }
            System.out.println("Created and loaded " + seatMap.size() + " seats in database");
            
        } catch (SQLException e) {
            System.err.println("Error creating fallback seats: " + e.getMessage());
            for (int i = 0; i < seatCodes.length; i++) {
                seatMap.put(seatCodes[i], i + 1);
            }
        }
    }

    public List<String> getOccupiedSeats(int scheduleId) {
        List<String> occupiedSeats = new ArrayList<>();
        // Updated to join with seats table to get seat_code
        String query = "SELECT s.seat_code " +
                      "FROM bookings b " +
                      "JOIN booking_seats bs ON b.booking_id = bs.booking_id " +
                      "JOIN seats s ON bs.seat_id = s.seat_id " +
                      "WHERE b.schedule_id = ? AND b.status = 'confirmed'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                occupiedSeats.add(rs.getString("seat_code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return occupiedSeats;
    }

    // Create a new booking and reserve seats
    public int createBooking(Booking booking, List<String> seats) {
        String bookingQuery = "INSERT INTO bookings (user_id, schedule_id, passenger_num, total_fare, status) VALUES (?, ?, ?, ?, ?)";
        String seatQuery = "INSERT INTO booking_seats (booking_id, seat_id, seat_status) VALUES (?, ?, 'occupied')";
        
        try {
            connection.setAutoCommit(false);
            int bookingId = -1;
            
            // 1. Create booking record
            try (PreparedStatement bookingStmt = connection.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS)) {
                bookingStmt.setInt(1, booking.getUserId());
                bookingStmt.setInt(2, booking.getScheduleId());
                bookingStmt.setInt(3, booking.getPassengerNum());
                bookingStmt.setDouble(4, booking.getTotalFare());
                bookingStmt.setString(5, booking.getStatus());
                
                int affectedRows = bookingStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    ResultSet generatedKeys = bookingStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        bookingId = generatedKeys.getInt(1);
                        booking.setBookingId(bookingId);
                        System.out.println("Created booking with ID: " + bookingId);
                    }
                }
            }
            
            if (bookingId == -1) {
                connection.rollback();
                return -1;
            }
            
            // 2. Reserve seats
            try (PreparedStatement seatStmt = connection.prepareStatement(seatQuery)) {
                for (String seat : seats) {
                    Integer seatId = seatIdMap.get(seat);
                    if (seatId == null) {
                        System.err.println("Invalid seat code: " + seat);
                        connection.rollback();
                        return -1;
                    }
                    
                    System.out.println("Reserving seat: " + seat + " (ID: " + seatId + ")");
                    
                    seatStmt.setInt(1, bookingId);
                    seatStmt.setInt(2, seatId);
                    seatStmt.addBatch();
                }
                
                int[] results = seatStmt.executeBatch();
                System.out.println("Reserved " + results.length + " seats");
                
                for (int result : results) {
                    if (result <= 0) {
                        connection.rollback();
                        return -1;
                    }
                }
            }
            
            // 3. Update available seats in schedules table
            if (!updateAvailableSeats(booking.getScheduleId(), booking.getPassengerNum())) {
                connection.rollback();
                return -1;
            }
            
            connection.commit();
            System.out.println("Booking completed successfully for booking ID: " + bookingId);
            return bookingId;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return -1;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Create payment record using Payment model
    public boolean createPayment(Payment payment) {
        String query = "INSERT INTO payments (booking_id, payment_date, payment_method, payment_status) VALUES (?, NOW(), ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, payment.getBookingId());
            stmt.setString(2, payment.getPaymentMethod());
            stmt.setString(3, "pending");
            
            boolean result = stmt.executeUpdate() > 0;
            System.out.println("Payment created: " + result);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update available seats in schedule
    public boolean updateAvailableSeats(int scheduleId, int seatsBooked) {
        String query = "UPDATE schedules SET available_seats = available_seats - ? WHERE schedule_id = ? AND available_seats >= ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, seatsBooked);
            stmt.setInt(2, scheduleId);
            stmt.setInt(3, seatsBooked);
            
            boolean result = stmt.executeUpdate() > 0;
            System.out.println("Updated available seats: " + result);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get booking by ID
    public Booking getBookingById(int bookingId) {
        String query = "SELECT * FROM bookings WHERE booking_id = ?";
        Booking booking = null;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setScheduleId(rs.getInt("schedule_id"));
                booking.setPassengerNum(rs.getInt("passenger_num"));
                booking.setBookingDate(rs.getTimestamp("booking_date"));
                booking.setStatus(rs.getString("status"));
                booking.setTotalFare(rs.getDouble("total_fare"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return booking;
    }

   
    public List<BookingSeat> getSeatsForBooking(int bookingId) {
        List<BookingSeat> bookingSeats = new ArrayList<>();
        String query = "SELECT bs.*, s.seat_code " +
                      "FROM booking_seats bs " +
                      "JOIN seats s ON bs.seat_id = s.seat_id " +
                      "WHERE bs.booking_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BookingSeat bookingSeat = new BookingSeat();
                bookingSeat.setBookingSeatId(rs.getInt("booking_seat_id"));
                bookingSeat.setBookingId(rs.getInt("booking_id"));
                bookingSeat.setSeatId(rs.getInt("seat_id"));
                bookingSeat.setSeatNumber(rs.getString("seat_code")); 
                bookingSeat.setSeatStatus(rs.getString("seat_status"));
                bookingSeats.add(bookingSeat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingSeats;
    }
 
}

