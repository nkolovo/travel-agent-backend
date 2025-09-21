package com.travelagent.app.services;

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

    public List<DateItem> getDateItemsByDate(Long dateId) {
        return dateItemRepository.findByDateId(dateId);
    }
}