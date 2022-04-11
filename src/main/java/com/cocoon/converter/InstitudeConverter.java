package com.cocoon.converter;

import com.cocoon.dto.InstitutionDTO;
import com.cocoon.service.InstitutionService;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
//@ConfigurationPropertiesBinding // this annotation is optional
public class InstitudeConverter implements Converter<String, InstitutionDTO> {

    InstitutionService institutionService;

    public InstitudeConverter(@Lazy InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @Override
    public InstitutionDTO convert(String id) {
        if (id.equals("")) return null;
        return institutionService.getInstitutionById(id);
    }
}
