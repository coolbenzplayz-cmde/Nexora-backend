package org.example.nexora.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finders
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    // Existence checks
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    // Status and role queries
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findAllActive();
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findUnverifiedUsers();
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isCreatorVerified = true")
    List<User> findVerifiedCreators(@Param("role") UserRole role);
    
    // Search queries
    @Query("SELECT u FROM User u WHERE u.username LIKE CONCAT('%', :query, '%') OR u.firstName LIKE CONCAT('%', :query, '%') OR u.lastName LIKE CONCAT('%', :query, '%')")
    List<User> searchUsers(@Param("query") String query);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE CONCAT('%', :query, '%') OR u.firstName LIKE CONCAT('%', :query, '%') OR u.lastName LIKE CONCAT('%', :query, '%') AND u.status = 'ACTIVE'")
    List<User> searchActiveUsers(@Param("query") String query);
    
    List<User> findByUsernameContainingIgnoreCase(String query);
    
    // Creator-specific queries
    @Query("SELECT u FROM User u WHERE u.role = 'CREATOR' AND u.status = 'ACTIVE' ORDER BY u.totalViews DESC")
    List<User> findTopCreatorsByViews();
    
    @Query("SELECT u FROM User u WHERE u.role = 'CREATOR' AND u.status = 'ACTIVE' ORDER BY u.followersCount DESC")
    List<User> findTopCreatorsByFollowers();
    
    @Query("SELECT u FROM User u WHERE u.role = 'CREATOR' AND u.status = 'ACTIVE' ORDER BY u.creatorEarnings DESC")
    List<User> findTopCreatorsByEarnings();
    
    // Statistics queries
    long countByStatus(User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    long countActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isCreatorVerified = true")
    long countVerifiedCreators();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countUsersCreatedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);
    
    // Analytics queries
    @Query("SELECT SUM(u.totalViews) FROM User u WHERE u.role = 'CREATOR'")
    Long sumTotalViewsByCreators();
    
    @Query("SELECT SUM(u.followersCount) FROM User u WHERE u.role = 'CREATOR'")
    Long sumTotalFollowersByCreators();
    
    @Query("SELECT SUM(u.creatorEarnings) FROM User u WHERE u.role = 'CREATOR'")
    Double sumTotalEarningsByCreators();
    
    // Location and demographic queries
    @Query("SELECT u FROM User u WHERE u.location IS NOT NULL AND u.location LIKE CONCAT('%', :location, '%')")
    List<User> findUsersByLocation(@Param("location") String location);
    
    @Query("SELECT u FROM User u WHERE u.dateOfBirth IS NOT NULL AND u.dateOfBirth <= :maxDate")
    List<User> findUsersOlderThan(@Param("maxDate") LocalDateTime maxDate);
    
    // Premium and verification queries
    @Query("SELECT u FROM User u WHERE u.isPremium = true AND u.status = 'ACTIVE'")
    List<User> findActivePremiumUsers();
    
    @Query("SELECT u FROM User u WHERE u.isCreatorVerified = true AND u.status = 'ACTIVE'")
    List<User> findActiveVerifiedCreators();
    
    // Activity queries
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since ORDER BY u.lastLogin DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegisteredUsers(@Param("since") LocalDateTime since);
    
    // Batch queries
    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findByUserIds(@Param("userIds") List<Long> userIds);
    
    @Query("SELECT u FROM User u WHERE u.id NOT IN :userIds AND u.role = 'CREATOR' AND u.status = 'ACTIVE'")
    List<User> findCreatorsNotInList(@Param("userIds") List<Long> userIds);
}
