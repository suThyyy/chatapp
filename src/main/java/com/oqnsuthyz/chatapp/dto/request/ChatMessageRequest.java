package com.oqnsuthyz.chatapp.dto.request;

import com.oqnsuthyz.chatapp.common.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ChatMessageRequest(

        String tempId, // Temporary ID từ client để map với message đã gửi (optimistic UI)

        @NotBlank(message = "Conversation id is required") String conversationId, // ID của conversation

        String content, // Nội dung tin nhắn (bắt buộc với TEXT, optional với MEDIA)

        @NotNull(message = "Message type is required") MessageType messageType, // TEXT hoặc MEDIA

        List<MessageMediaRequest> messageMedia // Danh sách media files (optional)
) {
}