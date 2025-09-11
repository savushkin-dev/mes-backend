package com.host.SpringBootAutomationProduction.controller;


import com.host.SpringBootAutomationProduction.dto.RoleDTO;
import com.host.SpringBootAutomationProduction.dto.UpdateRolesDTO;
import com.host.SpringBootAutomationProduction.dto.UserDTO;
import com.host.SpringBootAutomationProduction.model.postgres.Role;
import com.host.SpringBootAutomationProduction.service.RoleService;
import com.host.SpringBootAutomationProduction.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ResponseEntity<Set<RoleDTO>> getAvailableRoles() {
        return ResponseEntity.ok(roleService.findAll().stream().map(this::convertToReportTemplateDTO).collect(Collectors.toSet()));
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<UserDTO> updateUserRoles(@PathVariable int userId, @RequestBody UpdateRolesDTO request) {
        UserDTO updatedUser = userService.updateUserRoles(userId, request.getRoles());
        return ResponseEntity.ok(updatedUser);
    }


    private RoleDTO convertToReportTemplateDTO(Role role) {
        return new ModelMapper().map(role, RoleDTO.class);
    }

}
