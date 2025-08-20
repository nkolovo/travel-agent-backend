package com.travelagent.app.dto;

public class ItemDto {
    private Long id;
    private String country;
    private String location;
    private String category;
    private String name;
    private String description;

    public ItemDto() {
    }

    public ItemDto(Long id, String country,
            String location, String category,
            String name, String description) {
        this.id = id;
        this.country = country;
        this.location = location;
        this.category = category;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
