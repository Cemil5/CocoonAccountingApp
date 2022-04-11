package com.cocoon.service.impl;

import com.cocoon.entity.Role;
import com.cocoon.repository.RoleRepository;
import com.cocoon.service.RoleService;
import com.cocoon.util.MapperUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final MapperUtil mapperUtil;

    public RoleServiceImpl(RoleRepository roleRepository, MapperUtil mapperUtil) {
        this.roleRepository = roleRepository;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public Role findById(Long id) {
        Optional<Role> role = roleRepository.findById(id);
        return mapperUtil.convert(role,new Role());
    }


    @Override
    public List<Role> findAllRoles() {
        List<Role> allRoles = roleRepository.findAll();
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(obj -> obj.getAuthority().equals("MANAGER") || obj.getAuthority().equals("ADMIN"))){
            return roleRepository.findAllByIdNot(1L).stream().map(obj -> mapperUtil.convert(obj, new Role())).collect(Collectors.toList());
        }
        return allRoles.stream().map(obj -> mapperUtil.convert(obj, new Role()))
                .collect(Collectors.toList());
    }

}