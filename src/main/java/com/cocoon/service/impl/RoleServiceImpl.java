package com.cocoon.service.impl;

import com.cocoon.entity.Role;
import com.cocoon.repository.RoleRepo;
import com.cocoon.service.RoleService;
import com.cocoon.util.MapperUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private RoleRepo roleRepo;
    private MapperUtil mapperUtil;

    public RoleServiceImpl(RoleRepo roleRepo, MapperUtil mapperUtil) {
        this.roleRepo = roleRepo;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public Role findById(Long id) {
        Optional<Role> role = roleRepo.findById(id);
        return mapperUtil.convert(role,new Role());
    }


    @Override
    public List<Role> findAllRoles() {
        List<Role> allRoles = roleRepo.findAll();
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(obj -> obj.getAuthority().equals("MANAGER") || obj.getAuthority().equals("ADMIN"))){
            return roleRepo.findAllByIdNot(1L).stream().map(obj -> mapperUtil.convert(obj, new Role())).collect(Collectors.toList());
        }
        return allRoles.stream().map(obj -> mapperUtil.convert(obj, new Role()))
                .collect(Collectors.toList());
    }

}