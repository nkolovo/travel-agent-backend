package com.travelagent.app.controllers;

import com.travelagent.app.dto.SupplierDto;
import com.travelagent.app.services.SupplierService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierDto> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/all-names")
    public List<String> getAllSupplierNames() {
        return supplierService.getAllSupplierNames();
    }

    @GetMapping("/name/{name}")
    public SupplierDto getSupplier(@PathVariable String name) {
        return supplierService.getBySupplierName(name);
    }

    @PostMapping("/save")
    public Long saveSupplier(@RequestBody SupplierDto supplier) {
        Long id = supplierService.saveSupplier(supplier);
        return id;
    }

    @PostMapping("/remove/{id}")
    public void removeSupplier(@PathVariable Long id) {
        supplierService.removeSupplier(id);
    }

    @PostMapping("/restore/{id}")
    public void restoreSupplier(@PathVariable Long id) {
        supplierService.restoreSupplier(id);
    }

}
