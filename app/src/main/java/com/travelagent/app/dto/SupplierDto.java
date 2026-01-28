package com.travelagent.app.dto;

public class SupplierDto {
    private Long id;
    private String company;
    private String name;
    private String number;
    private String email;
    private String url;
    private boolean deleted;

    public SupplierDto() {
    }

    public SupplierDto(Long id, String company, String name, String number, String email, String url, boolean deleted) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.number = number;
        this.email = email;
        this.url = url;
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
