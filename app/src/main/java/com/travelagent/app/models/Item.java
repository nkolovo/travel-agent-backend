package com.travelagent.app.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

//import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String country;
    private String location;
    private String category;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageName;

    @OneToMany(mappedBy = "item")
    @JsonBackReference
    private Set<DateItem> dateItems = new HashSet<>();

    public Item() {
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

    public Set<DateItem> getDateItems() {
        return dateItems;
    }

    public void setDateItems(Set<DateItem> dateItems) {
        this.dateItems = dateItems;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
