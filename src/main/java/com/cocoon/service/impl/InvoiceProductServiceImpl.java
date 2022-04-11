package com.cocoon.implementation;

import com.cocoon.dto.InvoiceProductDTO;
import com.cocoon.entity.Invoice;
import com.cocoon.entity.InvoiceProduct;
import com.cocoon.enums.InvoiceStatus;
import com.cocoon.enums.InvoiceType;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.InvoiceProductRepository;
import com.cocoon.repository.InvoiceRepository;
import com.cocoon.service.InvoiceProductService;
import com.cocoon.service.ProductService;
import com.cocoon.util.MapperUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceProductServiceImpl implements InvoiceProductService {

    private final InvoiceProductRepository invoiceProductRepository;
    private final InvoiceRepository invoiceRepository;
    private final MapperUtil mapperUtil;
    private final ProductService productService;

    public InvoiceProductServiceImpl(InvoiceProductRepository invoiceProductRepository, InvoiceRepository invoiceRepository, MapperUtil mapperUtil, ProductService productService) {
        this.invoiceProductRepository = invoiceProductRepository;
        this.invoiceRepository = invoiceRepository;
        this.mapperUtil = mapperUtil;
        this.productService = productService;
    }

    @Override
    public InvoiceProductDTO save(InvoiceProductDTO invoiceProductDTO) {
        InvoiceProduct invoiceProduct = mapperUtil.convert(invoiceProductDTO, new InvoiceProduct());
        InvoiceProduct savedInvoiceProduct = invoiceProductRepository.save(invoiceProduct);
        return mapperUtil.convert(savedInvoiceProduct, new InvoiceProductDTO());
    }

    @Override
    public Set<InvoiceProductDTO> save(Set<InvoiceProductDTO> invoiceProductDTOSet) {

        return invoiceProductDTOSet
                .stream()
                .map(dto -> mapperUtil.convert(dto, new InvoiceProduct()))
                .map(invoiceProductRepository::save)
                .map(entity -> mapperUtil.convert(entity,new InvoiceProductDTO()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<InvoiceProductDTO> getAllInvoiceProductsByInvoiceId(Long id) {

        Set<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByInvoiceId(id);
        return invoiceProducts.stream().map(obj -> mapperUtil.convert(obj, new InvoiceProductDTO())).collect(Collectors.toSet());
    }

    @Override
    public List<InvoiceProductDTO> getAllInvoiceProductsByProductId(Long id) {

        List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByProductId(id);
        return invoiceProducts.stream().map(obj -> mapperUtil.convert(obj, new InvoiceProductDTO())).collect(Collectors.toList());
    }

    @Override
    public void updateInvoiceProducts(Long id, Set<InvoiceProductDTO> invoiceProductDTOs) {

        Set<InvoiceProduct> databaseInvoiceProducts = invoiceProductRepository.findAllByInvoiceId(id);
        databaseInvoiceProducts.forEach(obj -> obj.setIsDeleted(true));
        invoiceProductRepository.saveAll(databaseInvoiceProducts);

        Set<InvoiceProduct> convertedInvoiceProduct = invoiceProductDTOs.stream()
                .map(obj -> mapperUtil.convert(obj, new InvoiceProduct())).collect(Collectors.toSet());

        for (InvoiceProduct invoiceProduct : databaseInvoiceProducts){
            for (InvoiceProduct converted : convertedInvoiceProduct){
                if (Objects.equals(invoiceProduct.getId(), converted.getId())) converted.setProduct(invoiceProduct.getProduct());
            }
        }

        invoiceProductRepository.saveAll(convertedInvoiceProduct);
    }

    @Override
    public void approveInvoiceProduct(Long id) {
        Invoice invoice = invoiceRepository.getById(id);
        Set<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByInvoiceId(id);
        invoiceProducts.forEach(obj -> productService.updateProductQuantity(invoice.getInvoiceType(), obj));
    }

    @Override
    public void deleteInvoiceProducts(Long id) {
        Set<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByInvoiceId(id);
        invoiceProducts.forEach(obj -> productService.updateProductQuantity(InvoiceType.SALE, obj));
    }

    public boolean validateProductQtyForPendingInvoicesIncluded(InvoiceProductDTO dto) throws CocoonException {
        List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByProductIdAndInvoiceInvoiceStatus(dto.getProductDTO().getId(), InvoiceStatus.PENDING);
        int totalQty = invoiceProducts.stream().mapToInt(InvoiceProduct::getQty).sum();
        return totalQty + dto.getQty() <= productService.getProductById(dto.getProductDTO().getId()).getQty();
    }

    @Override
    public ArrayList<InvoiceProductDTO> getStockReportList() {
        ArrayList<InvoiceProduct> products = (ArrayList<InvoiceProduct>) invoiceProductRepository.getStockReportListProducts();
        return (ArrayList<InvoiceProductDTO>) products.stream().map(ip -> mapperUtil.convert(ip, new InvoiceProductDTO())).collect(Collectors.toList());
    }


}
