package com.travelagent.app.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
    private int retailPrice;
    private int netPrice;

    @ElementCollection
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "image_name")
    private Set<String> imageNames = new HashSet<>();

    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    @JsonBackReference("supplier-item")
    private Supplier supplier;

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

    public int getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(int retailPrice) {
        this.retailPrice = retailPrice;
    }

    public int getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(int netPrice) {
        this.netPrice = netPrice;
    }

    public Set<DateItem> getDateItems() {
        return dateItems;
    }

    public void setDateItems(Set<DateItem> dateItems) {
        this.dateItems = dateItems;
    }

    public Set<String> getImageNames() {
        return imageNames;
    }

    public void setImageNames(Set<String> imageNames) {
        this.imageNames = imageNames != null ? imageNames : new HashSet<>();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
}
