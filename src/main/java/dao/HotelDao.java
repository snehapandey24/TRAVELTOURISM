package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Hotel;
import utilities.DBConnection;

public class HotelDao {

    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotels ORDER BY hotel_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Hotel h = new Hotel();
                h.setHotelId(rs.getInt("hotel_id"));
                h.setHotelName(rs.getString("hotel_name"));
                h.setLocation(rs.getString("location"));
                h.setPricePerNight(rs.getDouble("price_per_night"));
                h.setDescription(rs.getString("description"));
                hotels.add(h);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hotels;
    }

    public boolean addHotel(Hotel h) {
        String sql = "INSERT INTO hotels (hotel_name, location, price_per_night, description) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, h.getHotelName());
            ps.setString(2, h.getLocation());
            ps.setDouble(3, h.getPricePerNight());
            ps.setString(4, h.getDescription());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateHotel(Hotel h) {
        String sql = "UPDATE hotels SET hotel_name = ?, location = ?, price_per_night = ?, description = ? WHERE hotel_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, h.getHotelName());
            ps.setString(2, h.getLocation());
            ps.setDouble(3, h.getPricePerNight());
            ps.setString(4, h.getDescription());
            ps.setInt(5, h.getHotelId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Returns 1 = deleted, 0 = hotel not found, -1 = hotel is in use, -2 = error
    public int deleteHotel(int hotelId) {
        String sql = "DELETE FROM hotels WHERE hotel_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, hotelId);
            return ps.executeUpdate() > 0 ? 1 : 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }
}