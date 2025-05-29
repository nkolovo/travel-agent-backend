package com.travelagent.app.repositories;

import com.travelagent.app.models.CustomItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomItemRepository extends JpaRepository<CustomItem, Long> {
    Optional<CustomItem> findByDateIdAndItemId(Long dateId, Long itemId);
    List<CustomItem> findByDateId(Long dateId);
}