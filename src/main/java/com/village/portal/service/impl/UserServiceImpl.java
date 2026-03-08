package com.village.portal.service.impl;

import com.village.portal.aspect.Auditable;
import com.village.portal.dto.request.CreateUserRequest;
import com.village.portal.dto.response.UserResponse;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.enums.Role;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.DuplicateResourceException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, tableName = "users",
               description = "New user account created")
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setCreatedBy(getCurrentUser());

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRoleAndIsActive(role, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "users",
               description = "User account activated")
    public UserResponse activateUser(Long id) {
        User user = findOrThrow(id);
        user.setIsActive(true);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "users",
               description = "User account deactivated")
    public UserResponse deactivateUser(Long id) {
        User user = findOrThrow(id);

        // Prevent self-deactivation
        UserDetailsImpl current = getCurrentUserDetails();
        if (current != null && current.getId().equals(id)) {
            throw new BusinessException("SELF_DEACTIVATION",
                    "You cannot deactivate your own account");
        }

        user.setIsActive(false);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "users",
               description = "User password reset by admin")
    public UserResponse resetPassword(Long id, String newPassword) {
        User user = findOrThrow(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return toResponse(userRepository.save(user));
    }

    // ── Helpers ──

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private User getCurrentUser() {
        UserDetailsImpl details = getCurrentUserDetails();
        if (details != null) {
            return userRepository.findById(details.getId()).orElse(null);
        }
        return null;
    }

    private UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) auth.getPrincipal();
        }
        return null;
    }

    // ── Mapper ──

    private UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setFullName(u.getFullName());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setRole(u.getRole());
        r.setIsActive(u.getIsActive());
        r.setLastLogin(u.getLastLogin());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
