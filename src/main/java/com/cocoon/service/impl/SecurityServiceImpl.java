package com.cocoon.service.impl;

import com.cocoon.entity.User;
import com.cocoon.repository.UserRepository;
import com.cocoon.entity.common.UserPrincipal;
import com.cocoon.service.SecurityService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    UserRepository userRepository;

    public SecurityServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if(user==null){
            throw new UsernameNotFoundException("This user does not exists");
        }

        return new UserPrincipal(user);
    }
}
