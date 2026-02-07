package ru.practicum.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.admin.service.AdminUserService;
import ru.practicum.web.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUsersController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Validated UserDto dto) {
        UserDto created = adminUserService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<UserDto> users = adminUserService.getUsers(from, size);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        adminUserService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}