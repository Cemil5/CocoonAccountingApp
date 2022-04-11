package com.cocoon.implementation;

import com.cocoon.dto.ClientDTO;
import com.cocoon.dto.CompanyDTO;
import com.cocoon.entity.Client;
import com.cocoon.entity.Company;
import com.cocoon.enums.CompanyType;
import com.cocoon.enums.InputConstraint;
import com.cocoon.exception.CocoonException;
import com.cocoon.exception.InputLengthExceededException;
import com.cocoon.repository.ClientVendorRepository;
import com.cocoon.service.ClientVendorService;
import com.cocoon.service.CompanyService;
import com.cocoon.util.MapperUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientVendorServiceImpl implements ClientVendorService {

    private final ClientVendorRepository clientVendorRepository;
    private final MapperUtil mapperUtil;
    private final CompanyService companyService;

    public ClientVendorServiceImpl(ClientVendorRepository clientVendorRepository, MapperUtil mapperUtil, CompanyService companyService) {
        this.clientVendorRepository = clientVendorRepository;
        this.mapperUtil = mapperUtil;
        this.companyService = companyService;
    }

    @Override
    public void save(ClientDTO clientDTO) {
        CompanyDTO userCompany = companyService.getCompanyByLoggedInUser();
        //if same client name already exists in client_vendor table an exception is thrown
        if (clientVendorRepository.existsByCompanyNameAndCompanyId(clientDTO.getCompanyName(), userCompany.getId()))
            throw new CocoonException("A company with the same name exists.");

        //when a client is saved it is assumed that it's status is enabled
        clientDTO.setEnabled(true);

        //if the length of the address exceeds 254 then it should be shortened
        if (clientDTO.getAddress().length() > InputConstraint.ADDRESS_INPUT.getMaxLength())
            throw new InputLengthExceededException(InputConstraint.ADDRESS_INPUT);

        Client toSave = mapperUtil.convert(clientDTO, new Client());
        toSave.setCompany(mapperUtil.convert(userCompany, new Company()));

        Client savedClient = clientVendorRepository.save(toSave);
    }

    public List<ClientDTO> getAllClientsVendorsActivesFirst() {
        List<Client> list = clientVendorRepository.findAllByCompanyId(companyService.getCompanyByLoggedInUser().getId());
        list.sort((o1, o2) -> o2.getEnabled().compareTo(o1.getEnabled()));
        return list.stream().map(client -> mapperUtil.convert(client, new ClientDTO())).collect(Collectors.toList());
    }

    @Override
    public ClientDTO findById(Long id) {
        Client client = clientVendorRepository.findByIdAndCompanyId(id, companyService.getCompanyByLoggedInUser().getId()).orElseThrow(() -> new CocoonException("Vendor/Client with " + id + " not exist"));
        return mapperUtil.convert(client, new ClientDTO());
    }

    @Override
    public ClientDTO update(ClientDTO clientDTO) {
        if (clientDTO.getAddress().length() > InputConstraint.ADDRESS_INPUT.getMaxLength())
            throw new InputLengthExceededException(InputConstraint.ADDRESS_INPUT);
        Client updatedClient = mapperUtil.convert(clientDTO, new Client());

        updatedClient.setCompany(mapperUtil.convert(companyService.getCompanyByLoggedInUser(), new Company()));

        Client savedClient = clientVendorRepository.save(updatedClient);
        return mapperUtil.convert(savedClient, new ClientDTO());
    }

    @Override
    public void deleteClientVendor(Long id) {
        Client client = clientVendorRepository.findByIdAndCompanyId(id, companyService.getCompanyByLoggedInUser().getId()).orElseThrow(()-> new CocoonException("Vendor/Client with " + id + " not exist"));
        client.setIsDeleted(true);
        clientVendorRepository.save(client);
    }

    @Override
    public List<ClientDTO> getAllClientVendorsByType(CompanyType type) {
        List<Client> clients = clientVendorRepository.findAllByTypeAndCompanyId(type,companyService.getCompanyByLoggedInUser().getId());
        return clients.stream().map(obj -> mapperUtil.convert(obj, new ClientDTO())).collect(Collectors.toList());
    }
}
