package com.cocoon.service;

import com.cocoon.dto.CompanyDTO;
import com.cocoon.entity.Company;
import com.cocoon.exception.CocoonException;

import java.util.List;

public interface CompanyService {

    CompanyDTO getCompanyById(Long id);

    CompanyDTO getCompanyByLoggedInUser();

    CompanyDTO update(CompanyDTO companyDTO);

    void close(CompanyDTO companyDTO);

    void open(CompanyDTO companyDTO);

    void delete(CompanyDTO companyDTO);

    List<CompanyDTO> getAllCompanies();

    void save(CompanyDTO company);
}
