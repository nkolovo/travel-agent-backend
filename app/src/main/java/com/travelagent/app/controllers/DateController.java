package com.travelagent.app.controllers;

import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.services.DateService;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/dates")
public class DateController {

    private final DateService dateService;

    public DateController(DateService dateService) {
        this.dateService = dateService;
    }

    @GetMapping("/items/{dateId}")
    public List<DateItemDto> getItemsForDate(@PathVariable Long dateId) {
        return dateService.getItemsForDate(dateId);
    }

    @PostMapping("/add/{dateId}/item/{itemId}/priority/{priority}")
    public DateItemDto addItemToDate(@PathVariable Long dateId, @PathVariable Long itemId,
            @PathVariable Short priority) {
        return dateService.addItemToDate(dateId, itemId, priority);
    }

    @PostMapping("/saveDateItem")
    public void saveDateItemToDate(@RequestBody DateItemDto dateItem) {
        dateService.saveDateItemToDate(dateItem);
    }

    @PostMapping("/remove/activity/{dateItemId}")
    public void removeItemFromDate(@PathVariable Long dateItemId) {
        dateService.removeItemFromDate(dateItemId);
    }

    @PostMapping("/move/activity/{activityId}/to/{targetDateId}")
    public String moveActivityToDate(@PathVariable Long activityId, @PathVariable Long targetDateId) {
        System.out.println("Received request to move activity " + activityId + " to date " + targetDateId);
        dateService.moveActivityToDate(activityId, targetDateId);
        return "Activity moved successfully";
    }

}
