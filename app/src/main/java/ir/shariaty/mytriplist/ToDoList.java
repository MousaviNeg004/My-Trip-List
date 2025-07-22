package ir.shariaty.mytriplist;

public class ToDoList {
    private String documentId, title, startDate, duration, travelers, imageUrl;

    public ToDoList(String documentId, String title, String startDate, String imageUrl, String duration, String travelers) {
        this.documentId = documentId;
        this.title = title;
        this.startDate = startDate;
        this.imageUrl = imageUrl;
        this.duration = duration;
        this.travelers = travelers;
    }

    public ToDoList(String title, String startDate) {
        this.title = title;
        this.startDate = startDate;
        this.imageUrl = "";
        this.duration = "";
        this.travelers = "";
    }

    // Getter and Setter
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTravelers() {
        return travelers;
    }

    public void setTravelers(String travelers) {
        this.travelers = travelers;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
