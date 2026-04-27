package team26.repository.user;

import team26.domain.user.User;
import team26.domain.user.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    // CREATE
    User save(User user);

    // READ
    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    List<User> findAll();
    List<User> findAllByRole(UserRole role);

    // UPDATE
    User update(User user);

    // DELETE
    void delete(UUID id);

    // EXISTS
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
