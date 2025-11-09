package model;

public class Station {
    private int stationId;
    private String stationName;
    
    public Station(int stationId, String stationName) {
        this.stationId = stationId;
        this.stationName = stationName;
    }
    
    public int getStationId() { return stationId; }
    public String getStationName() { return stationName; }
}