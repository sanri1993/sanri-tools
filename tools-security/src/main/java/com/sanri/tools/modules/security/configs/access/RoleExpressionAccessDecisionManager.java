package com.sanri.tools.modules.security.configs.access;

import com.sanri.tools.modules.security.service.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RoleExpressionAccessDecisionManager implements AccessDecisionManager {
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("js");

    @Autowired
    private RoleRepository roleService;

    /**
     * 暂时使用替换然后用 js 引擎解析来解决逻辑问题, 其实是有漏洞的, 如果某个角色是另一个的前缀
     * @param authentication  当前用户凭证
     * @param object  当前请求路径
     * @param configAttributes  当前请求路径所需要的角色列表 -- > 从 CustomFilterInvocationSecurityMetadataSource 返回
     * @throws AccessDeniedException
     * @throws InsufficientAuthenticationException
     */
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        final ConfigAttribute configAttribute = configAttributes.iterator().next();
        String attribute = configAttribute.getAttribute();
        final String originScript = attribute;

        // anon 为不需要登录的权限
        if (attribute.contains("anon")){
            return ;
        }
        // authc 为只要登录了就有权限
        if (attribute.contains("authc")){
            if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())){
                throw new AccessDeniedException("需要登录");
            }
            return ;
        }

        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            final String userHasRole = authority.getAuthority();
            attribute = attribute.replaceAll(userHasRole,"true");
        }

        final List<String> allRoles = roleService.findRoles().stream().collect(Collectors.toList());
        for (String role : allRoles) {
            attribute = attribute.replaceAll(role,"false");
        }
        try {
            final Object eval = engine.eval(attribute);
            if (eval == null || !(eval instanceof Boolean)){
                throw new AccessDeniedException(Objects.toString(eval));
            }
            if (!((Boolean)eval)){
                throw new AccessDeniedException("权限不足2");
            }
        } catch (ScriptException e) {
            log.error("脚本执行错误:[origin:{}][parse:{}][message:{}]",originScript,attribute,e.getMessage());
            throw new AccessDeniedException("权限不足");
        }
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return false;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

//    public static void main(String[] args) throws ScriptException {
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("js");
////        engine.put("true",true);
////        engine.put("false",false);
//        String a = "true && (false || true)";
//        System.out.println(engine.eval(a));
//    }
}
