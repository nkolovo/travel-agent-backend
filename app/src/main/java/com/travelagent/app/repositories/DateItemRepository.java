package com.travelagent.app.repositories;

import com.travelagent.app.models.DateItem;
import com.travelagent.app.models.DateItemId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DateItemRepository extends JpaRepository<DateItem, DateItemId> {
    Optional<DateItem> findByDateIdAndItemId(Long dateId, Long itemId);

    List<DateItem> findByDateId(Long dateId);

    List<DateItem> findByItemId(Long itemId);
}