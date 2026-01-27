package com.travelagent.app.services;

import com.travelagent.app.dto.SupplierDto;
import com.travelagent.app.models.Supplier;
import com.travelagent.app.repositories.SupplierRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(
            SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierDto> getAllSuppliers() {
        List<Supplier> allSuppliers = supplierRepository.findAllActive();
        List<Supplier> sortedSuppliers = allSuppliers.stream()
                .sorted((supplier1, supplier2) -> supplier1.getName().compareTo(supplier2.getName()))
                .toList();

        List<SupplierDto> sortedSupplierDtos = sortedSuppliers.stream()
                .map(this::mapToSupplierDto)
                .toList();
        return sortedSupplierDtos;
    }

    public List<String> getAllSupplierNames() {
        List<Supplier> allSuppliers = supplierRepository.findAllActive();
        return allSuppliers.stream()
                .sorted((supplier1, supplier2) -> supplier1.getName().compareTo(supplier2.getName()))
                .map(Supplier::getName)
                .toList();
    }

    public SupplierDto getBySupplierName(String name) {
        Optional<Supplier> supplierOpt = supplierRepository.findByName(name);
        if (supplierOpt.isPresent()) {
            return mapToSupplierDto(supplierOpt.get());
        } else {
            throw new RuntimeException("Supplier not found with name: " + name);
        }
    }

    public Long saveSupplier(SupplierDto supplier) {
        Supplier supplierToSave = mapToSupplier(supplier);
        return supplierRepository.save(supplierToSave).getId();
    }

    public Supplier mapToSupplier(SupplierDto supplier) {
        Supplier supplierToSave = new Supplier();
        supplierToSave.setId(supplier.getId());
        supplierToSave.setName(supplier.getName());
        supplierToSave.setContact(supplier.getContact());
        supplierToSave.setUrl(supplier.getUrl());

        return supplierToSave;
    }

    public SupplierDto mapToSupplierDto(Supplier supplier) {
        SupplierDto supplierDto = new SupplierDto();
        supplierDto.setId(supplier.getId());
        supplierDto.setName(supplier.getName());
        supplierDto.setContact(supplier.getContact());
        supplierDto.setUrl(supplier.getUrl());
        return supplierDto;
    }

    public void removeSupplier(Long id) {
        // Soft delete: mark the supplier as deleted instead of actually deleting it
        Optional<Supplier> supplierOpt = supplierRepository.findById(id);
        if (supplierOpt.isPresent()) {
            Supplier supplier = supplierOpt.get();
            supplier.setDeleted(true);
            supplierRepository.save(supplier);
        } else {
            throw new RuntimeException("Supplier not found with ID: " + id);
        }
    }

    public void restoreSupplier(Long id) {
        // Restore a soft-deleted supplier
        Optional<Supplier> supplierOpt = supplierRepository.findById(id);
        if (supplierOpt.isPresent()) {
            Supplier supplier = supplierOpt.get();
            supplier.setDeleted(false);
            supplierRepository.save(supplier);
        } else {
            throw new RuntimeException("Supplier not found with ID: " + id);
        }
    }
}
