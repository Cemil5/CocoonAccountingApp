package com.cocoon.repository;

import com.cocoon.entity.Client;
import com.cocoon.entity.Company;
import com.cocoon.enums.CompanyType;
import com.cocoon.exception.CocoonException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientVendorRepository extends JpaRepository<Client, Long> {

    List<Client> findAllByCompanyId(Long companyId);

    boolean existsByCompanyNameAndCompanyId(String companyName, Long usersCompanyId) throws CocoonException;

    Optional<Client> findByIdAndCompanyId(Long id, Long companyId);

    List<Client> findAllByTypeAndCompanyId(CompanyType type,Long companyId);

}
