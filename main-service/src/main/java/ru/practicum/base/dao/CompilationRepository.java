package ru.practicum.base.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.base.model.Compilation;
import ru.practicum.base.utils.MyPageRequest;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    List<Compilation> findAllByPinned(Boolean pinned, MyPageRequest pageable);
}
