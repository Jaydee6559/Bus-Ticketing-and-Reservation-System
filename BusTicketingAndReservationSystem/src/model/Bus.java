package model;

public class Bus {
    private int busId;
    private String busType;
    private int capacity;
    
    public Bus(int busId, String busType, int capacity) {
        this.busId = busId;
        this.busType = busType;
        this.capacity = capacity;
    }
    
    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }
    
    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}