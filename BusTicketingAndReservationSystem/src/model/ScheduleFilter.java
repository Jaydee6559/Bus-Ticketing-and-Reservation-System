package model;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFilter {
    private String busType;
    private List<String> stations;
    private String searchQuery;
    
    public ScheduleFilter() {
        this.stations = new ArrayList<>();
    }
    
    public void setBusType(String busType) { this.busType = busType; }
    public void setStations(List<String> stations) { this.stations = stations; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    
    public String getBusType() { return busType; }
    public List<String> getStations() { return stations; }
    public String getSearchQuery() { return searchQuery; }
    
    public void reset() {
        this.busType = null;
        this.stations.clear();
        this.searchQuery = null;
    }
}