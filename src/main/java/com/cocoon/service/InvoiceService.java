package com.cocoon.service;

import com.cocoon.dto.InvoiceDTO;
import com.cocoon.dto.ProfitDTO;
import com.cocoon.entity.Company;
import com.cocoon.entity.Invoice;
import com.cocoon.entity.jpa_customization.IInvoiceForDashBoard;
import com.cocoon.enums.InvoiceType;

import java.util.List;
import java.util.Map;

public interface InvoiceService {

    InvoiceDTO save(InvoiceDTO invoice);

    InvoiceDTO getInvoiceById(Long id);

    InvoiceDTO update(InvoiceDTO dto, Long id);

    String getInvoiceNumber(InvoiceType invoiceType);

    void deleteInvoiceById(Long id);

    List<InvoiceDTO> getAllInvoicesByCompanyAndType(InvoiceType type);

    InvoiceDTO calculateInvoiceCost(InvoiceDTO currentDTO);

    Map<String,Integer> calculateTotalProfitLoss();

    List<IInvoiceForDashBoard> getDashboardInvoiceTop3(Long companyId);

    List<ProfitDTO>  getProfitList();
}
