package com.travelagent.app.services;

import com.travelagent.app.models.Item;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.DateItem;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.DateItemId;
import com.travelagent.app.repositories.DateItemRepository;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItemRepository;
import com.travelagent.app.repositories.ItineraryRepository;

import org.springframework.beans.factory.annotation.Autowired;
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
    private final DateItemRepository dateItemRepository;

    @Autowired
    private GcsImageService gcsImageService;

    public DateService(DateRepository dateRepository, ItemRepository itemRepository,
            ItineraryRepository itineraryRepository, DateItemRepository dateItemRepository) {
        this.dateRepository = dateRepository;
        this.itemRepository = itemRepository;
        this.dateItemRepository = dateItemRepository;
    }

    public List<DateDto> getDatesForItinerary(Long itineraryId) {
        List<Date> dates = dateRepository.findAllByItineraryId(itineraryId);
        List<DateDto> dateDtos = dates.stream()
                .map(date -> new DateDto(date.getId(), date.getName(), date.getLocation(), date.getDate()))
                .toList();
        return dateDtos;
    }

    public List<DateItemDto> getItemsForDate(Long dateId) {
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        if (dateOpt.isPresent()) {
            Set<Item> itemsSet = dateOpt.get().getDateItems()
                    .stream()
                    .map(DateItem::getItem)
                    .collect(Collectors.toSet());
            List<DateItem> dateItems = dateItemRepository.findByDateId(dateId);

            // Map itemId to DateItem for quick lookup
            Map<Long, DateItem> dateItemMap = new HashMap<>();
            for (DateItem ci : dateItems) {
                dateItemMap.put(ci.getItem().getId(), ci);
            }

            Map<String, String> signedUrlCache = new HashMap<>();
            List<DateItemDto> result = new ArrayList<>();
            for (Item item : itemsSet) {
                String name = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getName()
                        : item.getName();
                String description = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getDescription()
                        : item.getDescription();
                String imageName = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getImageName()
                        : item.getImageName();
                Short priority = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getPriority()
                        : dateOpt.get().getDateItems()
                                .stream()
                                .filter(di -> di.getItem().getId().equals(item.getId()))
                                .findFirst()
                                .map(DateItem::getPriority)
                                .orElse(null);
                String signedUrl = null;
                if (imageName != null)
                    signedUrl = signedUrlCache.computeIfAbsent(imageName, gcsImageService::getSignedUrl);
                result.add(new DateItemDto(
                        item.getId(),
                        convertToDto(dateOpt.get()),
                        convertToDto(item),
                        item.getCountry(),
                        item.getLocation(),
                        item.getCategory(),
                        name,
                        description,
                        imageName,
                        signedUrl,
                        priority));
            }
            // Sort the result by Priority
            if (result.size() >= 2)
                result.sort(Comparator.comparing(DateItemDto::getPriority,
                        Comparator.nullsLast(Short::compareTo)));
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
            dateItem.setCountry(item.getCountry());
            dateItem.setLocation(item.getLocation());
            dateItem.setCategory(item.getCategory());
            dateItem.setName(item.getName());
            dateItem.setDescription(item.getDescription());
            dateItem.setImageName(item.getImageName());
            dateItem.setPriority(priority);
            date.getDateItems().add(dateItem);

            // Save the DateItem using a DateItemRepository
            dateRepository.save(date);

            return date; // or return dateItem if you prefer
        }
        throw new RuntimeException("Date or Item not found!");
    }

    public void saveDateItemToDate(Long dateId, Long itemId, DateItemDto dateItemDto) {
        Date date = dateRepository.findById(dateId)
                .orElseThrow(() -> new RuntimeException("Could not find date with ID " + dateId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Could not find item with ID " + itemId));
        DateItem dateItem = new DateItem();
        // If the DateItem already exists, retrieve it and get its original priority
        Optional<DateItem> originalDateItem = dateItemRepository.findByDateIdAndItemId(dateId, itemId);
        final Short originalPriority = originalDateItem.isPresent() ? originalDateItem.get().getPriority() : null;
        dateItem.setId(new DateItemId(dateId, itemId));
        dateItem = convertFromDto(dateItemDto);
        dateItem.setDate(date);
        dateItem.setItem(item);

        if (originalPriority != null) {
            List<DateItem> prioritiesToUpdate = dateItemRepository.findByDateId(dateId).stream()
                    .filter(di -> di.getPriority() != null && dateItemDto.getPriority() != null
                            && di.getPriority() >= dateItemDto.getPriority()
                            && di.getPriority() < originalPriority
                            && !di.getItem().getId().equals(itemId))
                    .collect(Collectors.toList());

            for (DateItem di : prioritiesToUpdate) {
                di.setPriority((short) (di.getPriority() + 1));
            }
            dateItemRepository.saveAll(prioritiesToUpdate);
        }
        dateItemRepository.save(dateItem);
    }

    public void removeItemFromDate(Long dateId, Long itemId) {
        // Remove the item from the date's items set
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        Optional<Item> itemOpt = itemRepository.findById(itemId);

        if (dateOpt.isPresent() && itemOpt.isPresent()) {
            Date date = dateOpt.get();
            date.getDateItems().removeIf(di -> di.getItem().getId().equals(itemId));
            dateRepository.save(date); // Update the relationship

            // Remove any DateItem for this date/item
            dateItemRepository.findByDateIdAndItemId(dateId, itemId)
                    .ifPresent(dateItemRepository::delete);
            return;
        }
        throw new RuntimeException("Date or Item not found!");
    }

    private DateItem convertFromDto(DateItemDto dateItemDto) {
        DateItem dateItem = new DateItem();
        dateItem.setId(new DateItemId(dateItemDto.getDate().getId(), dateItemDto.getItem().getId()));
        dateItem.setCountry(dateItemDto.getCountry());
        dateItem.setLocation(dateItemDto.getLocation());
        dateItem.setCategory(dateItemDto.getCategory());
        dateItem.setName(dateItemDto.getName());
        dateItem.setDescription(dateItemDto.getDescription());
        dateItem.setImageName(dateItemDto.getImageName());
        dateItem.setPriority(dateItemDto.getPriority());
        return dateItem;
    }

    private DateDto convertToDto(Date date) {
        return new DateDto(date.getId(), date.getName(), date.getLocation(), date.getDate());
    }

    private ItemDto convertToDto(Item item) {
        return new ItemDto(item.getId(), item.getCountry(), item.getLocation(),
                item.getCategory(), item.getName(), item.getDescription(), item.getImageName());
    }
}
