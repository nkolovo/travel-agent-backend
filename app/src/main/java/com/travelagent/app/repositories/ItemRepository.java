package com.travelagent.app.repositories;

import com.travelagent.app.models.Item;
import com.travelagent.app.dto.ItemDto;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT new com.travelagent.app.dto.ItemDto(" +
            "i.id, i.category, i.country, i.description, i.location, i.name, i.imageObjectName " +
            ") " +
            "FROM Item it WHERE it.id = :id")
    Optional<ItemDto> findByIdDto(@Param("id") Long id);

}
