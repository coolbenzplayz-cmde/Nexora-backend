package org.example.nexora.social;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingIdAndIsActiveTrue(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingIdAndIsActiveFalse(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingIdAndIsActiveTrue(Long followerId, Long followingId);

    @Query("SELECT f FROM Follow f WHERE f.followerId = :userId AND f.isActive = true")
    Page<Follow> findFollowingByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.followingId = :userId AND f.isActive = true")
    Page<Follow> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followerId = :userId AND f.isActive = true")
    long countFollowingByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followingId = :userId AND f.isActive = true")
    long countFollowersByUserId(@Param("userId") Long userId);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :userId AND f.isActive = true")
    List<Long> findFollowingIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.followerId FROM Follow f WHERE f.followingId = :userId AND f.isActive = true")
    List<Long> findFollowerIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Follow f SET f.isActive = false, f.unfollowedAt = CURRENT_TIMESTAMP " +
           "WHERE f.followerId = :followerId AND f.followingId = :followingId AND f.isActive = true")
    int unfollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Query("SELECT f FROM Follow f WHERE (f.followerId = :userId OR f.followingId = :userId) AND f.isActive = true")
    Page<Follow> findAllFollowActivityByUserId(@Param("userId") Long userId, Pageable pageable);
}
