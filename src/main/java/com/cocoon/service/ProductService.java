package com.cocoon.service;

import com.cocoon.dto.InvoiceProductDTO;
import com.cocoon.dto.ProductDTO;
import com.cocoon.entity.Invoice;
import com.cocoon.entity.InvoiceProduct;
import com.cocoon.entity.Product;
import com.cocoon.enums.InvoiceType;
import com.cocoon.enums.ProductStatus;
import com.cocoon.enums.Unit;
import com.cocoon.exception.CocoonException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ProductService {

    List<ProductDTO> getAllProducts();
    List<ProductDTO> getAllProductsByCompany();
    ProductDTO save(ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    void update(ProductDTO productDTO) throws CocoonException;
    ProductStatus getProductStatusById(Long id);
    Unit getUnitById(Long id);

    void deleteById (Long id);
    List<ProductDTO> findProductsByCategoryId(Long id);
    void updateProductQuantity(InvoiceType type, InvoiceProduct invoiceProduct);
    boolean validateProductQuantity(InvoiceProductDTO invoiceProductDTO);
}
