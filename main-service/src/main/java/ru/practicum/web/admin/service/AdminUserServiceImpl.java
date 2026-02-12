package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.exception.BadRequestException;
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
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email must not be blank");
        }
        if (request.getEmail().length() < 6 || request.getEmail().length() > 254) {
            throw new BadRequestException("Email length must be between 6 and 254 characters");
        }
        if (!request.getEmail().contains("@")) {
            throw new BadRequestException("Invalid email format");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Name must not be blank");
        }
        if (request.getName().length() < 2 || request.getName().length() > 250) {
            throw new BadRequestException("Name length must be between 2 and 250 characters");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        try {
            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .build();
            return UserMapper.toDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        if (ids != null && !ids.isEmpty()) {
            return userRepository.findByIdIn(ids, pageable)
                    .stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAll(pageable)
                    .stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        }
    }
}