package controller;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.BookingDao;
import model.Booking;

@WebServlet("/book")
public class BookingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private BookingDao bookingDao;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            reply(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            int tourId = Integer.parseInt(request.getParameter("tourId"));
            LocalDate travelDate = LocalDate.parse(request.getParameter("travelDate"));
            int numberOfPeople = Integer.parseInt(request.getParameter("numberOfPeople"));

            if (travelDate.isBefore(LocalDate.now())) {
                reply(response, HttpServletResponse.SC_BAD_REQUEST, "Travel date cannot be in the past.");
                return;
            }
            if (numberOfPeople < 1) {
                reply(response, HttpServletResponse.SC_BAD_REQUEST, "Number of people must be at least 1.");
                return;
            }

            String roomType = request.getParameter("roomType");
            if (!"AC".equals(roomType) && !"NONAC".equals(roomType)) {
                roomType = "NONAC";
            }

            // Up to three hotels; the same hotel chosen twice counts once
            Set<Integer> hotelIds = new LinkedHashSet<>();
            for (int i = 1; i <= 3; i++) {
                String value = request.getParameter("hotelId" + i);
                if (value != null && !value.trim().isEmpty()) {
                    hotelIds.add(Integer.parseInt(value.trim()));
                }
            }

            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setTourId(tourId);
            booking.setTravelDate(Date.valueOf(travelDate));
            booking.setNumberOfPeople(numberOfPeople);
            booking.setRoomType(roomType);
            booking.setHotelIds(new ArrayList<>(hotelIds));

            int result = bookingDao.addBooking(booking);

            if (result > 0) {
                reply(response, HttpServletResponse.SC_OK,
                        "Booking confirmed! Your Booking ID is " + result + ".");
            } else if (result == -1) {
                reply(response, HttpServletResponse.SC_CONFLICT,
                        "A hotel you chose is not available for this tour or has no rooms left.");
            } else {
                reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Something went wrong with your booking. Please try again.");
            }

        } catch (NumberFormatException | DateTimeParseException | NullPointerException e) {
            reply(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Please check the tour, travel date and number of people.");
        } catch (Exception e) {
            e.printStackTrace();
            reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something went wrong with your booking. Please try again.");
        }
    }

    private void reply(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(message);
    }
}