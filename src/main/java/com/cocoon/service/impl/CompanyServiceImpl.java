package com.cocoon.service.impl;

import com.cocoon.dto.CompanyDTO;
import com.cocoon.dto.UserDTO;
import com.cocoon.entity.Company;
import com.cocoon.exception.CocoonException;
import com.cocoon.repository.CompanyRepo;
import com.cocoon.service.CompanyService;
import com.cocoon.service.UserService;
import com.cocoon.util.MapperUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private CompanyRepo companyRepo;
    private MapperUtil mapperUtil;
    private UserService userService;

    @Override
    public CompanyDTO getCompanyById(Long id) throws CocoonException {
        Optional<Company> companyOptional = companyRepo.findById(id);
        if (companyOptional.isEmpty())
            throw new CocoonException("There is no company with id : " + id);

        return mapperUtil.convert(companyOptional.get(), new CompanyDTO());
    }

    @Override
    public CompanyDTO getCompanyByLoggedInUser() {
        var currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO userDTO = userService.findByEmail(currentUserEmail);
        return userDTO.getCompany();
    }

    @Override
    public CompanyDTO update(CompanyDTO companyDTO) throws CocoonException {

       Company company=companyRepo.getById(companyDTO.getId());
       Company convertedCompanyEntity=mapperUtil.convert(companyDTO,new Company());
       convertedCompanyEntity.setId(company.getId());
       convertedCompanyEntity.setEnabled(company.getEnabled());
       companyRepo.save(convertedCompanyEntity);
       return getCompanyById(companyDTO.getId());
    }

    @Override
    public void close(CompanyDTO companyDTO) throws CocoonException {
        Company company=companyRepo.getById(companyDTO.getId());
        Company convertedCompanyEntity=mapperUtil.convert(companyDTO,new Company());
        convertedCompanyEntity.setId(company.getId());
        convertedCompanyEntity.setEnabled((byte) 0);
        companyRepo.save(convertedCompanyEntity);

    }

    @Override
    public void open(CompanyDTO companyDTO) throws CocoonException {

        Company company=companyRepo.getById(companyDTO.getId());
        Company convertedCompanyEntity=mapperUtil.convert(companyDTO,new Company());
        convertedCompanyEntity.setId(company.getId());
        convertedCompanyEntity.setEnabled((byte) 1);
        companyRepo.save(convertedCompanyEntity);

    }

    @Override
    public void delete(CompanyDTO companyDTO) throws CocoonException {
        companyRepo.delete(companyRepo.getById(companyDTO.getId()));
    }

    @Override
    public List<CompanyDTO> getAllCompanies() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROOT")) {
            List<Company> companyList = companyRepo.findAll();
            return companyList.stream().map(company ->
                    mapperUtil.convert(company, new CompanyDTO())).collect(Collectors.toList());
        } else {
            return Collections.singletonList(getCompanyByLoggedInUser());
        }
    }

    @Override
    public void save(CompanyDTO companyDTO) throws CocoonException{
        //if same company name already exists in company table we have to throw exception
        if (companyRepo.existsByTitle(companyDTO.getTitle()))
            throw new CocoonException("This company name already saved to database.");

        //if establishment date is a date in the future, it makes no sense. it is not permitted
        checkEstablishmentDate(companyDTO);

        //when a company is saved it is assumed that it's status is enabled
        companyDTO.setEnabled(true);

        //if the input address value length exceeds 254 chars then we need to split it two
        // and save the rest in the second address field
        if (companyDTO.getAddress1().length() > 254){
            String fullAddress = companyDTO.getAddress1();
            int indexOfSpaceBeforeSplitLength = fullAddress.substring(0, 254).lastIndexOf(" ");
            companyDTO.setAddress1(fullAddress.substring(0, indexOfSpaceBeforeSplitLength));
            companyDTO.setAddress2(fullAddress.substring(indexOfSpaceBeforeSplitLength + 1));
        }

        //saving to database
        Company savedCompany = companyRepo.save(mapperUtil.convert(companyDTO, new Company()));
    }
/*
    @Override
    public CompanyDTO update(CompanyDTO companydto) throws CocoonException {

        //Find current company
        Optional<Company> company = companyRepo.findByCompanyTitle(companydto.getTitle());
        //Map update company  dto to entity object
       Company convertedtoCompany=mapperUtil.convert(companydto,new Company());

        TO DO BY MEMO

        convertedCompany.setPassWord(passwordEncoder.encode(convertedCompany.getPassWord()));
        convertedCompany.setEnabled(true);


        //set id to the converted object
        convertedtoCompany.setId(company.get().getId());
        //save updated company
        companyRepo.save(convertedtoCompany);
        return findCompanyByCompanyTitle(companydto.getTitle());

    }

    @Override
    public void delete(String companyTitle) throws CocoonException {
        Company company = companyRepo.findByCompanyTitle(companyTitle);

        if(company == null){
            throw new CocoonException("User Does Not Exists");
        }

        /*if(!checkIfCompanyCanBeDeleted(co)){
            throw new TicketingProjectException("User can not be deleted. It is linked by a project ot task");


        company.setTitle(company.getTitle()+"-Deleted");
        company.setEnabled(false);
        companyRepo.save(company);
    }
    //hard delete
    @Override
    public void deleteByCompanyTitle(String companyTitle) {
        companyRepo.deleteByCompanyTitle(companyTitle);

    }*/

    /**
     * checks whether the provided companyDTO object's establishment date is at a time in the future
     * @param companyDTO
     * @throws CocoonException
     */
    private void checkEstablishmentDate(CompanyDTO companyDTO) throws CocoonException{
        if (companyDTO.getEstablishmentDate().isAfter(LocalDate.now()))
            throw new CocoonException("Establishment date should not be at a time in future.");
    }

}
