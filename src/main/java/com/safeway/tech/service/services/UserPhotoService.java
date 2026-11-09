package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.User;
import com.safeway.tech.domain.models.UserPhoto;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.UserPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
public class UserPhotoService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;

    private final UserPhotoRepository photoRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public boolean hasCurrentUserPhoto() {
        return photoRepository.existsByUserIdAndActiveTrue(currentUser().getId());
    }

    @Transactional(readOnly = true)
    public UserPhoto findCurrentUserPhoto() {
        return photoRepository.findByUserIdAndActiveTrue(currentUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile photo was not found"));
    }

    @Transactional
    public void updateCurrentUserPhoto(MultipartFile file) {
        User user = currentUser();
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Photo must not exceed 5 MB");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read uploaded photo", exception);
        }
        String contentType = validateImage(content);
        UserPhoto photo = photoRepository.findByUserIdAndActiveTrue(user.getId()).orElseGet(UserPhoto::new);
        photo.setUser(user);
        photo.setContent(content);
        photo.setContentType(contentType);
        photoRepository.save(photo);
    }

    @Transactional
    public void deleteCurrentUserPhoto() {
        // Remove the binary data as well, rather than retaining it through soft delete.
        photoRepository.findByUserIdAndActiveTrue(currentUser().getId()).ifPresent(photoRepository::delete);
    }

    private User currentUser() {
        User user = userService.findCurrentUser();
        if (!user.isActive()) {
            throw new EntityNotFoundException("User was not found");
        }
        return user;
    }

    private String validateImage(byte[] content) {
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(content))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName();
                String contentType;
                if ("JPEG".equalsIgnoreCase(format)) {
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                } else if ("PNG".equalsIgnoreCase(format)) {
                    contentType = MediaType.IMAGE_PNG_VALUE;
                } else {
                    throw invalidImage();
                }
                if (reader.getWidth(0) > MAX_DIMENSION || reader.getHeight(0) > MAX_DIMENSION) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo dimensions must not exceed 4096 x 4096");
                }
                reader.read(0);
                return contentType;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JPEG or PNG image", exception);
        }
    }

    private ResponseStatusException invalidImage() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo must be a valid JPEG or PNG image");
    }
}
