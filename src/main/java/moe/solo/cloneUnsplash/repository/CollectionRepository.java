package moe.solo.cloneUnsplash.repository;

import moe.solo.cloneUnsplash.entity.Collection;
import moe.solo.cloneUnsplash.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Page<Collection> findByUser(User user, Pageable pageable);

    Page<Collection> findByUserId(Long userId, Pageable pageable);

    Page<Collection> findByIsPrivateFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<Collection> findByUserAndIsPrivateFalse(User user, Pageable pageable);
}
