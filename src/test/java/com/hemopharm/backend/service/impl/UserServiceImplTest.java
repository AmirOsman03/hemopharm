package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.User;
import com.hemopharm.backend.repository.BloodTestRepository;
import com.hemopharm.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BloodTestRepository bloodTestRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUsers_returnsAllUsers() {
        User user = new User();
        user.setId(1L);
        user.setName("Amar");
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertEquals(List.of(user), userService.getUsers());
    }

    @Test
    void getUser_returnsUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertEquals(user, userService.getUser(1L));
    }

    @Test
    void getUser_throwsWhenMissing() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUser(9L));
    }

    @Test
    void getBloodTests_returnsTestsForUser() {
        User user = new User();
        user.setId(1L);
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bloodTestRepository.findByUserId(1L)).thenReturn(List.of(bloodTest));

        assertEquals(List.of(bloodTest), userService.getBloodTests(1L));
    }

    @Test
    void getBloodTests_throwsWhenUserMissing() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getBloodTests(9L));
        verify(bloodTestRepository, never()).findByUserId(anyLong());
    }
}
