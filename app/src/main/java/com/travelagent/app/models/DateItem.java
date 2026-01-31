package com.travelagent.app.models;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "date_items")
public class DateItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "date_id")
    @JsonIgnore
    private Date date;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private Item item;

    @JsonProperty("supplier_company")
    private String supplierCompany;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("supplier_number")
    private String supplierNumber;

    @JsonProperty("supplier_email")
    private String supplierEmail;

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

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("pdf_name")
    private String pdfName;

    @JsonProperty("image_names")
    private Set<String> imageNames;

    @Column(name = "priority")
    private Short priority;

    // Constructors
    public DateItem() {
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
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

    public String getSupplierCompany() {
        return supplierCompany;
    }

    public void setSupplierCompany(String supplierCompany) {
        this.supplierCompany = supplierCompany;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(String supplierNumber) {
        this.supplierNumber = supplierNumber;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getSupplierUrl() {
        return supplierUrl;
    }

    public void setSupplierUrl(String supplierUrl) {
        this.supplierUrl = supplierUrl;
    }
}
