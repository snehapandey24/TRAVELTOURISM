package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.TourHotelDao;
import model.TourHotelInfo;

@WebServlet("/admin/links")
public class AdminLinkServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TourHotelDao linkDao;

    @Override
    public void init() throws ServletException {
        linkDao = new TourHotelDao();
    }

    // List all tour-hotel links (admins only)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) return;

        List<TourHotelInfo> links = linkDao.getAllLinks();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < links.size(); i++) {
            TourHotelInfo l = links.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"tourId\":").append(l.getTourId()).append(",")
                .append("\"destination\":\"").append(escape(l.getDestination())).append("\",")
                .append("\"hotelId\":").append(l.getHotelId()).append(",")
                .append("\"hotelName\":\"").append(escape(l.getHotelName())).append("\",")
                .append("\"allotment\":").append(l.getAllotment())
                .append("}");
        }
        json.append("]");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json.toString());
    }

    // Link or unlink (admins only)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) return;

        String action = request.getParameter("action");

        try {
            int tourId = Integer.parseInt(request.getParameter("tourId"));
            int hotelId = Integer.parseInt(request.getParameter("hotelId"));

            if ("link".equals(action)) {
                int allotment = Integer.parseInt(request.getParameter("allotment"));
                if (allotment < 0) {
                    reply(response, HttpServletResponse.SC_BAD_REQUEST, "Allotment cannot be negative.");
                    return;
                }

                int result = linkDao.link(tourId, hotelId, allotment);
                if (result == 1) {
                    reply(response, HttpServletResponse.SC_OK, "Hotel linked to the tour.");
                } else if (result == -1) {
                    reply(response, HttpServletResponse.SC_BAD_REQUEST, "That tour or hotel does not exist.");
                } else {
                    reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save the link.");
                }

            } else if ("unlink".equals(action)) {
                if (linkDao.unlink(tourId, hotelId)) {
                    reply(response, HttpServletResponse.SC_OK, "Link removed.");
                } else {
                    reply(response, HttpServletResponse.SC_NOT_FOUND, "Link not found.");
                }

            } else {
                reply(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }

        } catch (NumberFormatException e) {
            reply(response, HttpServletResponse.SC_BAD_REQUEST, "Tour, hotel and allotment must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong.");
        }
    }

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