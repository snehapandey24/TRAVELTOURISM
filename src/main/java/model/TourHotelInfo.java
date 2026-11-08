package model;

public class TourHotelInfo {

    private int tourId;
    private String destination;
    private int hotelId;
    private String hotelName;
    private int allotment;

    public TourHotelInfo() {}

    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public int getAllotment() { return allotment; }
    public void setAllotment(int allotment) { this.allotment = allotment; }
}