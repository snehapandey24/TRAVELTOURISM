package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.TourDao;
import model.Tour;

@WebServlet("/tours")
public class ToursServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TourDao tourDao;

    @Override
    public void init() throws ServletException {
        tourDao = new TourDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Tour> tours = tourDao.getAllTours();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tours.size(); i++) {
            Tour t = tours.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"tourId\":").append(t.getTourId()).append(",")
                .append("\"destination\":\"").append(escape(t.getDestination())).append("\",")
                .append("\"duration\":\"").append(escape(t.getDuration())).append("\",")
                .append("\"price\":").append(t.getPrice()).append(",")
                .append("\"description\":\"").append(escape(t.getDescription())).append("\",")
                .append("\"imageUrl\":\"").append(escape(t.getImageUrl())).append("\"")
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