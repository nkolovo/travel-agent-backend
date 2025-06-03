package com.travelagent.app.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "dates")
public class Date {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String date;

    @ManyToOne
    @JoinColumn(name = "itinerary_id", nullable = false)
    @JsonBackReference("itinerary-date")
    private Itinerary itinerary;

    @ManyToMany
    @JoinTable(name = "date_items", // Name of the join table
            joinColumns = @JoinColumn(name = "date_id"), // Foreign key for Date
            inverseJoinColumns = @JoinColumn(name = "item_id") // Foreign key for Item
    )
    @JsonManagedReference("date-item")
    private Set<Item> items = new HashSet<>(); // Many-to-Many relationship

    // Constructors
    public Date() {
    }

    // Getters and Setters
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

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }
}
