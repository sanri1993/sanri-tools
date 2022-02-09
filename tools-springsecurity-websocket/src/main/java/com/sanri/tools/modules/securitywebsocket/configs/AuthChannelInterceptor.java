package com.sanri.tools.modules.securitywebsocket.configs;

import com.sanri.tools.modules.core.security.UserService;
import com.sanri.tools.modules.security.configs.jwt.JwtAuthenticationToken;
import com.sanri.tools.modules.security.configs.jwt.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component("authChannelInterceptor")
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            if (StringUtils.isNotEmpty(jwtToken)) {
                final TokenService.TokenInfo tokenInfo = tokenService.parseTokenInfo(TokenService.parseToken(jwtToken));
                final String username = tokenInfo.getUsername();
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                final Authentication authentication = new JwtAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());
                log.info("当前线程[{}]已经设置用户:{}",Thread.currentThread().getName(),username);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);
            }
        }

        return message;
    }
}