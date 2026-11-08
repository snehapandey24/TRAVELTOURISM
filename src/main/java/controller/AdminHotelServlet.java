package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.HotelDao;
import model.Hotel;

@WebServlet("/admin/hotels")
public class AdminHotelServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private HotelDao hotelDao;

    @Override
    public void init() throws ServletException {
        hotelDao = new HotelDao();
    }

    // List all hotels (admins only)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) return;

        List<Hotel> hotels = hotelDao.getAllHotels();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < hotels.size(); i++) {
            Hotel h = hotels.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"hotelId\":").append(h.getHotelId()).append(",")
                .append("\"hotelName\":\"").append(escape(h.getHotelName())).append("\",")
                .append("\"location\":\"").append(escape(h.getLocation())).append("\",")
                .append("\"pricePerNight\":").append(h.getPricePerNight()).append(",")
                .append("\"description\":\"").append(escape(h.getDescription())).append("\"")
                .append("}");
        }
        json.append("]");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json.toString());
    }

    // Add / update / delete (admins only)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        if (!checkAdmin(request, response)) return;

        String action = request.getParameter("action");

        try {
            if ("add".equals(action) || "update".equals(action)) {
                String name = request.getParameter("hotelName");
                String location = request.getParameter("location");

                if (name == null || name.trim().isEmpty()
                        || location == null || location.trim().isEmpty()) {
                    reply(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Hotel name and place are required.");
                    return;
                }

                double price = 0;
                String priceText = request.getParameter("pricePerNight");
                if (priceText != null && !priceText.trim().isEmpty()) {
                    price = Double.parseDouble(priceText);
                    if (price < 0) {
                        reply(response, HttpServletResponse.SC_BAD_REQUEST, "Price cannot be negative.");
                        return;
                    }
                }

                Hotel hotel = new Hotel();
                hotel.setHotelName(name.trim());
                hotel.setLocation(location.trim());
                hotel.setPricePerNight(price);
                hotel.setDescription(request.getParameter("description"));

                if ("add".equals(action)) {
                    if (hotelDao.addHotel(hotel)) {
                        reply(response, HttpServletResponse.SC_OK, "Hotel added.");
                    } else {
                        reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not add the hotel.");
                    }
                } else {
                    hotel.setHotelId(Integer.parseInt(request.getParameter("hotelId")));
                    if (hotelDao.updateHotel(hotel)) {
                        reply(response, HttpServletResponse.SC_OK, "Hotel updated.");
                    } else {
                        reply(response, HttpServletResponse.SC_NOT_FOUND, "Hotel not found.");
                    }
                }

            } else if ("delete".equals(action)) {
                int hotelId = Integer.parseInt(request.getParameter("hotelId"));
                int result = hotelDao.deleteHotel(hotelId);

                if (result == 1) {
                    reply(response, HttpServletResponse.SC_OK, "Hotel deleted.");
                } else if (result == 0) {
                    reply(response, HttpServletResponse.SC_NOT_FOUND, "Hotel not found.");
                } else if (result == -1) {
                    reply(response, HttpServletResponse.SC_CONFLICT,
                            "This hotel is linked to a tour or has bookings, so it can't be deleted.");
                } else {
                    reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not delete the hotel.");
                }

            } else {
                reply(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }

        } catch (NumberFormatException e) {
            reply(response, HttpServletResponse.SC_BAD_REQUEST, "Price and hotel ID must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong.");
        }
    }

    // Returns true only for a logged-in admin; otherwise sends the error and returns false
    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            reply(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return false;
        }
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            reply(response, HttpServletResponse.SC_FORBIDDEN, "Admins only.");
            return false;
        }
        return true;
    }

    private void reply(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(message);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
