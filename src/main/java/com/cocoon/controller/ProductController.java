package com.cocoon.controller;

import com.cocoon.dto.ProductDTO;
import com.cocoon.enums.ProductStatus;
import com.cocoon.enums.Unit;
import com.cocoon.exception.CocoonException;
import com.cocoon.service.CategoryService;
import com.cocoon.service.CompanyService;
import com.cocoon.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Controller
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;
    private CategoryService categoryService;
    private final CompanyService companyService;

    public ProductController(ProductService productService, CategoryService categoryService, CompanyService companyService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.companyService = companyService;
    }

    @GetMapping("/list")
    public String getAllProducts(Model model){
        model.addAttribute("products", productService.getAllProductsByCompany());
        return "product/product-list";
    }

    @GetMapping("/create")
    public String getCreateProductPage(Model model){
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("productStatus", ProductStatus.values());
        model.addAttribute("unit", Unit.values());
        model.addAttribute("categories", categoryService.getCategoryByCompany_Id());
        return "product/product-add";
    }

    @PostMapping("/create")
    public String saveProduct(ProductDTO productDTO){
        productService.save(productDTO);
        return "redirect:/product/list";
    }

    @GetMapping("/update/{id}")
    public String getUpdateProductPage(@PathVariable("id") Long id, Model model) throws CocoonException {
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("productStatuses", ProductStatus.values());
        model.addAttribute("productStatus", productService.getProductStatusById(id));
        model.addAttribute("units", Unit.values());
        model.addAttribute("unit", productService.getUnitById(id));
        model.addAttribute("categories", categoryService.getCategoryByCompany_Id());
        return "product/product-edit";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") Long id, ProductDTO productDTO) throws CocoonException {
        productService.update(productDTO);
        return "redirect:/product/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, ProductDTO productDTO) throws CocoonException {
        productService.deleteById(id);
        return "redirect:/product/list";
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("date", new Date());
        model.addAttribute("localDateTime", LocalDateTime.now());
        model.addAttribute("localDate", LocalDate.now());
        model.addAttribute("java8Instant", Instant.now());
    }


}
