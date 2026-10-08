package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    List<User> findByRoleIn(List<String> roles);
    List<User> findByRole(String role);
    List<User> findByAccountStatus(String accountStatus);

    long countByRole(String role);

    @Query(value = "{ '$and': [ { 'role': ?0 }, { 'createdAt': { '$gte': ?1 } }, { 'createdAt': { '$lt': ?2 } } ] }", count = true)
    long countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String role, LocalDateTime start, LocalDateTime end);
}
