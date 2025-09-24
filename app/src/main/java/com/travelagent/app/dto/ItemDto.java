package com.travelagent.app.dto;

public class ItemDto {
    private Long id;
    private String country;
    private String location;
    private String category;
    private String name;
    private String description;
    private String imageName;
    private String imageUrl;

    public ItemDto() {
    }

    public ItemDto(Long id, String country,
            String location, String category,
            String name, String description, String imageName) {
        this.id = id;
        this.country = country;
        this.location = location;
        this.category = category;
        this.name = name;
        this.description = description;
        this.imageName = imageName;
    }

    public ItemDto(Long id, String country,
            String location, String category,
            String name, String description,
            String imageName, String imageUrl) {
        this.id = id;
        this.country = country;
        this.location = location;
        this.category = category;
        this.name = name;
        this.description = description;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
