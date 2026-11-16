package com.hemopharm.backend.controller;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.User;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getUsers_returnsUsersWithoutPassword() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Amar");
        user.setEmail("amar@example.com");
        user.setPassword("secret");
        when(userService.getUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Amar"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getUser_returnsUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Amar");
        when(userService.getUser(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amar"));
    }

    @Test
    void getUser_returns404WhenMissing() throws Exception {
        when(userService.getUser(9L)).thenThrow(new NotFoundException("User 9 not found"));

        mockMvc.perform(get("/api/users/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBloodTests_returnsTestsForUser() throws Exception {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(5L);
        bloodTest.setFileName("test.pdf");
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        when(userService.getBloodTests(1L)).thenReturn(List.of(bloodTest));

        mockMvc.perform(get("/api/users/1/blood-tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("test.pdf"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }
}
