package indi.etern.checkIn.service.web;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.entities.setting.oauth2.OAuth2ProviderInfo;
import indi.etern.checkIn.repositories.OAuth2ProviderInfoRepository;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.throwable.OAuth2Exception;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuth2Service implements ClientRegistrationRepository {
    private final Map<String, ClientRegistration> registrations = new ConcurrentHashMap<>();
    private final SettingService settingService;
    private final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);
    @Value("${oauth2.redirectUriTemplatePrefix}")
    protected String redirectUriTemplatePrefix;
    @Resource
    OAuth2ProviderInfoRepository oauth2ProviderInfoRepository;

    public OAuth2Service(SettingService settingService) {
        this.settingService = settingService;
    }

    public OAuth2ProviderInfo getProviderInfo(String providerId) {
        return oauth2ProviderInfoRepository.findById(providerId).orElseThrow(() -> new OAuth2Exception("OAuth2 provider not found"));
    }

    public List<OAuth2ProviderInfo> getProviderInfos() {
        return oauth2ProviderInfoRepository.findAll();
    }

    public void saveProviderInfo(OAuth2ProviderInfo info) {
        oauth2ProviderInfoRepository.save(info);
        reloadRegistrations();
    }

    @PostConstruct
    public void reloadRegistrations() {
        try {
            SettingItem item = settingService.getItem("oauth2", "appDomainURI");
            Object appDomainURI = item.getValue();
            String redirectUri = appDomainURI + "/checkIn" + redirectUriTemplatePrefix;

            List<OAuth2ProviderInfo> providers = getProviderInfos();
            for (OAuth2ProviderInfo provider : providers) {
                if (provider.isEnabledInLogin() || provider.getExamLoginMode() != OAuth2ProviderInfo.ExamLoginMode.DISABLED) {
                    ClientRegistration registration = ClientRegistration.withRegistrationId(provider.getId())
                            .clientName(provider.getName())
                            .clientId(provider.getClientId())
                            .clientSecret(provider.getClientSecret())
                            .authorizationUri(provider.getAuthorizationUri())
                            .tokenUri(provider.getJwksUri())
                            .userInfoUri(provider.getUserInfoUri())
                            .redirectUri(redirectUri/* + provider.getClientId()*/)
                            .scope(provider.getScope())
                            .userNameAttributeName(provider.getUserIdAttributeName())
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .build();
                    registrations.put(provider.getId(), registration);
//                    registrations.put(provider.getName(), registration);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load OAuth2 provider info, OAuth2 side login is disabled");
            if (logger.isDebugEnabled()) {
                logger.debug("Error details:", e);
            }
        }
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return registrations.get(registrationId);
    }

    public void saveProviderInfos(Collection<OAuth2ProviderInfo> providers) {
        int i = 1;
        for (OAuth2ProviderInfo providerInfo : providers) {
            providerInfo.setOrderIndex(i++);
        }
        oauth2ProviderInfoRepository.saveAll(providers);
    }
}