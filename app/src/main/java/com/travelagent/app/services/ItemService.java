package com.travelagent.app.services;

import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.Country;
import com.travelagent.app.models.Location;
import com.travelagent.app.models.Supplier;
import com.travelagent.app.models.Item;

import com.travelagent.app.repositories.ItemRepository;
import com.travelagent.app.repositories.LocationRepository;
import com.travelagent.app.repositories.CountryRepository;
import com.travelagent.app.repositories.SupplierRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final LocationRepository locationRepository;
    private final CountryRepository countryRepository;
    private final SupplierRepository supplierRepository;

    public ItemService(
            ItemRepository itemRepository,
            LocationRepository locationRepository,
            CountryRepository countryRepository,
            SupplierRepository supplierRepository) {
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.countryRepository = countryRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<ItemDto> getAllItems() {
        List<Item> allItems = itemRepository.findAllActive();
        return allItems.stream()
                .sorted((item1, item2) -> item1.getName().compareTo(item2.getName()))
                .map(this::convertToDto)
                .toList();
    }

    public Long saveItem(ItemDto item) {
        Item itemToSave = mapToItem(item);
        return itemRepository.save(itemToSave).getId();
    }

    public Item mapToItem(ItemDto item) {
        Item itemToSave = new Item();
        itemToSave.setId(item.getId());
        itemToSave.setCountry(item.getCountry());
        itemToSave.setLocation(item.getLocation());
        itemToSave.setCategory(item.getCategory());
        itemToSave.setName(item.getName());
        itemToSave.setDescription(item.getDescription());
        itemToSave.setRetailPrice(item.getRetailPrice());
        itemToSave.setNetPrice(item.getNetPrice());
        itemToSave.setImageName(item.getImageName().length() > 0 ? item.getImageName() : null);
        itemToSave.setDeleted(false);

        // Add supplier handling
        if (item.getSupplierName() != null && !item.getSupplierName().isEmpty()) {
            // Check if supplier already exists by name
            Optional<Supplier> existingSupplier = supplierRepository.findByName(item.getSupplierName());

            Supplier supplier;
            if (existingSupplier.isPresent()) {
                supplier = existingSupplier.get();
                // Update supplier info if provided
                supplier.setContact(item.getSupplierContact());
                supplier.setUrl(item.getSupplierUrl());
                supplier = supplierRepository.save(supplier);
            } else {
                // Create new supplier
                supplier = new Supplier();
                supplier.setName(item.getSupplierName());
                supplier.setContact(item.getSupplierContact());
                supplier.setUrl(item.getSupplierUrl());
                supplier = supplierRepository.save(supplier);
            }
            itemToSave.setSupplier(supplier);
        }

        return itemToSave;
    }

    public void removeItem(Long id) {
        // Soft delete: mark the item as deleted instead of actually deleting it
        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            item.setDeleted(true);
            itemRepository.save(item);
        } else {
            throw new RuntimeException("Item not found with ID: " + id);
        }
    }

    public void restoreItem(Long id) {
        // Restore a soft-deleted item
        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            item.setDeleted(false);
            itemRepository.save(item);
        } else {
            throw new RuntimeException("Item not found with ID: " + id);
        }
    }

    public ItemDto getItemById(Long id) {
        Optional<ItemDto> itemDtoOpt = itemRepository.findByIdDto(id);
        if (itemDtoOpt.isPresent()) {
            return itemDtoOpt.get();
        } else {
            throw new RuntimeException("Could not find itinerary with ID " + id);
        }
    }

    public Item getEntityById(Long id) {
        return itemRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public List<String> getCountries() {
        List<Country> countries = countryRepository.findAll();
        List<String> countryNames = countries.stream()
                .map(Country::getName)
                .sorted()
                .toList();
        return countryNames;
    }

    public List<String> getLocations() {
        List<Location> locations = locationRepository.findAll();
        List<String> locationNames = locations.stream()
                .map(Location::getName)
                .sorted()
                .toList();
        return locationNames;
    }

    public Long addCountry(String name) {
        Country country = new Country();
        country.setName(name);
        return countryRepository.save(country).getId();
    }

    public Long addLocation(String countryName, String locationName) {
        Location location = new Location();
        location.setName(locationName);
        Country country = countryRepository.findByName(countryName)
                .orElseThrow(() -> new RuntimeException("Country not found"));
        location.setCountry(country);
        return locationRepository.save(location).getId();
    }

    private ItemDto convertToDto(Item item) {
        var supplier = item.getSupplier();
        return new ItemDto(
                item.getId(),
                item.getCountry(),
                item.getLocation(),
                item.getCategory(),
                item.getName(),
                item.getDescription(),
                item.getRetailPrice(),
                item.getNetPrice(),
                item.getImageName(),
                supplier != null ? supplier.getName() : null,
                supplier != null ? supplier.getContact() : null,
                supplier != null ? supplier.getUrl() : null);
    }

}
