package com.host.SpringBootAutomationProduction.service;


import com.host.SpringBootAutomationProduction.model.postgres.Role;
import com.host.SpringBootAutomationProduction.repositories.postgres.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public Set<Role> findByRoleNames(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (Role role : roleRepository.findAll()) {
            if (roleNames.contains(role.getName())) {
                roles.add(role);
            }
        }
        return roles;
    }



}
