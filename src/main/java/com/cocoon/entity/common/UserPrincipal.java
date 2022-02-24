package com.cocoon.entity.common;

import com.cocoon.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//this is not a bean
public class UserPrincipal implements UserDetails {

    private User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>(Collections.singleton(new SimpleGrantedAuthority("ROOT")));

//        GrantedAuthority authority = new SimpleGrantedAuthority(this.user.getRole().getName());
//        authorityList.add(authority);


        /*  //ManyToMany için bu şekilde yazmalıyız...
        this.user.getRoles().forEach(role -> {
            GrantedAuthority authority = new SimpleGrantedAuthority(this.user.getRole().getDescription());
            authorityList.add(authority);
        });
         */

        return authorityList;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId(){
        return this.user.getId();
    }
}