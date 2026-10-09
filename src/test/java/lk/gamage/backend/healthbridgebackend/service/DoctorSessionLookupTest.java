package lk.gamage.backend.healthbridgebackend.service;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lk.gamage.backend.healthbridgebackend.enums.SessionStatus;
import lk.gamage.backend.healthbridgebackend.model.DoctorSession;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.DoctorSessionRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoctorSessionLookupTest {
    @Test
    void mapsDoctorNameForMultipleSessions() {
        User doctor = new User();
        doctor.setRole("DOCTOR");
        doctor.setFullName("Dr. Test");
        AtomicInteger lookups = new AtomicInteger();
        UserRepository users = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(), new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> { lookups.incrementAndGet(); return Optional.of(doctor); });
        DoctorSession session = DoctorSession.builder().id("session-1").doctorId("doctor-1")
                .sessionDate(LocalDate.now()).startTime(LocalTime.NOON).endTime(LocalTime.of(13, 0))
                .maxAppointments(10).status(SessionStatus.AVAILABLE).build();
        DoctorSessionRepository sessions = (DoctorSessionRepository) Proxy.newProxyInstance(
                DoctorSessionRepository.class.getClassLoader(), new Class<?>[]{DoctorSessionRepository.class},
                (proxy, method, args) -> List.of(session, session, session));
        DoctorSessionService service = new DoctorSessionService(sessions, users, null, null, null);
        var result = service.mine("doctor-1");
        assertEquals(3, result.size());
        assertEquals("Dr. Test", result.get(0).doctorName());
    }
}
