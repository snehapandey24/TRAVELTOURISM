package dao;

import java.sql.*;
import java.util.*;
import model.Tour;
import utilities.DBConnection;

public class TourDao {

    public List<Tour> getAllTours() {
        List<Tour> tours = new ArrayList<>();
        String sql = "SELECT * FROM tours ORDER BY tour_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tours.add(mapTour(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tours;
    }

    public Tour getTourById(int tourId) {
        String sql = "SELECT * FROM tours WHERE tour_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tourId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTour(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addTour(Tour t) {
        String sql = "INSERT INTO tours (destination, duration, price, description, image_url) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getDestination());
            ps.setString(2, t.getDuration());
            ps.setDouble(3, t.getPrice());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getImageUrl());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTour(Tour t) {
        String sql = "UPDATE tours SET destination = ?, duration = ?, price = ?, description = ?, image_url = ? WHERE tour_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getDestination());
            ps.setString(2, t.getDuration());
            ps.setDouble(3, t.getPrice());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getImageUrl());
            ps.setInt(6, t.getTourId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Returns 1 = deleted, 0 = tour not found, -1 = tour is in use (has bookings), -2 = error
    public int deleteTour(int tourId) {
        String sql = "DELETE FROM tours WHERE tour_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tourId);
            return ps.executeUpdate() > 0 ? 1 : 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }

    private Tour mapTour(ResultSet rs) throws SQLException {
        Tour t = new Tour();
        t.setTourId(rs.getInt("tour_id"));
        t.setDestination(rs.getString("destination"));
        t.setDuration(rs.getString("duration"));
        t.setPrice(rs.getDouble("price"));
        t.setDescription(rs.getString("description"));
        t.setImageUrl(rs.getString("image_url"));
        return t;
    }
}