package com.mhm_corp.APIGateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class AuthenticationJwt implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationJwt.class);
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principal-attribute}")
    private String principalAttribute;
    @Value("${jwt.auth.converter.resource-id}")
    private String resourceID;
    @Value("${jwt.auth.converter.claim}")
    private String claim;
    @Value("${jwt.auth.converter.client-roles}")
    private String clientRoles;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        logger.debug("Starting JWT conversion process");
        Collection<GrantedAuthority> authorities = Stream
                .concat(
                        Optional.ofNullable(jwtGrantedAuthoritiesConverter.convert(jwt))
                                .orElse(List.of())
                                .stream(),
                        extractResourceRoles(jwt).stream()
                )
                .toList();
        String principalName = getPrincipalName(jwt);
        logger.info("JWT conversion completed for principal: {}", principalName);
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalName(jwt));
    }

    private String getPrincipalName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;

        if (principalAttribute != null && !principalAttribute.isEmpty()){
            claimName = principalAttribute;
            logger.debug("Using custom principal attribute: {}", principalAttribute);
        }

        String principalName = jwt.getClaim(claimName);
        logger.debug("Retrieved principal name: {}", principalName);
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        logger.debug("Extracting resource roles from JWT");
        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        if (jwt.getClaim(claim) == null) {
            logger.warn("No claim found with name: {}", claim);
            return List.of();
        }

        resourceAccess = jwt.getClaim(claim);

        if (resourceAccess.get(resourceID) == null) {
            logger.warn("No resource found with ID: {}", resourceID);
            return List.of();
        }

        resource = (Map<String, Object>) resourceAccess.get(resourceID);

        if (resource.get(clientRoles) == null) {
            logger.warn("No roles found in resource for key: {}", clientRoles);
            return List.of();
        }

        resourceRoles = (Collection<String>) resource.get(clientRoles);
        logger.debug("Found {} roles in JWT", resourceRoles.size());

        Collection<SimpleGrantedAuthority> authorities = resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_".concat(role)))
                .toList();

        logger.debug("Converted roles to authorities: {}", authorities);
        return authorities;
    }
}
