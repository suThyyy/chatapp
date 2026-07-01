package com.oqnsuthyz.chatapp.service;

import com.oqnsuthyz.chatapp.common.ConversationType;
import com.oqnsuthyz.chatapp.dto.request.CreateConversationRequest;
import com.oqnsuthyz.chatapp.dto.response.ConversationDetailResponse;
import com.oqnsuthyz.chatapp.dto.response.CreateConversationResponse;
import com.oqnsuthyz.chatapp.dto.response.PageResponse;
import com.oqnsuthyz.chatapp.entity.Conversation;
import com.oqnsuthyz.chatapp.entity.User;
import com.oqnsuthyz.chatapp.exception.AppException;
import com.oqnsuthyz.chatapp.exception.ErrorCode;
import com.oqnsuthyz.chatapp.mapper.ConversationMapper;
import com.oqnsuthyz.chatapp.repository.ConversationRepository;
import com.oqnsuthyz.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public CreateConversationResponse createConversation(String creatorId, CreateConversationRequest request) {
        List<String> participantIds = request.participantIds();

        // Đảm bảo creator cũng nằm trong danh sách participants
        if (!participantIds.contains(creatorId)) {
            participantIds.add(creatorId);
        }

        // Lấy thông tin tất cả participants từ database
        List<User> participantInfos = userRepository.findAllById(participantIds);

        // Kiểm tra xem tất cả participants có tồn tại không
        if (participantInfos.size() != participantIds.size()) {
            throw new AppException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        ConversationType conversationType = request.conversationType();
        String participantHash = null;

        // Xử lý logic cho PRIVATE conversation
        if (conversationType == ConversationType.PRIVATE) {
            // Private conversation phải có đúng 2 người
            if (participantInfos.size() != 2)
                throw new AppException(ErrorCode.INVALID_PARTICIPANT_COUNT);

            // Tạo participant hash để identify unique conversation
            // Sort userId để đảm bảo hash luôn giống nhau cho cùng 2 người
            // Ví dụ: userId1="abc", userId2="xyz" -> hash="abc_xyz"
            participantHash = participantInfos.stream()
                    .map(User::getId)
                    .sorted()
                    .collect(Collectors.joining("_"));

            // Kiểm tra xem conversation giữa 2 người này đã tồn tại chưa
            Optional<Conversation> existing = conversationRepository.findByParticipantHash(participantHash);
            if (existing.isPresent()) {
                // Nếu đã tồn tại, trả về conversation cũ
                return ConversationMapper.toConversationResponse(creatorId, existing.get());
            }
        }

        // Xử lý logic cho GROUP conversation
        if (conversationType == ConversationType.GROUP) {
            // Group conversation phải có tên
            if (request.name() == null || request.name().trim().isEmpty())
                throw new AppException(ErrorCode.CONVERSATION_NAME_REQUIRED);

            // Group conversation phải có ít nhất 3 người
            if (participantIds.size() < 3)
                throw new AppException(ErrorCode.GROUP_CONVERSATION_MINIMUM_THREE_PARTICIPANTS);
        }

        // Tạo conversation mới
        Conversation conversation = Conversation.builder()
                .name(request.name())
                .conversationType(conversationType)
                .conversationAvatar(request.conversationAvatar())
                .participantHash(participantHash) // Chỉ có giá trị với PRIVATE
                .createdAt(LocalDateTime.now())
                .build();

        // Thêm tất cả participants vào conversation
        participantInfos.forEach(conversation::addParticipants);

        // Lưu conversation vào database
        conversationRepository.save(conversation);

        // Map entity sang response DTO
        return ConversationMapper.toConversationResponse(creatorId, conversation);
    }

    public PageResponse<ConversationDetailResponse> getMyConversation(String userId, int page, int size) {
        // Step 1: Query conversations với pagination (không fetch collections)
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Conversation> conversationPage = conversationRepository.findAllByUserId(userId, pageable);

        // Step 2: Extract conversation IDs từ paginated results
        List<String> conversationIds = conversationPage.getContent().stream()
                .map(Conversation::getId)
                .toList();

        // Step 3: Fetch collections cho conversations đã được paginate
        // Empty check để tránh query với empty list
        List<Conversation> conversationsWithParticipants = conversationIds.isEmpty() ? List.of()
                : conversationRepository.findByIdInWithParticipants(conversationIds);

        // Step 4: Map sang response DTO
        List<ConversationDetailResponse> responses = conversationsWithParticipants.stream()
                .map(conversation -> ConversationMapper.toConversationDetailResponse(userId, conversation))
                .toList();

        // Step 5: Build PageResponse
        return PageResponse.<ConversationDetailResponse>builder()
                .currentPage(page)
                .pageSize(pageable.getPageSize())
                .totalPages(conversationPage.getTotalPages())
                .totalElements(conversationPage.getTotalElements())
                .content(responses)
                .build();
    }
}