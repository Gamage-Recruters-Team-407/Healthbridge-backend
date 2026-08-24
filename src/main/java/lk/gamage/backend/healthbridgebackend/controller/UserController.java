package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.request.UpdateProfileRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.UserResponse;
import lk.gamage.backend.healthbridgebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/profile")
    public UserResponse getProfile(@PathVariable String userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/{userId}/profile")
    public UserResponse updateProfile(@PathVariable String userId,
            @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }

    @PutMapping("/{userId}/deactivate")
    public UserResponse deactivateAccount(@PathVariable String userId) {
        return userService.deactivateAccount(userId);
    }

    @PutMapping("/{userId}/reactivate")
    public UserResponse reactivateAccount(@PathVariable String userId) {
        return userService.reactivateAccount(userId);
    }
}  