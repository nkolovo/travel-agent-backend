package com.travelagent.app.dto;

public class DateDto {
    private Long id;
    private String name;
    private String location;
    private String date;

    public DateDto() {
    }

    public DateDto(Long id, String name, String location, String date) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
