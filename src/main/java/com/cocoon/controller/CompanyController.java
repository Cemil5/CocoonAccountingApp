package com.cocoon.controller;

import com.cocoon.dto.CompanyDTO;
import com.cocoon.entity.Company;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.StateRepository;
import com.cocoon.service.CompanyService;
import com.cocoon.util.MapperUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/company")
public class CompanyController {


    private CompanyService companyService;
    private StateRepository stateRepository;
    private MapperUtil mapperUtil;

    public CompanyController(CompanyService companyService, StateRepository stateRepository, MapperUtil mapperUtil) {
        this.companyService = companyService;
        this.stateRepository = stateRepository;
        this.mapperUtil = mapperUtil;
    }

    @GetMapping("/list")
    public String getCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());

        return "company/company-list";
    }

    @GetMapping("/create")
    public String getCreatePage(Model model) {
        model.addAttribute("company", new Company());
        model.addAttribute("states", stateRepository.findAll());

        return "company/company-add";
    }

    @PostMapping("/create")
    public String saveCompany(Company company, BindingResult result, Model model) {
        try{
            companyService.save(mapperUtil.convert(company, new CompanyDTO()));
            return "redirect:/company/list";
        }catch (Exception e){
            String err = e.getMessage();

            if (!err.isEmpty()) {
                ObjectError error = new ObjectError("globalError", err);
                result.addError(error);
            }

            model.addAttribute("states", stateRepository.findAll());
            return "company/company-add";
        }
    }

    @GetMapping("/update/{id}")
    public String getUpdatePage(@PathVariable("id") String id, Model model) throws CocoonException {
        model.addAttribute("company", companyService.getCompanyById(Long.valueOf(id)));
        model.addAttribute("states", stateRepository.findAll());
        return "company/company-edit";
    }

    @PostMapping("/update/{id}")
    public String updateCompany(@PathVariable("id") Long id, CompanyDTO companyDTO, Model model) throws CocoonException {
        companyService.update(companyDTO);
        return "redirect:/company/list";
    }

    @GetMapping("/close/{id}")
    public String getClosePage(@PathVariable("id") String id, Model model) throws CocoonException {
        companyService.close(companyService.getCompanyById(Long.valueOf(id)));
        return "redirect:/company/list";
    }

    @GetMapping("/open/{id}")
    public String getOpenPage(@PathVariable("id") String id, Model model) throws CocoonException {
        companyService.open(companyService.getCompanyById(Long.valueOf(id)));
        return "redirect:/company/list";
    }
/*
    @GetMapping("/delete/{id}")
    public String deleteCompany(@PathVariable("id") String id, Model model) throws CocoonException {
        companyService.delete(companyService.getCompanyById(Long.valueOf(id)));
        return "redirect:/company/list";
    }
*/

}
