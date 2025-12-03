package moe.solo.cloneUnsplash.repository;

import moe.solo.cloneUnsplash.entity.Like;
import moe.solo.cloneUnsplash.entity.Photo;
import moe.solo.cloneUnsplash.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPhoto(User user, Photo photo);

    boolean existsByUserAndPhoto(User user, Photo photo);

    void deleteByUserAndPhoto(User user, Photo photo);

    Page<Like> findByUser(User user, Pageable pageable);

    long countByPhoto(Photo photo);
}
