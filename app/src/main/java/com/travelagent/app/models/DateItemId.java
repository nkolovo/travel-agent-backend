package com.travelagent.app.models;

import java.io.Serializable;
import jakarta.persistence.Embeddable;

@Embeddable
public class DateItemId implements Serializable {
    private Long dateId;
    private Long itemId;

    // Default constructor
    public DateItemId() {}

    public DateItemId(Long dateId, Long itemId) {
        this.dateId = dateId;
        this.itemId = itemId;
    }

    // Getters and setters
    public Long getDateId() { return dateId; }
    public void setDateId(Long dateId) { this.dateId = dateId; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    // equals and hashCode (required for composite keys)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateItemId)) return false;
        DateItemId that = (DateItemId) o;
        return dateId.equals(that.dateId) && itemId.equals(that.itemId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dateId, itemId);
    }
}