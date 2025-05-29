package com.travelagent.app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "custom_items")
public class CustomItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "date_id", nullable = false)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    // Constructors
    public CustomItem() {
    }

    // public CustomItem(Date date, Item item, String name, String description) {
    //     this.date = date;
    //     this.item = item;
    //     this.name = name;
    //     this.description = description;
    // }

    // Getters and setters...
    public Long getId() {
        return id;
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
}