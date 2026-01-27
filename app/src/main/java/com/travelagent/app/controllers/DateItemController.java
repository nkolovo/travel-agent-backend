package com.travelagent.app.controllers;

import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.services.DateItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/date-items")
public class DateItemController {
    private final DateItemService dateItemService;

    public DateItemController(DateItemService dateItemService) {
        this.dateItemService = dateItemService;
    }

    @GetMapping("/date/{dateId}")
    public List<DateItemDto> getDateItemsByDate(@PathVariable Long dateId) {
        return dateItemService.getDateItemsByDate(dateId);
    }
}