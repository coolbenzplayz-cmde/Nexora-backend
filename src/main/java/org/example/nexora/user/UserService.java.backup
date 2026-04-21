package org.example.nexora.user;

import org.example.nexora.common.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
    }

    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findAllActive();
    }

    public User updateUser(String id, User updatedUser) {
        User user = getUserById(id);
        
        if (updatedUser.getFullName() != null) {
            user.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getBio() != null) {
            user.setBio(updatedUser.getBio());
        }
        if (updatedUser.getLocation() != null) {
            user.setLocation(updatedUser.getLocation());
        }
        if (updatedUser.getWebsite() != null) {
            user.setWebsite(updatedUser.getWebsite());
        }
        if (updatedUser.getAvatarUrl() != null) {
            user.setAvatarUrl(updatedUser.getAvatarUrl());
        }
        
        return userRepository.save(user);
    }

    public void deactivateUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(String id) {
        User user = getUserById(id);
        user.setActive(true);
        userRepository.save(user);
    }

    public void followUser(String currentUserId, String targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException("Cannot follow yourself", "SELF_FOLLOW");
        }
        
        User currentUser = getUserById(currentUserId);
        User targetUser = getUserById(targetUserId);
        
        currentUser.setFollowingCount(currentUser.getFollowingCount() + 1);
        targetUser.setFollowersCount(targetUser.getFollowersCount() + 1);
        
        userRepository.save(currentUser);
        userRepository.save(targetUser);
    }

    public void unfollowUser(String currentUserId, String targetUserId) {
        User currentUser = getUserById(currentUserId);
        User targetUser = getUserById(targetUserId);
        
        if (currentUser.getFollowingCount() > 0) {
            currentUser.setFollowingCount(currentUser.getFollowingCount() - 1);
        }
        if (targetUser.getFollowersCount() > 0) {
            targetUser.setFollowersCount(targetUser.getFollowersCount() - 1);
        }
        
        userRepository.save(currentUser);
        userRepository.save(targetUser);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getActiveUsers() {
        return userRepository.countByActive(true);
    }

    public long getInactiveUsers() {
        return userRepository.countByActive(false);
    }
}