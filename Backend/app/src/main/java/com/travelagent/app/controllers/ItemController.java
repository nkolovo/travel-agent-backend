package com.travelagent.app.controllers;

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
    public Long AddItem(@RequestBody Item item) {
        return itemService.addItem(item);
    }

    @PostMapping("/remove/{id}")
    public String removeItem(@PathVariable Long id) {
        itemService.removeItem(id);
        return "Item successfully removed";
    }
}
