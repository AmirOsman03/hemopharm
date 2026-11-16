package com.hemopharm.backend.service;

import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.User;

import java.util.List;

public interface UserService {

    List<User> getUsers();

    User getUser(Long id);

    List<BloodTest> getBloodTests(Long userId);
}
