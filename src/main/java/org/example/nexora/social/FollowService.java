package org.example.nexora.social;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public Follow followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException("Users cannot follow themselves");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new BusinessException("Follower not found"));
        
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new BusinessException("User to follow not found"));

        if (followRepository.existsByFollowerIdAndFollowingIdAndIsActiveTrue(followerId, followingId)) {
            throw new BusinessException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        follow.setCreatedAt(LocalDateTime.now());
        follow.setIsActive(true);

        Follow savedFollow = followRepository.save(follow);

        // Update follower counts
        updateFollowerCounts(followerId, followingId);

        log.info("User {} followed user {}", followerId, followingId);
        return savedFollow;
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingIdAndIsActiveTrue(followerId, followingId)
                .orElseThrow(() -> new BusinessException("Not following this user"));

        follow.unfollow();
        followRepository.save(follow);

        // Update follower counts
        updateFollowerCounts(followerId, followingId);

        log.info("User {} unfollowed user {}", followerId, followingId);
    }

    @Transactional
    public void refollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingIdAndIsActiveFalse(followerId, followingId)
                .orElseThrow(() -> new BusinessException("No previous follow relationship found"));

        follow.refollow();
        followRepository.save(follow);

        // Update follower counts
        updateFollowerCounts(followerId, followingId);

        log.info("User {} refollowed user {}", followerId, followingId);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingIdAndIsActiveTrue(followerId, followingId);
    }

    public Page<Follow> getFollowing(Long userId, Pageable pageable) {
        return followRepository.findFollowingByUserId(userId, pageable);
    }

    public Page<Follow> getFollowers(Long userId, Pageable pageable) {
        return followRepository.findFollowersByUserId(userId, pageable);
    }

    public long getFollowingCount(Long userId) {
        return followRepository.countFollowingByUserId(userId);
    }

    public long getFollowersCount(Long userId) {
        return followRepository.countFollowersByUserId(userId);
    }

    public List<Long> getFollowingIds(Long userId) {
        return followRepository.findFollowingIdsByUserId(userId);
    }

    public List<Long> getFollowerIds(Long userId) {
        return followRepository.findFollowerIdsByUserId(userId);
    }

    public Page<Follow> getAllFollowActivity(Long userId, Pageable pageable) {
        return followRepository.findAllFollowActivityByUserId(userId, pageable);
    }

    @Transactional
    public void updateFollowerCounts(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId).orElse(null);
        User following = userRepository.findById(followingId).orElse(null);

        if (follower != null) {
            long followingCount = followRepository.countFollowingByUserId(followerId);
            follower.setFollowingCount(followingCount);
            userRepository.save(follower);
        }

        if (following != null) {
            long followersCount = followRepository.countFollowersByUserId(followingId);
            following.setFollowersCount(followersCount);
            userRepository.save(following);
        }
    }

    @Transactional
    public void promoteToCreator(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.CREATOR);
            userRepository.save(user);
            log.info("User {} promoted to CREATOR role", userId);
        } else {
            throw new BusinessException("User is already a creator or has higher role");
        }
    }

    @Transactional
    public void verifyCreator(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (user.getRole() == UserRole.CREATOR) {
            user.setIsCreatorVerified(true);
            user.setCreatorVerifiedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Creator {} verified", userId);
        } else {
            throw new BusinessException("Only creators can be verified");
        }
    }

    public List<User> getRecommendedCreators(Long userId, int limit) {
        List<Long> followingIds = getFollowingIds(userId);
        
        // Find creators that the user is not following
        return userRepository.findByRoleAndIdNotIn(UserRole.CREATOR, followingIds)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
