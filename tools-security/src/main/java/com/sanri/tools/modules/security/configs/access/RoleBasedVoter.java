package com.sanri.tools.modules.security.configs.access;

import com.sanri.tools.modules.security.configs.UrlSecurityPermsLoad;
import com.sanri.tools.modules.security.service.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RoleBasedVoter implements AccessDecisionVoter<Object> {
    @Autowired
    private UrlSecurityPermsLoad urlPermsLoad;
    @Autowired
    private RoleRepository roleService;

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(FilterInvocation.class);
    }

    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("js");

    public static final Pattern pattern = Pattern.compile("(\\w+)");

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        if(authentication == null) {
            return ACCESS_DENIED;
        }
        int result = ACCESS_ABSTAIN;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (ConfigAttribute attribute : attributes) {
            if(attribute.getAttribute()==null){
                continue;
            }
            if (this.supports(attribute)) {
                result = ACCESS_DENIED;

                // Attempt to find a matching granted authority
                for (GrantedAuthority authority : authorities) {
                    if (attribute.getAttribute().equals(authority.getAuthority())) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }

        // ????????????????????????
        FilterInvocation fi = (FilterInvocation) object;
        String url = fi.getRequestUrl();
        String matchRoles = urlPermsLoad.findMatchRoles(url);
        if (StringUtils.isBlank(matchRoles)){
        	return ACCESS_ABSTAIN;
        }
        if (matchRoles.contains("anon")){
        	return ACCESS_GRANTED;
		}

		// ??????????????????????????????,??????????????????????????????, ?????????????????????
		if (matchRoles.contains("authc") && "anonymousUser".equals(authentication.getPrincipal())){
			return ACCESS_DENIED;
		}

        final String originScript = matchRoles;

        // ????????????, ????????????????????? true, ?????????????????? false
        for (GrantedAuthority authority : authorities) {
            final String userHasRole = authority.getAuthority();
            matchRoles = matchRoles.replaceAll(userHasRole,"true");
        }

        // ?????????????????????????????? flase
        final List<String> allRoles = roleService.findRoles().stream().collect(Collectors.toList());
        for (String role : allRoles) {
            matchRoles = matchRoles.replaceAll(role,"false");
        }

        // ????????????????????? undefined
        try {
            final Matcher matcher = pattern.matcher(originScript);
            while (matcher.find()){
                for (int i = 0; i < matcher.groupCount(); i++) {
                    matchRoles = matchRoles.replaceAll(matcher.group(i),"undefined");
                }
            }
        }catch (Exception e){
            log.warn("??????????????????,????????????????????????:{}",e.getMessage());
        }

        try {
            final Object eval = engine.eval(matchRoles);
            if (eval == null || !(eval instanceof Boolean)){
				log.error("??????????????????,???????????? Bool ??????,?????????????????????:{},{}",originScript,matchRoles);
				return ACCESS_DENIED;
            }
            if (((Boolean)eval)){

            }
            return ((Boolean)eval) ?  ACCESS_GRANTED : ACCESS_DENIED;
        } catch (ScriptException e) {
            log.error("??????????????????:[origin:{}][parse:{}][message:{}]",originScript,matchRoles,e.getMessage());
			return ACCESS_DENIED;
        }
    }
}
