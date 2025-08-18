package com.travelagent.app.controllers;

import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.Item;
import com.travelagent.app.services.ItemService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<Item> getAllItems() {
        return itemService.getAllItems();
    }

    @PostMapping("/save")
    public void saveItem(@RequestBody ItemDto item) { 
        itemService.saveItem(item);
    }

    @PostMapping("/remove/{id}")
    public void removeItem(@PathVariable Long id) {
        itemService.removeItem(id);
    }
}
