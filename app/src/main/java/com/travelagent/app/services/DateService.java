package com.travelagent.app.services;

import com.travelagent.app.models.Item;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.models.Supplier;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItemDto;

import com.travelagent.app.models.DateItem;
import com.travelagent.app.models.Date;
import com.travelagent.app.models.DateItemId;

import com.travelagent.app.repositories.ItineraryRepository;
import com.travelagent.app.repositories.DateItemRepository;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItemRepository;

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

    private final ItineraryRepository itineraryRepository;
    private final DateRepository dateRepository;
    private final ItemRepository itemRepository;
    private final DateItemRepository dateItemRepository;

    @Autowired
    private GcsImageService gcsImageService;

    public DateService(ItineraryRepository itineraryRepository, DateRepository dateRepository,
            ItemRepository itemRepository,
            DateItemRepository dateItemRepository) {
        this.itineraryRepository = itineraryRepository;
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
                // Get supplier info from DateItem if available, otherwise from Item
                String supplierCompany, supplierName, supplierNumber, supplierEmail, supplierUrl;
                if (dateItemMap.containsKey(item.getId())) {
                    DateItem dateItemInfo = dateItemMap.get(item.getId());
                    supplierCompany = dateItemInfo.getSupplierCompany();
                    supplierName = dateItemInfo.getSupplierName();
                    supplierNumber = dateItemInfo.getSupplierNumber();
                    supplierEmail = dateItemInfo.getSupplierEmail();
                    supplierUrl = dateItemInfo.getSupplierUrl();
                } else {
                    Supplier itemSupplier = item.getSupplier();
                    supplierCompany = itemSupplier != null ? itemSupplier.getCompany() : null;
                    supplierName = itemSupplier != null ? itemSupplier.getName() : null;
                    supplierNumber = itemSupplier != null ? itemSupplier.getNumber() : null;
                    supplierEmail = itemSupplier != null ? itemSupplier.getEmail() : null;
                    supplierUrl = itemSupplier != null ? itemSupplier.getUrl() : null;
                }
                int retailPrice = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getRetailPrice()
                        : item.getRetailPrice();
                int netPrice = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getNetPrice()
                        : item.getNetPrice();
                Set<String> imageNames = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getImageNames()
                        : item.getImageNames();
                Short priority = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getPriority()
                        : dateOpt.get().getDateItems()
                                .stream()
                                .filter(di -> di.getItem().getId().equals(item.getId()))
                                .findFirst()
                                .map(DateItem::getPriority)
                                .orElse(null);
                String notes = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getNotes()
                        : item.getNotes();
                String pdfName = dateItemMap.containsKey(item.getId())
                        ? dateItemMap.get(item.getId()).getPdfName()
                        : null;
                Set<String> signedUrlsSet = null;
                if (imageNames != null) {
                    signedUrlsSet = imageNames.stream()
                            .map(imageName -> signedUrlCache.computeIfAbsent(imageName, gcsImageService::getSignedUrl))
                            .collect(Collectors.toSet());
                }
                result.add(new DateItemDto(
                        item.getId(),
                        convertToDto(dateOpt.get()),
                        convertToDto(item),
                        item.getCountry(),
                        item.getLocation(),
                        item.getCategory(),
                        name,
                        description,
                        supplierCompany,
                        supplierName,
                        supplierNumber,
                        supplierEmail,
                        supplierUrl,
                        retailPrice,
                        netPrice,
                        pdfName,
                        imageNames,
                        signedUrlsSet,
                        priority,
                        notes));
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
        Optional<Item> itemOpt = itemRepository.findActiveById(itemId);

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
            dateItem.setRetailPrice(item.getRetailPrice());
            dateItem.setNetPrice(item.getNetPrice());
            dateItem.setImageNames(item.getImageNames());
            dateItem.setPriority(priority);
            dateItem.setNotes(item.getNotes());

            // Set supplier information from the item
            var supplier = item.getSupplier();
            if (supplier != null) {
                dateItem.setSupplierCompany(supplier.getCompany());
                dateItem.setSupplierName(supplier.getName());
                dateItem.setSupplierNumber(supplier.getNumber());
                dateItem.setSupplierEmail(supplier.getEmail());
                dateItem.setSupplierUrl(supplier.getUrl());
            }
            date.getDateItems().add(dateItem);

            // Add item prices to itinerary totals, if applicable
            if (dateItem.getRetailPrice() != 0 || dateItem.getNetPrice() != 0) {
                Itinerary itinerary = date.getItinerary();
                itinerary.setTripPrice(itinerary.getTripPrice() + item.getRetailPrice());
                itinerary.setNetPrice(itinerary.getNetPrice() + item.getNetPrice());
                itineraryRepository.save(itinerary);
            }
            // Save the DateItem using a DateItemRepository
            dateRepository.save(date);

            return date; // or return dateItem if you prefer
        }
        throw new RuntimeException("Date or Item not found!");
    }

    public void saveDateItemToDate(Long dateId, Long itemId, DateItemDto dateItemDto) {
        Date date = dateRepository.findById(dateId)
                .orElseThrow(() -> new RuntimeException("Could not find date with ID " + dateId));
        Item item = itemRepository.findActiveById(itemId)
                .orElseThrow(() -> new RuntimeException("Could not find item with ID " + itemId));
        DateItem dateItem = new DateItem();
        // If the DateItem already exists, retrieve it,
        // get its original priority, and check if prices have changed
        Optional<DateItem> originalDateItem = dateItemRepository.findByDateIdAndItemId(dateId, itemId);
        if (originalDateItem.isPresent()) {
            DateItem existingDateItem = originalDateItem.get();
            int retailPriceDiff = dateItemDto.getRetailPrice() - existingDateItem.getRetailPrice();
            int netPriceDiff = dateItemDto.getNetPrice() - existingDateItem.getNetPrice();
            if (retailPriceDiff != 0 || netPriceDiff != 0) {
                Itinerary itinerary = date.getItinerary();
                itinerary.setTripPrice(itinerary.getTripPrice() + retailPriceDiff);
                itinerary.setNetPrice(itinerary.getNetPrice() + netPriceDiff);
                itineraryRepository.save(itinerary);
            }
            final Short originalPriority = existingDateItem.getPriority();
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
        }
        dateItem.setId(new DateItemId(dateId, itemId));
        dateItem = convertFromDto(dateItemDto);
        dateItem.setDate(date);
        dateItem.setItem(item);

        dateItemRepository.save(dateItem);
    }

    public void removeItemFromDate(Long dateId, Long itemId) {
        // Remove the item from the date's items set
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        Optional<Item> itemOpt = itemRepository.findById(itemId); // Use findById here since we may need to remove
                                                                  // deleted items from itineraries

        if (dateOpt.isPresent() && itemOpt.isPresent()) {
            Date date = dateOpt.get();
            DateItem dateItem = date.getDateItems().stream()
                    .filter(di -> di.getItem().getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
            if (dateItem == null) {
                throw new RuntimeException("DateItem not found for the given date and item IDs!");
            } else if (dateItem.getRetailPrice() != 0 || dateItem.getNetPrice() != 0) {
                Itinerary itinerary = date.getItinerary();
                itinerary.setTripPrice(itinerary.getTripPrice() - dateItem.getRetailPrice());
                itinerary.setNetPrice(itinerary.getNetPrice() - dateItem.getNetPrice());
                itineraryRepository.save(itinerary);
            }

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
        dateItem.setRetailPrice(dateItemDto.getRetailPrice());
        dateItem.setNetPrice(dateItemDto.getNetPrice());
        dateItem.setNotes(dateItemDto.getNotes());
        dateItem.setPdfName(dateItemDto.getPdfName());
        dateItem.setImageNames(dateItemDto.getImageNames());
        dateItem.setPriority(dateItemDto.getPriority());

        // Set supplier information directly in DateItem (no relationship to Supplier
        // table)
        dateItem.setSupplierCompany(dateItemDto.getSupplierCompany());
        dateItem.setSupplierName(dateItemDto.getSupplierName());
        dateItem.setSupplierNumber(dateItemDto.getSupplierNumber());
        dateItem.setSupplierEmail(dateItemDto.getSupplierEmail());
        dateItem.setSupplierUrl(dateItemDto.getSupplierUrl());

        return dateItem;
    }

    private DateDto convertToDto(Date date) {
        return new DateDto(date.getId(), date.getName(), date.getLocation(), date.getDate());
    }

    private ItemDto convertToDto(Item item) {
        var supplier = item.getSupplier();
        return new ItemDto(item.getId(), item.getCountry(), item.getLocation(),
                item.getCategory(), item.getName(), item.getDescription(), item.getRetailPrice(), item.getNetPrice(),
                item.getImageNames(),
                supplier != null ? supplier.getCompany() : null,
                supplier != null ? supplier.getName() : null,
                supplier != null ? supplier.getNumber() : null,
                supplier != null ? supplier.getEmail() : null,
                supplier != null ? supplier.getUrl() : null,
                item.getNotes());
    }
}
