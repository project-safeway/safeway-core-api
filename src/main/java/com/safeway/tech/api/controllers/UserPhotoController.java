package com.safeway.tech.api.controllers;

import com.safeway.tech.domain.models.UserPhoto;
import com.safeway.tech.service.services.UserPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users/me/photo")
@RequiredArgsConstructor
public class UserPhotoController {

    private final UserPhotoService photoService;

    @GetMapping
    public ResponseEntity<byte[]> getPhoto() {
        UserPhoto photo = photoService.findCurrentUserPhoto();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .contentLength(photo.getContent().length)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(photo.getContent());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePhoto(@RequestPart("file") MultipartFile file) {
        photoService.updateCurrentUserPhoto(file);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePhoto() {
        photoService.deleteCurrentUserPhoto();
        return ResponseEntity.noContent().build();
    }
}
