package com.travelagent.app.services;

import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.Country;
import com.travelagent.app.models.Location;
import com.travelagent.app.models.Item;

import com.travelagent.app.repositories.ItemRepository;
import com.travelagent.app.repositories.LocationRepository;
import com.travelagent.app.repositories.CountryRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final LocationRepository locationRepository;
    private final CountryRepository countryRepository;

    @Autowired
    private GcsImageService gcsImageService;

    public ItemService(
            ItemRepository itemRepository,
            LocationRepository locationRepository,
            CountryRepository countryRepository) {
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.countryRepository = countryRepository;
    }

    public List<Item> getAllItems() {
        List<Item> allItems = itemRepository.findAllActive();
        List<Item> sortedItems = allItems.stream()
                .sorted((item1, item2) -> item1.getName().compareTo(item2.getName()))
                .toList();
        return sortedItems;
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
            ItemDto itemDto = itemDtoOpt.get();
            if (itemDto.getImageName() != null) {
                String signedUrl = gcsImageService.getSignedUrl(itemDto.getImageName());
                itemDto.setImageUrl(signedUrl);
            }
            return itemDto;
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

}
