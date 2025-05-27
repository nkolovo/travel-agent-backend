package com.travelagent.app.services;

import com.travelagent.app.models.Item;

import com.travelagent.app.repositories.ItemRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Long addItem(Item item) {
        return itemRepository.save(item).getId();
    }

    public void removeItem(Long id) {
        itemRepository.deleteById(id);
    }

}
