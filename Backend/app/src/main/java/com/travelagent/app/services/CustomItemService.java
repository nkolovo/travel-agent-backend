package com.travelagent.app.services;

import com.travelagent.app.models.CustomItem;
import com.travelagent.app.repositories.CustomItemRepository;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItemRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomItemService {
    private final CustomItemRepository customItemRepository;

    public CustomItemService(CustomItemRepository customItemRepository,
            DateRepository dateRepository,
            ItemRepository itemRepository) {
        this.customItemRepository = customItemRepository;
    }

    public Optional<CustomItem> getCustomItem(Long dateId, Long itemId) {
        return customItemRepository.findByDateIdAndItemId(dateId, itemId);
    }

    public List<CustomItem> getCustomItemsByDate(Long dateId) {
        return customItemRepository.findByDateId(dateId);
    }
}