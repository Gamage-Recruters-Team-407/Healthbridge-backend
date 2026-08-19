package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.request.UpdateProfileRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.UserResponse;

public interface UserService {

    UserResponse getProfile(String userId);

    UserResponse updateProfile(String userId, UpdateProfileRequest request);

    UserResponse deactivateAccount(String userId);

    UserResponse reactivateAccount(String userId);
}