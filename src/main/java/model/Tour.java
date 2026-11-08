package model;

public class Tour {

    private int tourId;
    private String destination;
    private String duration;
    private double price;
    private String description;
    private String imageUrl;

    public Tour() {}

    public Tour(int tourId, String destination, String duration, double price, String description, String imageUrl) {
        this.tourId = tourId;
        this.destination = destination;
        this.duration = duration;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}