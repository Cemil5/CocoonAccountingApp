package com.cocoon.service.impl;

import com.cocoon.dto.InvoiceProductDTO;
import com.cocoon.dto.ProductDTO;
import com.cocoon.entity.*;
import com.cocoon.enums.InvoiceType;
import com.cocoon.enums.ProductStatus;
import com.cocoon.enums.Unit;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.InvoiceProductRepo;
import com.cocoon.repository.ProductRepository;
import com.cocoon.service.CompanyService;
import com.cocoon.service.InvoiceService;
import com.cocoon.service.ProductService;
import com.cocoon.util.MapperUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private InvoiceService invoiceService;
    private MapperUtil mapperUtil;
    private CompanyService companyService;
    private InvoiceProductRepo invoiceProductRepo;

    public ProductServiceImpl(ProductRepository productRepository, @Lazy InvoiceService invoiceService, MapperUtil mapperUtil, CompanyService companyService, @Lazy InvoiceProductRepo invoiceProductRepo) {
        this.productRepository = productRepository;
        this.invoiceService = invoiceService;
        this.mapperUtil = mapperUtil;
        this.companyService = companyService;
        this.invoiceProductRepo = invoiceProductRepo;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product-> mapperUtil.convert(product, new ProductDTO())).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getAllProductsByCompany() {
        var company = companyService.getCompanyByLoggedInUser();
        List<Product> products = productRepository.findAllByCompanyId(company.getId());
        return products.stream().map(product-> mapperUtil.convert(product, new ProductDTO())).collect(Collectors.toList());
    }

    @Override
    public ProductDTO save(ProductDTO productDTO) {
        Product product = mapperUtil.convert(productDTO, new Product());
        product.setEnabled((byte) 1);
        Company company = mapperUtil.convert(companyService.getCompanyByLoggedInUser(), new Company());
        product.setCompany(company);
        productRepository.save(product);
        return mapperUtil.convert(product, new ProductDTO());
    }


    @Override
    public ProductDTO getProductById(Long id) throws CocoonException {
        Optional<Product> product = productRepository.findById(id);
        if(product.isEmpty()){
            throw new CocoonException("There is no product belongs to this id " + id);
        }
        return mapperUtil.convert(product.get(), new ProductDTO());
    }

    @Override
    public void update(ProductDTO productDTO) throws CocoonException {
        Optional<Product> product = productRepository.findById(productDTO.getId());
        Product convertedProduct = mapperUtil.convert(productDTO, new Product());
        convertedProduct.setId(product.get().getId());
        convertedProduct.setEnabled(product.get().getEnabled());
        convertedProduct.setQty(product.get().getQty());
        convertedProduct.setTax(product.get().getTax());
        convertedProduct.setCompany(product.get().getCompany());
        Company company = mapperUtil.convert(companyService.getCompanyByLoggedInUser(), new Company());
        convertedProduct.setCompany(company);
        productRepository.save(convertedProduct);
    }

    @Override
    public ProductStatus getProductStatusById(Long id) throws CocoonException {
        Product product =productRepository.findById(id)
                .orElseThrow(() -> new CocoonException("There is no product belongs to this id " + id));
        return product.getProductStatus();
    }

    @Override
    public Unit getUnitById(Long id) throws CocoonException {
        Product product =productRepository.findById(id)
                .orElseThrow(() -> new CocoonException("There is no product belongs to this id " + id));
        return product.getUnit();
    }

    @Override
    public void deleteById(Long id) throws CocoonException {
        Optional<Product> product = productRepository.findById(id);
        if(product.isEmpty()){
            throw new CocoonException("There is no product belongs to this id " + id);
        }
        // check if product has related invoice or not
        List<InvoiceProduct> invoiceProducts = invoiceProductRepo.findAllByProductId(id);
        if (invoiceProducts.size() ==0) {
            product.get().setIsDeleted(true); // soft delete
            productRepository.save(product.get());
        } else throw new CocoonException("This product has relation with Invoice Product and cannot be deleted");
    }


    @Override
    public List<ProductDTO> findProductsByCategoryId(Long id) {
        List<Product> products = productRepository.findAllByCategoryId(id);
        return products.stream().map((p) -> mapperUtil.convert(p, new ProductDTO())).collect(Collectors.toList());
    }

    @Override
    public void updateProductQuantity(InvoiceType type, InvoiceProduct invoiceProduct) {
        Product product = productRepository.getById(invoiceProduct.getProduct().getId());
        if (type==InvoiceType.SALE){
            product.setQty(product.getQty() - invoiceProduct.getQty());
        } else if (type==InvoiceType.PURCHASE) {
            product.setQty(product.getQty() + invoiceProduct.getQty());
        }
        productRepository.save(product);
    }

    @Override
    public boolean validateProductQuantity(InvoiceProductDTO invoiceProductDTO) {
        Product product = productRepository.getById(invoiceProductDTO.getProductDTO().getId());
        return (product.getQty() >= invoiceProductDTO.getQty()) || (product.getQty() - invoiceProductDTO.getQty()) < 0;
    }



}
