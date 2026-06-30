package com.oqnsuthyz.chatapp.controller;

import com.oqnsuthyz.chatapp.dto.request.CreateConversationRequest;
import com.oqnsuthyz.chatapp.dto.response.ApiResponse;
import com.oqnsuthyz.chatapp.dto.response.ConversationDetailResponse;
import com.oqnsuthyz.chatapp.dto.response.CreateConversationResponse;
import com.oqnsuthyz.chatapp.dto.response.PageResponse;
import com.oqnsuthyz.chatapp.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    ApiResponse<CreateConversationResponse> createConversation(
            @AuthenticationPrincipal Jwt jwt, // Lấy thông tin user từ JWT token
            @RequestBody @Valid CreateConversationRequest request) {

        // Lấy userId từ JWT subject (đã được set khi generate token)
        var creatorId = jwt.getSubject();

        // Gọi service để tạo conversation
        var data = conversationService.createConversation(creatorId, request);

        return ApiResponse.<CreateConversationResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Conversation created successfully")
                .data(data)
                .build();
    }

    @GetMapping("/my-conversation")
    ApiResponse<PageResponse<ConversationDetailResponse>> getMyConversation(
            @AuthenticationPrincipal Jwt jwt, // Lấy thông tin user từ JWT
            @RequestParam(required = false, defaultValue = "1") int page, // Page number (bắt đầu từ 1)
            @RequestParam(required = false, defaultValue = "10") int size) { // Số lượng items per page

        var userId = jwt.getSubject(); // Lấy userId từ JWT
        var data = conversationService.getMyConversation(userId, page, size);

        return ApiResponse.<PageResponse<ConversationDetailResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("My conversation retrieved successfully")
                .data(data)
                .build();
    }
}