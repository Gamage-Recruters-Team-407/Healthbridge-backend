package lk.gamage.backend.healthbridgebackend.controller;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lk.gamage.backend.healthbridgebackend.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoctorControllerTest {
    @Test
    void savesPhotoAndProfileWithoutChangingAuthenticationFields() {
        User user = new User();
        user.setId("doctor-1"); user.setRole("DOCTOR");
        user.setEmail("doctor@example.test"); user.setPassword("stored-hash");
        UserRepository users = (UserRepository) Proxy.newProxyInstance(UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findById")) { assertEquals("doctor-1", args[0]); return Optional.of(user); }
                    if (method.getName().equals("save")) return args[0];
                    throw new UnsupportedOperationException();
                });
        DoctorController controller = new DoctorController(users, null);
        var request = new DoctorController.ProfileUpdate("Dr. Updated", user.getEmail(), "+94771234567",
                "data:image/png;base64,photo", "Other", "1990-01-01", "Colombo", "General Medicine",
                List.of("MBBS"), 5, 5000, "Biography");
        var response = controller.update(new CustomUserDetails(user), request);
        assertEquals("Dr. Updated", response.fullName());
        assertEquals(request.profileImage(), controller.get(new CustomUserDetails(user)).profileImage());
        assertEquals("+94771234567", user.getPhoneNumber());
        assertEquals("stored-hash", user.getPassword());
        assertEquals("DOCTOR", user.getRole());
    }
}
