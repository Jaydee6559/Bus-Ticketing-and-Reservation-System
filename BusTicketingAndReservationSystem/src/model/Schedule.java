package model;

import java.sql.Date;
import java.sql.Time;

public class Schedule {
    private int scheduleId;
    private int busId;
    private String pickupPoint;
    private String arrivalPoint;
    private Date travelDate;
    private Time departureTime;
    private double fare;
    private int availableSeats;
    private int capacity;
    private String busType;
    
    public Schedule(int scheduleId, String pickupPoint, String arrivalPoint, 
                   Date travelDate, Time departureTime, double fare, 
                   int availableSeats, int capacity, String busType) {
        this.scheduleId = scheduleId;
        this.pickupPoint = pickupPoint;
        this.arrivalPoint = arrivalPoint;
        this.travelDate = travelDate;
        this.departureTime = departureTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
        this.capacity = capacity;
        this.busType = busType;
    }
    
    // Getters and setters
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    
    public String getPickupPoint() { return pickupPoint; }
    public void setPickupPoint(String pickupPoint) { this.pickupPoint = pickupPoint; }
    
    public String getArrivalPoint() { return arrivalPoint; }
    public void setArrivalPoint(String arrivalPoint) { this.arrivalPoint = arrivalPoint; }
    
    public Date getTravelDate() { return travelDate; }
    public void setTravelDate(Date travelDate) { this.travelDate = travelDate; }
    
    public Time getDepartureTime() { return departureTime; }
    public void setDepartureTime(Time departureTime) { this.departureTime = departureTime; }
    
    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }
    
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }
}