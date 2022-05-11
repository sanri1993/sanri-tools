package com.sanri.tools.modules.security.configs.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.sanri.tools.modules.core.exception.ToolException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;


import io.jsonwebtoken.*;
import lombok.Data;

@Component
@Slf4j
public class TokenService {
    // 密钥
    private static final String base64Secret = "MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY=";
    // 加密算法
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    // 过期时间 15 天 1296000 000
    private static final int expiresMillis = 1296000000;
    // 快过期的检测时间 2 小时
    public static final int issueAtSecond = 7200;

    /**
     * 根据用户信息生成 token
     * @param principal 用户信息
     * @return
     */
    public String generatorToken(TokenInfo tokenInfo) {
        //生成签名密钥
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Secret);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //添加构成JWT的参数
        JwtBuilder builder = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("username", tokenInfo.getUsername())
                .signWith(signatureAlgorithm, signingKey);

        //添加Token过期时间 15 天
        final long timeMillis = System.currentTimeMillis();
        long expMillis = timeMillis + expiresMillis;
        builder.setExpiration(new Date(expMillis)).setNotBefore(new Date(timeMillis)).setIssuedAt(new Date(expMillis - issueAtSecond));

        //生成JWT
        return builder.compact();
    }

    /**
     * 解析用户信息
     * @param token
     * @return
     */
    public TokenInfo parseTokenInfo(Claims body) {
        // 这里可以反射 tokenInfo , 因为只有一个字段, 就直接获取了
        final String username = body.get("username", String.class);
        return new TokenInfo(username);
    }

    public static Claims parseToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new BadCredentialsException("token 验证失败(空)");
        }
        try{
            final Jws<Claims> claimsJws = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(base64Secret))
                    .parseClaimsJws(token);
            return claimsJws.getBody();
        }catch (ExpiredJwtException e){
            log.debug("token 过期: {}",e.getMessage());
            throw new ToolException("token 过期: {}");
        }

    }


    /**
     * jwt token 中要写入的信息
     */
    @Data
    public static final class TokenInfo{
        private String username;

        public TokenInfo(String username) {
            this.username = username;
        }
    }
}
