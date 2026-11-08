package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.TourDao;
import model.Tour;

@WebServlet("/admin/tours")
public class AdminTourServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TourDao tourDao;

    @Override
    public void init() throws ServletException {
        tourDao = new TourDao();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // Only logged-in admins allowed
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            reply(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            reply(response, HttpServletResponse.SC_FORBIDDEN, "Admins only.");
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("add".equals(action) || "update".equals(action)) {
                String destination = request.getParameter("destination");
                String duration = request.getParameter("duration");
                String priceText = request.getParameter("price");

                if (destination == null || destination.trim().isEmpty()
                        || duration == null || duration.trim().isEmpty()
                        || priceText == null || priceText.trim().isEmpty()) {
                    reply(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Destination, duration and price are required.");
                    return;
                }

                double price = Double.parseDouble(priceText);
                if (price < 0) {
                    reply(response, HttpServletResponse.SC_BAD_REQUEST, "Price cannot be negative.");
                    return;
                }

                Tour tour = new Tour();
                tour.setDestination(destination.trim());
                tour.setDuration(duration.trim());
                tour.setPrice(price);
                tour.setDescription(request.getParameter("description"));

                String imageUrl = request.getParameter("imageUrl");
                tour.setImageUrl(imageUrl == null || imageUrl.trim().isEmpty() ? null : imageUrl.trim());

                if ("add".equals(action)) {
                    if (tourDao.addTour(tour)) {
                        reply(response, HttpServletResponse.SC_OK, "Tour added.");
                    } else {
                        reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not add the tour.");
                    }
                } else {
                    tour.setTourId(Integer.parseInt(request.getParameter("tourId")));
                    if (tourDao.updateTour(tour)) {
                        reply(response, HttpServletResponse.SC_OK, "Tour updated.");
                    } else {
                        reply(response, HttpServletResponse.SC_NOT_FOUND, "Tour not found.");
                    }
                }

            } else if ("delete".equals(action)) {
                int tourId = Integer.parseInt(request.getParameter("tourId"));
                int result = tourDao.deleteTour(tourId);

                if (result == 1) {
                    reply(response, HttpServletResponse.SC_OK, "Tour deleted.");
                } else if (result == 0) {
                    reply(response, HttpServletResponse.SC_NOT_FOUND, "Tour not found.");
                } else if (result == -1) {
                    reply(response, HttpServletResponse.SC_CONFLICT,
                            "This tour has bookings, so it can't be deleted.");
                } else {
                    reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not delete the tour.");
                }

            } else {
                reply(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }

        } catch (NumberFormatException e) {
            reply(response, HttpServletResponse.SC_BAD_REQUEST, "Price and tour ID must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            reply(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong.");
        }
    }

    private void reply(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
    }
}