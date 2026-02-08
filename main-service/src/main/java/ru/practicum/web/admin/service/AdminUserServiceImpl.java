package ru.practicum.web.admin.service;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.user.dto.NewUserRequest;
import ru.practicum.web.user.dto.UserDto;
import ru.practicum.web.user.entity.User;
import ru.practicum.web.user.mapper.UserMapper;
import ru.practicum.web.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email уже существует");
        }

        try {
            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .build();
            return UserMapper.toDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email уже существует");
        }
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsers(@Nullable List<Long> ids, int from, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Размер страницы должен быть больше 0");
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        if (ids != null && !ids.isEmpty()) {
            return userRepository.findByIdIn(ids, pageable)
                    .stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAll(pageable)
                    .map(UserMapper::toDto)
                    .getContent();
        }
    }
}