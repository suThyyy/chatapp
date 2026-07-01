package com.oqnsuthyz.chatapp.controller;

import com.oqnsuthyz.chatapp.dto.request.ChatMessageRequest;
import com.oqnsuthyz.chatapp.dto.response.ApiResponse;
import com.oqnsuthyz.chatapp.dto.response.ChatMessageResponse;
import com.oqnsuthyz.chatapp.dto.response.PageResponse;
import com.oqnsuthyz.chatapp.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping
    ApiResponse<ChatMessageResponse> sendChatMessage(
            @AuthenticationPrincipal Jwt jwt, // Lấy thông tin user từ JWT
            @RequestBody @Valid ChatMessageRequest request) {

        var senderId = jwt.getSubject(); // Lấy userId từ JWT
        var data = chatMessageService.sendChatMessage(senderId, request);

        return ApiResponse.<ChatMessageResponse>builder()
                .code(HttpStatus.CREATED.value()) // 201 Created
                .message("Chat message sent successfully")
                .data(data)
                .build();
    }

    @GetMapping("/conversations/{conversationId}/messages")
    ApiResponse<PageResponse<ChatMessageResponse>> getMessages(
            @PathVariable String conversationId, // Lấy conversationId từ URL path
            @RequestParam(required = false, defaultValue = "1") int page, // Page number (1-based)
            @RequestParam(required = false, defaultValue = "20") int size // Số tin nhắn per page
    ) {
        var data = chatMessageService.getMessagesByConversationId(conversationId, page, size);

        return ApiResponse.<PageResponse<ChatMessageResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Messages retrieved successfully")
                .data(data)
                .build();
    }
}