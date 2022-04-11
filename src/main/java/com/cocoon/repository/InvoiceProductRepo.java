package com.cocoon.repository;

import com.cocoon.entity.InvoiceProduct;
import com.cocoon.enums.InvoiceStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface InvoiceProductRepo extends JpaRepository<InvoiceProduct, Long> {

    Set<InvoiceProduct> findAllByInvoiceId(Long id);
    List<InvoiceProduct> findAllByProductId(Long id);
    List<InvoiceProduct> findAllByProductIdAndInvoiceInvoiceStatus(Long id, InvoiceStatus status);

    InvoiceProduct findInvoiceProductByInvoiceIdAndNameAndQtyAndPriceAndTax(Long id, String name, int qty, int price, int tax);

//    @Query(nativeQuery = true, value = "select ip.id, ip.invoice_id, ip.product_id, ip.qty, ip.price, i.invoice_type, i.invoice_date, p.name, p.unit from invoice_product ip " +
//                                        " left join invoice i on i.id = ip.invoice_id " +
//                                        " left join product p on p.id = ip.product_id ")
    @Query(nativeQuery = true, value = "select ip.*, i.*, p.name, p.unit from invoice_product ip " +
        " left join invoice i on i.id = ip.invoice_id " +
        " left join product p on p.id = ip.product_id ")
    List<InvoiceProduct> getStockReportList();


    @Query(value = "select iv from InvoiceProduct iv join Invoice  i on iv.invoice.id=i.id join Product p on iv.product.id=p.id")
    List<InvoiceProduct> getStockReportListProducts();



       /*
    @Query(nativeQuery = true, value = "SELECT * FROM product p INNER JOIN category c on p.category_id = c.id WHERE c.id =?1")
    List<Product> getProductsByCategoryId2(Long id);
*/
}
