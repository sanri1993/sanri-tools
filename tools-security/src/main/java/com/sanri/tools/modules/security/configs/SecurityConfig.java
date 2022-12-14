package com.sanri.tools.modules.security.configs;

import java.util.Arrays;
import java.util.List;

import com.sanri.tools.modules.security.configs.access.RoleBasedVoter;
import com.sanri.tools.modules.security.configs.jsonlogin.JsonLoginConfiguration;
import com.sanri.tools.modules.security.configs.jsonlogin.ResponseHandler;
import com.sanri.tools.modules.security.configs.jwt.JwtAuthenticationProvider;
import com.sanri.tools.modules.security.configs.jwt.JwtTokenValidationConfigurer;
import com.sanri.tools.modules.security.configs.jwt.LogoutTokenClean;
import com.sanri.tools.modules.security.configs.jwt.TokenService;
import com.sanri.tools.modules.security.configs.whitespace.WhiteSpaceFilter;
import com.sanri.tools.modules.security.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sanri.tools.modules.security.service.FileUserDetailServiceImpl;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JsonLoginConfiguration jsonLoginConfiguration;
    @Autowired
    private JwtTokenValidationConfigurer jwtTokenValidationConfigurer;

    @Autowired
    private LogoutTokenClean logoutTokenClean;
    @Autowired
    private TokenService tokenService;

    @Autowired
    private CustomAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;
    @Autowired
    private UrlSecurityPermsLoad urlPermsLoad;
    @Autowired
    private RoleBasedVoter roleBasedVoter;
    @Autowired
    private ResponseHandler responseHandler;

    @Bean
    public AccessDecisionManager accessDecisionManager(){
        List<AccessDecisionVoter<? extends Object>> decisionVoters
                = Arrays.asList(
                new WebExpressionVoter(),
                roleBasedVoter,
                new AuthenticatedVoter());
        return new UnanimousBased(decisionVoters);
    }

    @Bean
    public WhiteSpaceFilter whiteSpaceFilter(){
        return new WhiteSpaceFilter(urlPermsLoad,tokenService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // ??????????????????
        http.authorizeRequests()
                .antMatchers("/version/**","/cron/nextExecutionTime","/plugin/visited").permitAll()
                .antMatchers("/ws/**").permitAll()
                .anyRequest().authenticated()
                .accessDecisionManager(accessDecisionManager());
//                .and().anonymous().disable();

        // ?????? session
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().formLogin().disable()
                .csrf().disable();

        // ????????????
        http.cors().configurationSource(corsConfigurationSource());
//                .and().headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
//                new Header("Access-control-Allow-Origin","*"),
//                new Header("Access-Control-Expose-Headers","Authorization"))));

        //??????OPTIONS?????????????????????header
        http.addFilterAfter(new OptionsRequestFilter(), CorsFilter.class);

        // json ????????????
        http.apply(jsonLoginConfiguration);

        // jwt token ????????????
        http.apply(jwtTokenValidationConfigurer);

        // ???????????????
        http.addFilterBefore(whiteSpaceFilter(), AnonymousAuthenticationFilter.class);

        // ???????????????????????????????????????
        http.exceptionHandling()
                //???????????????????????????????????????????????????????????????
                .authenticationEntryPoint(authenticationEntryPoint)
                // ???????????????????????????????????????????????????
                .accessDeniedHandler(accessDeniedHandler);

        // ????????????,????????????????????????
        final CustomLogoutHandler logoutHandler = new CustomLogoutHandler(responseHandler);
        http.logout()
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler(logoutHandler);

    }

    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.addExposedHeader("Authorization");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/",
                "index.html",
                "/**/*.css","/**/*.js",
                "/**/*.png","/**/*.jpg","/**/*.gif","/**/*.ico","/**/img/*","/**/images/*","/**/images/**/*",
                "/**/fonts/*",
                "/static/**"
        ).antMatchers("/public/**");

        web.httpFirewall(logStrictHttpFirewall());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Autowired
    private UserRepository userRepository;

    @Override
    @Bean
    public UserDetailsService userDetailsService(){return new FileUserDetailServiceImpl(userRepository);}

    @Bean
    public AuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(userDetailsService());
        daoProvider.setPasswordEncoder(passwordEncoder());
        return daoProvider;
    }

    @Bean
    public AuthenticationProvider jwtAuthenticationProvider(){
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider();
        authenticationProvider.setTokenService(tokenService);
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider()).authenticationProvider(jwtAuthenticationProvider());
    }

    @Bean
    public LogStrictHttpFirewall logStrictHttpFirewall(){
        return new LogStrictHttpFirewall();
    }
}
