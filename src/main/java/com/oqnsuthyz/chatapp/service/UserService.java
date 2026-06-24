package com.oqnsuthyz.chatapp.service;

import com.oqnsuthyz.chatapp.dto.request.CreateUserRequest;
import com.oqnsuthyz.chatapp.dto.response.CreateUserResponse;
import com.oqnsuthyz.chatapp.dto.response.UserResponse;
import com.oqnsuthyz.chatapp.entity.Role;
import com.oqnsuthyz.chatapp.entity.User;
import com.oqnsuthyz.chatapp.exception.AppException;
import com.oqnsuthyz.chatapp.exception.ErrorCode;
import com.oqnsuthyz.chatapp.repository.RoleRepository;
import com.oqnsuthyz.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Tạm thời comment gói mã hóa mật khẩu lại
// import org.springframework.security.crypto.password.PasswordEncoder;
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
    // Tạm thời bỏ PasswordEncoder khi chưa cài Security
    // private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword()) // Tạm thời lưu mật khẩu thuần để test
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
}
