package com.cocoon.repository;

import com.cocoon.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findRoleById(Long id);
    List<Role> findAllByIdNot(Long id);
}
