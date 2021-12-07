package com.sanri.tools.modules.security.service;

import com.sanri.tools.modules.security.service.dtos.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class FileUserDetailServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final SecurityUser securityUser = UserManagerService.USERS.get(username);
        if (securityUser == null){
            throw new UsernameNotFoundException("未找到用户:"+username);
        }
        return securityUser;
    }
}
