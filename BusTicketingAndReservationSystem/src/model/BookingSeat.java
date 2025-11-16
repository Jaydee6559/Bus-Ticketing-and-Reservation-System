package model;

public class BookingSeat {
    private int bookingSeatId;
    private int bookingId;
    private int seatId;
    private String seatNumber;
    private String seatStatus;

    public BookingSeat() {}

    public BookingSeat(int bookingId, String seatNumber) {
        this.bookingId = bookingId;
        this.seatNumber = seatNumber;
        this.seatStatus = "occupied";
    }

    public BookingSeat(int bookingId, int seatId, String seatNumber) {
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.seatStatus = "occupied";
    }

    public int getBookingSeatId() { 
        return bookingSeatId; 
    }
    public void setBookingSeatId(int bookingSeatId) { 
        this.bookingSeatId = bookingSeatId; 
    }
    
    public int getBookingId() { 
        return bookingId; 
    }
    public void setBookingId(int bookingId) { 
        this.bookingId = bookingId; 
    }
    
    public int getSeatId() { 
        return seatId; 
    }
    public void setSeatId(int seatId) { 
        this.seatId = seatId; 
    }
    
    public String getSeatNumber() { 
        return seatNumber; 
    }
    public void setSeatNumber(String seatNumber) { 
        this.seatNumber = seatNumber; 
    }
    
    public String getSeatStatus() { 
        return seatStatus; 
    }
    public void setSeatStatus(String seatStatus) { 
        this.seatStatus = seatStatus; 
    }
}