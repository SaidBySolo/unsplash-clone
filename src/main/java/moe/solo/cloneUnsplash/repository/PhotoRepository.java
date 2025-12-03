package moe.solo.cloneUnsplash.repository;

import moe.solo.cloneUnsplash.entity.Photo;
import moe.solo.cloneUnsplash.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    Page<Photo> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Photo> findByUser(User user, Pageable pageable);

    Page<Photo> findByUserId(Long userId, Pageable pageable);

    /*
    태그가 지정된 사진 검색
    */
    @Query("SELECT p FROM Photo p JOIN p.tags t WHERE t.name = :tagName ORDER BY p.createdAt DESC")
    Page<Photo> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    /*
    제목또는 설명에서 대소문자 구분 없이 부분 일치 키워드로 사진 검색
    */
    @Query("SELECT p FROM Photo p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Photo> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Photo> findAllByOrderByLikesCountDesc(Pageable pageable);

    Page<Photo> findAllByOrderByViewsCountDesc(Pageable pageable);
}
