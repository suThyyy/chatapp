package com.oqnsuthyz.chatapp.mapper;

import com.oqnsuthyz.chatapp.common.ConversationType;
import com.oqnsuthyz.chatapp.dto.response.ConversationDetailResponse;
import com.oqnsuthyz.chatapp.dto.response.CreateConversationResponse;
import com.oqnsuthyz.chatapp.dto.response.ParticipantResponse;
import com.oqnsuthyz.chatapp.entity.Conversation;

public final class ConversationMapper {
    private ConversationMapper() {
    }

    public static CreateConversationResponse toConversationResponse(String creatorId, Conversation conversation) {
        ConversationType conversationType = conversation.getConversationType();

        // Build response với các thông tin cơ bản
        CreateConversationResponse response = CreateConversationResponse.builder()
                .id(conversation.getId())
                .conversationType(conversationType)
                // Map danh sách participants sang ParticipantResponse
                .participantInfo(conversation.getParticipants().stream()
                        .map(participants -> ParticipantResponse.builder()
                                .userId(participants.getUser().getId())
                                .username(participants.getUser().getUsername())
                                .build())
                        .toList())
                .createdAt(conversation.getCreatedAt())
                .build();

        // Xử lý tên conversation khác nhau cho PRIVATE và GROUP
        if (conversationType == ConversationType.PRIVATE) {
            // Với PRIVATE: tên là username của người còn lại (không phải creator)
            conversation.getParticipants()
                    .stream()
                    .filter(participants -> !participants.getUser().getId().equals(creatorId))
                    .findFirst()
                    .ifPresent(participantInfo -> response.setName(participantInfo.getUser().getUsername()));
        } else {
            // Với GROUP: dùng tên nhóm và avatar từ conversation
            response.setName(conversation.getName());
            response.setConversationAvatar(conversation.getConversationAvatar());
        }

        return response;
    }

    // Map cho Get My Conversations response
    public static ConversationDetailResponse toConversationDetailResponse(String creatorId, Conversation conversation) {
        ConversationType conversationType = conversation.getConversationType();

        ConversationDetailResponse response = ConversationDetailResponse.builder()
                .id(conversation.getId())
                .conversationType(conversationType)
                // Map danh sách participants
                .participantInfo(conversation.getParticipants().stream()
                        .map(participants -> ParticipantResponse.builder()
                                .userId(participants.getUser().getId())
                                .username(participants.getUser().getUsername())
                                .build())
                        .toList())
                // Thông tin tin nhắn cuối cùng
                .lastMessageId(conversation.getLastMessageId())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageTime(conversation.getLastMessageTime())
                .createdAt(conversation.getCreatedAt())
                .build();

        // Resolve tên conversation
        String name = resolveConversationName(creatorId, conversation);
        response.setName(name);

        // Chỉ set avatar cho GROUP conversation
        if (conversation.getConversationType() != ConversationType.PRIVATE) {
            response.setConversationAvatar(conversation.getConversationAvatar());
        }

        return response;
    }

    // Helper method để resolve tên conversation
    // PRIVATE: Tên của người còn lại (không phải creatorId)
    // GROUP: Tên nhóm
    private static String resolveConversationName(String creatorId, Conversation conversation) {
        if (conversation.getConversationType() == ConversationType.PRIVATE) {
            return conversation.getParticipants()
                    .stream()
                    .filter(p -> !p.getUser().getId().equals(creatorId)) // Lọc người còn lại
                    .findFirst()
                    .map(p -> p.getUser().getUsername()) // Lấy username
                    .orElse(null);
        }
        return conversation.getName(); // Trả về tên nhóm
    }
}