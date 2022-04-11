package com.cocoon.controller;

import com.cocoon.dto.ProfitDTO;
import com.cocoon.entity.InvoiceProduct;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.InvoiceProductRepository;
import com.cocoon.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

@Controller
@RequestMapping("/report")
public class ReportController {


    private ProductService productService;
    private InvoiceService invoiceService;
    private InvoiceProductService invoiceProductService;
    private InvoiceProductRepository invoiceProductRepository;
    private final CompanyService companyService;

    public ReportController(InvoiceService invoiceService, ProductService productService,
                            InvoiceProductService invoiceProductService, InvoiceProductRepository invoiceProductRepository, CompanyService companyService) {
        this.invoiceService = invoiceService;
        this.productService = productService;
        this.invoiceProductService = invoiceProductService;
        this.invoiceProductRepository = invoiceProductRepository;
        this.companyService = companyService;
    }

    @GetMapping("/stock")
    public String getStock(Model model){

        ArrayList<InvoiceProduct> stock = (ArrayList<InvoiceProduct>) invoiceProductRepository.getStockReportListProducts();
//        ArrayList<InvoiceProductDTO> stock = invoiceProductService.getStockReportList();

        model.addAttribute("stock", stock);

        return "report/stock-report.html";
    }

    @GetMapping("/profit")
    public String getProfitList(Model model){
        ArrayList<ProfitDTO> profit=   (ArrayList<ProfitDTO>)invoiceService.getProfitList();

        model.addAttribute("profit", profit);
        model.addAttribute("result", invoiceService.calculateTotalProfitLoss());

        return "report/profit-report.html";
    }


    @GetMapping("/toProfit")
    public String ProfittoPDF( Model model) throws CocoonException {

        ArrayList<ProfitDTO> profit=   (ArrayList<ProfitDTO>)invoiceService.getProfitList();
        model.addAttribute("profit", profit);
        model.addAttribute("company", companyService.getCompanyByLoggedInUser());
        model.addAttribute("result", invoiceService.calculateTotalProfitLoss());

        return "report/Profit-printed.html";
    }

}
