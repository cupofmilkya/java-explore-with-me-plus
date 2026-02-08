package ru.practicum.web.admin.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.admin.service.AdminUserService;
import ru.practicum.web.user.dto.NewUserRequest;
import ru.practicum.web.user.dto.UserDto;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUsersController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid NewUserRequest request) {
        UserDto created = adminUserService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @Min(0) @RequestParam(defaultValue = "0") int from,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size
    ) {
        List<UserDto> users = adminUserService.getUsers(ids, from, size);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        adminUserService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}