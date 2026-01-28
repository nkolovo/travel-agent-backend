package com.travelagent.app.models;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private Date date;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private Item item;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("supplier_contact")
    private String supplierContact;

    @JsonProperty("supplier_url")
    private String supplierUrl;

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

    @JsonProperty("retail_price")
    private int retailPrice;

    @JsonProperty("net_price")
    private int netPrice;

    @JsonProperty("image_name")
    private Set<String> imageNames;

    @Column(name = "priority")
    private Short priority;

    // Constructors
    public DateItem() {
    }

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

    public Set<String> getImageNames() {
        return imageNames;
    }

    public void setImageNames(Set<String> imageNames) {
        this.imageNames = imageNames;
    }

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierContact() {
        return supplierContact;
    }

    public void setSupplierContact(String supplierContact) {
        this.supplierContact = supplierContact;
    }

    public String getSupplierUrl() {
        return supplierUrl;
    }

    public void setSupplierUrl(String supplierUrl) {
        this.supplierUrl = supplierUrl;
    }
}
