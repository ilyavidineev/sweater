package ru.racoon.sweater.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.racoon.sweater.domain.User;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByActivationCode(String code);
}
