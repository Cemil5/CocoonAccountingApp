package com.cocoon.implementation;

import com.cocoon.dto.CategoryDTO;
import com.cocoon.dto.ProductDTO;
import com.cocoon.entity.Category;
import com.cocoon.entity.Company;
import com.cocoon.exception.CategoryDoesNotExistException;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.CategoryRepository;
import com.cocoon.service.CategoryService;
import com.cocoon.service.CompanyService;
import com.cocoon.service.ProductService;
import com.cocoon.util.MapperUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final MapperUtil mapperUtil;
    private final ProductService productService;
    private final CompanyService companyService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, MapperUtil mapperUtil, ProductService productService, CompanyService companyService) {
        this.categoryRepository = categoryRepository;
        this.mapperUtil = mapperUtil;
        this.productService = productService;
        this.companyService = companyService;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(category -> mapperUtil.convert(category,new CategoryDTO())).collect(Collectors.toList());
    }

    @Override
    public void save(CategoryDTO categoryDTO) throws CocoonException {
        Category category = mapperUtil.convert(categoryDTO, new Category());
        if (categoryRepository.existsByDescription(category.getDescription()))
        throw new CocoonException("category is already exist");
        category.setEnabled(true);

        category.setCompany(mapperUtil.convert(companyService.getCompanyByLoggedInUser(), new Company()));

        categoryRepository.save(category);
    }

    @Override
    public CategoryDTO getCategoryByDescription(String descrition) {
        Category category = categoryRepository.getByDescription(descrition);
        if (category==null)
            throw new CategoryDoesNotExistException();
        CategoryDTO categoryDTO = mapperUtil.convert(category, new CategoryDTO());
        return categoryDTO;
    }

    @Override
    public void update(CategoryDTO categoryDTO) throws CocoonException {
        Category category = categoryRepository.getById(categoryDTO.getId());
        if (category==null)
            throw new CategoryDoesNotExistException();
        category.setDescription(categoryDTO.getDescription());
        categoryRepository.save(category);


    }

    @Override
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.getById(id);
        CategoryDTO categoryDTO = mapperUtil.convert(category, new CategoryDTO());
        return categoryDTO;
    }

    @Override
    public void delete(CategoryDTO categoryDTO) {
        Category category = categoryRepository.getCategoryById(categoryDTO.getId());
        if (category==null)
            throw new CategoryDoesNotExistException();
        List<ProductDTO> productDTOList = productService.findProductsByCategoryId(categoryDTO.getId());
        if (productDTOList.size()>0)
            throw new CocoonException("category has relation");
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    @Override
    public List<CategoryDTO> getCategoryByCompany_Id() {
        List<Category> categories = categoryRepository.getCategoryByCompany_Id(companyService.getCompanyByLoggedInUser().getId());
        return categories.stream().map(category -> mapperUtil.convert(category,new CategoryDTO())).collect(Collectors.toList());
    }


}
