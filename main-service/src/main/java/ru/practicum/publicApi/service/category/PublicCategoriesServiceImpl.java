package ru.practicum.publicApi.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.base.dao.CategoriesRepository;
import ru.practicum.base.dto.category.CategoryDto;
import ru.practicum.base.exceptions.NotFoundException;
import ru.practicum.base.mapper.CategoryMapper;
import ru.practicum.base.model.Category;
import ru.practicum.base.utils.MyPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublicCategoriesServiceImpl implements PublicCategoriesService {
    private final CategoriesRepository categoriesRepository;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        MyPageRequest pageable = new MyPageRequest(from, size,
                Sort.by(Sort.Direction.ASC, "id"));
        List<Category> categories = categoriesRepository.findAll(pageable).toList();
        log.info("Получен список всех категорий");
        return CategoryMapper.toDtoList(categories);
    }

    @Override
    public CategoryDto get(Long catId) {
        final Category category = categoriesRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category not found with id = %s", catId)));
        log.info("Get Category: {}", category.getName());
        return CategoryMapper.toDto(category);
    }
}
