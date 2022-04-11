package com.cocoon.repository;

import com.cocoon.dto.CategoryDTO;
import com.cocoon.entity.Category;
import com.cocoon.exception.CocoonException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    boolean existsByDescription(String  desc) throws CocoonException;

    Category getByDescription(String description) throws CocoonException;

    Category getCategoryById(Long id);

    List<Category> getCategoryByCompany_Id(Long companyId);
}