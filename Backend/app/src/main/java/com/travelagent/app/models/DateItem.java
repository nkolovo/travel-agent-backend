package com.travelagent.app.models;

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
    private Date date;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "priority")
    private Short priority; // or Integer/Long

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

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }
}
