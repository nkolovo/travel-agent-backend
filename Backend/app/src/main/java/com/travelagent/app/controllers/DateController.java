package com.travelagent.app.controllers;

import com.travelagent.app.models.Date;
import com.travelagent.app.services.DateService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dates")
public class DateController {

    private final DateService dateService;

    public DateController(DateService dateService) {
        this.dateService = dateService;
    }

    @GetMapping("/{id}")
    public List<Date> getAllDatesForItinerary(Long itineraryId) {
        return dateService.getDatesForItinerary(itineraryId);
    }

    @PostMapping("/add/{dateId}/item/{itemId}")
    public Date addItemToDate(@PathVariable Long dateId, @PathVariable Long itemId) {
        return dateService.addItemToDate(dateId, itemId);
    }

    @PostMapping("/remove/{dateId}/item/{itemId}")
    public Date removeItemFromDate(@PathVariable Long dateId, @PathVariable Long itemId) {
        return dateService.removeItemFromDate(dateId, itemId);
    }
}
