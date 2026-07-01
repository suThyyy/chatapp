package com.oqnsuthyz.chatapp.service;

import com.oqnsuthyz.chatapp.dto.request.ChatMessageRequest;
import com.oqnsuthyz.chatapp.dto.response.ChatMessageResponse;
import com.oqnsuthyz.chatapp.dto.response.MessageMediaResponse;
import com.oqnsuthyz.chatapp.entity.ChatMessage;
import com.oqnsuthyz.chatapp.entity.Conversation;
import com.oqnsuthyz.chatapp.entity.MessageMedia;
import com.oqnsuthyz.chatapp.entity.User;
import com.oqnsuthyz.chatapp.exception.AppException;
import com.oqnsuthyz.chatapp.exception.ErrorCode;
import com.oqnsuthyz.chatapp.repository.ChatMessageRepository;
import com.oqnsuthyz.chatapp.repository.ConversationRepository;
import com.oqnsuthyz.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public ChatMessageResponse sendChatMessage(String senderId, ChatMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Conversation conversation = conversationRepository.findByIdAndMember(request.conversationId(), senderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

        List<MessageMedia> media = request.messageMedia() != null && !request.messageMedia().isEmpty()
                ? request.messageMedia().stream()
                        .map(messageMedia -> MessageMedia.builder()
                                .fileName(messageMedia.fileName())
                                .fileType(messageMedia.fileType())
                                .thumbnailUrl(messageMedia.thumbnailUrl())
                                .build())
                        .toList()
                : List.of();

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content())
                .messageType(request.messageType())
                .mediaFiles(media)
                .build();

        media.forEach(item -> item.setMessage(message));

        chatMessageRepository.save(message);

        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageContent(message.getContent());
        conversation.setLastMessageTime(message.getSentAt());
        conversationRepository.save(conversation);

        return ChatMessageResponse.builder()
                .id(message.getId())
                .tempId(request.tempId())
                .conversationId(message.getConversation().getId())
                .conversationAvatar(message.getConversation().getConversationAvatar())
                .senderId(sender.getId())
                .senderName(sender.getUsername())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .messageMedia(message.getMediaFiles().stream()
                        .map(messageMedia -> MessageMediaResponse.builder()
                                .fileName(messageMedia.getFileName())
                                .fileType(messageMedia.getFileType())
                                .thumbnailUrl(messageMedia.getThumbnailUrl())
                                .uploadedAt(messageMedia.getUploadedAt())
                                .build())
                        .toList())
                .build();
    }

}
