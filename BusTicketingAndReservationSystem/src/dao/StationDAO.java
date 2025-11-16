package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jayde
 */
public class StationDAO {
    
    public List<String> getAllStations() {
        List<String> stations = new ArrayList<>();
        String sql = "SELECT station_name FROM stations ORDER BY station_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                stations.add(rs.getString("station_name"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Return some default stations if table doesn't exist
            stations.add("Manila");
            stations.add("Quezon City");
            stations.add("Makati");
            stations.add("Pasig");
            stations.add("Taguig");
        }
        
        return stations;
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
    
    public int getStationIdByName(String stationName) {
        String sql = "SELECT station_id FROM stations WHERE station_name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, stationName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("station_id");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
}