package com.br.lottus.mobile.config.oauth2;

import com.br.lottus.mobile.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2UserProcessingService processingService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, oidcUser.getAttributes());

        Usuario usuario = processingService.process(userInfo);

        Map<String, Object> enrichedAttributes = enrichAttributes(oidcUser.getAttributes(), usuario);

        return new DefaultOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub"
        ) {
            @Override
            public Map<String, Object> getAttributes() {
                return enrichedAttributes;
            }
        };
    }

    private Map<String, Object> enrichAttributes(Map<String, Object> original, Usuario usuario) {
        Map<String, Object> enriched = new HashMap<>(original);
        enriched.put("lottus_user_id", usuario.getId());
        enriched.put("lottus_nome", usuario.getNome());
        enriched.put("lottus_email", usuario.getEmail());
        return enriched;
    }
}
