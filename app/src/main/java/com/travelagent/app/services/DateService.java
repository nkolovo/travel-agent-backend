package com.travelagent.app.services;

import com.travelagent.app.models.Item;
import com.travelagent.app.models.Itinerary;
import com.travelagent.app.models.Supplier;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.ItemDto;

import com.travelagent.app.models.DateItem;
import com.travelagent.app.models.Date;

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

import javax.management.RuntimeErrorException;

@Service
public class DateService {

    private final ItineraryRepository itineraryRepository;
    private final DateRepository dateRepository;
    private final ItemRepository itemRepository;
    private final DateItemRepository dateItemRepository;

    @Autowired
    private GcsImageService gcsImageService;
    @Autowired
    private GcsPdfService gcsPdfService;

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
            List<DateItem> dateItems = dateItemRepository.findByDateId(dateId);
            Map<String, String> signedUrlCache = new HashMap<>();

            List<DateItemDto> result = new ArrayList<>();
            for (DateItem dateItem : dateItems) {
                String pdfUrl = null;
                if (dateItem.getPdfName() != null) {
                    pdfUrl = signedUrlCache.computeIfAbsent(dateItem.getPdfName(), gcsPdfService::getSignedUrl);
                }

                Set<String> signedUrlsSet = null;
                if (dateItem.getImageNames() != null) {
                    signedUrlsSet = dateItem.getImageNames().stream()
                            .map(imageName -> signedUrlCache.computeIfAbsent(imageName, gcsImageService::getSignedUrl))
                            .collect(Collectors.toSet());
                }

                result.add(new DateItemDto(
                        dateItem.getId(), // Use DateItem's own ID, not Item ID
                        convertToDto(dateOpt.get()),
                        convertToDto(dateItem.getItem()),
                        dateItem.getCountry(),
                        dateItem.getLocation(),
                        dateItem.getCategory(),
                        dateItem.getName(),
                        dateItem.getDescription(),
                        dateItem.getSupplierCompany(),
                        dateItem.getSupplierName(),
                        dateItem.getSupplierNumber(),
                        dateItem.getSupplierEmail(),
                        dateItem.getSupplierUrl(),
                        dateItem.getRetailPrice(),
                        dateItem.getNetPrice(),
                        dateItem.getPdfName(),
                        pdfUrl,
                        dateItem.getImageNames(),
                        signedUrlsSet,
                        dateItem.getPriority(),
                        dateItem.getNotes()));
            }

            // Sort the result by Priority
            if (result.size() >= 2) {
                result.sort(Comparator.comparing(DateItemDto::getPriority,
                        Comparator.nullsLast(Short::compareTo)));
            }
            return result;
        }
        throw new RuntimeException("Date not found!");
    }

    public DateItemDto addItemToDate(Long dateId, Long itemId, Short priority) {
        Optional<Date> dateOpt = dateRepository.findById(dateId);
        Optional<Item> itemOpt = itemRepository.findActiveById(itemId);

        if (dateOpt.isPresent() && itemOpt.isPresent()) {
            Date date = dateOpt.get();
            Item item = itemOpt.get();
            DateItem dateItem = new DateItem();
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

            dateItemRepository.save(dateItem);
            dateRepository.save(date);

            return convertToDto(dateItem);

        }
        throw new RuntimeException("Date or Item not found!");
    }

    public void saveDateItemToDate(DateItemDto dateItemDto) {
        Date date = dateRepository.findById(dateItemDto.getDate().getId())
                .orElseThrow(
                        () -> new RuntimeException("Could not find date with ID " + dateItemDto.getDate().getId()));

        DateItem dateItem;

        // Update existing DateItem (DTO has an ID)
        if (dateItemDto.getId() != null) {
            Optional<DateItem> existingDateItemOpt = dateItemRepository.findById(dateItemDto.getId());
            if (existingDateItemOpt.isPresent()) {
                DateItem existingDateItem = existingDateItemOpt.get();
                int retailPriceDiff = dateItemDto.getRetailPrice() - existingDateItem.getRetailPrice();
                int netPriceDiff = dateItemDto.getNetPrice() - existingDateItem.getNetPrice();

                // Update itinerary prices based on the difference
                if (retailPriceDiff != 0 || netPriceDiff != 0) {
                    Itinerary itinerary = existingDateItem.getDate().getItinerary();
                    itinerary.setTripPrice(itinerary.getTripPrice() + retailPriceDiff);
                    itinerary.setNetPrice(itinerary.getNetPrice() + netPriceDiff);
                    itineraryRepository.save(itinerary);
                }

                // Handle priority changes
                final Short originalPriority = existingDateItem.getPriority();
                if (originalPriority != null && dateItemDto.getPriority() != null
                        && !originalPriority.equals(dateItemDto.getPriority())) {
                    List<DateItem> prioritiesToUpdate = dateItemRepository.findByDateId(date.getId()).stream()
                            .filter(di -> di.getPriority() != null
                                    && di.getPriority() >= dateItemDto.getPriority()
                                    && di.getPriority() < originalPriority
                                    && !di.getId().equals(existingDateItem.getId()))
                            .collect(Collectors.toList());

                    for (DateItem di : prioritiesToUpdate) {
                        di.setPriority((short) (di.getPriority() + 1));
                    }
                    dateItemRepository.saveAll(prioritiesToUpdate);
                }

                dateItem = convertFromDto(dateItemDto);

            } else {
                throw new RuntimeException("DateItem with ID " + dateItemDto.getId() + " not found");
            }
        } else {
            throw new RuntimeErrorException(new Error("DateItem not found"));
        }

        dateItemRepository.save(dateItem);
    }

    public void removeItemFromDate(Long dateItemId) {
        Optional<DateItem> dateItemOpt = dateItemRepository.findById(dateItemId);
        if (dateItemOpt.isEmpty()) {
            throw new RuntimeException("DateItem with ID " + dateItemId + " not found!");
        }
        DateItem dateItem = dateItemOpt.get();
        Date date = dateItem.getDate();
        Item item = dateItem.getItem();

        // Subtract prices from itinerary totals
        if (dateItem.getRetailPrice() != 0 || dateItem.getNetPrice() != 0) {
            Itinerary itinerary = date.getItinerary();
            itinerary.setTripPrice(itinerary.getTripPrice() - dateItem.getRetailPrice());
            itinerary.setNetPrice(itinerary.getNetPrice() - dateItem.getNetPrice());
            itineraryRepository.save(itinerary);
        }

        dateItemRepository.delete(dateItem);
        date.getDateItems().remove(dateItem);
        item.getDateItems().remove(dateItem);
        dateRepository.save(date);
        itemRepository.save(item);
        return;
    }

    private Date convertFromDto(DateDto dateDto) {
        Date date = new Date();
        date.setId(dateDto.getId());
        date.setName(dateDto.getName());
        date.setLocation(dateDto.getLocation());
        date.setDate(dateDto.getDate());
        return date;
    }

    private Item convertFromDto(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setCountry(itemDto.getCountry());
        item.setLocation(itemDto.getLocation());
        item.setCategory(itemDto.getCategory());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setRetailPrice(itemDto.getRetailPrice());
        item.setNetPrice(itemDto.getNetPrice());
        item.setImageNames(itemDto.getImageNames());
        return item;
    }

    private DateItem convertFromDto(DateItemDto dateItemDto) {
        DateItem dateItem = new DateItem();
        dateItem.setId(dateItemDto.getId());
        dateItem.setDate(convertFromDto(dateItemDto.getDate()));
        dateItem.setItem(convertFromDto(dateItemDto.getItem()));
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

    private DateItemDto convertToDto(DateItem dateItem) {
        DateItemDto dto = new DateItemDto();
        dto.setId(dateItem.getId());
        dto.setDate(convertToDto(dateItem.getDate()));
        dto.setItem(convertToDto(dateItem.getItem()));
        dto.setName(dateItem.getName());
        dto.setDescription(dateItem.getDescription());
        dto.setCountry(dateItem.getCountry());
        dto.setLocation(dateItem.getLocation());
        dto.setCategory(dateItem.getCategory());
        dto.setSupplierCompany(dateItem.getSupplierCompany());
        dto.setSupplierName(dateItem.getSupplierName());
        dto.setSupplierNumber(dateItem.getSupplierNumber());
        dto.setSupplierEmail(dateItem.getSupplierEmail());
        dto.setSupplierUrl(dateItem.getSupplierUrl());
        dto.setRetailPrice(dateItem.getRetailPrice());
        dto.setNetPrice(dateItem.getNetPrice());
        dto.setImageNames(dateItem.getImageNames());
        dto.setPriority(dateItem.getPriority());
        dto.setNotes(dateItem.getNotes());
        dto.setPdfName(dateItem.getPdfName());
        return dto;
    }
}
