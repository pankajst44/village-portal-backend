package com.village.portal.service;

import com.village.portal.dto.request.CreateUserRequest;
import com.village.portal.dto.response.UserResponse;
import com.village.portal.enums.Role;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(Role role);

    UserResponse activateUser(Long id);

    UserResponse deactivateUser(Long id);

    UserResponse resetPassword(Long id, String newPassword);
}
