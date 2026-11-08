package model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Booking {

    private int bookingId;
    private int userId;
    private int tourId;
    private Date travelDate;
    private int numberOfPeople;
    private String roomType = "NONAC";
    private List<Integer> hotelIds = new ArrayList<>();

    public Booking() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }

    public Date getTravelDate() { return travelDate; }
    public void setTravelDate(Date travelDate) { this.travelDate = travelDate; }

    public int getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public List<Integer> getHotelIds() { return hotelIds; }
    public void setHotelIds(List<Integer> hotelIds) { this.hotelIds = hotelIds; }
}