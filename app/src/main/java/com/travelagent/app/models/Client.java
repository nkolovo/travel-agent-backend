package com.travelagent.app.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonManagedReference("client-itinerary")
    private List<Itinerary> itineraries;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonManagedReference("client-traveler")
    private List<Traveler> travelers;

    public Client() {
    }

    public Client(String name) {
        this.name = name;
    }
}
