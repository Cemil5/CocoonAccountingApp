package com.cocoon.converter;

import com.cocoon.entity.Role;
import com.cocoon.repository.RoleRepository;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class RoleConverter implements Converter<String, Role> {

    private RoleRepository roleRepository;

    public RoleConverter(@Lazy RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role convert(String id) {
       return roleRepository.findRoleById(Long.parseLong(id));
    }
}
