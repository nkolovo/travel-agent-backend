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
    public Long SaveItem(@RequestBody Item item) { 
        System.out.println("Saving item: " + item.getId());
        System.out.println("Saving item: " + item.getLocation());
        System.out.println("Saving item: " + item.getCategory());
        System.out.println("Saving item: " + item.getName());
        System.out.println("Saving item: " + item.getDescription());
        return itemService.saveItem(item);
    }

    @PostMapping("/remove/{id}")
    public void removeItem(@PathVariable Long id) {
        itemService.removeItem(id);
    }
}
