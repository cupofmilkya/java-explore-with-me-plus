package ru.practicum.web.admin.service;

import ru.practicum.web.user.dto.UserDto;

import java.util.List;

public interface AdminUserService {

    UserDto create(UserDto dto);

    void delete(Long userId);

    List<UserDto> getUsers(int from, int size);
}