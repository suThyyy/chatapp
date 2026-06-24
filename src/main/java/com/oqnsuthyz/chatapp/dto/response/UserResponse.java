package com.oqnsuthyz.chatapp.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private List<String> roles;
}
