package com.travelagent.app.controllers;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItemWithCustomDescriptionDto;
import com.travelagent.app.models.CustomItem;
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
    public List<ItemWithCustomDescriptionDto> getItemsForDate(@PathVariable Long dateId) {
        return dateService.getItemsForDate(dateId);
    }

    @PostMapping("/add/{dateId}/item/{itemId}")
    public Date addItemToDate(@PathVariable Long dateId, @PathVariable Long itemId) {
        return dateService.addItemToDate(dateId, itemId);
    }

    @PostMapping("/remove/{dateId}/item/{itemId}")
    public void removeItemFromDate(@PathVariable Long dateId, @PathVariable Long itemId) {
        dateService.removeItemFromDate(dateId, itemId);
    }

    @PostMapping("saveCustomItem/{dateId}/item/{itemId}")
    public CustomItem saveCustomItemToDate(@PathVariable Long dateId, @PathVariable Long itemId,
            @RequestBody ItemWithCustomDescriptionDto customItem) {
       return dateService.saveCustomItemToDate(dateId, itemId, customItem);
    }
}
