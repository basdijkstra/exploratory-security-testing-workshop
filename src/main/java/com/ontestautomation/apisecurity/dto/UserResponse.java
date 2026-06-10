package com.ontestautomation.apisecurity.dto;

import com.ontestautomation.apisecurity.model.Role;

public record UserResponse(Long id, String username, String email, Role role) {}
