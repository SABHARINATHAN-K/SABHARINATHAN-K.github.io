package com.careerplanning.backend.modules.users.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.users.dto.UpdateUserProfileRequest;
import com.careerplanning.backend.modules.users.dto.UserProfileResponse;
import com.careerplanning.backend.modules.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMe(@RequestHeader("X-Auth-Token") String token) {
        return ApiResponse.success(userService.getCurrentUserProfile(token));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateMe(@RequestHeader("X-Auth-Token") String token,
                                                     @Valid @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.success(userService.updateCurrentUserProfile(token, request));
    }
}
