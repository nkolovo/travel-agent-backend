package com.travelagent.app.services;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.DateItem;
import com.travelagent.app.repositories.DateItemRepository;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItemRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DateItemService {
    private final DateItemRepository dateItemRepository;

    public DateItemService(DateItemRepository dateItemRepository,
            DateRepository dateRepository,
            ItemRepository itemRepository) {
        this.dateItemRepository = dateItemRepository;
    }

    public Optional<DateItem> getDateItem(Long dateId, Long itemId) {
        return dateItemRepository.findByDateIdAndItemId(dateId, itemId);
    }

    public List<DateItemDto> getDateItemsByDate(Long dateId) {
        List<DateItem> dateItems = dateItemRepository.findByDateId(dateId);
        return dateItems.stream().map(this::convertToDto).toList();
    }

    private DateItemDto convertToDto(DateItem dateItem) {
        DateItemDto dto = new DateItemDto();
        dto.setId(dateItem.getId().getItemId());
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

        // Convert related entities to DTOs
        if (dateItem.getDate() != null) {
            DateDto dateDto = new DateDto();
            dateDto.setId(dateItem.getDate().getId());
            dateDto.setDate(dateItem.getDate().getDate());
            dateDto.setName(dateItem.getDate().getName());
            dateDto.setLocation(dateItem.getDate().getLocation());
            dto.setDate(dateDto);
        }

        if (dateItem.getItem() != null) {
            var item = dateItem.getItem();
            ItemDto itemDto = new ItemDto(
                    item.getId(),
                    item.getCountry(),
                    item.getLocation(),
                    item.getCategory(),
                    item.getName(),
                    item.getDescription(),
                    item.getRetailPrice(),
                    item.getNetPrice(),
                    item.getImageNames());
            dto.setItem(itemDto);
        }

        return dto;
    }
}