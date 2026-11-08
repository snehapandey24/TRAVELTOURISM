package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.TourHotelDao;
import model.TourHotelInfo;

@WebServlet("/tour-hotels")
public class TourHotelsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TourHotelDao linkDao;

    @Override
    public void init() throws ServletException {
        linkDao = new TourHotelDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int tourId;
        try {
            tourId = Integer.parseInt(request.getParameter("tourId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<TourHotelInfo> hotels = linkDao.getHotelsForTour(tourId);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < hotels.size(); i++) {
            TourHotelInfo h = hotels.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"hotelId\":").append(h.getHotelId()).append(",")
                .append("\"hotelName\":\"").append(escape(h.getHotelName())).append("\",")
                .append("\"allotment\":").append(h.getAllotment())
                .append("}");
        }
        json.append("]");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json.toString());
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