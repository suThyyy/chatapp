package com.oqnsuthyz.chatapp.service;

import com.oqnsuthyz.chatapp.dto.request.CreateUserRequest;
import com.oqnsuthyz.chatapp.dto.response.CreateUserResponse;
import com.oqnsuthyz.chatapp.dto.response.PageResponse;
import com.oqnsuthyz.chatapp.dto.response.UserDetailResponse;
import com.oqnsuthyz.chatapp.dto.response.UserResponse;
import com.oqnsuthyz.chatapp.entity.Role;
import com.oqnsuthyz.chatapp.entity.User;
import com.oqnsuthyz.chatapp.exception.AppException;
import com.oqnsuthyz.chatapp.exception.ErrorCode;
import com.oqnsuthyz.chatapp.repository.RoleRepository;
import com.oqnsuthyz.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.oqnsuthyz.chatapp.constant.AppConstant.USER_ROLE;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;

        @Transactional(rollbackFor = Exception.class)
        public CreateUserResponse createUser(CreateUserRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new AppException(ErrorCode.USER_EXISTED);
                }

                User user = User.builder()
                                .email(request.getEmail())
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request
                                                .getPassword()))
                                .build();

                // Đoạn này viết rất tốt: Nếu chưa có Role USER dưới DB thì tự tạo mới luôn
                Role role = roleRepository.findByName(USER_ROLE)
                                .orElseGet(() -> roleRepository.save(Role.builder()
                                                .name(USER_ROLE)
                                                .build()));
                user.addRole(role);

                userRepository.save(user);

                return CreateUserResponse.builder()
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .build();
        }

        public List<UserResponse> getAllUser() {
                return userRepository.findAll().stream()
                                .map(user -> UserResponse.builder()
                                                .id(user.getId())
                                                .username(user.getUsername())
                                                .email(user.getEmail())
                                                .roles(user.getUserHasRoles().stream()
                                                                .map(userHasRole -> userHasRole.getRole().getName())
                                                                .collect(Collectors.toList()))
                                                .build())
                                .collect(Collectors.toList());
        }

        public UserDetailResponse myInfo(String userId) {
                // Tìm user theo userId từ JWT token
                return userRepository.findById(userId)
                                .map(user -> UserDetailResponse.builder()
                                                .userId(user.getId())
                                                .email(user.getEmail())
                                                .username(user.getUsername())
                                                .build())
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        public PageResponse<UserDetailResponse> searchUsers(String keyword, int page, int size) {
                // 1. Lấy thông tin current user từ SecurityContext
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null)
                        throw new AppException(ErrorCode.UNAUTHORIZED);

                String userId = authentication.getName();

                // 2. Tạo Pageable object
                Pageable pageable = PageRequest.of(page - 1, size);

                // 3. Search users từ database
                Page<User> userPage = userRepository.searchUsers(keyword, pageable);

                // 4. Filter để loại bỏ current user và map sang DTO
                List<UserDetailResponse> content = userPage.getContent()
                                .stream()
                                .filter(user -> !user.getId().equals(userId)) // Loại bỏ current user
                                .map(user -> UserDetailResponse.builder()
                                                .userId(user.getId())
                                                .email(user.getEmail())
                                                .username(user.getUsername())
                                                .build())
                                .toList();

                // 5. Build PageResponse
                return PageResponse.<UserDetailResponse>builder()
                                .currentPage(page)
                                .pageSize(size)
                                .totalPages(userPage.getTotalPages())
                                .totalElements(userPage.getTotalElements())
                                .content(content)
                                .build();
        }
}
