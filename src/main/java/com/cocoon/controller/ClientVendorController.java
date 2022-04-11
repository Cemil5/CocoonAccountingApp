package com.cocoon.controller;

import com.cocoon.dto.ClientDTO;
import com.cocoon.entity.Client;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.StateRepository;
import com.cocoon.service.ClientVendorService;
import com.cocoon.util.MapperUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/client-vendor")
public class ClientVendorController {

    private ClientVendorService clientVendorService;
    private StateRepository stateRepository;
    private MapperUtil mapperUtil;

    public ClientVendorController(ClientVendorService clientVendorService, StateRepository stateRepository, MapperUtil mapperUtil) {
        this.clientVendorService = clientVendorService;
        this.stateRepository = stateRepository;
        this.mapperUtil = mapperUtil;
    }

    @GetMapping("/list")
    public String readAllClientVendor(Model model) {

        model.addAttribute("clientVendorList", clientVendorService.getAllClientsVendorsActivesFirst());

        return "clientvendor/client-vendor-list";
    }

    @GetMapping("/update/{id}")
    public String editCompany(@PathVariable("id") long id, Model model) throws CocoonException {
        model.addAttribute("client", clientVendorService.findById(id));
        model.addAttribute("states", stateRepository.findAll());
        return "clientvendor/client-vendor-edit";
    }

    @PostMapping("/update/{id}")
    public String updateClientVendor(ClientDTO vendorClientDto) throws CocoonException {
        clientVendorService.update(vendorClientDto);
        return "redirect:/client-vendor/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteClientVendor(ClientDTO vendorClientDto) throws CocoonException {
        clientVendorService.deleteClientVendor(vendorClientDto.getId());
        return "redirect:/client-vendor/list";
    }

    @GetMapping("/create")
    public String getCreatePage(Model model){
        model.addAttribute("client", new Client());
        model.addAttribute("states", stateRepository.findAll());

        return "clientvendor/client-vendor-add";
    }

    @PostMapping("/create")
    public String saveClient(Client client, BindingResult result, Model model) {
        try{
            clientVendorService.save(mapperUtil.convert(client, new ClientDTO()));
            return "redirect:/client-vendor/list";
        }catch (Exception e){
            String err = e.getMessage();

            if (!err.isEmpty()) {
                ObjectError error = new ObjectError("globalError", err);
                result.addError(error);
            }

            model.addAttribute("states", stateRepository.findAll());
            return "clientvendor/client-vendor-add";
        }
    }

}
