package dao;

import model.Bus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusDAO {
    
    // Get all buses 
    public List<Bus> getAllBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT bus_id, plate_number, bus_type, capacity, status FROM buses";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("Fetching buses from database...");
            
            while (rs.next()) {
                Bus bus = new Bus();
                bus.setBusId(rs.getInt("bus_id"));
                bus.setPlateNumber(rs.getString("plate_number"));
                bus.setBusType(rs.getString("bus_type"));
                bus.setCapacity(rs.getInt("capacity"));
                bus.setStatus(rs.getString("status"));
                buses.add(bus);
                
                System.out.println("Found bus: " + bus.getPlateNumber() + " - " + bus.getBusType() + " - " + bus.getStatus());
            }
            
            System.out.println("Total buses found: " + buses.size());
            
        } catch (SQLException e) {
            System.err.println("Error fetching buses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return buses;
    }
    
    // Get only available buses
    public List<Bus> getAvailableBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT bus_id, plate_number, bus_type, capacity, status FROM buses WHERE status = 'available'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("Fetching available buses from database...");
            
            while (rs.next()) {
                Bus bus = new Bus();
                bus.setBusId(rs.getInt("bus_id"));
                bus.setPlateNumber(rs.getString("plate_number"));
                bus.setBusType(rs.getString("bus_type"));
                bus.setCapacity(rs.getInt("capacity"));
                bus.setStatus(rs.getString("status"));
                buses.add(bus);
                
                System.out.println("Available bus: " + bus.getPlateNumber() + " - " + bus.getBusType());
            }
            
            System.out.println("Available buses found: " + buses.size());
            
        } catch (SQLException e) {
            System.err.println("Error fetching available buses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return buses;
    }
    
    public boolean addBus(String plateNumber, String busType) {
        String sql = "INSERT INTO buses (plate_number, bus_type, capacity, status) VALUES (?, ?, 31, 'available')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, plateNumber);
            stmt.setString(2, busType);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated bus_id
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int busId = generatedKeys.getInt(1);
                    // Create seats for this new bus
                    createSeatsForBus(busId);
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //create seats for a bus
    private void createSeatsForBus(int busId) {
        String[] seatCodes = {"A1", "A2", "A3", "B1", "B2", "B3", "B4", "B5", "C1", "C2", 
                             "C3", "C4", "C5", "D1", "D2", "D3", "D4", "D5", "D6", "D7", 
                             "D8", "E1", "E2", "E3", "E4", "E5", "F1", "F2", "F3", "F4", "F5"};

        String insertSeatSQL = "INSERT INTO seats (bus_id, seat_code) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSeatSQL)) {

            for (String seatCode : seatCodes) {
                stmt.setInt(1, busId);
                stmt.setString(2, seatCode);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            System.out.println("Created " + results.length + " seats for bus ID: " + busId);

        } catch (SQLException e) {
            System.err.println("Error creating seats for bus " + busId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean updateBusStatus(int busId, String status) {
        String sql = "UPDATE buses SET status = ? WHERE bus_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, busId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteBus(int busId) {
        String sql = "DELETE FROM buses WHERE bus_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, busId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Bus> getUnavailableBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT bus_id, plate_number, bus_type, capacity, status FROM buses WHERE status = 'unavailable' OR status = 'maintenance'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Fetching unavailable buses from database...");

            while (rs.next()) {
                Bus bus = new Bus();
                bus.setBusId(rs.getInt("bus_id"));
                bus.setPlateNumber(rs.getString("plate_number"));
                bus.setBusType(rs.getString("bus_type"));
                bus.setCapacity(rs.getInt("capacity"));
                bus.setStatus(rs.getString("status"));
                buses.add(bus);

                System.out.println("Unavailable bus: " + bus.getPlateNumber() + " - " + bus.getBusType());
            }

            System.out.println("Unavailable buses found: " + buses.size());

        } catch (SQLException e) {
            System.err.println("Error fetching unavailable buses: " + e.getMessage());
            e.printStackTrace();
        }

        return buses;
    }
    
    public boolean plateNumberExists(String plateNumber) {
        String sql = "SELECT COUNT(*) FROM buses WHERE plate_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plateNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
}