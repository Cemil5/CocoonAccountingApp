package com.cocoon.converter;

import com.cocoon.dto.ClientDTO;
import com.cocoon.entity.Client;
import com.cocoon.repository.ClientVendorRepository;
import com.cocoon.util.MapperUtil;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class ClientConverter implements Converter<String, ClientDTO> {

    private final ClientVendorRepository clientVendorRepository;
    private final MapperUtil mapperUtil;

    public ClientConverter(@Lazy ClientVendorRepository clientVendorRepository, MapperUtil mapperUtil) {
        this.clientVendorRepository = clientVendorRepository;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public ClientDTO convert(String id) {

        Client client = clientVendorRepository.findById(Long.parseLong(id)).orElseThrow();
        return mapperUtil.convert(client, new ClientDTO());
    }
}
