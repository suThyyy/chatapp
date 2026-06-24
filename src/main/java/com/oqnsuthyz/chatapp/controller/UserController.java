package com.oqnsuthyz.chatapp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oqnsuthyz.chatapp.dto.request.CreateUserRequest;
import com.oqnsuthyz.chatapp.dto.response.ApiResponse;
import com.oqnsuthyz.chatapp.dto.response.CreateUserResponse;
import com.oqnsuthyz.chatapp.dto.response.UserResponse;
import com.oqnsuthyz.chatapp.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/home")
    public String getMethodName(@RequestParam String param) {
        return new String("Hello world");
    }

    @PostMapping
    public ApiResponse<CreateUserResponse> createAUser(@Valid @RequestBody CreateUserRequest registerUser) {
        CreateUserResponse data = userService.createUser(registerUser);

        return ApiResponse.<CreateUserResponse>builder()
                .message("User created sucessfully")
                .code(HttpStatus.OK.value())
                .data(data)
                .build();
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUser() {
        List<UserResponse> users = userService.getAllUser();

        return ApiResponse.<List<UserResponse>>builder()
                .message("Get all users sucessfully")
                .code(HttpStatus.OK.value())
                .data(users)
                .build();
    }

}
