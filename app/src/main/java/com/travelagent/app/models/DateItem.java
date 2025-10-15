package com.travelagent.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "date_items")
public class DateItem {
    @EmbeddedId
    private DateItemId id;

    @ManyToOne
    @MapsId("dateId")
    @JoinColumn(name = "date_id")
    private Date date;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonProperty("country")
    private String country;

    @JsonProperty("location")
    private String location;

    @JsonProperty("category")
    private String category;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonProperty("trip_cost")
    private int tripCost;

    @JsonProperty("net_cost")
    private int netCost;

    @JsonProperty("image_name")
    private String imageName;

    // Constructors
    public DateItem() {
    }

    @Column(name = "priority")
    private Short priority; // or Integer/Long

    // getters and setters
    public DateItemId getId() {
        return id;
    }

    public void setId(DateItemId id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
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

    public void setName(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@JsonProperty("description") String description) {
        this.description = description;
    }

    public int getTripCost() {
        return tripCost;
    }

    public void setTripCost(int tripCost) {
        this.tripCost = tripCost;
    }

    public int getNetCost() {
        return netCost;
    }

    public void setNetCost(int netCost) {
        this.netCost = netCost;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }
}
