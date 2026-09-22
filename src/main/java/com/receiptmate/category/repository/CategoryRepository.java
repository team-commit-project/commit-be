package com.receiptmate.category.repository;

import com.receiptmate.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

}
