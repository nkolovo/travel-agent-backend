package com.travelagent.app.repositories;

import com.travelagent.app.models.Item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
