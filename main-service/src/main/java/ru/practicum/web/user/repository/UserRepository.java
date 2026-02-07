package ru.practicum.web.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.web.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}