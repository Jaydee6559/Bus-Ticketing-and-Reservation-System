package model;

public class Payment {
    private int paymentId;
    private int bookingId;
    private String paymentDate;
    private String paymentMethod;
    private String paymentStatus;

    // Constructors
    public Payment() {}

    public Payment(int bookingId, String paymentMethod) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "completed";
    }

    // Getters and Setters
    public int getPaymentId() { 
        return paymentId; 
    }
    public void setPaymentId(int paymentId) { 
        this.paymentId = paymentId; 
    }
    
    public int getBookingId() { 
        return bookingId; 
    }
    public void setBookingId(int bookingId) { 
        this.bookingId = bookingId; 
    }
    
    public String getPaymentDate() { 
        return paymentDate; 
    }
    public void setPaymentDate(String paymentDate) { 
        this.paymentDate = paymentDate; 
    }
    
    public String getPaymentMethod() { 
        return paymentMethod; 
    }
    public void setPaymentMethod(String paymentMethod) { 
        this.paymentMethod = paymentMethod; 
    }
    
    public String getPaymentStatus() { 
        return paymentStatus; 
    }
    public void setPaymentStatus(String paymentStatus) { 
        this.paymentStatus = paymentStatus; 
    }
}