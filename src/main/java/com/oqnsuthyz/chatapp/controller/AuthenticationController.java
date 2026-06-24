package com.oqnsuthyz.chatapp.controller;

import com.oqnsuthyz.chatapp.dto.request.LoginRequest;
import com.oqnsuthyz.chatapp.dto.response.ApiResponse;
import com.oqnsuthyz.chatapp.dto.response.LoginResponse;
import com.oqnsuthyz.chatapp.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        var data = authenticationService.login(request);

        return ApiResponse.<LoginResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successfully")
                .data(data)
                .build();
    }
}