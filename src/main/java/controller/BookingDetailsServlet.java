package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.BookingDao;
import model.BookingInfo;

@WebServlet("/booking-details")
public class BookingDetailsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private BookingDao bookingDao;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(request.getParameter("bookId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(session.getAttribute("userRole"));

        BookingInfo b = bookingDao.getBookingById(bookingId, userId, isAdmin);

        if (b == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(b.toJson());
    }
}