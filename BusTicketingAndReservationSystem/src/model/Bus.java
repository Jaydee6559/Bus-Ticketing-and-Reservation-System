package model;

public class Bus {
    private int busId;
    private String plateNumber;
    private String busType;
    private int capacity;
    private String status;
    

    public Bus() {}
    
    public Bus(int busId, String plateNumber, String busType, int capacity, String status) {
        this.busId = busId;
        this.plateNumber = plateNumber;
        this.busType = busType;
        this.capacity = capacity;
        this.status = status;
    }
    
    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }
    
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    
    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}