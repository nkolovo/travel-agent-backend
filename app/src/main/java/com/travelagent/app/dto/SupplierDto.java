package com.travelagent.app.dto;

public class SupplierDto {
    private Long id;
    private String name;
    private String contact;
    private String url;
    private boolean deleted;

    public SupplierDto() {
    }

    public SupplierDto(Long id, String name, String contact, String url, boolean deleted) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.url = url;
        this.deleted = deleted;
    }

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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
