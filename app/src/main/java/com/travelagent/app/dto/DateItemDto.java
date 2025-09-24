package com.travelagent.app.dto;

public class DateItemDto {
    private Long id;
    private DateDto date;
    private ItemDto item;
    private String country;
    private String location;
    private String category;
    private String name;
    private String description;
    private String imageName;
    private String imageUrl;
    private Short priority;

    public DateItemDto() {
    }

    public DateItemDto(Long id, DateDto date,
            ItemDto item, String country,
            String location, String category,
            String name, String description,
            String imageName, String imageUrl, Short priority) {
        this.id = id;
        this.date = date;
        this.item = item;
        this.country = country;
        this.location = location;
        this.category = category;
        this.name = name;
        this.description = description;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.priority = priority;
    }

    // Getters and setters...
    public Long getId() {
        return id;
    }

    public DateDto getDate() {
        return date;
    }

    public void setDate(DateDto date) {
        this.date = date;
    }

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
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

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageName() {
        return imageName;
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

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }
}