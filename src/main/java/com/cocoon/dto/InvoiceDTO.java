package com.cocoon.dto;

import com.cocoon.entity.Company;
import com.cocoon.enums.InvoiceStatus;
import com.cocoon.enums.InvoiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvoiceDTO {

    private Long id;
    private String invoiceNumber;
    private InvoiceStatus invoiceStatus;
    private InvoiceType invoiceType;
    private Byte enabled;
    private Company company;
    private ClientDTO client;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;

    private Set<InvoiceProductDTO> invoiceProduct = new HashSet<>();

    private int InvoiceCostWithoutTax;
    private int InvoiceCostWithTax;
    private int totalCost;

}
