package ru.practicum.base.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.base.model.Category;

public interface CategoriesRepository extends JpaRepository<Category, Long> {
}
