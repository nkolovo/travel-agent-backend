package com.travelagent.app.services;

import com.travelagent.app.models.Item;
import com.travelagent.app.dto.CustomItemDto;
import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.CustomItem;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.DateItem;
import com.travelagent.app.models.DateItemId;
import com.travelagent.app.repositories.CustomItemRepository;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItemRepository;
import com.travelagent.app.repositories.ItineraryRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<CustomItemDto> getItemsForDate(Long dateId) {
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        if (dateOpt.isPresent()) {
            Set<Item> itemsSet = dateOpt.get().getDateItems()
                    .stream()
                    .map(DateItem::getItem)
                    .collect(Collectors.toSet());
            List<CustomItem> customItems = customItemRepository.findByDateId(dateId);

            // Map itemId to CustomItem for quick lookup
            Map<Long, CustomItem> customItemMap = new HashMap<>();
            for (CustomItem ci : customItems) {
                customItemMap.put(ci.getItem().getId(), ci);
            }

            List<CustomItemDto> result = new ArrayList<>();
            for (Item item : itemsSet) {
                String name = customItemMap.containsKey(item.getId())
                        ? customItemMap.get(item.getId()).getName()
                        : item.getName();
                String description = customItemMap.containsKey(item.getId())
                        ? customItemMap.get(item.getId()).getDescription()
                        : item.getDescription();
                Short priority = customItemMap.containsKey(item.getId())
                        ? customItemMap.get(item.getId()).getPriority()
                        : dateOpt.get().getDateItems()
                                .stream()
                                .filter(di -> di.getItem().getId().equals(item.getId()))
                                .findFirst()
                                .map(DateItem::getPriority)
                                .orElse(null);
                System.out.println(priority);
                result.add(new CustomItemDto(
                        item.getId(),
                        convertToDto(dateOpt.get()),
                        convertToDto(item),
                        item.getCountry(),
                        item.getLocation(),
                        item.getCategory(),
                        name,
                        description,
                        priority));
            }
            // Sort the result by Priority
            if (result.size() >= 2)
                result.sort(Comparator.comparing(CustomItemDto::getPriority,
                        Comparator.nullsLast(Short::compareTo)));
            for (int i = 0; i < result.size(); i++) {
                System.out.println(
                        result.get(i).getPriority() + " with name: " + result.get(i).getName() + " at index " + i);
            }
            return result;
        }
        throw new RuntimeException("Date not found!");
    }

    public Date addItemToDate(Long dateId, Long itemId, Short priority) {
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        Optional<Item> itemOpt = itemRepository.findById(itemId);

        if (dateOpt.isPresent() && itemOpt.isPresent()) {
            Date date = dateOpt.get();
            Item item = itemOpt.get();

            DateItem dateItem = new DateItem();
            dateItem.setId(new DateItemId(date.getId(), item.getId()));
            dateItem.setDate(date);
            dateItem.setItem(item);
            System.out.println("Setting priority: " + priority);
            dateItem.setPriority(priority);

            date.getDateItems().add(dateItem);

            // Save the DateItem using a DateItemRepository
            dateRepository.save(date);

            return date; // or return dateItem if you prefer
        }
        throw new RuntimeException("Date or Item not found!");
    }

    public CustomItem saveCustomItemToDate(Long dateId, Long itemId, CustomItemDto customItemDto) {
        Date date = dateRepository.findById(dateId)
                .orElseThrow(() -> new RuntimeException("Could not find date with ID " + dateId));
        System.out.println("Saving custom item");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Could not find item with ID " + itemId));
        CustomItem customItem = new CustomItem();
        customItem.setDate(date);
        customItem.setItem(item);
        customItem.setCountry(customItemDto.getCountry());
        customItem.setLocation(customItemDto.getLocation());
        customItem.setCategory(customItemDto.getCategory());
        customItem.setName(customItemDto.getName());
        customItem.setDescription(customItemDto.getDescription());
        customItem.setPriority(customItemDto.getPriority());
        return customItemRepository.save(customItem);
    }

    public void removeItemFromDate(Long dateId, Long itemId) {
        // Remove the item from the date's items set
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        Optional<Item> itemOpt = itemRepository.findById(itemId);

        if (dateOpt.isPresent() && itemOpt.isPresent()) {
            Date date = dateOpt.get();
            date.getDateItems().removeIf(di -> di.getItem().getId().equals(itemId));
            dateRepository.save(date); // Update the relationship

            // Remove any CustomItem for this date/item
            customItemRepository.findByDateIdAndItemId(dateId, itemId)
                    .ifPresent(customItemRepository::delete);
            return;
        }
        throw new RuntimeException("Date or Item not found!");
    }

    private DateDto convertToDto(Date date) {
        return new DateDto(date.getId(), date.getName(), date.getLocation(), date.getDate());
    }

    private ItemDto convertToDto(Item item) {
        return new ItemDto(item.getId(), item.getCountry(), item.getLocation(),
                item.getCategory(), item.getName(), item.getDescription());
    }
}
