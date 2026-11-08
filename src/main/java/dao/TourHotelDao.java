package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.TourHotelInfo;
import utilities.DBConnection;

public class TourHotelDao {

    private static final String BASE =
            "SELECT th.tour_id, t.destination, th.hotel_id, h.hotel_name, th.allotment "
          + "FROM tour_hotel th "
          + "JOIN tours t ON th.tour_id = t.tour_id "
          + "JOIN hotels h ON th.hotel_id = h.hotel_id ";

    public List<TourHotelInfo> getAllLinks() {
        List<TourHotelInfo> list = new ArrayList<>();
        String sql = BASE + "ORDER BY t.destination, h.hotel_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Hotels of one tour that still have rooms (used on the customer side)
    public List<TourHotelInfo> getHotelsForTour(int tourId) {
        List<TourHotelInfo> list = new ArrayList<>();
        String sql = BASE + "WHERE th.tour_id = ? AND th.allotment > 0 ORDER BY h.hotel_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tourId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Links a hotel to a tour. If the link already exists, only the allotment is updated.
    // Returns 1 = saved, -1 = tour or hotel doesn't exist, -2 = error
    public int link(int tourId, int hotelId, int allotment) {
        String sql = "INSERT INTO tour_hotel (tour_id, hotel_id, allotment) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE allotment = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tourId);
            ps.setInt(2, hotelId);
            ps.setInt(3, allotment);
            ps.setInt(4, allotment);
            ps.executeUpdate();
            return 1;

        } catch (SQLIntegrityConstraintViolationException e) {
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }

    public boolean unlink(int tourId, int hotelId) {
        String sql = "DELETE FROM tour_hotel WHERE tour_id = ? AND hotel_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tourId);
            ps.setInt(2, hotelId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private TourHotelInfo map(ResultSet rs) throws SQLException {
        TourHotelInfo info = new TourHotelInfo();
        info.setTourId(rs.getInt("tour_id"));
        info.setDestination(rs.getString("destination"));
        info.setHotelId(rs.getInt("hotel_id"));
        info.setHotelName(rs.getString("hotel_name"));
        info.setAllotment(rs.getInt("allotment"));
        return info;
    }
}