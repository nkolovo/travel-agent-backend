package com.travelagent.app.services;

import org.springframework.stereotype.Service;

import com.travelagent.app.models.Role;
import com.travelagent.app.repositories.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name).orElseThrow(() -> new RuntimeException("Count not find Role"));
    }
}
