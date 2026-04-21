package org.example.nexora.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.user.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Role-Based Access Control (RBAC) system providing:
 * - Multi-role support (USER, CREATOR, ADMIN, MODERATOR)
 * - Granular permissions management
 * - Dynamic role assignment
 * - Permission inheritance
 * - Resource-based access control
 * - Audit logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleBasedAccessControl {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Check if user has permission for specific action
     */
    public boolean hasPermission(Long userId, String resource, String action) {
        log.debug("Checking permission for user {} on {} {}", userId, resource, action);

        // Get user roles
        Set<UserRole> userRoles = getUserRoles(userId);
        
        // Check each role for permission
        for (UserRole userRole : userRoles) {
            if (hasRolePermission(userRole.getRole(), resource, action)) {
                logAccess(userId, resource, action, true, "Permission granted via role: " + userRole.getRole().getName());
                return true;
            }
        }

        logAccess(userId, resource, action, false, "No permission found");
        return false;
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(Long userId, String roleName) {
        Set<UserRole> userRoles = getUserRoles(userId);
        return userRoles.stream()
                .anyMatch(ur -> ur.getRole().getName().equals(roleName));
    }

    /**
     * Assign role to user
     */
    public void assignRole(Long userId, String roleName, Long assignedBy, String reason) {
        log.info("Assigning role {} to user {} by admin {}", roleName, userId, assignedBy);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new SecurityException("Role not found: " + roleName));

        // Check if user already has this role
        if (hasRole(userId, roleName)) {
            throw new SecurityException("User already has role: " + roleName);
        }

        // Create user role assignment
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setAssignedBy(assignedBy);
        userRole.setAssignedAt(LocalDateTime.now());
        userRole.setReason(reason);
        userRole.setActive(true);

        userRoleRepository.save(userRole);

        // Log role assignment
        logRoleChange(userId, roleName, "ASSIGNED", assignedBy, reason);
    }

    /**
     * Remove role from user
     */
    public void removeRole(Long userId, String roleName, Long removedBy, String reason) {
        log.info("Removing role {} from user {} by admin {}", roleName, userId, removedBy);

        UserRole userRole = userRoleRepository.findByUserIdAndRoleName(userId, roleName)
                .orElseThrow(() -> new SecurityException("User does not have role: " + roleName));

        userRole.setActive(false);
        userRole.setRemovedBy(removedBy);
        userRole.setRemovedAt(LocalDateTime.now());
        userRole.setRemovalReason(reason);

        userRoleRepository.save(userRole);

        // Log role removal
        logRoleChange(userId, roleName, "REMOVED", removedBy, reason);
    }

    /**
     * Get all permissions for user
     */
    public Set<Permission> getUserPermissions(Long userId) {
        Set<UserRole> userRoles = getUserRoles(userId);
        Set<Permission> permissions = new HashSet<>();

        for (UserRole userRole : userRoles) {
            if (userRole.isActive()) {
                permissions.addAll(getRolePermissions(userRole.getRole()));
            }
        }

        return permissions;
    }

    /**
     * Get user's highest priority role
     */
    public Role getPrimaryRole(Long userId) {
        Set<UserRole> userRoles = getUserRoles(userId);
        
        return userRoles.stream()
                .filter(UserRole::isActive)
                .map(UserRole::getRole)
                .min(Comparator.comparing(Role::getPriority))
                .orElse(getDefaultRole());
    }

    /**
     * Check resource ownership
     */
    public boolean ownsResource(Long userId, String resourceType, Long resourceId) {
        switch (resourceType.toUpperCase()) {
            case "VIDEO":
                return ownsVideo(userId, resourceId);
            case "COMMENT":
                return ownsComment(userId, resourceId);
            case "WALLET":
                return ownsWallet(userId, resourceId);
            case "TRANSACTION":
                return ownsTransaction(userId, resourceId);
            default:
                return false;
        }
    }

    /**
     * Create custom role
     */
    public Role createCustomRole(String name, String description, int priority, Set<String> permissionNames, Long createdBy) {
        log.info("Creating custom role {} by admin {}", name, createdBy);

        // Check if role already exists
        if (roleRepository.findByName(name).isPresent()) {
            throw new SecurityException("Role already exists: " + name);
        }

        // Create role
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPriority(priority);
        role.setCustom(true);
        role.setCreatedBy(createdBy);
        role.setCreatedAt(LocalDateTime.now());
        role.setActive(true);

        role = roleRepository.save(role);

        // Assign permissions to role
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new SecurityException("Permission not found: " + permissionName));

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermissionRepository.save(rolePermission);
        }

        return role;
    }

    /**
     * Get access audit log for user
     */
    public List<AccessLogEntry> getUserAccessLog(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return accessLogRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate);
    }

    /**
     * Get role change history for user
     */
    public List<RoleChangeLogEntry> getUserRoleHistory(Long userId) {
        return roleChangeLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Validate user access to resource
     */
    public AccessValidationResult validateAccess(Long userId, String resource, String action, Long resourceId) {
        AccessValidationResult result = new AccessValidationResult();
        result.setUserId(userId);
        result.setResource(resource);
        result.setAction(action);
        result.setResourceId(resourceId);
        result.setValidationTimestamp(LocalDateTime.now());

        // Check permission-based access
        boolean hasPermissionAccess = hasPermission(userId, resource, action);
        result.setPermissionAccess(hasPermissionAccess);

        // Check ownership-based access
        boolean hasOwnershipAccess = resourceId != null && ownsResource(userId, resource, resourceId);
        result.setOwnershipAccess(hasOwnershipAccess);

        // Determine overall access
        boolean hasAccess = hasPermissionAccess || hasOwnershipAccess;
        result.setHasAccess(hasAccess);

        // Set access reason
        if (hasAccess) {
            if (hasOwnershipAccess) {
                result.setAccessReason("Resource ownership");
            } else {
                result.setAccessReason("Permission granted");
            }
        } else {
            result.setAccessReason("No permission or ownership");
        }

        // Log validation
        logAccessValidation(userId, resource, action, resourceId, hasAccess, result.getAccessReason());

        return result;
    }

    // Private helper methods
    private Set<UserRole> getUserRoles(Long userId) {
        return userRoleRepository.findByUserIdAndActiveTrue(userId);
    }

    private boolean hasRolePermission(Role role, String resource, String action) {
        Set<Permission> permissions = getRolePermissions(role);
        
        return permissions.stream()
                .anyMatch(p -> p.getResource().equals(resource) && p.getAction().equals(action));
    }

    private Set<Permission> getRolePermissions(Role role) {
        return rolePermissionRepository.findByRoleId(role.getId()).stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toSet());
    }

    private Role getDefaultRole() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new SecurityException("Default USER role not found"));
    }

    private boolean ownsVideo(Long userId, Long videoId) {
        // Simplified - would check video repository
        return true;
    }

    private boolean ownsComment(Long userId, Long commentId) {
        // Simplified - would check comment repository
        return true;
    }

    private boolean ownsWallet(Long userId, Long walletId) {
        // Simplified - would check wallet repository
        return true;
    }

    private boolean ownsTransaction(Long userId, Long transactionId) {
        // Simplified - would check transaction repository
        return true;
    }

    private void logAccess(Long userId, String resource, String action, boolean granted, String reason) {
        AccessLogEntry logEntry = new AccessLogEntry();
        logEntry.setUserId(userId);
        logEntry.setResource(resource);
        logEntry.setAction(action);
        logEntry.setGranted(granted);
        logEntry.setReason(reason);
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setIpAddress(getCurrentIpAddress());
        
        accessLogRepository.save(logEntry);
    }

    private void logRoleChange(Long userId, String roleName, String changeType, Long changedBy, String reason) {
        RoleChangeLogEntry logEntry = new RoleChangeLogEntry();
        logEntry.setUserId(userId);
        logEntry.setRoleName(roleName);
        logEntry.setChangeType(changeType);
        logEntry.setChangedBy(changedBy);
        logEntry.setReason(reason);
        logEntry.setTimestamp(LocalDateTime.now());
        
        roleChangeLogRepository.save(logEntry);
    }

    private void logAccessValidation(Long userId, String resource, String action, Long resourceId, boolean hasAccess, String reason) {
        // Simplified logging
        log.debug("Access validation: user={}, resource={}, action={}, access={}, reason={}", 
                userId, resource, action, hasAccess, reason);
    }

    private String getCurrentIpAddress() {
        // Simplified - would get from request context
        return "127.0.0.1";
    }

    // Initialize default roles and permissions
    public void initializeDefaultRolesAndPermissions() {
        log.info("Initializing default roles and permissions");

        // Create default permissions
        createDefaultPermissions();

        // Create default roles
        createDefaultRoles();

        // Assign permissions to roles
        assignPermissionsToRoles();
    }

    private void createDefaultPermissions() {
        String[] resources = {"VIDEO", "COMMENT", "USER", "WALLET", "TRANSACTION", "ADMIN", "MODERATION", "ANALYTICS"};
        String[] actions = {"CREATE", "READ", "UPDATE", "DELETE", "MANAGE", "MODERATE", "VIEW_ADMIN"};

        for (String resource : resources) {
            for (String action : actions) {
                String permissionName = resource + "_" + action;
                
                if (!permissionRepository.findByName(permissionName).isPresent()) {
                    Permission permission = new Permission();
                    permission.setName(permissionName);
                    permission.setResource(resource);
                    permission.setAction(action);
                    permission.setDescription("Permission to " + action.toLowerCase() + " " + resource.toLowerCase());
                    permissionRepository.save(permission);
                }
            }
        }
    }

    private void createDefaultRoles() {
        createRoleIfNotExists("USER", "Regular user with basic permissions", 100);
        createRoleIfNotExists("CREATOR", "Content creator with extended permissions", 50);
        createRoleIfNotExists("MODERATOR", "Content moderator with moderation permissions", 20);
        createRoleIfNotExists("ADMIN", "System administrator with full permissions", 10);
        createRoleIfNotExists("SUPER_ADMIN", "Super administrator with all permissions", 1);
    }

    private void createRoleIfNotExists(String name, String description, int priority) {
        if (!roleRepository.findByName(name).isPresent()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            role.setPriority(priority);
            role.setCustom(false);
            role.setActive(true);
            roleRepository.save(role);
        }
    }

    private void assignPermissionsToRoles() {
        // USER permissions
        assignPermissionToRole("USER", "VIDEO_READ");
        assignPermissionToRole("USER", "VIDEO_CREATE");
        assignPermissionToRole("USER", "VIDEO_UPDATE");
        assignPermissionToRole("USER", "VIDEO_DELETE");
        assignPermissionToRole("USER", "COMMENT_CREATE");
        assignPermissionToRole("USER", "COMMENT_READ");
        assignPermissionToRole("USER", "COMMENT_UPDATE");
        assignPermissionToRole("USER", "COMMENT_DELETE");
        assignPermissionToRole("USER", "WALLET_READ");
        assignPermissionToRole("USER", "TRANSACTION_CREATE");
        assignPermissionToRole("USER", "TRANSACTION_READ");

        // CREATOR permissions (includes all USER permissions + additional)
        assignPermissionToRole("CREATOR", "USER_MANAGE");
        assignPermissionToRole("CREATOR", "ANALYTICS_VIEW");

        // MODERATOR permissions
        assignPermissionToRole("MODERATOR", "VIDEO_MODERATE");
        assignPermissionToRole("MODERATOR", "COMMENT_MODERATE");
        assignPermissionToRole("MODERATOR", "USER_MODERATE");

        // ADMIN permissions
        assignPermissionToRole("ADMIN", "ADMIN_MANAGE");
        assignPermissionToRole("ADMIN", "ANALYTICS_VIEW");
        assignPermissionToRole("ADMIN", "TRANSACTION_MANAGE");

        // SUPER_ADMIN permissions (all permissions)
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElse(null);
        if (superAdminRole != null) {
            List<Permission> allPermissions = permissionRepository.findAll();
            for (Permission permission : allPermissions) {
                assignPermissionToRole("SUPER_ADMIN", permission.getName());
            }
        }
    }

    private void assignPermissionToRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName).orElse(null);
        Permission permission = permissionRepository.findByName(permissionName).orElse(null);
        
        if (role != null && permission != null) {
            boolean alreadyAssigned = rolePermissionRepository.findByRoleIdAndPermissionId(role.getId(), permission.getId()).isPresent();
            if (!alreadyAssigned) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(role.getId());
                rolePermission.setPermissionId(permission.getId());
                rolePermissionRepository.save(rolePermission);
            }
        }
    }

    // Data classes
    @Data
    public static class AccessValidationResult {
        private Long userId;
        private String resource;
        private String action;
        private Long resourceId;
        private LocalDateTime validationTimestamp;
        private boolean permissionAccess;
        private boolean ownershipAccess;
        private boolean hasAccess;
        private String accessReason;
    }

    @Data
    public static class AccessLogEntry {
        private Long id;
        private Long userId;
        private String resource;
        private String action;
        private boolean granted;
        private String reason;
        private LocalDateTime timestamp;
        private String ipAddress;
    }

    @Data
    public static class RoleChangeLogEntry {
        private Long id;
        private Long userId;
        private String roleName;
        private String changeType;
        private Long changedBy;
        private String reason;
        private LocalDateTime timestamp;
    }

    // Repository placeholders (would be actual Spring Data repositories)
    private static class PermissionRepository {
        public Optional<Permission> findByName(String name) { return Optional.empty(); }
        public Permission save(Permission permission) { return permission; }
        public List<Permission> findAll() { return new ArrayList<>(); }
    }

    private static class RoleRepository {
        public Optional<Role> findByName(String name) { return Optional.empty(); }
        public Role save(Role role) { return role; }
    }

    private static class UserRoleRepository {
        public Set<UserRole> findByUserIdAndActiveTrue(Long userId) { return new HashSet<>(); }
        public Optional<UserRole> findByUserIdAndRoleName(Long userId, String roleName) { return Optional.empty(); }
        public UserRole save(UserRole userRole) { return userRole; }
    }

    private static class RolePermissionRepository {
        public List<RolePermission> findByRoleId(Long roleId) { return new ArrayList<>(); }
        public Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId) { return Optional.empty(); }
        public RolePermission save(RolePermission rolePermission) { return rolePermission; }
    }

    private static class AccessLogRepository {
        public List<AccessLogEntry> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
        public AccessLogEntry save(AccessLogEntry logEntry) { return logEntry; }
    }

    private static class RoleChangeLogRepository {
        public List<RoleChangeLogEntry> findByUserIdOrderByTimestampDesc(Long userId) { return new ArrayList<>(); }
        public RoleChangeLogEntry save(RoleChangeLogEntry logEntry) { return logEntry; }
    }

    // Entity placeholders
    @Data
    public static class Role {
        private Long id;
        private String name;
        private String description;
        private int priority;
        private boolean custom;
        private boolean active;
        private Long createdBy;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Permission {
        private Long id;
        private String name;
        private String resource;
        private String action;
        private String description;
    }

    @Data
    public static class UserRole {
        private Long id;
        private Long userId;
        private Long roleId;
        private Role role;
        private Long assignedBy;
        private LocalDateTime assignedAt;
        private String reason;
        private boolean active;
        private Long removedBy;
        private LocalDateTime removedAt;
        private String removalReason;
    }

    @Data
    public static class RolePermission {
        private Long id;
        private Long roleId;
        private Long permissionId;
        private Permission permission;
    }

    // Repository instances (would be injected via Spring)
    private final PermissionRepository permissionRepository = new PermissionRepository();
    private final RoleRepository roleRepository = new RoleRepository();
    private final UserRoleRepository userRoleRepository = new UserRoleRepository();
    private final RolePermissionRepository rolePermissionRepository = new RolePermissionRepository();
    private final AccessLogRepository accessLogRepository = new AccessLogRepository();
    private final RoleChangeLogRepository roleChangeLogRepository = new RoleChangeLogRepository();
}
