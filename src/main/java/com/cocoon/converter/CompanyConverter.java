package com.cocoon.converter;

import com.cocoon.dto.CompanyDTO;
import com.cocoon.entity.Company;
import com.cocoon.repository.CompanyRepository;
import com.cocoon.util.MapperUtil;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
@ConfigurationPropertiesBinding
public class CompanyConverter implements Converter<String, CompanyDTO> {

    private CompanyRepository companyRepository;
    MapperUtil mapperUtil;

    public CompanyConverter(@Lazy CompanyRepository companyRepository, MapperUtil mapperUtil) {
        this.companyRepository = companyRepository;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public CompanyDTO convert(String id) {
        Company company = companyRepository.findById(Long.parseLong(id)).orElseThrow();
        return mapperUtil.convert(company, new CompanyDTO());
    }
}
