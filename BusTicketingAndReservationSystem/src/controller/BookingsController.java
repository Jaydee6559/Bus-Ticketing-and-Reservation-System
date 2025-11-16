package controller;

import model.Schedule;
import dao.ScheduleDAO;
import view.BookingsPanel;
import java.util.List;
import javax.swing.JOptionPane;

public class BookingsController {
    private BookingsPanel view;
    private ScheduleDAO scheduleDAO;
    
    public BookingsController(BookingsPanel view) {
        this.view = view;
        this.scheduleDAO = new ScheduleDAO();
    }
    
    // Load only ongoing schedules for users
    public void loadAllSchedules() {
        System.out.println(" Loading ONGOING schedules for user...");
        List<Schedule> schedules = scheduleDAO.getOngoingSchedules(); // Only ongoing schedules
        System.out.println(" Found " + schedules.size() + " ongoing schedules for user");
        view.displaySchedules(schedules);
    }
    
    // Filter only ongoing schedules for users
    public void loadFilteredSchedules(String busType, List<String> stations) {
        System.out.println(" Filtering ONGOING schedules for user...");
        System.out.println("   Bus Type: " + busType);
        System.out.println("   Stations: " + stations);
        List<Schedule> schedules = scheduleDAO.getFilteredSchedules(busType, stations); // Only ongoing schedules
        System.out.println(" Found " + schedules.size() + " filtered ongoing schedules");
        view.displaySchedules(schedules);
    }
    
    public Schedule getScheduleDetails(int scheduleId) {
        return scheduleDAO.getScheduleById(scheduleId);
    }
    
    public void handleBookNow(int scheduleId) {
        System.out.println(" Handling book now for schedule ID: " + scheduleId);
        Schedule schedule = getScheduleDetails(scheduleId);
        if (schedule != null) {
            // Additional validation - check if schedule is still ongoing and has seats
            if (schedule.getAvailableSeats() > 0) {
                System.out.println(" Schedule is available for booking");
                view.navigateToBookingDetails(schedule);
            } else {
                System.out.println(" Schedule has no available seats");
                javax.swing.JOptionPane.showMessageDialog(view, 
                    "Sorry, this schedule is no longer available for booking.", 
                    "Not Available", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        } else {
            System.out.println(" Schedule not found");
            javax.swing.JOptionPane.showMessageDialog(view, 
                "Schedule not found!", 
                "Error", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void loadFilteredSchedulesByDateAndStation(String busType, List<String> stations, java.sql.Date date) {
        try {
            System.out.println("   CONTROLLER: Loading filtered schedules by date and station");
            System.out.println("   Bus Type: " + busType);
            System.out.println("   Stations: " + stations);
            System.out.println("   Date: " + date);

            List<Schedule> filteredSchedules = scheduleDAO.getFilteredSchedulesByDateAndStation(busType, stations, date);
            System.out.println("   Found " + filteredSchedules.size() + " schedules");

            view.displaySchedules(filteredSchedules);

            if (filteredSchedules.isEmpty()) {
                JOptionPane.showMessageDialog(view, 
                    "No schedules found for the selected station and date.", 
                    "No Results", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, 
                "Error loading filtered schedules: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}