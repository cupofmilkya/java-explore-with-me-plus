package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.user.dto.NewUserRequest;
import ru.practicum.web.user.dto.UserDto;
import ru.practicum.web.user.entity.User;
import ru.practicum.web.user.mapper.UserMapper;
import ru.practicum.web.user.repository.UserRepository;
import ru.practicum.web.user.validator.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserValidator validator;

    @Override
    public UserDto create(NewUserRequest request) {
        validator.validateCreateRequest(request);

        try {
            User user = UserMapper.fromNewUserRequest(request);
            return UserMapper.toDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            validator.checkEmailUnique(request.getEmail());
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }
    }

    @Override
    public void delete(Long userId) {
        validator.validateUserExists(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        validator.validatePagination(from, size);

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