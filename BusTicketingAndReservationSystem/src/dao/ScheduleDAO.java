package dao;

import model.Schedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    
   
    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();

        String sql = "SELECT " +
                     "s.schedule_id, " +
                     "pickup.station_name as pickup_point, " +
                     "dropoff.station_name as arrival_point, " +
                     "s.departure_time as departure_timestamp, " + 
                     "s.fare_per_seat as fare, " +
                     "s.available_seats, " +
                     "b.capacity, " +
                     "b.bus_type, " +
                     "b.plate_number, " +
                     "s.status " +
                     "FROM schedules s " +
                     "JOIN buses b ON s.bus_id = b.bus_id " +
                     "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
                     "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
                     "ORDER BY s.departure_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("=== DEBUG: Retrieving all schedules ===");
            int count = 0;

            while (rs.next()) {
                count++;
                // Get the full timestamp from database
                Timestamp departureTimestamp = rs.getTimestamp("departure_timestamp");

                // Extract date and time components from the timestamp
                Date travelDate = new Date(departureTimestamp.getTime());
                Time departureTime = new Time(departureTimestamp.getTime());

                System.out.println("Schedule #" + count + ":");
                System.out.println("  ID: " + rs.getInt("schedule_id"));
                System.out.println("  Departure Timestamp: " + departureTimestamp);
                System.out.println("  Extracted Date: " + travelDate);
                System.out.println("  Extracted Time: " + departureTime);
                System.out.println("  Route: " + rs.getString("pickup_point") + " → " + rs.getString("arrival_point"));

                Schedule schedule = new Schedule(
                    rs.getInt("schedule_id"),
                    rs.getString("pickup_point"),
                    rs.getString("arrival_point"),
                    travelDate,  // Use the extracted date
                    departureTime,  // Use the extracted time
                    rs.getDouble("fare"),
                    rs.getInt("available_seats"),
                    rs.getInt("capacity"),
                    rs.getString("bus_type"),
                    rs.getString("plate_number"),
                    rs.getString("status")
                );
                schedules.add(schedule);
            }

            System.out.println("Total schedules retrieved: " + count);
            System.out.println("=====================================");

        } catch (SQLException e) {
            System.err.println("Error getting all schedules: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }

    public int getCompletedSchedulesCount() {
        String sql = "SELECT COUNT(*) FROM schedules WHERE status = 'completed'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error getting completed schedules count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public List<Schedule> getOngoingSchedules() {
        List<Schedule> schedules = new ArrayList<>();

        String sql = "SELECT " +
                     "s.schedule_id, " +
                     "pickup.station_name as pickup_point, " +
                     "dropoff.station_name as arrival_point, " +
                     "s.departure_time as departure_timestamp, " + // Get full timestamp
                     "s.fare_per_seat as fare, " +
                     "s.available_seats, " +
                     "b.capacity, " +
                     "b.bus_type, " +
                     "b.plate_number, " +
                     "s.status " +
                     "FROM schedules s " +
                     "JOIN buses b ON s.bus_id = b.bus_id " +
                     "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
                     "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
                     "WHERE s.status = 'ongoing' " +
                     "AND s.available_seats > 0 " +
                     "ORDER BY s.departure_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Timestamp departureTimestamp = rs.getTimestamp("departure_timestamp");
                Date travelDate = new Date(departureTimestamp.getTime());
                Time departureTime = new Time(departureTimestamp.getTime());

                Schedule schedule = new Schedule(
                    rs.getInt("schedule_id"),
                    rs.getString("pickup_point"),
                    rs.getString("arrival_point"),
                    travelDate,
                    departureTime,
                    rs.getDouble("fare"),
                    rs.getInt("available_seats"),
                    rs.getInt("capacity"),
                    rs.getString("bus_type"),
                    rs.getString("plate_number"),
                    rs.getString("status")
                );
                schedules.add(schedule);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public List<Schedule> getFilteredSchedules(String busType, List<String> stations) {
        List<Schedule> schedules = new ArrayList<>();

        System.out.println("=== FILTER DEBUG (USER) ===");
        System.out.println("Bus Type: " + busType);
        System.out.println("Stations: " + stations);

        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "s.schedule_id, " +
            "pickup.station_name as pickup_point, " +
            "dropoff.station_name as arrival_point, " +
            "s.departure_time as departure_timestamp, " + // Get full timestamp
            "s.fare_per_seat as fare, " +
            "s.available_seats, " +
            "b.capacity, " +
            "b.bus_type, " +
            "b.plate_number, " +
            "s.status " +
            "FROM schedules s " +
            "JOIN buses b ON s.bus_id = b.bus_id " +
            "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
            "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
            "WHERE s.status = 'ongoing' " +
            "AND s.available_seats > 0 "
        );

        List<Object> parameters = new ArrayList<>();

        // Add bus type filter
        if (busType != null && !busType.isEmpty() && !busType.equals("All")) {
            sql.append(" AND b.bus_type = ? ");
            parameters.add(busType);
            System.out.println("Adding bus filter: " + busType);
        }

        // Add station filter
        if (stations != null && !stations.isEmpty()) {
            sql.append(" AND (pickup.station_name IN (");
            for (int i = 0; i < stations.size(); i++) {
                sql.append("?");
                if (i < stations.size() - 1) sql.append(",");
                parameters.add(stations.get(i));
            }
            sql.append(") OR dropoff.station_name IN (");
            for (int i = 0; i < stations.size(); i++) {
                sql.append("?");
                if (i < stations.size() - 1) sql.append(",");
                parameters.add(stations.get(i));
            }
            sql.append(")) ");
            System.out.println("Adding station filter: " + stations);
        }

        sql.append(" ORDER BY s.departure_time");

        System.out.println("Final SQL (User Filter): " + sql.toString());
        System.out.println("Parameters: " + parameters);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    Timestamp departureTimestamp = rs.getTimestamp("departure_timestamp");
                    Date travelDate = new Date(departureTimestamp.getTime());
                    Time departureTime = new Time(departureTimestamp.getTime());

                    Schedule schedule = new Schedule(
                        rs.getInt("schedule_id"),
                        rs.getString("pickup_point"),
                        rs.getString("arrival_point"),
                        travelDate,
                        departureTime,
                        rs.getDouble("fare"),
                        rs.getInt("available_seats"),
                        rs.getInt("capacity"),
                        rs.getString("bus_type"),
                        rs.getString("plate_number"),
                        rs.getString("status")
                    );
                    schedules.add(schedule);
                    count++;
                    System.out.println("User Filter match: " + schedule.getPickupPoint() + " to " + 
                        schedule.getArrivalPoint() + " (" + schedule.getBusType() + ") - Departure: " + departureTimestamp);
                }
                System.out.println("Total filtered results (User): " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public Schedule getScheduleById(int scheduleId) {
        String sql = "SELECT " +
                     "s.schedule_id, " +
                     "pickup.station_name as pickup_point, " +
                     "dropoff.station_name as arrival_point, " +
                     "s.departure_time as departure_timestamp, " + // Get full timestamp
                     "s.fare_per_seat as fare, " +
                     "s.available_seats, " +
                     "b.capacity, " +
                     "b.bus_type, " +
                     "b.plate_number, " +
                     "s.status " +
                     "FROM schedules s " +
                     "JOIN buses b ON s.bus_id = b.bus_id " +
                     "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
                     "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
                     "WHERE s.schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp departureTimestamp = rs.getTimestamp("departure_timestamp");
                    Date travelDate = new Date(departureTimestamp.getTime());
                    Time departureTime = new Time(departureTimestamp.getTime());

                    return new Schedule(
                        rs.getInt("schedule_id"),
                        rs.getString("pickup_point"),
                        rs.getString("arrival_point"),
                        travelDate,
                        departureTime,
                        rs.getDouble("fare"),
                        rs.getInt("available_seats"),
                        rs.getInt("capacity"),
                        rs.getString("bus_type"),
                        rs.getString("plate_number"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Schedule> getFilteredSchedulesByDateAndStation(String busType, List<String> stations, java.sql.Date date) {
        List<Schedule> schedules = new ArrayList<>();

        System.out.println("=== FILTER DEBUG (DATE + STATION) ===");
        System.out.println("Bus Type: " + busType);
        System.out.println("Stations: " + stations);
        System.out.println("Date: " + date);

        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "s.schedule_id, " +
            "pickup.station_name as pickup_point, " +
            "dropoff.station_name as arrival_point, " +
            "s.departure_time as departure_timestamp, " +
            "s.fare_per_seat as fare, " +
            "s.available_seats, " +
            "b.capacity, " +
            "b.bus_type, " +
            "b.plate_number, " +
            "s.status " +
            "FROM schedules s " +
            "JOIN buses b ON s.bus_id = b.bus_id " +
            "JOIN stations pickup ON s.pickup_station_id = pickup.station_id " +
            "JOIN stations dropoff ON s.dropping_station_id = dropoff.station_id " +
            "WHERE s.status = 'ongoing' " +
            "AND s.available_seats > 0 " +
            "AND DATE(s.departure_time) = ? "  // Filter by date
        );

        List<Object> parameters = new ArrayList<>();
        parameters.add(date);

        // Add bus type filter
        if (busType != null && !busType.isEmpty() && !busType.equals("All")) {
            sql.append(" AND b.bus_type = ? ");
            parameters.add(busType);
            System.out.println("Adding bus filter: " + busType);
        }

        // Add station filter
        if (stations != null && !stations.isEmpty()) {
            sql.append(" AND (pickup.station_name IN (");
            for (int i = 0; i < stations.size(); i++) {
                sql.append("?");
                if (i < stations.size() - 1) sql.append(",");
                parameters.add(stations.get(i));
            }
            sql.append(") OR dropoff.station_name IN (");
            for (int i = 0; i < stations.size(); i++) {
                sql.append("?");
                if (i < stations.size() - 1) sql.append(",");
                parameters.add(stations.get(i));
            }
            sql.append(")) ");
            System.out.println("Adding station filter: " + stations);
        }

        sql.append(" ORDER BY s.departure_time");

        System.out.println("Final SQL (Date + Station Filter): " + sql.toString());
        System.out.println("Parameters: " + parameters);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    Timestamp departureTimestamp = rs.getTimestamp("departure_timestamp");
                    Date travelDate = new Date(departureTimestamp.getTime());
                    Time departureTime = new Time(departureTimestamp.getTime());

                    Schedule schedule = new Schedule(
                        rs.getInt("schedule_id"),
                        rs.getString("pickup_point"),
                        rs.getString("arrival_point"),
                        travelDate,
                        departureTime,
                        rs.getDouble("fare"),
                        rs.getInt("available_seats"),
                        rs.getInt("capacity"),
                        rs.getString("bus_type"),
                        rs.getString("plate_number"),
                        rs.getString("status")
                    );
                    schedules.add(schedule);
                    count++;
                    System.out.println("Date+Station Filter match: " + schedule.getPickupPoint() + " to " + 
                        schedule.getArrivalPoint() + " (" + schedule.getBusType() + ") - Departure: " + departureTimestamp);
                }
                System.out.println("Total filtered results (Date+Station): " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }
    public boolean addSchedule(int busId, int pickupStationId, int dropoffStationId, 
                              Date travelDate, Time departureTime, double farePerSeat) {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First, update the bus status to 'unavailable'
            String updateBusSql = "UPDATE buses SET status = 'unavailable' WHERE bus_id = ?";
            try (PreparedStatement updateBusStmt = conn.prepareStatement(updateBusSql)) {
                updateBusStmt.setInt(1, busId);
                int busRowsAffected = updateBusStmt.executeUpdate();

                if (busRowsAffected <= 0) {
                    conn.rollback();
                    System.err.println("Failed to update bus status. Bus might not exist.");
                    return false;
                }

                System.out.println("Updated bus " + busId + " status to: unavailable");
            }

            // Then insert the schedule
            String insertScheduleSql = "INSERT INTO schedules (bus_id, pickup_station_id, dropping_station_id, " +
                                     "departure_time, fare_per_seat, available_seats, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertScheduleSql)) {

                // Combine date and time into timestamp
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(travelDate);

                java.util.Calendar timeCal = java.util.Calendar.getInstance();
                timeCal.setTime(departureTime);

                cal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
                cal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
                cal.set(java.util.Calendar.SECOND, timeCal.get(java.util.Calendar.SECOND));
                cal.set(java.util.Calendar.MILLISECOND, 0);

                Timestamp departureTimestamp = new Timestamp(cal.getTimeInMillis());

                System.out.println("=== DEBUG: Inserting schedule ===");
                System.out.println("Bus ID: " + busId);
                System.out.println("Pickup Station ID: " + pickupStationId);
                System.out.println("Dropoff Station ID: " + dropoffStationId);
                System.out.println("Travel Date: " + travelDate);
                System.out.println("Departure Time: " + departureTime);
                System.out.println("Combined Departure Timestamp: " + departureTimestamp);
                System.out.println("Fare: " + farePerSeat);
                System.out.println("Status: ongoing");

                pstmt.setInt(1, busId);
                pstmt.setInt(2, pickupStationId);
                pstmt.setInt(3, dropoffStationId);
                pstmt.setTimestamp(4, departureTimestamp);
                pstmt.setDouble(5, farePerSeat);
                pstmt.setInt(6, 31); // Default available seats (bus capacity)
                pstmt.setString(7, "ongoing");

                int scheduleRowsAffected = pstmt.executeUpdate();

                if (scheduleRowsAffected > 0) {
                    conn.commit(); // Commit both operations
                    System.out.println("Schedule added successfully and bus status updated to unavailable");

                    // Verify the inserted data
                    verifyInsertedSchedule(conn, busId, pickupStationId, dropoffStationId);
                    return true;
                } else {
                    conn.rollback(); // Rollback if schedule insertion fails
                    System.err.println("Failed to add schedule");
                    return false;
                }
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback on error
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }

            System.err.println("Error adding schedule: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Error closing connection: " + closeEx.getMessage());
            }
        }
    }

    // verify the inserted schedule
    private void verifyInsertedSchedule(Connection conn, int busId, int pickupStationId, int dropoffStationId) {
        String sql = "SELECT schedule_id, departure_time FROM schedules " +
                    "WHERE bus_id = ? AND pickup_station_id = ? AND dropping_station_id = ? " +
                    "ORDER BY schedule_id DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, busId);
            pstmt.setInt(2, pickupStationId);
            pstmt.setInt(3, dropoffStationId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("=== VERIFICATION: Inserted Schedule ===");
                System.out.println("Schedule ID: " + rs.getInt("schedule_id"));
                System.out.println("Departure Time in DB: " + rs.getTimestamp("departure_time"));
                System.out.println("======================================");
            }
        } catch (SQLException e) {
            System.err.println("Error verifying inserted schedule: " + e.getMessage());
        }
    }

    public boolean updateScheduleStatus(int scheduleId, String status) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First, get the bus_id for this schedule
            String getBusSql = "SELECT bus_id FROM schedules WHERE schedule_id = ?";
            int busId = -1;

            try (PreparedStatement getBusStmt = conn.prepareStatement(getBusSql)) {
                getBusStmt.setInt(1, scheduleId);
                ResultSet rs = getBusStmt.executeQuery();

                if (rs.next()) {
                    busId = rs.getInt("bus_id");
                } else {
                    conn.rollback();
                    System.err.println("Schedule not found with ID: " + scheduleId);
                    return false;
                }
            }

            // Update schedule status
            String updateScheduleSql = "UPDATE schedules SET status = ? WHERE schedule_id = ?";
            try (PreparedStatement updateScheduleStmt = conn.prepareStatement(updateScheduleSql)) {
                updateScheduleStmt.setString(1, status);
                updateScheduleStmt.setInt(2, scheduleId);
                int scheduleRowsAffected = updateScheduleStmt.executeUpdate();

                if (scheduleRowsAffected <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            // If schedule is being completed or cancelled, set bus back to available
            if ("completed".equals(status) || "cancelled".equals(status)) {
                String updateBusSql = "UPDATE buses SET status = 'available' WHERE bus_id = ?";
                try (PreparedStatement updateBusStmt = conn.prepareStatement(updateBusSql)) {
                    updateBusStmt.setInt(1, busId);
                    updateBusStmt.executeUpdate();
                    System.out.println("Set bus " + busId + " back to available status");
                }
            }

            conn.commit(); // Commit all changes
            System.out.println("Schedule status updated to: " + status + " and bus status handled accordingly");
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback on error
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }

            System.err.println("Error updating schedule status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Error closing connection: " + closeEx.getMessage());
            }
        }
    }
}