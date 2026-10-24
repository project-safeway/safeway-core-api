package com.safeway.tech.api.controllers;

import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.service.services.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    private MockMvc mockMvc;
    private final UUID studentId = UUID.randomUUID();
    private final UUID guardianId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentController(studentService)).build();
    }

    @Test
    void linksExistingGuardianWithoutRequestBody() throws Exception {
        mockMvc.perform(put("/students/{studentId}/guardians/{guardianId}", studentId, guardianId))
                .andExpect(status().isNoContent());

        verify(studentService).addGuardian(studentId, guardianId);
    }

    @Test
    void listsLinkedGuardiansAsResponses() throws Exception {
        Guardian guardian = new Guardian();
        guardian.setId(guardianId);
        guardian.setName("Guardian");
        guardian.setAddress(new Address());
        when(studentService.findGuardians(studentId)).thenReturn(List.of(guardian));

        mockMvc.perform(get("/students/{studentId}/guardians", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(guardianId.toString()))
                .andExpect(jsonPath("$[0].name").value("Guardian"));
    }

    @Test
    void removesGuardianLink() throws Exception {
        mockMvc.perform(delete("/students/{studentId}/guardians/{guardianId}", studentId, guardianId))
                .andExpect(status().isNoContent());

        verify(studentService).removeGuardian(studentId, guardianId);
    }
}
