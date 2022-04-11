package com.cocoon.implementation;

import com.cocoon.dto.UserDTO;
import com.cocoon.entity.User;
import com.cocoon.exception.CocoonException;
import com.cocoon.exception.UserDoesNotExistException;
import com.cocoon.repository.UserRepository;
import com.cocoon.service.CompanyService;
import com.cocoon.service.UserService;
import com.cocoon.util.MapperUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MapperUtil mapperUtil;
    private final PasswordEncoder passwordEncoder;
    private final CompanyService companyService;

    public UserServiceImpl(UserRepository userRepository, MapperUtil mapperUtil, PasswordEncoder passwordEncoder, @Lazy CompanyService companyService) {
        this.userRepository = userRepository;
        this.mapperUtil = mapperUtil;
        this.passwordEncoder = passwordEncoder;
        this.companyService = companyService;
    }

    @Override
    public List<UserDTO> findAllUsers() {

        if (isUserRoot()) {
            List<User> allUsers = userRepository.findAll();
            return allUsers.stream().map(obj -> mapperUtil.convert(obj, new UserDTO()))
                    .collect(Collectors.toList());
        } else {
            List<User> allUsers = userRepository.findAllByCompanyId(companyService.getCompanyByLoggedInUser().getId());
            return allUsers.stream().map(obj -> mapperUtil.convert(obj, new UserDTO()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UserDTO update(UserDTO userDTO) throws CocoonException {
        User updatedUser = mapperUtil.convert(userDTO, new User());
        updatedUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User savedUser = userRepository.save(updatedUser);
        return mapperUtil.convert(savedUser, new UserDTO());
    }

    @Override
    public UserDTO save(UserDTO userDTO) throws CocoonException {
        User foundUser = userRepository.findByEmail(userDTO.getEmail());
        if (foundUser != null) throw new CocoonException("User already exists");
        User convertedUser = mapperUtil.convert(userDTO, new User());
        convertedUser.setPassword(passwordEncoder.encode(convertedUser.getPassword()));
        User savedUser = userRepository.save(convertedUser);
        return mapperUtil.convert(savedUser, new UserDTO());
    }

    @Override
    public UserDTO findById(Long id) {
        if (isUserRoot()) {
            User user = userRepository.findById(id).orElseThrow(() -> new UserDoesNotExistException(id));
            return mapperUtil.convert(user, new UserDTO());
        } else {
            User user = userRepository.findByIdAndCompanyId(id, companyService.getCompanyByLoggedInUser().getId()).orElseThrow(() -> new UserDoesNotExistException(id));
            return mapperUtil.convert(user, new UserDTO());
        }
    }

    @Override
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return mapperUtil.convert(user, new UserDTO());
    }

    @Override
    public void delete(Long id) {

        if (isUserRoot()) {
            User user = userRepository.findById(id).orElseThrow(() -> new UserDoesNotExistException(id));
            user.setIsDeleted(true);
            userRepository.save(user);
        } else {
            User user = userRepository.findByIdAndCompanyId(id, companyService.getCompanyByLoggedInUser().getId()).orElseThrow(() -> new UserDoesNotExistException(id));
            user.setIsDeleted(true);
            userRepository.save(user);
        }
    }

    @Override
    public List<UserDTO> findAllUsersForLogging() {
        List<User> allUsers = userRepository.findAll();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROOT"))
            return allUsers.stream().map(obj -> mapperUtil.convert(obj, new UserDTO())).collect(Collectors.toList());
        else if (roles.contains("ADMIN")) {
            User user = userRepository.findByEmail(authentication.getName());
            return allUsers.stream().filter(o -> o.getCompany().getId().equals(user.getCompany().getId()))
                    .map(obj -> mapperUtil.convert(obj, new UserDTO()))
                    .collect(Collectors.toList());
        } else return null;
    }

    private Boolean isUserRoot() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        return roles.contains("ROOT");
    }

}
