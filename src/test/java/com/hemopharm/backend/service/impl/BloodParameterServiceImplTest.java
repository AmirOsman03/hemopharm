package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.repository.BloodParameterRepository;
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
class BloodParameterServiceImplTest {

    @Mock
    private BloodParameterRepository bloodParameterRepository;

    @InjectMocks
    private BloodParameterServiceImpl bloodParameterService;

    @Test
    void getParameters_returnsAllParameters() {
        BloodParameter parameter = new BloodParameter();
        parameter.setId(1L);
        parameter.setName("Hemoglobin");
        when(bloodParameterRepository.findAll()).thenReturn(List.of(parameter));

        assertEquals(List.of(parameter), bloodParameterService.getParameters());
    }

    @Test
    void getParameter_returnsParameter() {
        BloodParameter parameter = new BloodParameter();
        parameter.setId(1L);
        when(bloodParameterRepository.findById(1L)).thenReturn(Optional.of(parameter));

        assertEquals(parameter, bloodParameterService.getParameter(1L));
    }

    @Test
    void getParameter_throwsWhenMissing() {
        when(bloodParameterRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bloodParameterService.getParameter(9L));
    }
}
