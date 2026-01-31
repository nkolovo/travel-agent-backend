package com.travelagent.app.services;

import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.ItemDto;
import com.travelagent.app.models.DateItem;
import com.travelagent.app.repositories.DateItemRepository;
import com.travelagent.app.repositories.DateRepository;
import com.travelagent.app.repositories.ItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DateItemService {
    private final DateItemRepository dateItemRepository;
    private final GcsPdfService gcsPdfService;

    public DateItemService(DateItemRepository dateItemRepository,
            DateRepository dateRepository,
            ItemRepository itemRepository,
            GcsPdfService gcsPdfService) {
        this.dateItemRepository = dateItemRepository;
        this.gcsPdfService = gcsPdfService;
    }

    public List<DateItem> getDateItems(Long dateId, Long itemId) {
        return dateItemRepository.findByDateIdAndItemId(dateId, itemId);
    }

    public List<DateItemDto> getDateItemsByDate(Long dateId) {
        List<DateItem> dateItems = dateItemRepository.findByDateId(dateId);
        return dateItems.stream().map(this::convertToDto).toList();
    }

    public String uploadPdfForDateItem(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }

        // Check if the PDF already exists in GCS
        boolean exists = gcsPdfService.doesPdfExist(originalFilename);
        System.out.println("PDF " + originalFilename + " exists in GCS: " + exists);

        if (exists) {
            System.out.println("Skipping existing PDF: " + originalFilename);
            return "PDF already exists: " + originalFilename;
        } else {
            System.out.println("Uploading new PDF: " + originalFilename);
            String uploadedFileName = gcsPdfService.uploadPdf(file, originalFilename);
            System.out.println("Successfully uploaded: " + uploadedFileName);
            return "PDF uploaded successfully: " + uploadedFileName;
        }
    }

    public String getPdfUrl(String pdfName) {
        if (pdfName == null || pdfName.trim().isEmpty()) {
            throw new IllegalArgumentException("PDF name must be provided");
        }

        return gcsPdfService.getSignedUrl(pdfName);
    }

    private DateItemDto convertToDto(DateItem dateItem) {
        DateItemDto dto = new DateItemDto();
        dto.setId(dateItem.getId());
        dto.setName(dateItem.getName());
        dto.setDescription(dateItem.getDescription());
        dto.setCountry(dateItem.getCountry());
        dto.setLocation(dateItem.getLocation());
        dto.setCategory(dateItem.getCategory());
        dto.setSupplierCompany(dateItem.getSupplierCompany());
        dto.setSupplierName(dateItem.getSupplierName());
        dto.setSupplierNumber(dateItem.getSupplierNumber());
        dto.setSupplierEmail(dateItem.getSupplierEmail());
        dto.setSupplierUrl(dateItem.getSupplierUrl());
        dto.setRetailPrice(dateItem.getRetailPrice());
        dto.setNetPrice(dateItem.getNetPrice());
        dto.setImageNames(dateItem.getImageNames());
        dto.setPriority(dateItem.getPriority());
        dto.setPdfName(dateItem.getPdfName());

        // Convert related entities to DTOs
        if (dateItem.getDate() != null) {
            DateDto dateDto = new DateDto();
            dateDto.setId(dateItem.getDate().getId());
            dateDto.setDate(dateItem.getDate().getDate());
            dateDto.setName(dateItem.getDate().getName());
            dateDto.setLocation(dateItem.getDate().getLocation());
            dto.setDate(dateDto);
        }

        if (dateItem.getItem() != null) {
            var item = dateItem.getItem();
            ItemDto itemDto = new ItemDto(
                    item.getId(),
                    item.getCountry(),
                    item.getLocation(),
                    item.getCategory(),
                    item.getName(),
                    item.getDescription(),
                    item.getRetailPrice(),
                    item.getNetPrice(),
                    item.getImageNames(),
                    item.getNotes());
            dto.setItem(itemDto);
        }

        return dto;
    }
}