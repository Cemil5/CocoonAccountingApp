package com.cocoon.controller;

import com.cocoon.client.CurrencyClient;
import com.cocoon.entity.jpa_customization.IInvoiceForDashBoard;
import com.cocoon.service.CompanyService;
import com.cocoon.service.InvoiceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final CompanyService companyService;
    private final InvoiceService invoiceService;
    private final CurrencyClient currencyClient;

    private final WebClient webClient = WebClient.create("http://data.fixer.io");

    public DashboardController(CompanyService companyService, InvoiceService invoiceService,
                               @Qualifier("mockCurrencyClientImpl") CurrencyClient currencyClient) {
        this.companyService = companyService;
        this.invoiceService = invoiceService;
        this.currencyClient = currencyClient;
    }


    @GetMapping()
    public String getDashboard(Model model){
        getInvoiceTopThreeList(model);
        getExchangeRates(model);
        return "dashboard";
    }

    private void getInvoiceTopThreeList(Model model){
        List<IInvoiceForDashBoard> updatedInvoices = invoiceService.getDashboardInvoiceTop3(companyService.getCompanyByLoggedInUser().getId());
        model.addAttribute("invoices", updatedInvoices);
        model.addAttribute("result", invoiceService.calculateTotalProfitLoss());
    }

    //getting the current exchange rates
    //http://data.fixer.io/api/latest?access_key=0efa2bc3ea0bb4f378496f560590a648&symbols=USD,GBP,RUB,JPY&format=1
    private void getExchangeRates(Model model){
        model.addAttribute("rates", currencyClient.retrieveCurrencyDetails().rates);
    }

}