package com.travelagent.app.controllers;

import com.travelagent.app.models.CustomItem;
import com.travelagent.app.services.CustomItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/custom-items")
public class CustomItemController {
    private final CustomItemService customItemService;

    public CustomItemController(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @GetMapping("/date/{dateId}")
    public List<CustomItem> getCustomItemsByDate(@PathVariable Long dateId) {
        return customItemService.getCustomItemsByDate(dateId);
    }
}