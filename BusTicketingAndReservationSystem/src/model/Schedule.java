package model;

import java.sql.Date;
import java.sql.Time;

public class Schedule {
    private int scheduleId;
    private String pickupPoint;
    private String arrivalPoint;
    private Date travelDate;
    private Time departureTime;
    private double fare;
    private int availableSeats;
    private int capacity;
    private String busType;
    private String plateNumber;
    private String status; // Add this field
    
    // Updated constructor with status
    public Schedule(int scheduleId, String pickupPoint, String arrivalPoint, 
                   Date travelDate, Time departureTime, double fare, 
                   int availableSeats, int capacity, String busType, String plateNumber, String status) {
        this.scheduleId = scheduleId;
        this.pickupPoint = pickupPoint;
        this.arrivalPoint = arrivalPoint;
        this.travelDate = travelDate;
        this.departureTime = departureTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
        this.capacity = capacity;
        this.busType = busType;
        this.plateNumber = plateNumber;
        this.status = status;
    }
    
    // Old constructor for backward compatibility (set default status)
    public Schedule(int scheduleId, String pickupPoint, String arrivalPoint, 
                   Date travelDate, Time departureTime, double fare, 
                   int availableSeats, int capacity, String busType, String plateNumber) {
        this(scheduleId, pickupPoint, arrivalPoint, travelDate, departureTime, 
             fare, availableSeats, capacity, busType, plateNumber, "ongoing");
    }

    // ======= Getters =======
    public int getScheduleId() { return scheduleId; }
    public String getPickupPoint() { return pickupPoint; }
    public String getArrivalPoint() { return arrivalPoint; }
    public Date getTravelDate() { return travelDate; }
    public Time getDepartureTime() { return departureTime; }
    public double getFare() { return fare; }
    public int getAvailableSeats() { return availableSeats; }
    public int getCapacity() { return capacity; }
    public String getBusType() { return busType; }
    public String getPlateNumber() { return plateNumber; }
    public String getStatus() { return status; }

    // ======= Setters =======
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public void setPickupPoint(String pickupPoint) { this.pickupPoint = pickupPoint; }
    public void setArrivalPoint(String arrivalPoint) { this.arrivalPoint = arrivalPoint; }
    public void setTravelDate(Date travelDate) { this.travelDate = travelDate; }
    public void setDepartureTime(Time departureTime) { this.departureTime = departureTime; }
    public void setFare(double fare) { this.fare = fare; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setBusType(String busType) { this.busType = busType; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public void setStatus(String status) { this.status = status; }
}
