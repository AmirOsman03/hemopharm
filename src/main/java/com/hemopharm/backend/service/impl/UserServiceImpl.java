package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.User;
import com.hemopharm.backend.repository.BloodTestRepository;
import com.hemopharm.backend.repository.UserRepository;
import com.hemopharm.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BloodTestRepository bloodTestRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    @Override
    public List<BloodTest> getBloodTests(Long userId) {
        getUser(userId);
        return bloodTestRepository.findByUserId(userId);
    }
}
