package org.example.nexora.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActive();
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findUnverifiedUsers();
    
    @Query("SELECT u FROM User u WHERE u.username LIKE CONCAT('%', :query, '%') OR u.fullName LIKE CONCAT('%', :query, '%')")
    List<User> searchUsers(String query);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(Role role);
    
    long countByActive(boolean active);
}