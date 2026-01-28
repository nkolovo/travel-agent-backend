package com.travelagent.app.dto;

import java.util.HashSet;
import java.util.Set;

public class ItemDto {
    private Long id;
    private String country;
    private String location;
    private String category;
    private String name;
    private String description;
    private int retailPrice;
    private int netPrice;
    private Set<String> imageNames = new HashSet<>();
    private String supplierName;
    private String supplierContact;
    private String supplierUrl;

    public ItemDto() {
    }

    public ItemDto(Long id, String country,
            String location, String category,
            String name, String description,
            int retailPrice, int netPrice,
            Set<String> imageNames) {
        this.id = id;
        this.country = country;
        this.location = location;
        this.category = category;
        this.name = name;
        this.description = description;
        this.retailPrice = retailPrice;
        this.netPrice = netPrice;
        this.imageNames = imageNames != null ? imageNames : new HashSet<>();
    }

    public ItemDto(Long id, String country,
            String location, String category,
            String name, String description,
            int retailPrice, int netPrice,
            Set<String> imageNames, String supplierName,
            String supplierContact, String supplierUrl) {
        this.id = id;
        this.country = country;
        this.location = location;
        this.category = category;
        this.name = name;
        this.description = description;
        this.retailPrice = retailPrice;
        this.netPrice = netPrice;
        this.imageNames = imageNames != null ? imageNames : new HashSet<>();
        this.supplierName = supplierName;
        this.supplierContact = supplierContact;
        this.supplierUrl = supplierUrl;
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

    public Set<String> getImageNames() {
        return imageNames;
    }

    public void setImageNames(Set<String> imageNames) {
        this.imageNames = imageNames != null ? imageNames : new HashSet<>();
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
