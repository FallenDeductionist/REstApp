package com.fallendeductionist.evaluacion4;

public class Solicitude {

    private int id;
    private String title;
    private String email;
    private String navigation;
    private String image;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNavigation() {
        return navigation;
    }

    public void setNavigation(String navigation) {
        this.navigation = navigation;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Solicitude{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", email='" + email + '\'' +
                ", image='" + image + '\'' +
                ", navigation='" + navigation + '\'' +
                '}';
    }



}
