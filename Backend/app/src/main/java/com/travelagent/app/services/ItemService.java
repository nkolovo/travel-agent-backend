package com.travelagent.app.services;

import com.travelagent.app.dto.ItemDto;
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
        List<Item> sortedItems = allItems.stream()
                .sorted((item1, item2) -> item1.getName().compareTo(item2.getName()))
                .toList();
        return sortedItems;
    }

    public void saveItem(ItemDto item) {
        Item itemToSave = mapToItem(item);
        itemRepository.save(itemToSave);
    }

    private Item mapToItem(ItemDto item) {
        Item itemToSave = new Item();
        itemToSave.setId(item.getId());
        itemToSave.setCountry(item.getCountry());
        itemToSave.setLocation(item.getLocation());
        itemToSave.setCategory(item.getCategory());
        itemToSave.setName(item.getName());
        itemToSave.setDescription(item.getDescription());
        return itemToSave;
    }

    public void removeItem(Long id) {
        itemRepository.deleteById(id);
    }
}
