package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.Booking;
import model.BookingInfo;
import utilities.DBConnection;

public class BookingDao {

    private static final String SELECT_INFO =
            "SELECT b.booking_id, t.destination, b.travel_date, b.number_of_people, b.room_type, b.book_date, "
          + "GROUP_CONCAT(h.hotel_name ORDER BY h.hotel_name SEPARATOR ', ') AS hotels "
          + "FROM bookings b "
          + "JOIN tours t ON b.tour_id = t.tour_id "
          + "LEFT JOIN booking_hotels bh ON bh.booking_id = b.booking_id "
          + "LEFT JOIN hotels h ON h.hotel_id = bh.hotel_id ";

    private static final String GROUP_INFO =
            "GROUP BY b.booking_id, t.destination, b.travel_date, b.number_of_people, b.room_type, b.book_date ";

    // Saves the booking and takes one room from each chosen hotel, all or nothing.
    // Returns the new booking ID (> 0), -1 = a hotel is not available for this tour, -2 = error
    public int addBooking(Booking booking) {
        String insertBooking = "INSERT INTO bookings (user_id, tour_id, travel_date, number_of_people, room_type, book_date) "
                             + "VALUES (?, ?, ?, ?, ?, CURDATE())";
        String takeRoom = "UPDATE tour_hotel SET allotment = allotment - 1 "
                        + "WHERE tour_id = ? AND hotel_id = ? AND allotment > 0";
        String insertHotel = "INSERT INTO booking_hotels (booking_id, hotel_id) VALUES (?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            for (int hotelId : booking.getHotelIds()) {
                try (PreparedStatement ps = con.prepareStatement(takeRoom)) {
                    ps.setInt(1, booking.getTourId());
                    ps.setInt(2, hotelId);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return -1;
                    }
                }
            }

            int bookingId = -1;
            try (PreparedStatement ps = con.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, booking.getUserId());
                ps.setInt(2, booking.getTourId());
                ps.setDate(3, booking.getTravelDate());
                ps.setInt(4, booking.getNumberOfPeople());
                ps.setString(5, booking.getRoomType());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return -2;
                    }
                    bookingId = keys.getInt(1);
                }
            }

            for (int hotelId : booking.getHotelIds()) {
                try (PreparedStatement ps = con.prepareStatement(insertHotel)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, hotelId);
                    ps.executeUpdate();
                }
            }

            con.commit();
            return bookingId;

        } catch (SQLException e) {
            e.printStackTrace();
            rollbackQuietly(con);
            return -2;
        } finally {
            closeQuietly(con);
        }
    }

    public List<BookingInfo> getBookingsByUser(int userId) {
        List<BookingInfo> list = new ArrayList<>();
        String sql = SELECT_INFO + "WHERE b.user_id = ? " + GROUP_INFO + "ORDER BY b.travel_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInfo(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Finds one booking by its ID. Admins can see any booking; other users only their own.
    public BookingInfo getBookingById(int bookingId, int userId, boolean isAdmin) {
        String sql = SELECT_INFO
                   + "WHERE b.booking_id = ? " + (isAdmin ? "" : "AND b.user_id = ? ")
                   + GROUP_INFO;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            if (!isAdmin) {
                ps.setInt(2, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapInfo(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Cancels one of the user's own bookings and gives the rooms back.
    // Returns 1 = cancelled, 0 = not found, -1 = trip date already passed, -2 = error
    public int cancelBooking(int bookingId, int userId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int tourId = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT tour_id, travel_date FROM bookings WHERE booking_id = ? AND user_id = ?")) {
                ps.setInt(1, bookingId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return 0;
                    }
                    tourId = rs.getInt("tour_id");
                    if (rs.getDate("travel_date").toLocalDate().isBefore(LocalDate.now())) {
                        con.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE tour_hotel SET allotment = allotment + 1 WHERE tour_id = ? "
                  + "AND hotel_id IN (SELECT hotel_id FROM booking_hotels WHERE booking_id = ?)")) {
                ps.setInt(1, tourId);
                ps.setInt(2, bookingId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM bookings WHERE booking_id = ?")) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            con.commit();
            return 1;

        } catch (SQLException e) {
            e.printStackTrace();
            rollbackQuietly(con);
            return -2;
        } finally {
            closeQuietly(con);
        }
    }

    private BookingInfo mapInfo(ResultSet rs) throws SQLException {
        BookingInfo info = new BookingInfo();
        info.setBookingId(rs.getInt("booking_id"));
        info.setDestination(rs.getString("destination"));
        info.setTravelDate(rs.getDate("travel_date"));
        info.setNumberOfPeople(rs.getInt("number_of_people"));
        info.setRoomType(rs.getString("room_type"));
        info.setBookDate(rs.getDate("book_date"));
        String hotels = rs.getString("hotels");
        info.setHotels(hotels == null ? "-" : hotels);
        return info;
    }

    private void rollbackQuietly(Connection con) {
        if (con != null) {
            try { con.rollback(); } catch (SQLException ignored) { }
        }
    }

    private void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignored) { }
        }
    }
}