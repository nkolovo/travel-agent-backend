package com.travelagent.app.dto;

public class ItemWithCustomDescriptionDto {
    private Long id;
    private String location;
    private String category;
    private String name;
    private String description; // This will be the custom description if present

    public ItemWithCustomDescriptionDto() {
    }

    public ItemWithCustomDescriptionDto(Long id, String location, String category, String name, String description) {
        this.id = id;
        this.location = location;
        this.category = category;
        this.name = name;
        this.description = description;
    }

    // Getters and setters...
    public Long getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}