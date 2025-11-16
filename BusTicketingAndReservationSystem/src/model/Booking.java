package model;

import java.sql.Timestamp;

public class Booking {
    private int bookingId;
    private int userId;
    private int scheduleId;
    private int passengerNum;
    private Timestamp bookingDate;
    private String status;
    private double totalFare;

    public Booking() {}

    public Booking(int userId, int scheduleId, int passengerNum, double totalFare) {
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.passengerNum = passengerNum;
        this.totalFare = totalFare;
        this.status = "confirmed";
    }

    public int getBookingId() { 
        return bookingId; 
    }
    public void setBookingId(int bookingId) { 
        this.bookingId = bookingId; 
    }
    
    public int getUserId() { 
        return userId; 
    }
    public void setUserId(int userId) { 
        this.userId = userId; 
    }
    
    public int getScheduleId() { 
        return scheduleId; 
    }
    public void setScheduleId(int scheduleId) { 
        this.scheduleId = scheduleId; 
    }
    
    public int getPassengerNum() { 
        return passengerNum; 
    }
    public void setPassengerNum(int passengerNum) { 
        this.passengerNum = passengerNum; 
    }
    
    public Timestamp getBookingDate() { 
        return bookingDate; 
    }
    public void setBookingDate(Timestamp bookingDate) { 
        this.bookingDate = bookingDate; 
    }
    
    public String getStatus() { 
        return status; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public double getTotalFare() { 
        return totalFare; 
    }
    public void setTotalFare(double totalFare) { 
        this.totalFare = totalFare; 
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", userId=" + userId +
                ", scheduleId=" + scheduleId +
                ", passengerNum=" + passengerNum +
                ", bookingDate=" + bookingDate +
                ", status='" + status + '\'' +
                ", totalFare=" + totalFare +
                '}';
    }
}