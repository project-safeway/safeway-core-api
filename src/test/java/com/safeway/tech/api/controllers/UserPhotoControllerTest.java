package com.safeway.tech.api.controllers;

import com.safeway.tech.domain.models.User;
import com.safeway.tech.domain.models.UserPhoto;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.infra.exception.handler.GlobalExceptionHandler;
import com.safeway.tech.service.services.UserPhotoService;
import com.safeway.tech.service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserPhotoControllerTest {

    @Mock
    private UserPhotoService photoService;
    @Mock
    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new UserPhotoController(photoService), new UserController(userService, photoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsBinaryPhotoWithoutJsonEncodingOrCaching() throws Exception {
        UserPhoto photo = new UserPhoto();
        photo.setContent(new byte[]{1, 2, 3});
        photo.setContentType(MediaType.IMAGE_PNG_VALUE);
        when(photoService.findCurrentUserPhoto()).thenReturn(photo);

        mockMvc.perform(get("/users/me/photo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(photo.getContent()))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void acceptsMultipartPutWithFilePart() throws Exception {
        var file = new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1});

        mockMvc.perform(multipart(HttpMethod.PUT, "/users/me/photo").file(file))
                .andExpect(status().isNoContent());

        verify(photoService).updateCurrentUserPhoto(file);
    }

    @Test
    void rejectsMissingFilePart() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/users/me/photo"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsJsonInsteadOfMultipart() throws Exception {
        mockMvc.perform(put("/users/me/photo").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void returns413ForMultipartSizeLimit() throws Exception {
        var file = new MockMultipartFile("file", new byte[]{1});
        doThrow(new MaxUploadSizeExceededException(5 * 1024 * 1024))
                .when(photoService).updateCurrentUserPhoto(file);

        mockMvc.perform(multipart(HttpMethod.PUT, "/users/me/photo").file(file))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void returns404WhenPhotoIsMissing() throws Exception {
        when(photoService.findCurrentUserPhoto()).thenThrow(new EntityNotFoundException("Profile photo was not found"));

        mockMvc.perform(get("/users/me/photo")).andExpect(status().isNotFound());
    }

    @Test
    void removesPhoto() throws Exception {
        mockMvc.perform(delete("/users/me/photo")).andExpect(status().isNoContent());

        verify(photoService).deleteCurrentUserPhoto();
    }

    @Test
    void profileIncludesPhotoPresenceWithoutBinaryData() throws Exception {
        User user = new User();
        user.setName("Driver");
        when(userService.findCurrentUser()).thenReturn(user);
        when(photoService.hasCurrentUserPhoto()).thenReturn(true);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Driver"))
                .andExpect(jsonPath("$.hasProfilePhoto").value(true))
                .andExpect(jsonPath("$.content").doesNotExist());
    }
}
