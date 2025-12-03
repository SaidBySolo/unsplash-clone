package moe.solo.cloneUnsplash.repository;

import moe.solo.cloneUnsplash.entity.Follow;
import moe.solo.cloneUnsplash.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerAndFollowing(User follower, User following);

    Page<Follow> findByFollower(User follower, Pageable pageable);

    Page<Follow> findByFollowing(User following, Pageable pageable);

    long countByFollower(User follower);

    long countByFollowing(User following);
}
