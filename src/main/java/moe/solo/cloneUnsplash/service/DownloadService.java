package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.solo.cloneUnsplash.entity.Download;
import moe.solo.cloneUnsplash.entity.Photo;
import moe.solo.cloneUnsplash.entity.User;
import moe.solo.cloneUnsplash.exception.ResourceNotFoundException;
import moe.solo.cloneUnsplash.repository.DownloadRepository;
import moe.solo.cloneUnsplash.repository.PhotoRepository;
import moe.solo.cloneUnsplash.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadService {

    private final DownloadRepository downloadRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordDownload(Long photoId, String username, String ipAddress) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다"));

        User user = username != null
                ? userRepository.findByUsername(username).orElse(null)
                : null;

        Download download = Download.builder()
                .photo(photo)
                .user(user)
                .ipAddress(ipAddress)
                .build();

        downloadRepository.save(download);

        // 다운로드 수 증가
        photo.setDownloadsCount(photo.getDownloadsCount() + 1);
        photoRepository.save(photo);

        log.info("Download recorded for photo {} by {}", photoId, username != null ? username : ipAddress);
    }
}
