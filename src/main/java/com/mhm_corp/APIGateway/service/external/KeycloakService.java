package com.mhm_corp.APIGateway.service.external;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

@Service
public class KeycloakService {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);


    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwksUrl;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    public boolean validateToken(String token) {
        logger.debug("Starting token validation process");
        try {
            logger.debug("Parsing JWT token");
            SignedJWT signedJWT = SignedJWT.parse(token);

            logger.debug("Loading public keys from: {}", jwksUrl);
            JWKSet publicKeys = JWKSet.load(new URL(jwksUrl));

            String kid = signedJWT.getHeader().getKeyID();
            logger.debug("Searching for public key with kid: {}", kid);
            JWK jwk = publicKeys.getKeyByKeyId(kid);

            if (jwk == null) {
                logger.info("Public key not found for kid: {}", kid);
                return false;
            }

            logger.debug("Converting to RSA public key");
            RSAKey rsaKey = (RSAKey) jwk;
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            logger.debug("Verifying token signature");
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            boolean signatureValid = signedJWT.verify(verifier);

            if (!signatureValid) {
                logger.warn("Token signature verification failed");
                return false;
            }

            logger.debug("Validating token claims");
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!claims.getIssuer().equals(issuer)) {
                logger.warn("Token issuer validation failed. Expected: {}, Found: {}", issuer, claims.getIssuer());
                return false;
            }

            boolean isExpired = claims.getExpirationTime() == null ||
                    claims.getExpirationTime().before(new java.util.Date());
            if (isExpired) {
                logger.warn("Token has expired or has no expiration date");
                return false;
            }

            logger.info("Token validation successful");
            return true;
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage(), e);
            return false;
        }
    }

}
