package com.cocoon.controller;

import com.cocoon.dto.InvoiceDTO;
import com.cocoon.dto.InvoiceProductDTO;
import com.cocoon.service.*;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Set;

@Controller
@RequestMapping("/email")
public class EmailSendController {

    private final CompanyService companyService;
    private final ServletContext servletContext;
    private final TemplateEngine templateEngine;
    private final InvoiceService invoiceService;
    private final InvoiceProductService invoiceProductService;

    private final EmailSenderService emailSenderService;

    public EmailSendController(CompanyService companyService, ServletContext servletContext, TemplateEngine templateEngine, InvoiceService invoiceService, InvoiceProductService invoiceProductService, EmailSenderService emailSenderService) {

        this.companyService = companyService;
        this.servletContext = servletContext;
        this.templateEngine = templateEngine;
        this.invoiceService = invoiceService;
        this.invoiceProductService = invoiceProductService;
        this.emailSenderService = emailSenderService;

    }

    @GetMapping("/send/{id}")
    public void sendMail(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {

        InvoiceDTO invoiceDTO = invoiceService.getInvoiceById(id);
        InvoiceDTO updatedInvoiceDTO = invoiceService.calculateInvoiceCost(invoiceDTO);
        Set<InvoiceProductDTO> invoiceProducts = invoiceProductService.getAllInvoiceProductsByInvoiceId(id);

        /*Create HTML using Thymeleaf template*/
        WebContext context = new WebContext(request, response, servletContext);

        context.setVariable("company", companyService.getCompanyByLoggedInUser());
        context.setVariable("invoice", updatedInvoiceDTO);
        context.setVariable("invoiceProducts", invoiceProducts);

        String companyListHTML = templateEngine.process("invoice/invoiceSuccess.html", context);

        /*Setup Source and target I/O streams*/
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setBaseUri("http://localhost:8080");
        /* Call convert method */
        HtmlConverter.convertToPdf(companyListHTML, target, converterProperties);

        /* extract output as bytes */
        byte[] bytes = target.toByteArray();

        // send with email
        emailSenderService.sendEmailWithAttachment("omererden18@gmail.com", updatedInvoiceDTO.getClient().getEmail(), invoiceDTO.getInvoiceNumber(), "Your invoice ...", bytes);

    }

}
