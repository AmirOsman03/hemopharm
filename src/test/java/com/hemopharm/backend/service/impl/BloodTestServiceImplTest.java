package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.repository.BloodTestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodTestServiceImplTest {

    @Mock
    private BloodTestRepository bloodTestRepository;

    @InjectMocks
    private BloodTestServiceImpl bloodTestService;

    @Test
    void getBloodTests_returnsAllTests() {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);
        when(bloodTestRepository.findAll()).thenReturn(List.of(bloodTest));

        assertEquals(List.of(bloodTest), bloodTestService.getBloodTests());
    }

    @Test
    void getBloodTest_returnsTest() {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);
        when(bloodTestRepository.findById(1L)).thenReturn(Optional.of(bloodTest));

        assertEquals(bloodTest, bloodTestService.getBloodTest(1L));
    }

    @Test
    void getBloodTest_throwsWhenMissing() {
        when(bloodTestRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bloodTestService.getBloodTest(9L));
    }
}
