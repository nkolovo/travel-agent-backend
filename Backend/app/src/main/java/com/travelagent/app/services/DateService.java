package com.travelagent.app.services;

import com.travelagent.app.models.Item;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItemWithCustomDescriptionDto;
import com.travelagent.app.models.CustomItem;
import com.travelagent.app.models.Date;
import com.travelagent.app.repositories.CustomItemRepository;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItemRepository;
import com.travelagent.app.repositories.ItineraryRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class DateService {

    private final DateRepository dateRepository;
    private final ItemRepository itemRepository;
    private final CustomItemRepository customItemRepository;

    public DateService(DateRepository dateRepository, ItemRepository itemRepository,
            ItineraryRepository itineraryRepository, CustomItemRepository customItemRepository) {
        this.dateRepository = dateRepository;
        this.itemRepository = itemRepository;
        this.customItemRepository = customItemRepository;
    }

    public List<DateDto> getDatesForItinerary(Long itineraryId) {
        List<Date> dates = dateRepository.findAllByItineraryId(itineraryId);
        List<DateDto> dateDtos = dates.stream()
                .map(date -> new DateDto(date.getId(), date.getName(), date.getLocation(), date.getDate()))
                .toList();
        return dateDtos;
    }

    public List<ItemWithCustomDescriptionDto> getItemsForDate(Long dateId) {
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        if (dateOpt.isPresent()) {
            Set<Item> itemsSet = dateOpt.get().getItems();
            List<CustomItem> customItems = customItemRepository.findByDateId(dateId);

            // Map itemId to CustomItem for quick lookup
            Map<Long, CustomItem> customItemMap = new HashMap<>();
            for (CustomItem ci : customItems) {
                customItemMap.put(ci.getItem().getId(), ci);
            }

            List<ItemWithCustomDescriptionDto> result = new ArrayList<>();
            for (Item item : itemsSet) {
                String name = customItemMap.containsKey(item.getId())
                        ? customItemMap.get(item.getId()).getName()
                        : item.getName();
                String description = customItemMap.containsKey(item.getId())
                        ? customItemMap.get(item.getId()).getDescription()
                        : item.getDescription();
                result.add(new ItemWithCustomDescriptionDto(
                        item.getId(),
                        item.getLocation(),
                        item.getCategory(),
                        name,
                        description));
            }
            // Sort the result by Category, staring with "Info"
            result.sort((a, b) -> {
                if (a.getCategory().equals("Info"))
                    return -1;
                if (b.getCategory().equals("Info"))
                    return 1;
                return a.getCategory().compareTo(b.getCategory());
            });
            return result;
        }
        throw new RuntimeException("Date not found!");
    }

    public Date addItemToDate(Long dateId, Long itemId) {
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        Optional<Item> itemOpt = itemRepository.findById(itemId);

        if (dateOpt.isPresent() && itemOpt.isPresent()) {
            Date date = dateOpt.get();
            Item item = itemOpt.get();
            date.getItems().add(item);
            return dateRepository.save(date); // Saves relationship
        }
        throw new RuntimeException("Date or Item not found!");
    }

    public CustomItem saveCustomItemToDate(Long dateId, Long itemId, ItemWithCustomDescriptionDto customItemDto) {
        Date date = dateRepository.findById(dateId)
                .orElseThrow(() -> new RuntimeException("Could not find date with ID " + dateId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Could not find item with ID " + itemId));
        CustomItem customItem = new CustomItem();
        customItem.setDate(date);
        customItem.setItem(item);
        customItem.setName(customItemDto.getName());
        customItem.setDescription(customItemDto.getDescription());
        return customItemRepository.save(customItem);
        // Long dateId = customItem.getDate().getId();
        // Long itemId = customItem.getItem().getId();
        // Date date = dateRepository.findById(dateId)
        // .orElseThrow(() -> new RuntimeException("Date not found with ID: " +
        // dateId));
        // Item item = itemRepository.findById(itemId)
        // .orElseThrow(() -> new RuntimeException("Item not found with ID: " +
        // itemId));
        // customItem.setDate(date);
        // customItem.setItem(item);
        // return customItemRepository.save(customItem);
    }

    public void removeItemFromDate(Long dateId, Long itemId) {
        // Remove the item from the date's items set
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        Optional<Item> itemOpt = itemRepository.findById(itemId);

        if (dateOpt.isPresent() && itemOpt.isPresent()) {
            Date date = dateOpt.get();
            Item item = itemOpt.get();
            date.getItems().remove(item);
            dateRepository.save(date); // Update the relationship

            // Remove any CustomItem for this date/item
            customItemRepository.findByDateIdAndItemId(dateId, itemId)
                    .ifPresent(customItemRepository::delete);
            return;
        }
        throw new RuntimeException("Date or Item not found!");
    }
}
