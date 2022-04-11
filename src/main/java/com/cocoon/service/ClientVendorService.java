package com.cocoon.service;

import com.cocoon.dto.ClientDTO;
import com.cocoon.enums.CompanyType;
import com.cocoon.exception.CocoonException;

import java.util.List;

public interface ClientVendorService {

    void save(ClientDTO clientDTO);

    List<ClientDTO> getAllClientsVendorsActivesFirst();

    ClientDTO findById(Long id);

    ClientDTO update (ClientDTO clientDTO);

    void deleteClientVendor(Long id);

    List<ClientDTO> getAllClientVendorsByType(CompanyType type);

}
