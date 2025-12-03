package moe.solo.cloneUnsplash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.solo.cloneUnsplash.entity.Like;
import moe.solo.cloneUnsplash.entity.Photo;
import moe.solo.cloneUnsplash.entity.User;
import moe.solo.cloneUnsplash.exception.BadRequestException;
import moe.solo.cloneUnsplash.exception.ResourceNotFoundException;
import moe.solo.cloneUnsplash.repository.LikeRepository;
import moe.solo.cloneUnsplash.repository.PhotoRepository;
import moe.solo.cloneUnsplash.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Transactional
    public void likePhoto(Long photoId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다"));

        if (likeRepository.existsByUserAndPhoto(user, photo)) {
            throw new BadRequestException("이미 좋아요를 눌렀습니다");
        }

        Like like = Like.builder()
                .user(user)
                .photo(photo)
                .build();

        likeRepository.save(like);

        // 좋아요 수 증가
        photo.setLikesCount(photo.getLikesCount() + 1);
        photoRepository.save(photo);

        log.info("User {} liked photo {}", username, photoId);
    }

    @Transactional
    public void unlikePhoto(Long photoId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진을 찾을 수 없습니다"));

        if (!likeRepository.existsByUserAndPhoto(user, photo)) {
            throw new BadRequestException("좋아요를 누르지 않았습니다");
        }

        likeRepository.deleteByUserAndPhoto(user, photo);

        // 좋아요 수 감소
        photo.setLikesCount(Math.max(0, photo.getLikesCount() - 1));
        photoRepository.save(photo);

        log.info("User {} unliked photo {}", username, photoId);
    }
}
