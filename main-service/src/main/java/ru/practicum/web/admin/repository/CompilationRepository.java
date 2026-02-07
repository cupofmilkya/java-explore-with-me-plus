package ru.practicum.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.web.admin.entity.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
}