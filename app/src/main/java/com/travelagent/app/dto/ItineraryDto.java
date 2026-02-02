package com.travelagent.app.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ItineraryDto {
    private Long id;

    private String name;
    private String agent;
    private LocalDate createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editedDate;
    private LocalDate dateSold;
    private String reservationNumber;
    private String leadName;
    private int numTravelers;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private int tripPrice;
    private int netPrice;
    private String status;
    private boolean docsSent;
    private List<DateDto> dates;
    private String imageName;
    private String coverImageUrl;
    private String notes;

    public ItineraryDto() {
    }

    public ItineraryDto(Long id, String agent, LocalDate createdDate, LocalDateTime editedDate, LocalDate dateSold,
            String reservationNumber, String leadName, int numTravelers, LocalDate arrivalDate,
            LocalDate departureDate, int tripPrice, int netPrice, String status, boolean docsSent, String notes) {
        this.id = id;
        this.agent = agent;
        this.createdDate = createdDate;
        this.editedDate = editedDate;
        this.dateSold = dateSold;
        this.reservationNumber = reservationNumber;
        this.leadName = leadName;
        this.numTravelers = numTravelers;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.tripPrice = tripPrice;
        this.netPrice = netPrice;
        this.status = status;
        this.docsSent = docsSent;
        this.notes = notes;
    }

    public ItineraryDto(Long id, String name, String agent, LocalDate createdDate, LocalDateTime editedDate,
            LocalDate dateSold, String reservationNumber, String leadName, int numTravelers, LocalDate arrivalDate,
            LocalDate departureDate, int tripPrice, int netPrice, String status, boolean docsSent, String imageName,
            String notes) {
        this.id = id;
        this.name = name;
        this.agent = agent;
        this.createdDate = createdDate;
        this.editedDate = editedDate;
        this.dateSold = dateSold;
        this.reservationNumber = reservationNumber;
        this.leadName = leadName;
        this.numTravelers = numTravelers;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.tripPrice = tripPrice;
        this.netPrice = netPrice;
        this.status = status;
        this.docsSent = docsSent;
        this.imageName = imageName;
        this.notes = notes;
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

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getEditedDate() {
        return editedDate;
    }

    public void setEditedDate(LocalDateTime editedDate) {
        this.editedDate = editedDate;
    }

    public LocalDate getDateSold() {
        return dateSold;
    }

    public void setDateSold(LocalDate dateSold) {
        this.dateSold = dateSold;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(String reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public String getLeadName() {
        return leadName;
    }

    public void setLeadName(String leadName) {
        this.leadName = leadName;
    }

    public int getNumTravelers() {
        return numTravelers;
    }

    public void setNumTravelers(int numTravelers) {
        this.numTravelers = numTravelers;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public int getTripPrice() {
        return tripPrice;
    }

    public void setTripPrice(int tripPrice) {
        this.tripPrice = tripPrice;
    }

    public int getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(int netPrice) {
        this.netPrice = netPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDocsSent() {
        return docsSent;
    }

    public void setDocsSent(boolean docsSent) {
        this.docsSent = docsSent;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getClientName() {
        return leadName;
    }

    public void setClientName(String clientName) {
        this.leadName = clientName;
    }

    public List<DateDto> getDates() {
        return dates;
    }

    public void setDates(List<DateDto> dates) {
        this.dates = dates;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
