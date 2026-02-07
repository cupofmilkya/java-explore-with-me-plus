package ru.practicum.web.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.web.user.dto.UserDto;
import ru.practicum.web.user.entity.User;
import ru.practicum.web.user.mapper.UserMapper;
import ru.practicum.web.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto dto) {
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsers(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return userRepository.findAll(pageable)
                .map(UserMapper::toDto)
                .getContent();
    }
}