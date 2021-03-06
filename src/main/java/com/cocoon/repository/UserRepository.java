package com.cocoon.repository;

import com.cocoon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findAllByCompanyId(Long id);

    Optional<User> findByIdAndCompanyId(Long id, Long companyId);
}
