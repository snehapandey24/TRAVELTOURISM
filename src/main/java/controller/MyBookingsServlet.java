package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.BookingDao;
import model.BookingInfo;

@WebServlet("/my-bookings")
public class MyBookingsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private BookingDao bookingDao;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
    }

    // List the logged-in user's bookings
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        List<BookingInfo> bookings = bookingDao.getBookingsByUser(userId);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < bookings.size(); i++) {
            if (i > 0) json.append(",");
            json.append(bookings.get(i).toJson());
        }
        json.append("]");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json.toString());
    }

    // Cancel one of the user's own bookings
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            reply(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }

        if (!"cancel".equals(request.getParameter("action"))) {
            reply(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            return;
        }

        try {
            int bookingId = Integer.parseInt(request.getParameter("bookingId"));
            int userId = (Integer) session.getAttribute("userId");

            int result = bookingDao.cancelBooking(bookingId, userId);

            if (result == 1) {
                reply(response, HttpServletResponse.SC_OK, "Booking #" + bookingId + " has been cancelled.");
            } else if (result == 0) {
                reply(response, HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
            } else if (result == -1) {
                reply(response, HttpServletResponse.SC_CONFLICT,
                        "This trip's date has already passed, so it can't be cancelled.");
            } else {
                reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not cancel the booking.");
            }

        } catch (NumberFormatException e) {
            reply(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid booking ID.");
        }
    }

    private void reply(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(message);
    }
}