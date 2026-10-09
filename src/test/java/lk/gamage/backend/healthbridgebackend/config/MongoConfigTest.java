package lk.gamage.backend.healthbridgebackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MongoConfigTest {

    @Test
    void retriesTransientStartupTimeouts() {
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        MongoConfig.warmUp(() -> {
            if (attempts.incrementAndGet() < 3) throw new com.mongodb.MongoTimeoutException("Connecting");
        });
        org.junit.jupiter.api.Assertions.assertEquals(3, attempts.get());
    }

    @Test
    void stopsAfterFourFailedAttempts() {
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        org.junit.jupiter.api.Assertions.assertThrows(com.mongodb.MongoTimeoutException.class,
                () -> MongoConfig.warmUp(() -> {
                    attempts.incrementAndGet();
                    throw new com.mongodb.MongoTimeoutException("Unavailable");
                }));
        org.junit.jupiter.api.Assertions.assertEquals(4, attempts.get());
    }

    @Test
    void exposesGridFsTemplateAsSpringBean() throws NoSuchMethodException {
        Method gridFsTemplate = MongoConfig.class.getDeclaredMethod(
                "gridFsTemplate",
                org.springframework.data.mongodb.MongoDatabaseFactory.class,
                org.springframework.data.mongodb.core.convert.MongoConverter.class);

        assertTrue(gridFsTemplate.isAnnotationPresent(Bean.class));
    }
}
