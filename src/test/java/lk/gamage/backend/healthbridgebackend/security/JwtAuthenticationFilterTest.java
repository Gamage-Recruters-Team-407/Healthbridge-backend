package lk.gamage.backend.healthbridgebackend.security;

import jakarta.servlet.FilterChain;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

class JwtAuthenticationFilterTest {
    @Test
    void databaseFailureReturns503InsteadOfLoggingTheUserOut() throws Exception {
        JwtService jwt = new JwtService() {
            @Override public String extractUsername(String token) { return "doctor@example.test"; }
        };
        UserRepository users = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(), new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> { throw new DataAccessResourceFailureException("Unavailable"); });
        AtomicBoolean continued = new AtomicBoolean();
        FilterChain chain = (request, response) -> continued.set(true);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtService", jwt);
        ReflectionTestUtils.setField(filter, "userRepository", users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        assertEquals(503, response.getStatus());
        assertTrue(response.getContentAsString().contains("Database temporarily unavailable"));
        assertFalse(continued.get());
    }
}
