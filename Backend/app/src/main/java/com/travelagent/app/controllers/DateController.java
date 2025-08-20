package com.travelagent.app.controllers;

import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.models.DateItem;
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

    @GetMapping("/items/{dateId}")
    public List<DateItemDto> getItemsForDate(@PathVariable Long dateId) {
        return dateService.getItemsForDate(dateId);
    }

    @PostMapping("/add/{dateId}/item/{itemId}/priority/{priority}")
    public Date addItemToDate(@PathVariable Long dateId, @PathVariable Long itemId, @PathVariable Short priority) {
        return dateService.addItemToDate(dateId, itemId, priority);
    }

    @PostMapping("/remove/{dateId}/item/{itemId}")
    public void removeItemFromDate(@PathVariable Long dateId, @PathVariable Long itemId) {
        dateService.removeItemFromDate(dateId, itemId);
    }

    @PostMapping("saveDateItem/{dateId}/item/{itemId}")
    public DateItem saveDateItemToDate(@PathVariable Long dateId, @PathVariable Long itemId,
            @RequestBody DateItemDto dateItem) {
        return dateService.saveDateItemToDate(dateId, itemId, dateItem);
    }
}
