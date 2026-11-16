package com.hemopharm.backend.controller;

import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.User;
import com.hemopharm.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping("/{id}/blood-tests")
    public List<BloodTest> getBloodTests(@PathVariable Long id) {
        return userService.getBloodTests(id);
    }
}
