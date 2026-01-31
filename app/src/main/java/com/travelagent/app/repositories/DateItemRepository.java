package com.travelagent.app.repositories;

import com.travelagent.app.models.DateItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DateItemRepository extends JpaRepository<DateItem, Long> {
    @Query("SELECT di FROM DateItem di WHERE di.date.id = :dateId AND di.item.id = :itemId")
    List<DateItem> findByDateIdAndItemId(@Param("dateId") Long dateId, @Param("itemId") Long itemId);

    @Query("SELECT di FROM DateItem di WHERE di.date.id = :dateId")
    List<DateItem> findByDateId(@Param("dateId") Long dateId);

    @Query("SELECT di FROM DateItem di WHERE di.item.id = :itemId")
    List<DateItem> findByItemId(@Param("itemId") Long itemId);
}