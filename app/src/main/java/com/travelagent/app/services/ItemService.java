package com.travelagent.app.services;

import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.Item;
import com.travelagent.app.repositories.ItemRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    private GcsImageService gcsImageService;

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

    public Long saveItem(ItemDto item) {
        Item itemToSave = mapToItem(item);
        return itemRepository.save(itemToSave).getId();
    }

    public Item mapToItem(ItemDto item) {
        Item itemToSave = new Item();
        itemToSave.setId(item.getId());
        itemToSave.setCountry(item.getCountry());
        itemToSave.setLocation(item.getLocation());
        itemToSave.setCategory(item.getCategory());
        itemToSave.setName(item.getName());
        itemToSave.setDescription(item.getDescription());
        itemToSave.setRetailPrice(item.getRetailPrice());
        itemToSave.setNetPrice(item.getNetPrice());
        itemToSave.setImageName(item.getImageName().length() > 0 ? item.getImageName() : null);
        return itemToSave;
    }

    public void removeItem(Long id) {
        itemRepository.deleteById(id);
    }

    public ItemDto getItemById(Long id) {
        Optional<ItemDto> itemDtoOpt = itemRepository.findByIdDto(id);
        if (itemDtoOpt.isPresent()) {
            ItemDto itemDto = itemDtoOpt.get();
            if (itemDto.getImageName() != null) {
                String signedUrl = gcsImageService.getSignedUrl(itemDto.getImageName());
                itemDto.setImageUrl(signedUrl);
            }
            return itemDto;
        } else {
            throw new RuntimeException("Could not find itinerary with ID " + id);
        }
    }

    public Item getEntityById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));
    }
}
