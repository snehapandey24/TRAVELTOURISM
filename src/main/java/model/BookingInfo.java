package model;

import java.sql.Date;

public class BookingInfo {

    private int bookingId;
    private String destination;
    private Date travelDate;
    private int numberOfPeople;
    private String roomType;
    private String hotels;
    private Date bookDate;

    public BookingInfo() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Date getTravelDate() { return travelDate; }
    public void setTravelDate(Date travelDate) { this.travelDate = travelDate; }

    public int getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getHotels() { return hotels; }
    public void setHotels(String hotels) { this.hotels = hotels; }

    public Date getBookDate() { return bookDate; }
    public void setBookDate(Date bookDate) { this.bookDate = bookDate; }

    // Turns this booking into a JSON object for the web page
    public String toJson() {
        return "{"
            + "\"bookingId\":" + bookingId
            + ",\"destination\":\"" + esc(destination) + "\""
            + ",\"travelDate\":\"" + (travelDate == null ? "" : travelDate.toString()) + "\""
            + ",\"numberOfPeople\":" + numberOfPeople
            + ",\"roomType\":\"" + esc(roomType) + "\""
            + ",\"hotels\":\"" + esc(hotels) + "\""
            + ",\"bookDate\":\"" + (bookDate == null ? "" : bookDate.toString()) + "\""
            + "}";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}