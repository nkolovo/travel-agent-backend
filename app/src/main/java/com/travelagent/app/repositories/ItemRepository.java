package com.travelagent.app.repositories;

import com.travelagent.app.models.Item;
import com.travelagent.app.dto.ItemDto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Find all non-deleted items
    @Query("SELECT i FROM Item i WHERE i.deleted = false")
    List<Item> findAllActive();
    
    // Find all deleted items
    @Query("SELECT i FROM Item i WHERE i.deleted = true")
    List<Item> findAllDeleted();
    
    // Find active item by ID
    @Query("SELECT i FROM Item i WHERE i.id = :id AND i.deleted = false")
    Optional<Item> findActiveById(@Param("id") Long id);
    
    @Query("SELECT new com.travelagent.app.dto.ItemDto(" +
            "it.id, it.category, it.country, it.description, it.location, it.name, it.retailPrice, it.netPrice, it.imageName "
            +
            ") " +
            "FROM Item it WHERE it.id = :id AND it.deleted = false")
    Optional<ItemDto> findByIdDto(@Param("id") Long id);

}
