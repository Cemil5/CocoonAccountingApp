package com.cocoon.controller;

import com.cocoon.dto.InvoiceProductDTO;
import com.cocoon.dto.ProfitDTO;
import com.cocoon.entity.InvoiceProduct;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.InvoiceProductRepo;
import com.cocoon.service.*;
import lombok.AllArgsConstructor;
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
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/report")
public class ReportController {


    private ProductService productService;
    private InvoiceService invoiceService;
    private InvoiceProductService invoiceProductService;
    private InvoiceProductRepo invoiceProductRepo;
    private final CompanyService companyService;


    @GetMapping("/stock")
    public String getStock(Model model){

        List<InvoiceProduct> stock = invoiceProductRepo.getStockReportListProducts();
  //      List<InvoiceProductDTO> stock = invoiceProductService.getStockReportList();

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

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("date", new Date());
        model.addAttribute("localDateTime", LocalDateTime.now());
        model.addAttribute("localDate", LocalDate.now());
        model.addAttribute("java8Instant", Instant.now());
    }
}
