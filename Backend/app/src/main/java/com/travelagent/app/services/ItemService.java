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
        List<Item> allItems = itemRepository.findAll();
        int count = 0;

        List<Item> sortedItems = allItems.stream()
                .sorted((item1, item2) -> item1.getName().compareTo(item2.getName()))
                .toList();
        for (Item item : sortedItems) {
            System.out.println("Item " + count + ": " + item.getName());
            count++;
        }
        return sortedItems;
    }

    public Long saveItem(Item item) {
        return itemRepository.save(item).getId();
    }

    public void removeItem(Long id) {
        itemRepository.deleteById(id);
    }

}
