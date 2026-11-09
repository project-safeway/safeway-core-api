package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.User;
import com.safeway.tech.domain.models.UserPhoto;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.UserPhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPhotoServiceTest {

    @Mock
    private UserPhotoRepository photoRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private UserPhotoService photoService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        when(userService.findCurrentUser()).thenReturn(user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"jpeg", "png"})
    void storesImageUsingDetectedContentTypeInsteadOfClientMetadata(String format) throws Exception {
        byte[] content = image(format, 2, 2);
        when(photoRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.empty());

        photoService.updateCurrentUserPhoto(new MockMultipartFile("file", "fake.txt", "text/plain", content));

        var saved = ArgumentCaptor.forClass(UserPhoto.class);
        verify(photoRepository).save(saved.capture());
        assertSame(user, saved.getValue().getUser());
        assertEquals("image/" + format, saved.getValue().getContentType());
        assertArrayEquals(content, saved.getValue().getContent());
    }

    @Test
    void replacesExistingPhotoWithoutCreatingAnotherRecord() throws Exception {
        UserPhoto existing = new UserPhoto();
        existing.setId(UUID.randomUUID());
        when(photoRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(existing));
        byte[] content = image("png", 2, 2);

        photoService.updateCurrentUserPhoto(new MockMultipartFile("file", content));

        verify(photoRepository).save(existing);
        assertArrayEquals(content, existing.getContent());
    }

    @Test
    void rejectsEmptyPhoto() {
        assertRejected(new byte[0], HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsOversizedPhoto() {
        assertRejected(new byte[5 * 1024 * 1024 + 1], HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @Test
    void rejectsFakeImageEvenWhenDeclaredAsJpeg() {
        assertRejected("not an image".getBytes(java.nio.charset.StandardCharsets.UTF_8), HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsUnsupportedImageFormat() throws Exception {
        assertRejected(image("gif", 2, 2), HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsExcessiveDimensionsBeforeDecodingPixels() throws Exception {
        assertRejected(image("png", 4097, 1), HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsTruncatedImage() throws Exception {
        byte[] content = image("png", 2, 2);
        assertRejected(java.util.Arrays.copyOf(content, 35), HttpStatus.BAD_REQUEST);
    }

    @Test
    void readsOnlyAuthenticatedUsersPhoto() {
        UserPhoto photo = new UserPhoto();
        when(photoRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(photo));

        assertSame(photo, photoService.findCurrentUserPhoto());
        verify(photoRepository).findByUserIdAndActiveTrue(user.getId());
    }

    @Test
    void reportsMissingPhoto() {
        when(photoRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> photoService.findCurrentUserPhoto());
    }

    @Test
    void deletesOnlyAuthenticatedUsersPhotoIncludingBinaryData() {
        UserPhoto photo = new UserPhoto();
        when(photoRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(photo));

        photoService.deleteCurrentUserPhoto();

        verify(photoRepository).delete(photo);
    }

    @Test
    void removalWithoutPhotoIsIdempotent() {
        when(photoRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.empty());

        photoService.deleteCurrentUserPhoto();

        verify(photoRepository, never()).delete(any(UserPhoto.class));
    }

    @Test
    void rejectsInactiveUserWithoutAccessingPhotos() {
        user.setActive(false);

        assertThrows(EntityNotFoundException.class, () -> photoService.findCurrentUserPhoto());
        verifyNoInteractions(photoRepository);
    }

    private void assertRejected(byte[] content, HttpStatus status) {
        var exception = assertThrows(ResponseStatusException.class,
                () -> photoService.updateCurrentUserPhoto(
                        new MockMultipartFile("file", "photo.jpg", "image/jpeg", content)));
        assertEquals(status, exception.getStatusCode());
        verifyNoInteractions(photoRepository);
    }

    private byte[] image(String format, int width, int height) throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, output);
        return output.toByteArray();
    }
}
