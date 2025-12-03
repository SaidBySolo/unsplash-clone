package moe.solo.cloneUnsplash.repository;

import moe.solo.cloneUnsplash.entity.Download;
import moe.solo.cloneUnsplash.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    long countByPhoto(Photo photo);
}
